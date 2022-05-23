# Pull OpenJDK, it uses Oracle Linux
FROM openjdk:11

# Update the environment and perform apt installations
RUN apt-get update -y \
    && apt-get install -y maven \
    && apt-get clean

# create a directory for app
WORKDIR /workflow-composer

# Copy project directory into the container at /workflow-composer
COPY  . /workflow-composer

# Generate fat jar
RUN mvn package

# Expose port
EXPOSE 8080

# Production FAT jar
CMD java -jar target/workflow-composer-0.1.jar server settings.yml