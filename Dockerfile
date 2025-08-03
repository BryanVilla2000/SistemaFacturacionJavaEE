FROM icr.io/appcafe/open-liberty:full-java21-openj9-ubi-minimal

# Copiar el conector JDBC correctamente
COPY lib/mysql-connector-java-8.0.33.jar /config/

# Copiar server.xml desde su ubicación real en src
COPY src/main/liberty/config/server.xml /config/

# Copiar la aplicación .war generada por Maven
COPY target/proyect-FitSystem.war /config/dropins/

# Exponer los puertos
EXPOSE 9080 9443
