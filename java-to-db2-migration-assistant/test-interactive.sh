#!/bin/bash

# Interactive MCP Tool Testing
# Allows testing individual tools with custom input

BASE_URL="http://localhost:9090/mcp"

echo "=========================================="
echo "Interactive MCP Tool Testing"
echo "=========================================="
echo ""
echo "Available tools:"
echo "1. convertPostgresSchemaToDb2"
echo "2. convertPostgresQueryToDb2"
echo "3. generateDb2ConnectionConfig"
echo "4. generateMigrationTestSuite"
echo ""

# Function to test schema conversion
test_schema_conversion() {
    echo "Enter PostgreSQL DDL (or press Enter for example):"
    read -r schema
    if [ -z "$schema" ]; then
        schema="CREATE TABLE products (id SERIAL PRIMARY KEY, name VARCHAR(100), price DECIMAL(10,2), in_stock BOOLEAN DEFAULT TRUE);"
    fi
    
    echo ""
    echo "Converting schema..."
    curl -s -X POST "$BASE_URL/tools/call" \
      -H "Content-Type: application/json" \
      -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 1,
        \"method\": \"tools/call\",
        \"params\": {
          \"name\": \"convertPostgresSchemaToDb2\",
          \"arguments\": {
            \"schemaSQL\": \"$schema\"
          }
        }
      }" | jq -r '.result.content[0].text'
}

# Function to test query conversion
test_query_conversion() {
    echo "Enter PostgreSQL query (or press Enter for example):"
    read -r query
    if [ -z "$query" ]; then
        query="SELECT * FROM products WHERE name ILIKE '%laptop%' AND in_stock = TRUE LIMIT 5;"
    fi
    
    echo ""
    echo "Converting query..."
    curl -s -X POST "$BASE_URL/tools/call" \
      -H "Content-Type: application/json" \
      -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 2,
        \"method\": \"tools/call\",
        \"params\": {
          \"name\": \"convertPostgresQueryToDb2\",
          \"arguments\": {
            \"querySQL\": \"$query\"
          }
        }
      }" | jq -r '.result.content[0].text'
}

# Function to test config generation
test_config_generation() {
    echo "Using example DB2 credentials..."
    
    echo ""
    echo "Generating configuration..."
    curl -s -X POST "$BASE_URL/tools/call" \
      -H "Content-Type: application/json" \
      -d '{
        "jsonrpc": "2.0",
        "id": 3,
        "method": "tools/call",
        "params": {
          "name": "generateDb2ConnectionConfig",
          "arguments": {
            "credentials": "{\"hostname\":\"my-db2-server.cloud.ibm.com\",\"port\":\"50000\",\"database\":\"MYDB\",\"username\":\"dbuser\",\"password\":\"SecurePass123\",\"ssl\":\"true\"}"
          }
        }
      }' | jq -r '.result.content[0].text'
}

# Function to test test generation
test_test_generation() {
    echo "Enter entity class code (or press Enter for example):"
    read -r entity
    if [ -z "$entity" ]; then
        entity="@Entity @Table(name = \"products\") public class Product { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; private String name; private BigDecimal price; private Boolean inStock; }"
    fi
    
    echo ""
    echo "Generating test suite..."
    curl -s -X POST "$BASE_URL/tools/call" \
      -H "Content-Type: application/json" \
      -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 4,
        \"method\": \"tools/call\",
        \"params\": {
          \"name\": \"generateMigrationTestSuite\",
          \"arguments\": {
            \"entityCode\": \"$entity\"
          }
        }
      }" | jq -r '.result.content[0].text'
}

# Main menu
while true; do
    echo ""
    echo "Select a tool to test (or 'q' to quit):"
    echo "1) Schema Conversion"
    echo "2) Query Conversion"
    echo "3) Config Generation"
    echo "4) Test Suite Generation"
    echo "5) Run all examples"
    echo "q) Quit"
    echo ""
    read -p "Your choice: " choice
    
    case $choice in
        1)
            test_schema_conversion
            ;;
        2)
            test_query_conversion
            ;;
        3)
            test_config_generation
            ;;
        4)
            test_test_generation
            ;;
        5)
            echo ""
            echo "=== Schema Conversion Example ==="
            test_schema_conversion
            echo ""
            echo "=== Query Conversion Example ==="
            test_query_conversion
            echo ""
            echo "=== Config Generation Example ==="
            test_config_generation
            echo ""
            echo "=== Test Generation Example ==="
            test_test_generation
            ;;
        q|Q)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            ;;
    esac
done

# Made with Bob
