package org.filirom1.jaxrs;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.ext.jaxrs.*;
import org.restlet.security.*;
import javax.ws.rs.core.Application;

/**
 * A JAXRS-Server based on RESTLet.
 * See http://wiki.restlet.org/docs_2.0/13-restlet/21-restlet.html
 *
 * I just tried to provide a simpler way to create a Jax-RS Server with SSL and
 * Basic Authentication with the Restlet framework.
 *
 * For more details see :
 * JAX-RS extension http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/57-restlet.html
 * SSL extension    http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/153-restlet.html
 * Authentication   http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/46-restlet/112-restlet.html
 *
 *
 * If you want to create an HTTPS server you need certificates.
 * You could generate self signed certificates like this (for test purpose) :
 *
 * <pre>
 * ######        on Ubuntu / Debian        ######
 *
 * # Generate a Self-Signed Certificate #
 * keytool -genkey -v -alias localhost -dname "CN=localhost, OU=IT, O=MyCompany, L=Lyon, ST=France, C=FR" -keypass password -keystore localhost.jks -storepass password -keysize 1024;
 *
 * # Export your certificate in order to import it in your trustore
 * keytool -storepass password -alias localhost -export -file localhost.cer -keystore localhost.jks
 *
 * # Import your certificate into the default JAVA truststore.
 * sudo keytool -keystore /etc/java-6-sun/security/cacerts -storepass changeit -import -trustcacerts -v -alias localhost -file localhost.cer
 * </pre>
 *
 */
public class JAXRSServer {

    private static final Logger log = Logger.getLogger(JAXRSServer.class.getName());
    private final List<Class<?>> resources;
    private final int port;
    private boolean ssl = false;
    private boolean basic = false;
    private String realm;
    private String login;
    private String password;
    private String hostname;
    private File keyStorePath;
    private String keyPassword;
    private String keyStorePassword;
    private List<String> disabledCiphers;
    private String truststoreType;
    private File truststorePath;
    private String keystoreType;
    private String sslContextFactory = "org.restlet.ext.ssl.PkixSslContextFactory";
    private Map<String, String> params = new HashMap<String, String>();
    private String truststorePassword;
    private Component webComponent;

    /**
     * Default Contrsuctor. 
     *
     * A JAX-RS resource is something like this :
     * <pre>
     * import javax.ws.rs.GET;
     * import javax.ws.rs.Path;
     * import javax.ws.rs.Produces;
     *
     * @Path("/")
     * class MyRessource{
     *
     *     @GET
     *     @Produces("text/plain")
     *     public String getPlain() {
     *         return "This is an easy resource (as plain text)";
     *     }
     * }
     * </pre>
     *
     * @param resources a list of resource Class.
     *
     * @param port to listen to for exemple HTTP : 80 or 8080, HTTPS : 443 or 8443
     */
    public JAXRSServer(List<Class<?>> resources, int port) {
        this.resources = new ArrayList<Class<?>>();
        this.resources.addAll(resources);
        this.port = port;

    }

    /**
     * Same as public JAXRSServer(List<Class<?>> resources, int port)
     * but with only one resource.
     *
     * @param resource the resource class.
     * @param port
     */
    public JAXRSServer(Class<?> resource, int port) {
        this.resources = new ArrayList<Class<?>>();
        this.resources.add(resource);
        this.port = port;

    }

    /**
     * Enable BASIC Authentication.
     * See http://en.wikipedia.org/wiki/Basic_access_authentication
     *
     * Without the good login/password you can not access the resources.
     *
     * Without SSL, the login/password will be tramsmited in plain text.
     * We encourage you to use SSL with BASIC Auth.
     * 
     * @param realm will be printed if no credentials are given.
     * @param login
     * @param password
     */
    public void enableBasicAuthentication(String realm, String login, String password) {
        basic = true;
        this.realm = realm;
        this.login = login;
        this.password = password;
    }

    /**
     * Enable SSL. You will need to use the prefix 'https://' instead of 'http://'
     * in order to access your resources.
     *
     * @param hostname  specify the hostname of your server for exemple google.com
     *                  if you are google.
     *                  If the hostname provided here does not match the hostname given
     *                  in the certificate, a warning will be printed in the browser.
     *
     * @param keyStorePath      the keystore File. You can generate one with keytool
     * @param keyStorePassword  the KEYSTORE passowrd you set when you generate your keystore
     * @param keyPassword       THE KEY passowrd you set when you generate your keystore
     */
    public void enableSSL(String hostname, File keyStorePath, String keyStorePassword, String keyPassword) {
        enableSSL(hostname, keyStorePath, keyStorePassword, keyPassword, null, null, null, null, null);
    }

    /**
     * See enableSSL(String hostname, File keyStorePath, String keyStorePassword, String keyPassword)
     *
     * @param hostname
     * @param keyStorePath
     * @param keyStorePassword
     * @param keyPassword
     * @param sslContextFactory     default "org.restlet.ext.ssl.PkixSslContextFactory"
     * @param keystoreType          default "JKS"
     * @param truststorePath        default "/etc/java-6-sun/security/cacerts" or windows equivalent
     * @param truststorePassword    default "changeit"
     * @param truststoreType        default "JKS"
     */
    public void enableSSL(
            String hostname,
            File keyStorePath,
            String keyStorePassword,
            String keyPassword,
            String sslContextFactory,
            String keystoreType,
            File truststorePath,
            String truststorePassword,
            String truststoreType) {
        ssl = true;
        this.hostname = hostname;
        if (!keyStorePath.exists()) {
            throw new RuntimeException("File " + keyStorePath.getAbsolutePath() + " does not exist.");
        }
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.sslContextFactory = sslContextFactory;
        this.keystoreType = keystoreType;
        if (truststorePath != null && (!truststorePath.exists())) {
            throw new RuntimeException("File " + truststorePath.getAbsolutePath() + " does not exist.");
        }
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
        this.truststoreType = truststoreType;
    }

    /**
     * Give us a list of cipher to disable.
     *
     * You can view the list ciphers supported by your HTTPS Server like this :
     * <pre>
     * sslscan --no-failed -connect 127.0.0.1:8443                                                _
     * </pre>
     *
     * See sslscan project : http://sourceforge.net/projects/sslscan/
     * Caution : the cipher naming is different betweeen sslscan and JAVA.
     *
     * Here is the list of weak ciphers I disable :
     * <pre>
     * SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA
     * SSL_DHE_DSS_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA
     * SSL_DHE_DSS_WITH_DES_CBC_SHA
     * SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA
     * SSL_DHE_RSA_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA
     * SSL_DHE_RSA_WITH_DES_CBC_SHA
     * SSL_DH_ANON_EXPORT_WITH_DES40_CBC_SHA
     * SSL_DH_ANON_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_DH_ANON_EXPORT_WITH_RC4_40_MD5
     * SSL_DH_ANON_WITH_3DES_EDE_CBC_SHA
     * SSL_DH_ANON_WITH_DES_CBC_SHA
     * SSL_DH_ANON_WITH_RC4_MD5
     * SSL_DH_DSS_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA
     * SSL_DH_DSS_WITH_DES_CBC_SHA
     * SSL_DH_RSA_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA
     * SSL_DH_RSA_WITH_DES_CBC_SHA
     * SSL_RSA_EXPORT_WITH_DES40_CBC_SHA
     * SSL_RSA_EXPORT_WITH_DES_40_CBC_SHA
     * SSL_RSA_EXPORT_WITH_RC2_40_CBC_MD5
     * SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5
     * SSL_RSA_EXPORT_WITH_RC4_40_MD5
     * SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA
     * SSL_RSA_FIPS_WITH_DES_CBC_SHA
     * SSL_RSA_WITH_3DES_EDE_CBC_MD5
     * SSL_RSA_WITH_3DES_EDE_CBC_SHA
     * SSL_RSA_WITH_DES_CBC_MD5
     * SSL_RSA_WITH_DES_CBC_SHA
     * SSL_RSA_WITH_NULL_MD5
     * SSL_RSA_WITH_NULL_SHA
     * SSL_RSA_WITH_RC2_CBC_MD5
     * SSL_RSA_WITH_RC4_MD5
     * SSL_RSA_WITH_RC4_SHA
     * TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5
     * TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA
     * TLS_KRB5_EXPORT_WITH_RC4_40_MD5
     * TLS_KRB5_EXPORT_WITH_RC4_40_SHA
     * TLS_KRB5_WITH_3DES_EDE_CBC_MD5
     * TLS_KRB5_WITH_3DES_EDE_CBC_SHA
     * TLS_KRB5_WITH_DES_CBC_MD5
     * TLS_KRB5_WITH_DES_CBC_SHA
     * SSL_CK_DES_64_CBC_WITH_MD5
     * </pre>
     *
     *
     * @param disabledCiphers the list of ciphers to disable.
     */
    public void setDisabledCipherSuites(List<String> disabledCiphers) {
        this.disabledCiphers = new ArrayList<String>();
        this.disabledCiphers.addAll(disabledCiphers);
    }

    /**
     * If you want to inject params directly to restlet
     *
     * @param params
     */
    public void setRestletParams(Map<String, String> params) {
        this.params = new HashMap<String, String>();
        this.params.putAll(params);
    }

    /**
     * Start the JAX-RS Server.
     *
     * @throws Exception if something goes wrong.
     */
    public void start() throws Exception {
        if (webComponent != null) {
            throw new RuntimeException("JAX-RS Server already started.");
        }
        webComponent = new Component();
        Server httpServer = webComponent.getServers().add(ssl ? Protocol.HTTPS : Protocol.HTTP, port);
        if (sslContextFactory != null) {
            params.put("sslContextFactory", sslContextFactory);
        }
        if (keyStorePath != null) {
            params.put("keystorePath", keyStorePath.getAbsolutePath());
        }
        if (keyStorePassword != null) {
            params.put("keystorePassword", keyStorePassword);
        }
        if (hostname != null) {
            params.put("hostname", hostname);
        }
        if (keyPassword != null) {
            params.put("keyPassword", keyPassword);
        }
        if (keystoreType != null) {
            params.put("keystoreType", keystoreType);
        }
        if (truststorePath != null) {
            params.put("truststorePath", truststorePath.getAbsolutePath());
        }
        if (truststorePassword != null) {
            params.put("truststorePassword", truststorePassword);
        }
        if (truststoreType != null) {
            params.put("truststoreType", truststoreType);
        }
        if (disabledCiphers != null) {
            StringWriter writer = new StringWriter();
            for (String cipher : disabledCiphers) {
                writer.write(cipher + " ");
            }
            params.put("disabledCipherSuites", writer.toString());
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            httpServer.getContext().getParameters().add(entry.getKey(), entry.getValue());
        }
        Context context = webComponent.getContext().createChildContext();
        JaxRsApplication application = new JaxRsApplication(context);

        if (basic) {
            if (!ssl) {
                log.warning("Security warning : BASIC Authentication is used without SSL."
                        + "Login and password are transmited in plain text.");
            }
            MemoryRealm memoryRealm = new MemoryRealm();
            application.getContext().setDefaultEnroler(memoryRealm.getEnroler());
            application.getContext().setDefaultVerifier(memoryRealm.getVerifier());
            memoryRealm.getUsers().add(new User(login, password.toCharArray()));

            ChallengeAuthenticator guard = new ChallengeAuthenticator(
                    application.getContext(), false, ChallengeScheme.HTTP_BASIC, "My WebService");

            application.setGuard(guard);
        }
        application.add(new RestletApplication(this.resources));

        // Attach the application to the component and start it
        webComponent.getDefaultHost().attach(application);
        webComponent.start();
    }

    /**
     * Stop the JAX-RS Server.
     *
     * @throws Exception
     */
    public void stop() throws Exception {
        webComponent.stop();
    }

    /**
     * class needed for JAX-RS
     */
    private static class RestletApplication extends Application {

        private final List<Class<?>> clazzes = new ArrayList<Class<?>>();

        public RestletApplication(List<Class<?>> clazzes) {
            this.clazzes.addAll(clazzes);
        }

        @Override
        public Set<Class<?>> getClasses() {
            Set<Class<?>> rrcs = new HashSet<Class<?>>();
            for (Class<?> clazz : clazzes) {
                rrcs.add(clazz);
            }
            return rrcs;
        }
    }
}
