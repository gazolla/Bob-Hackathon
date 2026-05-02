package com.ibm.migration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

/**
 * MCP Client Simulation Test
 * Demonstrates how to interact with the MCP server
 */
@QuarkusTest
public class McpClientSimulationTest {

    @Test
    public void testMcpHealthCheck() {
        given()
            .when().get("/mcp/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("server", equalTo("Java to DB2 Migration Assistant MCP Server"))
                .body("version", equalTo("1.0.0"));
    }

    @Test
    public void testMcpInitialize() {
        String initRequest = """
            {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "initialize",
                "params": {
                    "protocolVersion": "2024-11-05",
                    "clientInfo": {
                        "name": "Test Client",
                        "version": "1.0.0"
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(initRequest)
            .when().post("/mcp/initialize")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(1))
                .body("result.protocolVersion", equalTo("2024-11-05"))
                .body("result.serverInfo.name", equalTo("Java to DB2 Migration Assistant"))
                .body("result.serverInfo.version", equalTo("1.0.0"));
    }

    @Test
    public void testMcpToolsList() {
        String listRequest = """
            {
                "jsonrpc": "2.0",
                "id": 2,
                "method": "tools/list",
                "params": {}
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(listRequest)
            .when().post("/mcp/tools/list")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(2))
                .body("result.tools.size()", equalTo(4))
                .body("result.tools[0].name", equalTo("convertPostgresSchemaToDb2"))
                .body("result.tools[1].name", equalTo("convertPostgresQueryToDb2"))
                .body("result.tools[2].name", equalTo("generateDb2ConnectionConfig"))
                .body("result.tools[3].name", equalTo("generateMigrationTestSuite"));
    }

    @Test
    public void testMcpToolCallConvertSchema() {
        String callRequest = """
            {
                "jsonrpc": "2.0",
                "id": 3,
                "method": "tools/call",
                "params": {
                    "name": "convertPostgresSchemaToDb2",
                    "arguments": {
                        "schemaSQL": "CREATE TABLE users (id SERIAL PRIMARY KEY, name TEXT, active BOOLEAN DEFAULT TRUE);"
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(callRequest)
            .when().post("/mcp/tools/call")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(3))
                .body("result.content[0].type", equalTo("text"))
                .body("result.content[0].text", containsString("GENERATED ALWAYS AS IDENTITY"))
                .body("result.content[0].text", containsString("CLOB"))
                .body("result.content[0].text", containsString("SMALLINT"));
    }

    @Test
    public void testMcpToolCallConvertQuery() {
        String callRequest = """
            {
                "jsonrpc": "2.0",
                "id": 4,
                "method": "tools/call",
                "params": {
                    "name": "convertPostgresQueryToDb2",
                    "arguments": {
                        "querySQL": "SELECT * FROM users WHERE email ILIKE '%@example.com%' AND active = TRUE LIMIT 10 OFFSET 20;"
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(callRequest)
            .when().post("/mcp/tools/call")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(4))
                .body("result.content[0].type", equalTo("text"))
                .body("result.content[0].text", containsString("UPPER"))
                .body("result.content[0].text", containsString("FETCH FIRST"));
    }

    @Test
    public void testMcpToolCallGenerateConfig() {
        String callRequest = """
            {
                "jsonrpc": "2.0",
                "id": 5,
                "method": "tools/call",
                "params": {
                    "name": "generateDb2ConnectionConfig",
                    "arguments": {
                        "credentials": "{\\"hostname\\":\\"db2.cloud.ibm.com\\",\\"port\\":\\"50000\\",\\"database\\":\\"mydb\\"}"
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(callRequest)
            .when().post("/mcp/tools/call")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(5))
                .body("result.content[0].type", equalTo("text"))
                .body("result.content[0].text", containsString("spring.datasource.url"))
                .body("result.content[0].text", containsString("DB2Driver"));
    }

    @Test
    public void testMcpToolCallGenerateTests() {
        String callRequest = """
            {
                "jsonrpc": "2.0",
                "id": 6,
                "method": "tools/call",
                "params": {
                    "name": "generateMigrationTestSuite",
                    "arguments": {
                        "entityCode": "@Entity public class User { @Id private Long id; private String name; }"
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(callRequest)
            .when().post("/mcp/tools/call")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(6))
                .body("result.content[0].type", equalTo("text"))
                .body("result.content[0].text", containsString("@SpringBootTest"))
                .body("result.content[0].text", containsString("testEntityPersistence"));
    }

    @Test
    public void testMcpToolCallInvalidTool() {
        String callRequest = """
            {
                "jsonrpc": "2.0",
                "id": 7,
                "method": "tools/call",
                "params": {
                    "name": "nonExistentTool",
                    "arguments": {}
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(callRequest)
            .when().post("/mcp/tools/call")
            .then()
                .statusCode(200)
                .body("jsonrpc", equalTo("2.0"))
                .body("id", equalTo(7))
                .body("error.code", equalTo(-32603))
                .body("error.message", containsString("Unknown tool"));
    }
}

// Made with Bob
