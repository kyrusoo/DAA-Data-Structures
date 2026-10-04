@echo off
setlocal
call mvn -q -DskipTests compile
if errorlevel 1 exit /b 1
java -cp target\classes benchmark.BenchmarkRunner %*
