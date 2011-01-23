A simple JAX-RS Server over SSL with BASIC Auth
====================================================

I just tried to provide a simpler way to create a Jax-RS Server with SSL and Basic Authentication with the Restlet framework.


How to create a JAX-RS Resource
--------------------------------
A JAX-RS resource is something like this :

    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import javax.ws.rs.Produces;
    
    @Path("/")
    class MyRessource{
    
        @GET
        @Produces("text/plain")
        public String getPlain() {
            return "This is an easy resource (as plain text)";
        }
    }


How to create a JAX-RS Server
------------------------------
You just need to wite few lines of code :

    JAXRSServer server = new JAXRSServer(MyResource.class, 8080);
    try {
        server.start();
        
        /* do what you have to do */
        
        server.stop();
    } catch (Exception ex) {
        Assert.fail("Unable to start", ex);
    }


How to create a JAX-RS Server over SSL with BASIC Authentication
----------------------------------------------------------------

    JAXRSServer server = new JAXRSServer(MyResource.class, 8443);
    server.enableSSL("www.hostname.fr", new File("path-to-your-keystore.jks"), "keyStorePassword", "keyPassword");
    try {
        server.start();
        
        /* do what you have to do */
        
        server.stop();
    } catch (Exception ex) {
        Assert.fail("Unable to start", ex);
    }


For more details : 
------------------
Read the javadoc of this project, or the source (only one file), or the tests (for usage exemples).

Or refer to the Restlet doc : 

-   JAX-RS extension <http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/57-restlet.html>
-   SSL extension    <http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/153-restlet.html>
-   Authentication   <http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/46-restlet/112-restlet.html>


Create your own certificate : 
------------------------------
If you want to create an HTTPS server you need a certificate.
You could generate self signed certificates like this (for test purpose) :

    # Generate a Self-Signed Certificate #
    keytool -genkey -v -alias localhost -dname "CN=localhost, OU=IT, O=MyCompany, L=Lyon, ST=France, C=FR" -keypass password -keystore localhost.jks -storepass password -keysize 1024;
    
    # Export your certificate in order to import it in your trustore
    keytool -storepass password -alias localhost -export -file localhost.cer -keystore localhost.jks
    
    # Import your certificate into the default JAVA truststore.
    sudo keytool -keystore /etc/java-6-sun/security/cacerts -storepass changeit -import -trustcacerts -v -alias localhost -file localhost.cer


Or you could use maven to create for a self-signed certificate (look at the pom of this project) : 
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>keytool-maven-plugin</artifactId>
        <executions>
            <execution>
        <phase>generate-resources</phase>
        <id>clean</id>
        <goals>
            <goal>clean</goal>
        </goals>
            </execution>
            <execution>
        <phase>generate-resources</phase>
        <id>genkey</id>
        <goals>
            <goal>genkey</goal>
        </goals>
            </execution>
        </executions>
        <configuration>
            <keystore>${project.build.directory}/localhost.jks</keystore>
            <dname>CN=localhost, OU=IT, O=MyCompany, L=Lyon, ST=France, C=FR</dname>
            <keypass>password</keypass>
            <storepass>password</storepass>
            <alias>localhost</alias>
            <keyalg>RSA</keyalg>
        </configuration>
    </plugin>

