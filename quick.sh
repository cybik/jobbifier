#! /bin/bash

./gradlew clean :yafot:shadowJar;  java -jar yafot/build/libs/yafot-all.jar --usage
