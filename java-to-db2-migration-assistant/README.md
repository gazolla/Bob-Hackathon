# Java to DB2 Migration Assistant - MCP Server

A Quarkus-based MCP (Model Context Protocol) server that provides AI-powered assistance for migrating Java applications from PostgreSQL to IBM DB2.

## 🚀 Features

This MCP server exposes 4 powerful tools to IBM Bob AI assistant:

### 1. **convertPostgresSchemaToDb2**
Converts PostgreSQL database schemas (DDL) to DB2-compatible syntax with Flyway migration scripts.

**Handles:**
- SERIAL/BIGSERIAL → INTEGER/BIGINT GENERATED ALWAYS AS IDENTITY
- TEXT → CLOB
- BOOLEAN → SMALLINT (0/1)
- BYTEA → BLOB
- UUID → CHAR(36)
- JSONB → CLOB
- Timestamp and date type conversions
- Index and constraint syntax adjustments

### 2. **convertPostgresQueryToDb2**
Converts PostgreSQL SQL queries and JPA code to DB2-compatible syntax.

**Handles:**
- String concatenation (|| → CONCAT)
- Case-insensitive search (ILIKE → LIKE)
- Boolean logic (TRUE/FALSE → 1/0)
- Pagination (LIMIT/OFFSET → FETCH FIRST/OFFSET ROWS)
- Date/time functions (NOW() → CURRENT TIMESTAMP)
- Array and JSON operations
- Spring Data JPA adjustments

### 3. **generateDb2ConnectionConfig**
Generates complete DB2 connection configuration from IBM Cloud credentials.

**Provides:**
- application.properties with JDBC URL, Hibernate dialect, Flyway setup
- Connection pool configuration
- Maven dependencies (DB2 JDBC driver, Quarkus/Spring Data JPA, Flyway)
- SSL configuration support

### 4. **generateMigrationTestSuite**
Generates comprehensive JUnit 5 test suites for validating DB2 migration.

**Creates:**
- Entity persistence tests (IDENTITY columns, CLOB fields, boolean conversion)
- Repository query tests (pagination, case-insensitive search)
- Service layer tests (transactions, rollback)
- DB2-specific tests (connection, data types, functions)
- Test configuration for H2 (DB2 mode) or Testcontainers

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** (optional, for containerized deployment)

## 🏗️ Project Structure

```
java-to-db2-migration-assistant/
├── src/main/java/com/ibm/migration/
│   ├── tools/
│   │   └── Db2MigrationTools.java          # MCP Tool definitions
│   ├── service/
│   │   ├── SchemaConversionService.java    # Schema conversion logic
│   │   ├── QueryConversionService.java     # Query conversion logic
│   │   ├── ConfigGenerationService.java    # Config generation logic
│   │   └── TestGenerationService.java      # Test generation logic
│   └── model/
│       └── MigrationResult.java            # Result model
├── src/main/resources/
│   └── application.properties              # Quarkus configuration
├── .bob/
│   └── mcp.json                            # Bob MCP configuration
├── pom.xml                                 # Maven dependencies
└── README.md                               # This file
```

## 🚀 Getting Started

### 1. Clone or Create the Project

```bash
cd java-to-db2-migration-assistant
```

### 2. Run in Development Mode

```bash
mvn quarkus:dev
```

The MCP server will start on `http://localhost:8080` with:
- SSE (Server-Sent Events) transport enabled
- CORS enabled for Bob integration
- Health check at `/health`

### 3. Verify the Server

```bash
# Check health
curl http://localhost:8080/health

# Check MCP endpoint
curl http://localhost:8080/mcp
```

## 🤖 Integrating with IBM Bob

### Option 1: Using .bob/mcp.json (Recommended)

Create or update `.bob/mcp.json` in your project root:

```json
{
  "mcpServers": {
    "java-to-db2-migration-assistant": {
      "url": "http://localhost:8080/mcp",
      "transport": "sse",
      "description": "AI-powered assistant for migrating Java applications from PostgreSQL to IBM DB2",
      "tools": [
        "convertPostgresSchemaToDb2",
        "convertPostgresQueryToDb2",
        "generateDb2ConnectionConfig",
        "generateMigrationTestSuite"
      ]
    }
  }
}
```

### Option 2: Manual Registration in Bob

1. Open IBM Bob
2. Go to Settings → MCP Servers
3. Add new server:
   - **Name**: Java to DB2 Migration Assistant
   - **URL**: `http://localhost:8080/mcp`
   - **Transport**: SSE
   - **Description**: PostgreSQL to DB2 migration tools

### Option 3: Environment Variable

```bash
export BOB_MCP_SERVERS='{"java-to-db2-migration-assistant":{"url":"http://localhost:8080/mcp","transport":"sse"}}'
```

## 📖 Usage Examples

### Example 1: Convert PostgreSQL Schema

**Ask Bob:**
```
"Convert this PostgreSQL schema to DB2:

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);
"
```

**Bob will use:** `convertPostgresSchemaToDb2`

**Result:**
- DB2-compatible DDL
- Flyway migration script
- Warnings about data type changes
- Optimization recommendations

### Example 2: Convert Query

**Ask Bob:**
```
"Convert this PostgreSQL query to DB2:

SELECT * FROM users 
WHERE email ILIKE '%@example.com%' 
  AND active = TRUE 
ORDER BY created_at DESC 
LIMIT 10 OFFSET 20;
"
```

**Bob will use:** `convertPostgresQueryToDb2`

**Result:**
- DB2-compatible query
- Spring Data JPA adjustments
- Warnings about syntax changes

### Example 3: Generate DB2 Configuration

**Ask Bob:**
```
"Generate DB2 configuration for these IBM Cloud credentials:
{
  'hostname': 'db2.cloud.ibm.com',
  'port': '50000',
  'database': 'mydb',
  'username': 'admin',
  'password': 'secret123',
  'ssl': 'true'
}
"
```

**Bob will use:** `generateDb2ConnectionConfig`

**Result:**
- Complete application.properties
- Maven dependencies
- Connection pool configuration
- Security recommendations

### Example 4: Generate Test Suite

**Ask Bob:**
```
"Generate tests for this migrated entity:

@Entity
@Table(name = 'users')
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @Column(columnDefinition = 'CLOB')
    private String bio;
    
    private Integer active; // Was Boolean in PostgreSQL
}
"
```

**Bob will use:** `generateMigrationTestSuite`

**Result:**
- JUnit 5 test class
- Entity persistence tests
- Data type conversion tests
- Test configuration

## 🔧 Configuration

### Application Properties

Key configuration options in `src/main/resources/application.properties`:

```properties
# HTTP Configuration
quarkus.http.port=8080
quarkus.http.host=0.0.0.0

# CORS - Allow Bob to connect
quarkus.http.cors=true
quarkus.http.cors.origins=*

# MCP Server
quarkus.mcp.server.transport=sse
quarkus.mcp.server.name=Java to DB2 Migration Assistant

# Logging
quarkus.log.level=INFO
quarkus.log.category."com.ibm.migration".level=DEBUG
```

### Customization

To customize the conversion logic, edit the service classes:
- `SchemaConversionService.java` - Schema conversion rules
- `QueryConversionService.java` - Query conversion rules
- `ConfigGenerationService.java` - Configuration templates
- `TestGenerationService.java` - Test generation templates

## 🐳 Docker Deployment

### Build Docker Image

```bash
mvn clean package
docker build -f src/main/docker/Dockerfile.jvm -t java-to-db2-migration-assistant .
```

### Run Container

```bash
docker run -p 8080:8080 java-to-db2-migration-assistant
```

### Docker Compose

```yaml
version: '3.8'
services:
  migration-assistant:
    image: java-to-db2-migration-assistant
    ports:
      - "8080:8080"
    environment:
      - QUARKUS_LOG_LEVEL=INFO
    restart: unless-stopped
```

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Test MCP Tools Directly

```bash
# Test schema conversion
curl -X POST http://localhost:8080/mcp/tools/convertPostgresSchemaToDb2 \
  -H "Content-Type: application/json" \
  -d '{"schemaSQL":"CREATE TABLE test (id SERIAL PRIMARY KEY);"}'
```

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:8080/health
```

### Metrics (if enabled)

```bash
curl http://localhost:8080/q/metrics
```

## 🔍 Troubleshooting

### Server Won't Start

1. Check Java version: `java -version` (must be 21+)
2. Check port 8080 is available: `lsof -i :8080`
3. Review logs: `mvn quarkus:dev`

### Bob Can't Connect

1. Verify server is running: `curl http://localhost:8080/health`
2. Check CORS configuration in `application.properties`
3. Verify `.bob/mcp.json` has correct URL
4. Check Bob logs for connection errors

### Conversion Issues

1. Check input format (valid SQL/Java code)
2. Review service logs for detailed errors
3. Test with simpler examples first
4. Check for unsupported PostgreSQL features

## 📚 Additional Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [IBM DB2 Documentation](https://www.ibm.com/docs/en/db2)
- [PostgreSQL to DB2 Migration Guide](https://www.ibm.com/docs/en/db2/11.5?topic=migration-postgresql-db2)

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new features
4. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🆘 Support

For issues or questions:
- Open an issue on GitHub
- Contact the development team
- Check the troubleshooting section above

---

**Built with ❤️ using Quarkus and MCP**