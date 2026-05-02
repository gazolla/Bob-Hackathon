#!/bin/bash

# Real-world Migration Test Script
# Tests MCP tools with actual code from todo-java-postgres application

BASE_URL="http://localhost:9090/mcp"

echo "=========================================="
echo "Real-world Migration Testing"
echo "=========================================="
echo ""

# Test 1: Convert actual Task entity schema
echo "1. Converting Task table schema from todo-java-postgres..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, status VARCHAR(20) NOT NULL DEFAULT '\''PENDING'\'', completed BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 2: Convert actual repository query
echo "2. Converting Task repository query..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresQueryToDb2",
      "arguments": {
        "querySQL": "SELECT * FROM tasks WHERE status = '\''PENDING'\'' AND completed = FALSE ORDER BY created_at DESC LIMIT 10;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 3: Convert complex search query
echo "3. Converting case-insensitive search query..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresQueryToDb2",
      "arguments": {
        "querySQL": "SELECT * FROM tasks WHERE title ILIKE '\''%urgent%'\'' OR description ILIKE '\''%urgent%'\'' ORDER BY updated_at DESC OFFSET 5 LIMIT 20;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 4: Generate DB2 config for todo app
echo "4. Generating DB2 configuration for todo application..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "generateDb2ConnectionConfig",
      "arguments": {
        "credentials": "{\"hostname\":\"dashdb-txn-sbox-yp-lon02-01.services.eu-gb.bluemix.net\",\"port\":\"50000\",\"database\":\"BLUDB\",\"username\":\"admin\",\"password\":\"MySecurePassword123\",\"ssl\":\"true\"}"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 5: Generate tests for Task entity
echo "5. Generating test suite for Task entity..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "generateMigrationTestSuite",
      "arguments": {
        "entityCode": "@Entity @Table(name = \"tasks\") public class Task { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; @Column(nullable = false) private String title; @Column(columnDefinition = \"TEXT\") private String description; @Enumerated(EnumType.STRING) private TaskStatus status; private Boolean completed; @CreationTimestamp private LocalDateTime createdAt; @UpdateTimestamp private LocalDateTime updatedAt; }"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 6: Convert JPA query method
echo "6. Converting JPA repository method..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 6,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresQueryToDb2",
      "arguments": {
        "querySQL": "@Query(\"SELECT t FROM Task t WHERE t.completed = :completed AND t.status IN :statuses ORDER BY t.createdAt DESC\") List<Task> findByCompletedAndStatusIn(@Param(\"completed\") Boolean completed, @Param(\"statuses\") List<TaskStatus> statuses);"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 7: Convert migration script
echo "7. Converting Flyway migration script..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 7,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TABLE tasks (id BIGSERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, status VARCHAR(20) NOT NULL DEFAULT '\''PENDING'\'', completed BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP); CREATE INDEX idx_tasks_status ON tasks(status); CREATE INDEX idx_tasks_completed ON tasks(completed); CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

echo "=========================================="
echo "All real-world tests completed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review the converted schemas and queries"
echo "2. Apply the DB2 configuration to your application"
echo "3. Run the generated test suites"
echo "4. Test with actual DB2 database"
echo ""

# Made with Bob
