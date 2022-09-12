FROM openjdk:17
COPY ./build/libs/t1-p2p-1.jar .
CMD ["java","-jar","t1-p2p-1.jar","0"]