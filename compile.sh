#!/usr/bin/env bash
# ==============================================================================
# Compilation script for Student Placement Management System
# ==============================================================================

set -e

echo "========================================================"
echo "Compiling Student Placement Management System (Java 17+)"
echo "========================================================"

mkdir -p bin

javac -d bin -sourcepath src src/com/placement/main/MainApp.java

cp -f resources/db.properties bin/db.properties 2>/dev/null || true

echo "[SUCCESS] Compilation successful! Classes built in bin/"
