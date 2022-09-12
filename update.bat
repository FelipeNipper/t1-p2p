@REM para matar images none
@REM docker system prune -f

@REM PORT 8765
./gradlew build
docker build --rm -t t1:latest .