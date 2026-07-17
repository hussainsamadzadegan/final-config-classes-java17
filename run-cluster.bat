@echo off
echo ================================================
echo    Starting FinalConfigClasses Cluster
echo ================================================

set JAR=configclasses-framework-3.0.0-with-dependencies.jar

echo Starting Node 1 on port 8081...
start "Node 1" c:\java\jdk17\bin\java -D"node.id=node-1" -Dserver.port=8081 -jar %JAR%

echo Starting Node 2 on port 8082...
start "Node 2" c:\java\jdk17\bin\java -D"node.id=node-2" -Dserver.port=8082 -jar %JAR%

echo Starting Node 3 on port 8083...
start "Node 3" c:\java\jdk17\bin\java -D"node.id=node-3" -Dserver.port=8083 -jar %JAR%

echo.
echo Cluster started!
echo Node 1 → http://localhost:8081
echo Node 2 → http://localhost:8082
echo Node 3 → http://localhost:8083
echo.
pause