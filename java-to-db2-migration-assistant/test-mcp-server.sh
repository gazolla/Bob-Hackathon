#!/bin/bash

# MCP Server Test Script
# Tests the Java to DB2 Migration Assistant MCP Server

BASE_URL="http://localhost:9090/mcp"

echo "=========================================="
echo "MCP Server Test Suite"
echo "=========================================="
echo ""

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/health" | jq '.'
echo ""
echo ""

# Test 2: Initialize
echo "2. Testing MCP Initialize..."
curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
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
  }' | jq '.'
echo ""
echo ""

# Test 3: List Tools
echo "3. Testing Tools List..."
curl -s -X POST "$BASE_URL/tools/list" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }' | jq '.'
echo ""
echo ""

# Test 4: Convert PostgreSQL Schema to DB2
echo "4. Testing Schema Conversion..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TABLE users (id SERIAL PRIMARY KEY, username VARCHAR(255) NOT NULL, email TEXT, active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT NOW());"
      }
    }
  }' | jq '.'
echo ""
echo ""

# Test 5: Convert PostgreSQL Query to DB2
echo "5. Testing Query Conversion..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresQueryToDb2",
      "arguments": {
        "querySQL": "SELECT * FROM users WHERE email ILIKE '\''%@example.com%'\'' AND active = TRUE ORDER BY created_at DESC LIMIT 10 OFFSET 20;"
      }
    }
  }' | jq '.'
echo ""
echo ""

# Test 6: Generate DB2 Configuration
echo "6. Testing Configuration Generation..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "generateDb2ConnectionConfig",
      "arguments": {
        "credentials": "{\"hostname\":\"db2.cloud.ibm.com\",\"port\":\"50000\",\"database\":\"mydb\",\"username\":\"admin\",\"password\":\"secret123\",\"ssl\":\"true\"}"
      }
    }
  }' | jq '.'
echo ""
echo ""

# Test 7: Generate Migration Test Suite
echo "7. Testing Test Suite Generation..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 6,
    "method": "tools/call",
    "params": {
      "name": "generateMigrationTestSuite",
      "arguments": {
        "entityCode": "@Entity @Table(name = \"users\") public class User { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; private String username; @Column(columnDefinition = \"CLOB\") private String bio; private Integer active; }"
      }
    }
  }' | jq '.'
echo ""
echo ""

echo "=========================================="
echo "All tests completed!"
echo "=========================================="

# Made with Bob
