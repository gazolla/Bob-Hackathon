# Testing the Java to DB2 Migration Assistant MCP Server

This guide explains how to test all four MCP tools provided by the migration assistant.

## Prerequisites

- MCP server running on `http://localhost:9090` (start with `mvn quarkus:dev`)
- `curl` and `jq` installed for command-line testing
- Optional: Postman or similar API client for GUI testing

## Available Tools

1. **convertPostgresSchemaToDb2** - Converts PostgreSQL DDL to DB2
2. **convertPostgresQueryToDb2** - Converts PostgreSQL queries to DB2
3. **generateDb2ConnectionConfig** - Generates Spring Boot DB2 configuration
4. **generateMigrationTestSuite** - Generates JUnit test suites

## Testing Methods

### Method 1: Automated Test Scripts

#### Basic MCP Protocol Test
Tests all MCP protocol endpoints and basic tool functionality:
```bash
./test-mcp-server.sh
```

#### Real-world Migration Test
Tests with actual code from the todo-java-postgres application:
```bash
./test-real-migration.sh
```

#### Interactive Testing
Interactive menu for testing individual tools with custom input:
```bash
chmod +x test-interactive.sh
./test-interactive.sh
```

### Method 2: Direct curl Commands

#### Test Schema Conversion
```bash
curl -X POST http://localhost:9090/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TABLE users (id SERIAL PRIMARY KEY, email TEXT, active BOOLEAN);"
      }
    }
  }' | jq -r '.result.content[0].text'
```

#### Test Query Conversion
```bash
curl -X POST http://localhost:9090/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresQueryToDb2",
      "arguments": {
        "querySQL": "SELECT * FROM users WHERE email ILIKE '\''%@example.com%'\'' LIMIT 10;"
      }
    }
  }' | jq -r '.result.content[0].text'
```

#### Test Config Generation
```bash
curl -X POST http://localhost:9090/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "generateDb2ConnectionConfig",
      "arguments": {
        "credentials": "{\"hostname\":\"db2.cloud.ibm.com\",\"port\":\"50000\",\"database\":\"mydb\",\"username\":\"admin\",\"password\":\"secret\",\"ssl\":\"true\"}"
      }
    }
  }' | jq -r '.result.content[0].text'
```

#### Test Test Suite Generation
```bash
curl -X POST http://localhost:9090/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "generateMigrationTestSuite",
      "arguments": {
        "entityCode": "@Entity public class User { @Id @GeneratedValue private Long id; private String name; }"
      }
    }
  }' | jq -r '.result.content[0].text'
```

### Method 3: Using Postman or API Client

1. Import the following base URL: `http://localhost:9090/mcp`
2. Create POST requests to `/tools/call`
3. Set Content-Type header: `application/json`
4. Use the JSON-RPC 2.0 format shown in the curl examples above

### Method 4: Integration with Bob (MCP Client)

Configure Bob to use this MCP server:

```json
{
  "mcpServers": {
    "java-to-db2-migration": {
      "url": "http://localhost:9090/mcp",
      "description": "Java to DB2 Migration Assistant"
    }
  }
}
```

Then use Bob's natural language interface:
- "Convert this PostgreSQL schema to DB2"
- "Generate DB2 configuration for my application"
- "Create test suite for my Task entity"

## Example Test Cases

### 1. Schema Conversion Examples

**Simple table:**
```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10,2)
);
```

**Complex table with constraints:**
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    is_paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 2. Query Conversion Examples

**Case-insensitive search:**
```sql
SELECT * FROM products 
WHERE name ILIKE '%laptop%' 
ORDER BY price DESC 
LIMIT 10 OFFSET 20;
```

**Boolean and pagination:**
```sql
SELECT * FROM orders 
WHERE is_paid = TRUE 
AND status IN ('COMPLETED', 'SHIPPED')
ORDER BY created_at DESC
LIMIT 50;
```

### 3. Config Generation Example

**IBM Cloud DB2 credentials:**
```json
{
  "hostname": "dashdb-txn-sbox-yp-lon02-01.services.eu-gb.bluemix.net",
  "port": "50000",
  "database": "BLUDB",
  "username": "admin",
  "password": "MySecurePassword123",
  "ssl": "true"
}
```

### 4. Test Generation Example

**JPA Entity:**
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private BigDecimal price;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Boolean inStock;
}
```

## Verifying Results

### Schema Conversion
- Check that SERIAL → IDENTITY
- Verify TEXT → CLOB
- Confirm BOOLEAN → SMALLINT
- Review warnings and recommendations

### Query Conversion
- Verify ILIKE → UPPER() LIKE UPPER()
- Check LIMIT/OFFSET → FETCH FIRST
- Confirm boolean conversions (TRUE → 1, FALSE → 0)

### Config Generation
- Verify JDBC URL format
- Check Hibernate dialect (DB2Dialect)
- Review connection pool settings
- Confirm Flyway configuration

### Test Generation
- Verify test class structure
- Check repository autowiring
- Review test methods for DB2-specific scenarios
- Confirm test configuration

## Troubleshooting

### Server not responding
```bash
# Check if server is running
curl http://localhost:9090/mcp/health

# Restart server
cd java-to-db2-migration-assistant
mvn quarkus:dev
```

### jq not installed
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq
```

### Port conflict
If port 9090 is in use, update `application.properties`:
```properties
quarkus.http.port=8080
```

## Running Unit Tests

```bash
cd java-to-db2-migration-assistant
mvn test
```

All 9 tests should pass, including the MCP client simulation tests.

## Next Steps

1. Test with your actual PostgreSQL schemas and queries
2. Review generated DB2 code for accuracy
3. Apply configurations to your Spring Boot application
4. Run generated test suites against DB2 database
5. Integrate with Bob for AI-assisted migration

## Support

For issues or questions:
- Check the logs in the Quarkus dev console
- Review the test output for detailed error messages
- Consult the main README.md for architecture details