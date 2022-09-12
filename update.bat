@REM para matar images none
@REM docker system prune -f

echo comeou
./gradlew build
docker build -t t1:latest .
docker run --rm -it t1

@REM # RUN ./gradlew build
@REM esse -t é de tag
docker build -t t1:latest .
@REM docker run --rm -it t1

docker run --rm -it t1
