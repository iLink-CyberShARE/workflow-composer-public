# Workflow Composer
The workflow composer automates the process to create a workflow plan for mode-to-model integration.  
The current version exposes one enpoint that consumes an abstract workflow request and model catalog. The composer will 
generate a workflow plan serialized in JSON format. 

This microservice forms part of the SWIM model orchestration pool, for more information view:   
https://water.cybershare.utep.edu/resources/docs/en2/backend/swim-broker/

## Build and Run

### Option 1: Pull Docker Image 
Environment Requirements: Docker and Docker Compose

The public image of the Workflow Composer can be pulled from the Dockerhub repo:  
lagarnicachavira/workflow-composer-public   
https://hub.docker.com/r/lagarnicachavira/workflow-composer-public   

You can directly run the application using the docker-compose.yml in this repo.

1) Modify the file docker-compose.yml if necesary.
2) Run the container: > docker-compose up (linux)  |  docker compose up (windows)
3) Once running, the OpenAPI documentation will be locally available at http://localhost:8091/swagger


### Option 2: Build Docker Container
Environment Requirements: Docker

The Workflow Composer can be deployed as a docker container using Docker Build or Compose.

1) Download this repository into a folder on your machine.
2) Install Docker and Docker composer on your target machine.
3) Setup your docker account at: https://www.docker.com/get-started
4) Using a command line or terminal navigate to the base path of the project.
5) Build the image: > docker build -t workflow-composer-public:latest .
6) Run the container: > docker run -p 8080:8080 workflow-composer-public:latest .
7) Once running, the OpenAPI documentation will be locally available at http://localhost:8080/swagger

### Option 3: Build and Run Natively
Environment Requirements: Java (JDK) & MAVEN
    - Clone or download this repo to your target machine.   
    - Open a terminal and run the command > mvn install   
    - Run jar command: > java -jar target/workflow-composer-0.1.jar server settings.yml   
    - Admin tools (dropwizard) at: http://localhost:8081   and OpenAPI Docs at http://localhost:8080/swagger   

Potential Build Issues   
    - Make sure to set the POM file to a compatible java version on the target machine.
    - Ports 8080 and 8081 must be available

## Testing
The tests folder in this repository contains input and output files used as an abstract test case
and the SWIM (http://purl.org/swim) case study. You may use the abstract sample files for quick demo purposes.

## OpenAPI Screenshots

### Abstract Example Input
![Composer Input](/images/input_capture.png "Abstract Example Input")

### Abstract Example Response
![Composer Output](/images/response_capture.png "Abstract Example Response")

## Contributors
Raul Alejandro Vargas Acosta   
Luis Garnica Chavira   
Natalia Villanueva-Rosales   
Deana D. Pennington   

## Acknowledgements
This material is based upon work supported by the National Science Foundation (NSF) under Grant No. 1835897. This work used resources from Cyber-ShARE Center of Excellence, which is supported by NSF Grant number HRD-1242122.   

Any opinions, findings, and conclusions or recommendations expressed in this material are those of the author(s) and do not necessarily reflect the views of the NSF.

## License
This software code is licensed under the [GNU GENERAL PUBLIC LICENSE v3.0](./license) and uses third party libraries that are distributed under their own terms (see [LICENSE-3RD-PARTY.md](./LICENSE-3RD-PARTY.md)).

## Copyright
© 2019-2022 - University of Texas at El Paso (SWIM Project).

