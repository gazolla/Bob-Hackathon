package com.ibm.migration.mcp;

import com.ibm.migration.mcp.model.McpRequest;
import com.ibm.migration.mcp.model.McpResponse;
import com.ibm.migration.mcp.model.McpTool;
import com.ibm.migration.service.*;
import com.ibm.migration.model.MigrationResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * MCP Server implementing the Model Context Protocol
 * Provides tools for PostgreSQL to DB2 migration
 */
@Path("/mcp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpServer {

    private static final Logger LOG = Logger.getLogger(McpServer.class);

    @Inject
    SchemaConversionService schemaConversionService;

    @Inject
    QueryConversionService queryConversionService;

    @Inject
    ConfigGenerationService configGenerationService;

    @Inject
    TestGenerationService testGenerationService;

    /**
     * MCP Initialize - Returns server capabilities
     */
    @POST
    @Path("/initialize")
    public McpResponse initialize(McpRequest request) {
        LOG.info("MCP Initialize request received");
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("serverInfo", Map.of(
            "name", "Java to DB2 Migration Assistant",
            "version", "1.0.0"
        ));
        result.put("capabilities", Map.of(
            "tools", Map.of()
        ));

        return new McpResponse(request.getId(), result);
    }

    /**
     * MCP Tools List - Returns available tools
     */
    @POST
    @Path("/tools/list")
    public McpResponse listTools(McpRequest request) {
        LOG.info("MCP Tools List request received");

        List<McpTool> tools = Arrays.asList(
            createTool(
                "convertPostgresSchemaToDb2",
                "Converts PostgreSQL database schema (DDL) to DB2-compatible syntax with Flyway migration scripts. Handles data type conversions (SERIAL→IDENTITY, TEXT→CLOB, BOOLEAN→SMALLINT, etc.), index and constraint adjustments.",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "schemaSQL", Map.of(
                            "type", "string",
                            "description", "PostgreSQL DDL statements (CREATE TABLE, CREATE INDEX, etc.)"
                        )
                    ),
                    "required", List.of("schemaSQL")
                )
            ),
            createTool(
                "convertPostgresQueryToDb2",
                "Converts PostgreSQL SQL queries and JPA code to DB2-compatible syntax. Handles string concatenation (||→CONCAT), case-insensitive search (ILIKE→LIKE), boolean logic, pagination (LIMIT/OFFSET→FETCH FIRST), date/time functions, and Spring Data JPA adjustments.",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "querySQL", Map.of(
                            "type", "string",
                            "description", "PostgreSQL SQL query or JPA code to convert"
                        )
                    ),
                    "required", List.of("querySQL")
                )
            ),
            createTool(
                "generateDb2ConnectionConfig",
                "Generates complete DB2 connection configuration from IBM Cloud credentials. Provides application.properties with JDBC URL, Hibernate dialect, Flyway setup, connection pool configuration, Maven dependencies, and SSL configuration.",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "credentials", Map.of(
                            "type", "string",
                            "description", "IBM Cloud DB2 credentials JSON (hostname, port, database, username, password, ssl)"
                        )
                    ),
                    "required", List.of("credentials")
                )
            ),
            createTool(
                "generateMigrationTestSuite",
                "Generates comprehensive JUnit 5 test suites for validating DB2 migration. Creates entity persistence tests, repository query tests, service layer tests, DB2-specific tests, and test configuration for H2 (DB2 mode) or Testcontainers.",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "entityCode", Map.of(
                            "type", "string",
                            "description", "Java entity class code to generate tests for"
                        )
                    ),
                    "required", List.of("entityCode")
                )
            )
        );

        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);

        return new McpResponse(request.getId(), result);
    }

    /**
     * MCP Tools Call - Executes a tool
     */
    @POST
    @Path("/tools/call")
    public McpResponse callTool(McpRequest request) {
        try {
            Map<String, Object> params = request.getParams();
            String toolName = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

            LOG.infof("MCP Tool Call: %s", toolName);

            Object result = switch (toolName) {
                case "convertPostgresSchemaToDb2" -> {
                    String schemaSQL = (String) arguments.get("schemaSQL");
                    yield schemaConversionService.convertSchema(schemaSQL);
                }
                case "convertPostgresQueryToDb2" -> {
                    String querySQL = (String) arguments.get("querySQL");
                    yield queryConversionService.convertQuery(querySQL);
                }
                case "generateDb2ConnectionConfig" -> {
                    String credentials = (String) arguments.get("credentials");
                    yield configGenerationService.generateConfig(credentials);
                }
                case "generateMigrationTestSuite" -> {
                    String entityCode = (String) arguments.get("entityCode");
                    yield testGenerationService.generateTests(entityCode);
                }
                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };

            Map<String, Object> toolResult = new HashMap<>();
            toolResult.put("content", List.of(
                Map.of(
                    "type", "text",
                    "text", formatResult(result)
                )
            ));

            return new McpResponse(request.getId(), toolResult);

        } catch (Exception e) {
            LOG.error("Error executing tool", e);
            return new McpResponse(
                request.getId(),
                new McpResponse.McpError(-32603, "Internal error: " + e.getMessage())
            );
        }
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "server", "Java to DB2 Migration Assistant MCP Server",
            "version", "1.0.0",
            "protocol", "MCP 2024-11-05"
        );
    }

    private McpTool createTool(String name, String description, Map<String, Object> inputSchema) {
        return new McpTool(name, description, inputSchema);
    }

    private String formatResult(Object result) {
        if (result instanceof MigrationResult mr) {
            StringBuilder sb = new StringBuilder();
            sb.append("# Migration Result\n\n");
            sb.append("## Converted Code\n```sql\n");
            sb.append(mr.getConvertedCode());
            sb.append("\n```\n\n");
            
            if (mr.getWarnings() != null && !mr.getWarnings().isEmpty()) {
                sb.append("## Warnings\n");
                mr.getWarnings().forEach(w -> sb.append("- ").append(w).append("\n"));
                sb.append("\n");
            }
            
            if (mr.getRecommendations() != null && !mr.getRecommendations().isEmpty()) {
                sb.append("## Recommendations\n");
                mr.getRecommendations().forEach(r -> sb.append("- ").append(r).append("\n"));
            }
            
            return sb.toString();
        }
        return result.toString();
    }
}

// Made with Bob
