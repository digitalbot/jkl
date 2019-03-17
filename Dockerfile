FROM adoptopenjdk/openjdk11
RUN apt-get update && apt-get install -y git
RUN git clone https://github.com/digitalbot/jkl.git && \
    cd jkl && \
    ./gradlew installDist
    
ENV PATH $PATH:/jkl/build/install/jkl/bin

## TODO: remove "Picked up JAVA_TOOL_OPTIONS:"
RUN unset JAVA_TOOL_OPTIONS
