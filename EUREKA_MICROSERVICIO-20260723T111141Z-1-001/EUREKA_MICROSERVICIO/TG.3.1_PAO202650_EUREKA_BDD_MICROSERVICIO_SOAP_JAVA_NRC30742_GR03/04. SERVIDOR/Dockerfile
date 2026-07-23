FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app

COPY app/pom.xml .
# Download dependencies first to cache them
RUN mvn dependency:go-offline -B

COPY app/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to GlassFish 7
FROM eclipse-temurin:17-jre-jammy

ENV GLASSFISH_HOME=/opt/glassfish7

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl unzip \
	&& rm -rf /var/lib/apt/lists/* \
	&& curl -fsSL -o /tmp/glassfish.zip "https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.1.0/glassfish-7.1.0.zip" \
	&& unzip -q /tmp/glassfish.zip -d /opt \
	&& rm -f /tmp/glassfish.zip

COPY --from=build /app/target/WSEurekaBank_Restfull_Java_G4-1.0-SNAPSHOT.war /opt/glassfish7/glassfish/domains/domain1/autodeploy/ROOT.war

EXPOSE 8080 4848
CMD ["/opt/glassfish7/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]
