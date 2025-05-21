# Stage 1: Build the frontend
FROM node:18-alpine as frontend
LABEL stage="frontend-builder"

WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock if used)
# This leverages Docker cache: if these files don't change, subsequent npm install won't run
COPY frontend/package*.json ./

# Install frontend dependencies
RUN npm install

# Copy the rest of the frontend application code
COPY frontend .

# Build the frontend application for production
# The output will typically be in a directory like /app/build or /app/dist
RUN npm run build

# Port 3000 is exposed by the Node development server (e.g., when running npm start).
# In the final image, the frontend build artifacts are served by the backend.
# This EXPOSE here is more for documentation or if this stage were run standalone.
EXPOSE 3000

# Stage 2: Build the backend
FROM maven:3.8.6-openjdk-18 as backend
LABEL stage="backend-builder"

WORKDIR /app

# Copy the Maven project file (pom.xml)
COPY backend/pom.xml .

# Download Maven dependencies to a separate layer
# This leverages Docker cache: if pom.xml doesn't change, dependencies won't be re-downloaded.
RUN mvn dependency:go-offline -B

# Copy the backend source code
COPY backend/src ./src

# Package the backend application into a JAR file
# Skipping tests for faster image builds; tests should ideally run in a CI/CD pipeline.
RUN mvn package -DskipTests

# Stage 3: Create the final application image
# Using a slim OpenJDK image for a smaller final image size
FROM openjdk:18-alpine
LABEL stage="final-app"

WORKDIR /app

# Copy the packaged backend JAR from the backend builder stage
COPY --from=backend /app/target/*.jar app.jar

# Copy the built frontend application from the frontend builder stage
# The Spring Boot application should be configured to serve static files from this directory.
# For example, by placing them in src/main/resources/static or configuring static resources path.
# Here, they are copied to /app/frontend/build inside the container.
# Spring Boot needs to be configured to serve from this path if it's outside the JAR.
# A common Spring Boot configuration in application.properties would be:
# spring.web.resources.static-locations=classpath:/static/,file:/app/frontend/build/
COPY --from=frontend /app/build /app/frontend/build

# Expose the port the backend Spring Boot application will run on
EXPOSE 8080

# Command to run the backend application when the container starts
ENTRYPOINT ["java","-jar","app.jar"]