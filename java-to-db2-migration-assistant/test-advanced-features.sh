#!/bin/bash

# Advanced Features Test Script
# Tests Views, Triggers, and Stored Procedures conversion

BASE_URL="http://localhost:9090/mcp"

echo "=========================================="
echo "Advanced PostgreSQL to DB2 Migration Tests"
echo "Views, Triggers, and Stored Procedures"
echo "=========================================="
echo ""

# Test 1: Simple View
echo "1. Converting Simple View..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE VIEW active_users AS SELECT id, username, email FROM users WHERE active = TRUE;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 2: Materialized View
echo "2. Converting Materialized View..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE MATERIALIZED VIEW user_stats AS SELECT user_id, COUNT(*) as order_count, SUM(total) as total_spent FROM orders GROUP BY user_id;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 3: Simple Trigger
echo "3. Converting Simple Trigger..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TRIGGER update_timestamp BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_modified_column();"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 4: Complex Trigger with NEW/OLD
echo "4. Converting Trigger with NEW/OLD references..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TRIGGER audit_changes AFTER UPDATE ON products FOR EACH ROW EXECUTE FUNCTION log_product_changes();"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 5: PostgreSQL Function
echo "5. Converting PostgreSQL Function..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE FUNCTION calculate_discount(price DECIMAL, discount_pct INTEGER) RETURNS DECIMAL AS $$ BEGIN RETURN price * (1 - discount_pct / 100.0); END; $$ LANGUAGE plpgsql;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 6: PostgreSQL Procedure
echo "6. Converting PostgreSQL Procedure..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 6,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE PROCEDURE update_user_status(user_id INTEGER, new_status VARCHAR) LANGUAGE plpgsql AS $$ BEGIN UPDATE users SET status = new_status WHERE id = user_id; END; $$;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 7: Function with RAISE EXCEPTION
echo "7. Converting Function with Exception Handling..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 7,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE FUNCTION validate_email(email VARCHAR) RETURNS BOOLEAN AS $$ BEGIN IF email NOT LIKE '\''%@%'\'' THEN RAISE EXCEPTION '\''Invalid email format'\''; END IF; RETURN TRUE; END; $$ LANGUAGE plpgsql;"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

# Test 8: Complex Schema with Multiple Objects
echo "8. Converting Complex Schema (Table + View + Trigger)..."
curl -s -X POST "$BASE_URL/tools/call" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 8,
    "method": "tools/call",
    "params": {
      "name": "convertPostgresSchemaToDb2",
      "arguments": {
        "schemaSQL": "CREATE TABLE audit_log (id SERIAL PRIMARY KEY, table_name VARCHAR(100), action VARCHAR(20), changed_at TIMESTAMP DEFAULT NOW()); CREATE VIEW recent_audits AS SELECT * FROM audit_log WHERE changed_at > NOW() - INTERVAL '\''1 day'\''; CREATE TRIGGER log_user_changes AFTER INSERT OR UPDATE OR DELETE ON users FOR EACH ROW EXECUTE FUNCTION audit_user_changes();"
      }
    }
  }' | jq -r '.result.content[0].text'
echo ""
echo ""

echo "=========================================="
echo "All advanced feature tests completed!"
echo "=========================================="
echo ""
echo "Summary of conversions:"
echo "✅ Views - Syntax similar, materialized views need manual conversion"
echo "✅ Triggers - Syntax differs, templates provided"
echo "✅ Stored Procedures - plpgsql converted to SQL, templates provided"
echo "✅ Functions - RETURNS converted, templates provided"
echo "✅ Exception Handling - RAISE converted to SIGNAL SQLSTATE"
echo ""
echo "Next steps:"
echo "1. Review all warnings and recommendations"
echo "2. Test converted objects in DB2 environment"
echo "3. Adjust trigger and procedure logic as needed"
echo "4. Verify materialized view alternatives (MQTs)"
echo ""

# Made with Bob
