#!/usr/bin/env bash
# ==============================================================================
# Execution script for Student Placement Management System
# ==============================================================================

set -e

if [ ! -f "bin/com/placement/main/MainApp.class" ]; then
    echo "[INFO] Running compilation first..."
    bash compile.sh
fi

CP="bin:resources"
if [ -d "lib" ]; then
    for jar in lib/*.jar; do
        if [ -f "$jar" ]; then
            CP="$CP:$jar"
        fi
    done
fi

echo "Classpath: $CP"
echo "Starting Application..."
echo ""

java -cp "$CP" com.placement.main.MainApp
