# Setup Guide - Bob Legacy Modernizer

## 🚀 Complete Installation and Setup Instructions

This guide provides detailed step-by-step instructions for setting up the Bob Legacy Modernizer MCP tool for the IBM Bob Dev Day Hackathon.

---

## 📋 Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java** | 21+ | Runtime for MCP server and demo app |
| **Maven** | 3.8+ | Build tool |
| **Docker** | Latest | PostgreSQL container |
| **Docker Compose** | Latest | Multi-container orchestration |
| **IBM Bob IDE** | Latest | AI development interface |
| **Git** | Latest | Version control |

### System Requirements

- **OS:** macOS, Linux, or Windows (with WSL2)
- **RAM:** 8GB minimum, 16GB recommended
- **Disk Space:** 5GB free space
- **Network:** Internet connection for dependencies

---

## 🔧 Installation Steps

### Step 1: Install Java 21

#### macOS (using Homebrew)
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

#### Windows
1. Download from [Adoptium](https://adoptium.net/)
2. Install and add to PATH
3. Verify: `java -version`

---

### Step 2: Install Maven

#### macOS
```bash
brew install maven
mvn -version
```

#### Linux
```bash
sudo apt update
sudo apt install maven
mvn -version
```

#### Windows
1. Download from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract and add to PATH
3. Verify: `mvn -version`

---

### Step 3: Install Docker & Docker Compose

#### macOS
```bash
# Install Docker Desktop
brew install --cask docker

# Start Docker Desktop from Applications
# Verify installation
docker --version
docker-compose --version
```

#### Linux
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
docker-compose --version
```

#### Windows
1. Install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. Enable WSL2 backend
3. Verify in PowerShell: `docker --version`

---

### Step 4: Install IBM Bob IDE

#### Download and Install

1. Visit [IBM Bob IDE Download Page](https://www.ibm.com/products/bob)
2. Download for your operating system
3. Install following the platform-specific instructions
4. Launch Bob IDE

#### Sign In

1. Open Bob IDE
2. Click "Sign In"
3. Use your IBM Bob hackathon account credentials
4. Verify you have Bobcoins available

---

### Step 5: Clone the Repository

```bash
# Navigate to your projects directory
cd ~/Projects

# Clone the repository
git clone <your-repository-url>
cd "Bob Hackathon"

# Verify structure
ls -la
# Should see: todo-java-postgres/, java-to-db2-migration-assistant/, BobHackathon/
```

---

## 🏗️ Project Setup

### Setup 1: MCP Server (java-to-db2-migration-assistant)

#### Navigate to Project
```bash
cd java-to-db2-migration-assistant
```

#### Build the Project
```bash
# Download dependencies and compile
mvn clean install

# Expected output: BUILD SUCCESS
```

#### Run in Development Mode
```bash
mvn quarkus:dev
```

**Expected Output:**
```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2026-05-02 16:00:00,000 INFO  [io.quarkus] (Quarkus Main Thread) java-to-db2-migration-assistant 1.0.0 on JVM started in 0.856s. Listening on: http://0.0.0.0:9090
```

#### Verify MCP Server
```bash
# In a new terminal
curl http://localhost:9090/health

# Expected: {"status":"UP"}
```

#### Test MCP Endpoints
```bash
# Test tools list
curl -X POST http://localhost:9090/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'

# Should return list of 4 tools
```

---

### Setup 2: Demo Application (todo-java-postgres)

#### Navigate to Project
```bash
cd ../todo-java-postgres
```

#### Start PostgreSQL Database
```bash
# Start PostgreSQL container
docker-compose up -d postgres

# Wait for database to be ready (about 10 seconds)
sleep 10

# Verify PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres
```

#### Build the Application
```bash
# Download dependencies and compile
./mvnw clean install

# Expected output: BUILD SUCCESS
```

#### Run the Application
```bash
./mvnw spring-boot:run
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.0)

2026-05-02 16:01:00.000  INFO 12345 --- [           main] c.e.todo.TodoApplication                 : Started TodoApplication in 3.456 seconds
```

#### Verify Application
```bash
# In a new terminal
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}

# Test API
curl http://localhost:8080/api/tasks

# Expected: [] (empty array initially)
```

#### Create Test Data
```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Migration",
    "description": "Testing PostgreSQL to DB2 migration",
    "status": "PENDING",
    "dueDate": "2026-05-10T15:00:00"
  }'

# Get all tasks
curl http://localhost:8080/api/tasks
```

---

### Setup 3: Configure Bob IDE with MCP Server

#### Verify MCP Configuration File

The `.bob/mcp.json` file should already exist in the `java-to-db2-migration-assistant` directory:

```bash
cd java-to-db2-migration-assistant
cat .bob/mcp.json
```

**Expected Content:**
```json
{
  "mcpServers": {
    "java-to-db2-migration-assistant": {
      "url": "http://localhost:9090/mcp",
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

#### Connect Bob IDE to MCP Server

**Option 1: Automatic Discovery (Recommended)**

1. Open Bob IDE
2. Open the `java-to-db2-migration-assistant` folder
3. Bob should automatically detect the `.bob/mcp.json` file
4. Click "Connect" when prompted

**Option 2: Manual Configuration**

1. Open Bob IDE
2. Go to Settings → MCP Servers
3. Click "Add Server"
4. Enter:
   - **Name:** Java to DB2 Migration Assistant
   - **URL:** `http://localhost:9090/mcp`
   - **Transport:** SSE
   - **Description:** PostgreSQL to DB2 migration tools
5. Click "Save"
6. Click "Connect"

#### Verify Connection

In Bob IDE chat, type:
```
What tools do you have available for database migration?
```

Bob should respond with information about the 4 migration tools.

---

## ✅ Verification Checklist

### MCP Server Verification

- [ ] Server starts without errors
- [ ] Health endpoint responds: `curl http://localhost:9090/health`
- [ ] Tools list endpoint works
- [ ] All 4 tools are listed

### Demo Application Verification

- [ ] PostgreSQL container is running: `docker-compose ps`
- [ ] Application starts without errors
- [ ] Health endpoint responds: `curl http://localhost:8080/actuator/health`
- [ ] Can create tasks via API
- [ ] Can retrieve tasks via API

### Bob IDE Verification

- [ ] Bob IDE is running
- [ ] Signed in with hackathon account
- [ ] MCP server is connected
- [ ] Can see migration tools in Bob's capabilities
- [ ] Can execute a test conversion

---

## 🧪 Test the Complete Setup

### Test 1: Schema Conversion

In Bob IDE, type:
```
Convert this PostgreSQL schema to DB2:

CREATE TABLE test (
    id SERIAL PRIMARY KEY,
    name TEXT,
    active BOOLEAN
);
```

**Expected:** Bob responds with DB2-compatible schema, warnings, and recommendations.

### Test 2: Query Conversion

In Bob IDE, type:
```
Convert this query to DB2:

SELECT * FROM test WHERE name ILIKE '%test%' AND active = TRUE LIMIT 5;
```

**Expected:** Bob responds with DB2-compatible query.

### Test 3: Configuration Generation

In Bob IDE, type:
```
Generate DB2 configuration for hostname: localhost, port: 50000, database: testdb
```

**Expected:** Bob responds with complete application.properties and Maven dependencies.

---

## 🐛 Troubleshooting

### Issue: Port 9090 Already in Use

**Solution:**
```bash
# Find process using port 9090
lsof -ti:9090

# Kill the process
kill -9 $(lsof -ti:9090)

# Or change port in application.properties
echo "quarkus.http.port=8090" >> src/main/resources/application.properties
```

### Issue: Port 8080 Already in Use

**Solution:**
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9

# Or change port
echo "server.port=8081" >> src/main/resources/application.properties
```

### Issue: PostgreSQL Container Won't Start

**Solution:**
```bash
# Stop all containers
docker-compose down

# Remove volumes
docker-compose down -v

# Restart
docker-compose up -d postgres

# Check logs
docker-compose logs -f postgres
```

### Issue: Maven Build Fails

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U

# If still fails, check Java version
java -version  # Should be 21+
```

### Issue: Bob IDE Can't Connect to MCP Server

**Solution:**
1. Verify MCP server is running: `curl http://localhost:9090/health`
2. Check `.bob/mcp.json` syntax (valid JSON)
3. Restart Bob IDE
4. Check Bob IDE logs for connection errors
5. Verify firewall isn't blocking port 9090

### Issue: Quarkus Dev Mode Fails

**Solution:**
```bash
# Check for conflicting processes
ps aux | grep quarkus

# Kill any existing Quarkus processes
pkill -f quarkus

# Clear target directory
rm -rf target/

# Rebuild and run
mvn clean quarkus:dev
```

---

## 🔄 Restart Everything

If you need to restart the entire setup:

```bash
# Stop all services
cd todo-java-postgres
docker-compose down
pkill -f spring-boot
cd ../java-to-db2-migration-assistant
pkill -f quarkus

# Wait a moment
sleep 5

# Start MCP Server
cd java-to-db2-migration-assistant
mvn quarkus:dev &

# Start PostgreSQL
cd ../todo-java-postgres
docker-compose up -d postgres
sleep 10

# Start Demo App
./mvnw spring-boot:run &

# Verify all services
curl http://localhost:9090/health
curl http://localhost:8080/actuator/health
```

---

## 📊 Resource Usage

### Expected Resource Consumption

| Component | CPU | Memory | Disk |
|-----------|-----|--------|------|
| MCP Server (Quarkus) | 5-10% | 50-100MB | 200MB |
| Demo App (Spring Boot) | 10-15% | 200-300MB | 300MB |
| PostgreSQL | 5-10% | 50-100MB | 100MB |
| **Total** | **20-35%** | **300-500MB** | **600MB** |

---

## 🎯 Next Steps

After successful setup:

1. ✅ Review the [DEMO.md](./DEMO.md) for demonstration scenarios
2. ✅ Read the [ARCHITECTURE.md](./ARCHITECTURE.md) for technical details
3. ✅ Explore the [README.md](./README.md) for project overview
4. ✅ Check the [SUBMISSION.md](./SUBMISSION.md) for hackathon details
5. ✅ Export Bob task sessions to `bob_sessions/` folder

---

## 📞 Support

### Getting Help

- **Documentation:** Check all markdown files in `BobHackathon/` folder
- **Logs:** Review terminal output for error messages
- **Health Checks:** Use curl commands to verify services
- **Community:** Ask in hackathon Slack/Discord channel

### Useful Commands

```bash
# Check all services status
curl http://localhost:9090/health && \
curl http://localhost:8080/actuator/health && \
docker-compose ps

# View logs
docker-compose logs -f postgres
tail -f java-to-db2-migration-assistant/target/quarkus.log

# Stop all services
docker-compose down
pkill -f quarkus
pkill -f spring-boot
```

---

## 🎓 Learning Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [IBM Bob IDE Documentation](https://www.ibm.com/docs/bob)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [IBM DB2 Documentation](https://www.ibm.com/docs/en/db2)

---

**Setup Complete! 🎉**

You're now ready to demonstrate the Bob Legacy Modernizer at the IBM Bob Dev Day Hackathon!