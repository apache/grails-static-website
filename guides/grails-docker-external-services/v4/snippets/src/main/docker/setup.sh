#!/bin/bash
echo "######### CREATING DATABASE ##########"

# Perform all actions as user 'postgres'
export PGUSER=postgres

# Create specific users for the application and also the databases for dev and test
psql <<EOSQL
    -- <1>
    CREATE DATABASE dev_demo_db;
    CREATE ROLE dev_user WITH LOGIN PASSWORD 'dev_password';
    GRANT ALL PRIVILEGES ON DATABASE dev_demo_db TO dev_user;

    -- <2>
    CREATE DATABASE test_demo_db;
    CREATE ROLE test_user WITH LOGIN PASSWORD 'test_password';
    GRANT ALL PRIVILEGES ON DATABASE test_demo_db TO test_user;
EOSQL

echo ""
echo "######### DATABASE CREATED ##########"
