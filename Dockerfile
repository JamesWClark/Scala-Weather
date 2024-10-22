# Use an official Scala image as a parent image
FROM hseeberger/scala-sbt:11.0.11_1.5.5_2.13.6

# Set the working directory in the container
WORKDIR /app

# Copy the project files into the container
COPY . .

# Install project dependencies and build the project
RUN sbt clean compile

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["sbt", "run"]