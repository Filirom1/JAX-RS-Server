package org.filirom1.jaxrs;

import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;
import us.monoid.web.Resty;

public class JAXRSServerNGTest {

    static {
        SSLUtilities.trustAllHttpsCertificates();
    }

    @Test
    public void testHttpServer() throws InterruptedException {
        //fixture
        JAXRSServer server = new JAXRSServer(MyResource.class, 8080);

        //execute
        try {
            server.start();
        } catch (Exception ex) {
            Assert.fail("Unable to start", ex);
        }


        //check
        Resty r = new Resty();
        try {
            String resp = r.text("http://localhost:8080").toString();
            Assert.assertEquals(resp, MyResource.CONTENT);
        } catch (IOException ex) {
            Assert.fail("Unable to GET content", ex);
        }

        //clean
        try {
            server.stop();
        } catch (Exception ex) {
            Assert.fail("unable to stop the server", ex);
        }
    }

    @Test
    public void testHttpServerWithBasicAuth() throws InterruptedException {
        //fixture
        JAXRSServer server = new JAXRSServer(MyResource.class, 8080);
        server.enableBasicAuthentification("Realm", "login", "password");

        //execute
        try {
            server.start();
        } catch (Exception ex) {
            Assert.fail("Unable to start", ex);
        }

        //check
        Resty r = new Resty();
        try {
            r.text("http://localhost:8080").toString();
            Assert.fail("No credentials was asked.");
        } catch (IOException ex) {
        }

        try {
            r.authenticate("http://localhost:8080", "bad-login", "bad-password".toCharArray());
            r.text("http://localhost:8080").toString();
            Assert.fail("Wrong login/password are accepted.");
        } catch (IOException ex) {
        }

        try {
            r.authenticate("http://localhost:8080", "login", "password".toCharArray());
            String resp = r.text("http://localhost:8080").toString();
            Assert.assertEquals(resp, MyResource.CONTENT);
        } catch (IOException ex) {
            Assert.fail("Unable to GET content", ex);
        }

        //clean
        try {
            server.stop();
        } catch (Exception ex) {
            Assert.fail("unable to stop the server", ex);
        }
    }

    @Test
    public void testHttpOverSSLServer() throws InterruptedException {
        //fixture
        JAXRSServer server = new JAXRSServer(MyResource.class, 8443);
        server.enableSSL("localhost", new File("./target/localhost.jks"), "password", "password");

        //execute
        try {
            server.start();
        } catch (Exception ex) {
            Assert.fail("Unable to start", ex);
        }

        //check
        Resty r = new Resty();
        try {
            String resp = r.text("https://localhost:8443").toString();
            Assert.assertEquals(resp, MyResource.CONTENT);
        } catch (IOException ex) {
            Assert.fail("Unable to GET content", ex);
        }

        //clean
        try {
            server.stop();
        } catch (Exception ex) {
            Assert.fail("unable to stop the server", ex);
        }
    }

    @Test
    public void testHttpServerWithBasicAuthOverSSL() throws InterruptedException {
        //fixture
        JAXRSServer server = new JAXRSServer(MyResource.class, 8443);
        server.enableBasicAuthentification("Realm", "login", "password");
        server.enableSSL("localhost", new File("./target/localhost.jks"), "password", "password");

        //execute
        try {
            server.start();
        } catch (Exception ex) {
            Assert.fail("Unable to start", ex);
        }

        //check
        Resty r = new Resty();
        try {
            String resp = r.text("https://localhost:8443").toString();
            Assert.fail("No credentials was asked.");
        } catch (IOException ex) {
        }

        try {
            r.authenticate("http://localhost:8080", "bad-login", "bad-password".toCharArray());
            r.text("http://localhost:8080").toString();
            Assert.fail("Wrong login/password are accepted.");
        } catch (IOException ex) {
        }

        try {
            r.authenticate("https://localhost:8443", "login", "password".toCharArray());
            String resp = r.text("https://localhost:8443").toString();
            Assert.assertEquals(resp, MyResource.CONTENT);
        } catch (IOException ex) {
            Assert.fail("Unable to GET content", ex);
        }

        //clean
        try {
            server.stop();
        } catch (Exception ex) {
            Assert.fail("unable to stop the server", ex);
        }
    }
}
