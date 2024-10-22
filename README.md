# Weather App

This is a Scala-based weather application.

## Prerequisites

- Docker (for Docker setup)
- Scala and SBT (for local setup)

## Building and Running the Application

### Using Docker

1. **Clone the repository:**  
    git clone https://github.com/JamesWClark/Scala-Weather
    cd weather-app

2. **Build the Docker image:**  
    docker build -t weather-app .

3. **Run the Docker container:**  
    docker run -p 8080:8080 weather-app

4. **Visit the application in your browser:**  
    [http://localhost:8080/](http://localhost:8080)

### Running Locally

1. **Clone the repository:**  
    git clone https://github.com/JamesWClark/Scala-Weather
    cd weather-app

2. **Install dependencies and run the application:**  
    sbt run

3. **Visit the application in your browser:**  
    [http://localhost:8080/](http://localhost:8080)