FROM openjdk:17
WORKDIR /home/T1-P2P
COPY . .
CMD ["java","-jar","build/libs/t1-p2p-1.jar"]