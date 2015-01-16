#!/bin/bash
psql -U postgres -c "DROP DATABASE ylopstest;"
psql -U postgres -c "CREATE DATABASE ylopstest;"
psql -U postgres -c "CREATE USER ylopstest WITH PASSWORD 'ylopstest';"
psql -U postgres -c "GRANT ALL ON DATABASE ylopstest TO ylopstest;"
