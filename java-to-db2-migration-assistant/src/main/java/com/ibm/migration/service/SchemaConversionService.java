package com.ibm.migration.service;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.ibm.migration.model.MigrationResult;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for converting PostgreSQL schemas to DB2
 */
@ApplicationScoped
public class SchemaConversionService {

    private static final Logger LOG = Logger.getLogger(SchemaConversionService.class);

    public MigrationResult convertSchema(String schemaSQL) {
        LOG.infof("Converting PostgreSQL schema to DB2");
        
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        String converted = schemaSQL;
        
        // SERIAL → INTEGER GENERATED ALWAYS AS IDENTITY
        if (converted.contains("SERIAL")) {
            converted = converted.replaceAll("\\bSERIAL\\b", "INTEGER GENERATED ALWAYS AS IDENTITY");
            warnings.add("SERIAL converted to INTEGER GENERATED ALWAYS AS IDENTITY");
        }
        
        // BIGSERIAL → BIGINT GENERATED ALWAYS AS IDENTITY
        if (converted.contains("BIGSERIAL")) {
            converted = converted.replaceAll("\\bBIGSERIAL\\b", "BIGINT GENERATED ALWAYS AS IDENTITY");
            warnings.add("BIGSERIAL converted to BIGINT GENERATED ALWAYS AS IDENTITY");
        }
        
        // TEXT → CLOB
        if (converted.contains("TEXT")) {
            converted = converted.replaceAll("\\bTEXT\\b", "CLOB");
            warnings.add("TEXT converted to CLOB - consider VARCHAR if data is < 32KB");
        }
        
        // BOOLEAN → SMALLINT
        if (converted.contains("BOOLEAN")) {
            converted = converted.replaceAll("\\bBOOLEAN\\b", "SMALLINT");
            converted = converted.replaceAll("\\bTRUE\\b", "1");
            converted = converted.replaceAll("\\bFALSE\\b", "0");
            warnings.add("BOOLEAN converted to SMALLINT (0=false, 1=true)");
            recommendations.add("Update application code to use 0/1 instead of true/false");
        }
        
        // BYTEA → BLOB
        if (converted.contains("BYTEA")) {
            converted = converted.replaceAll("\\bBYTEA\\b", "BLOB");
            warnings.add("BYTEA converted to BLOB");
        }
        
        // UUID → CHAR(36)
        if (converted.contains("UUID")) {
            converted = converted.replaceAll("\\bUUID\\b", "CHAR(36)");
            warnings.add("UUID converted to CHAR(36)");
            recommendations.add("Consider using VARCHAR(36) for flexibility");
        }
        
        // JSONB → CLOB
        if (converted.contains("JSONB") || converted.contains("JSON")) {
            converted = converted.replaceAll("\\bJSONB\\b", "CLOB");
            converted = converted.replaceAll("\\bJSON\\b", "CLOB");
            warnings.add("JSON/JSONB converted to CLOB - JSON functions not available");
            recommendations.add("Consider using DB2 JSON functions or store as VARCHAR");
        }
        
        // NOW() → CURRENT TIMESTAMP
        if (converted.contains("NOW()")) {
            converted = converted.replace("NOW()", "CURRENT TIMESTAMP");
            warnings.add("NOW() converted to CURRENT TIMESTAMP");
        }
        
        // Handle Views
        if (converted.toUpperCase().contains("CREATE VIEW") || converted.toUpperCase().contains("CREATE OR REPLACE VIEW")) {
            converted = convertViews(converted, warnings, recommendations);
        }
        
        // Handle Triggers
        if (converted.toUpperCase().contains("CREATE TRIGGER")) {
            converted = convertTriggers(converted, warnings, recommendations);
        }
        
        // Handle Stored Procedures/Functions
        if (converted.toUpperCase().contains("CREATE FUNCTION") || converted.toUpperCase().contains("CREATE PROCEDURE")) {
            converted = convertStoredProcedures(converted, warnings, recommendations);
        }
        
        // Generate Flyway migration
        String flywayMigration = generateFlywayMigration(converted);
        recommendations.add("Flyway migration script generated");
        recommendations.add("Test migration on a copy of production data");
        recommendations.add("Review all data type conversions carefully");
        
        return new MigrationResult(
            converted,
            flywayMigration,
            warnings,
            recommendations
        );
    }

    private String convertViews(String sql, List<String> warnings, List<String> recommendations) {
        String converted = sql;
        
        // PostgreSQL: CREATE OR REPLACE VIEW → DB2: CREATE OR REPLACE VIEW (same syntax)
        // Materialized views need special handling
        if (converted.toUpperCase().contains("MATERIALIZED VIEW")) {
            warnings.add("MATERIALIZED VIEW detected - DB2 uses different syntax (CREATE TABLE AS with REFRESH)");
            recommendations.add("Convert materialized views to regular tables with refresh procedures");
            recommendations.add("Consider using DB2 MQTs (Materialized Query Tables) instead");
            
            // Add comment to indicate manual conversion needed
            converted = "-- MANUAL CONVERSION REQUIRED: Materialized View\n" +
                       "-- PostgreSQL materialized views must be converted to DB2 MQTs or regular tables\n" +
                       "-- Original PostgreSQL code:\n" +
                       converted.lines()
                           .map(line -> "-- " + line)
                           .collect(java.util.stream.Collectors.joining("\n")) +
                       "\n\n-- DB2 Alternative 1: Use MQT (Materialized Query Table)\n" +
                       "-- CREATE TABLE view_name AS (\n" +
                       "--   SELECT ... FROM ...\n" +
                       "-- ) DATA INITIALLY DEFERRED REFRESH DEFERRED;\n" +
                       "-- REFRESH TABLE view_name;\n\n" +
                       "-- DB2 Alternative 2: Use regular table with refresh procedure\n" +
                       "-- CREATE TABLE view_name AS (SELECT ... FROM ...) WITH NO DATA;\n" +
                       "-- CREATE PROCEDURE refresh_view_name() ...\n";
        } else {
            // Regular views work similarly in DB2
            warnings.add("VIEW converted - syntax is similar but verify complex queries");
            recommendations.add("Test view queries with DB2 to ensure compatibility");
        }
        
        return converted;
    }

    private String convertTriggers(String sql, List<String> warnings, List<String> recommendations) {
        String converted = sql;
        
        warnings.add("TRIGGER detected - DB2 trigger syntax differs from PostgreSQL");
        recommendations.add("Review trigger timing (BEFORE/AFTER/INSTEAD OF)");
        recommendations.add("Test trigger logic thoroughly in DB2");
        
        // PostgreSQL: FOR EACH ROW → DB2: FOR EACH ROW (same)
        // PostgreSQL: EXECUTE PROCEDURE → DB2: Different syntax
        
        if (converted.contains("EXECUTE PROCEDURE") || converted.contains("EXECUTE FUNCTION")) {
            converted = converted.replaceAll("EXECUTE PROCEDURE", "-- EXECUTE PROCEDURE (needs conversion)");
            converted = converted.replaceAll("EXECUTE FUNCTION", "-- EXECUTE FUNCTION (needs conversion)");
            
            warnings.add("EXECUTE PROCEDURE/FUNCTION must be converted to DB2 trigger body");
            
            // Add conversion template
            converted = converted + "\n\n" + """
                -- DB2 Trigger Syntax Template:
                -- CREATE TRIGGER trigger_name
                -- AFTER INSERT ON table_name
                -- REFERENCING NEW AS new_row
                -- FOR EACH ROW
                -- BEGIN ATOMIC
                --   -- Trigger logic here
                --   -- Access new values: new_row.column_name
                --   -- Access old values: old_row.column_name (for UPDATE/DELETE)
                -- END;
                """;
        }
        
        // PostgreSQL NEW/OLD → DB2 uses REFERENCING clause
        if (converted.contains("NEW.") || converted.contains("OLD.")) {
            warnings.add("NEW/OLD references must use DB2 REFERENCING clause");
            recommendations.add("Use REFERENCING NEW AS new_row OLD AS old_row");
        }
        
        return converted;
    }

    private String convertStoredProcedures(String sql, List<String> warnings, List<String> recommendations) {
        String converted = sql;
        
        boolean isFunction = converted.toUpperCase().contains("CREATE FUNCTION");
        boolean isProcedure = converted.toUpperCase().contains("CREATE PROCEDURE");
        
        if (isFunction) {
            warnings.add("FUNCTION detected - DB2 uses different syntax for functions");
            recommendations.add("PostgreSQL functions may need to be converted to DB2 procedures or SQL functions");
        }
        
        if (isProcedure) {
            warnings.add("PROCEDURE detected - DB2 procedure syntax differs from PostgreSQL");
        }
        
        // PostgreSQL: LANGUAGE plpgsql → DB2: LANGUAGE SQL
        if (converted.contains("LANGUAGE plpgsql")) {
            converted = converted.replace("LANGUAGE plpgsql", "LANGUAGE SQL");
            warnings.add("LANGUAGE plpgsql converted to LANGUAGE SQL - logic may need adjustment");
        }
        
        // PostgreSQL: RETURNS → DB2: Different syntax
        if (converted.contains("RETURNS")) {
            warnings.add("RETURNS clause needs conversion - DB2 uses OUT parameters or RETURN statement");
            recommendations.add("Convert RETURNS to OUT parameters or use CREATE FUNCTION with RETURNS");
        }
        
        // PostgreSQL: $$ delimiter → DB2: Different delimiter
        if (converted.contains("$$")) {
            converted = converted.replace("$$", "");
            warnings.add("$$ delimiter removed - DB2 uses different delimiters");
            recommendations.add("Use @ or ! as statement delimiter in DB2");
        }
        
        // PostgreSQL: DECLARE → DB2: DECLARE (similar but check syntax)
        if (converted.contains("DECLARE")) {
            warnings.add("DECLARE syntax may differ - verify variable declarations");
        }
        
        // PostgreSQL: RAISE EXCEPTION → DB2: SIGNAL SQLSTATE
        if (converted.contains("RAISE EXCEPTION") || converted.contains("RAISE NOTICE")) {
            converted = converted.replaceAll("RAISE EXCEPTION", "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT =");
            converted = converted.replaceAll("RAISE NOTICE", "-- RAISE NOTICE (use DBMS_OUTPUT or logging)");
            warnings.add("RAISE EXCEPTION converted to SIGNAL SQLSTATE");
            warnings.add("RAISE NOTICE removed - use DB2 logging mechanisms");
        }
        
        // Add conversion template
        converted = converted + "\n\n" + """
            -- DB2 Procedure/Function Template:
            -- CREATE PROCEDURE procedure_name (
            --   IN param1 VARCHAR(100),
            --   OUT result INTEGER
            -- )
            -- LANGUAGE SQL
            -- BEGIN
            --   -- Procedure logic here
            --   DECLARE variable_name INTEGER;
            --   SET variable_name = 0;
            --   SET result = variable_name;
            -- END @
            
            -- For functions that return a value:
            -- CREATE FUNCTION function_name (param1 VARCHAR(100))
            -- RETURNS INTEGER
            -- LANGUAGE SQL
            -- BEGIN ATOMIC
            --   DECLARE result INTEGER;
            --   SET result = 0;
            --   RETURN result;
            -- END @
            """;
        
        recommendations.add("Test stored procedures/functions thoroughly in DB2");
        recommendations.add("Review control flow statements (IF, LOOP, WHILE)");
        recommendations.add("Verify exception handling logic");
        
        return converted;
    }

    private String generateFlywayMigration(String ddl) {
        return String.format("""
            -- Flyway Migration: V1__initial_schema.sql
            -- Generated by Java to DB2 Migration Assistant
            -- Date: %s
            
            %s
            
            -- End of migration
            """, java.time.LocalDateTime.now(), ddl);
    }
}

// Made with Bob
