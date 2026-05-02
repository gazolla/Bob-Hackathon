package com.ibm.migration.model;

import java.util.List;

/**
 * Result of a migration operation
 */
public class MigrationResult {
    
    private String convertedCode;
    private String flywayMigration;
    private List<String> warnings;
    private List<String> recommendations;

    public MigrationResult() {
    }

    public MigrationResult(String convertedCode) {
        this.convertedCode = convertedCode;
    }

    public MigrationResult(String convertedCode, String flywayMigration, List<String> warnings, List<String> recommendations) {
        this.convertedCode = convertedCode;
        this.flywayMigration = flywayMigration;
        this.warnings = warnings;
        this.recommendations = recommendations;
    }

    public String getConvertedCode() {
        return convertedCode;
    }

    public void setConvertedCode(String convertedCode) {
        this.convertedCode = convertedCode;
    }

    public String getFlywayMigration() {
        return flywayMigration;
    }

    public void setFlywayMigration(String flywayMigration) {
        this.flywayMigration = flywayMigration;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}

// Made with Bob
