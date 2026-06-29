#!/bin/sh

cd /app
echo "====================================================="
echo "[$(date)] STARTING MONTHLY BILLING CYCLE"
echo "====================================================="

echo "-> Triggering Aggregator..."
java -jar /app/Aggregator.jar

echo "-> Triggering Bill Reporter..."
java -jar /app/BillReporter.jar

echo "====================================================="
echo "[$(date)] BILLING CYCLE COMPLETE"
echo "====================================================="
