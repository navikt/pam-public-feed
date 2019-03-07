package no.nav.pam.feed.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Only use the provided objects in dev/test environments.
 */
public class UnsafeSSLContextUtil {

    /**
     * @return a new SSLContext using the provided trust manager instance, and otherwise defaults
     */
    public static SSLContext newSSLContext(X509TrustManager manager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{manager}, new SecureRandom());
        return sc;
    }

    /**
     * @return an X509TrustManager which trusts all clients and servers unconditionally.
     */
    public static X509TrustManager newUnsafeTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    /**
     * @return an <code>HttpClientBuilder</code> preconfigured to accept all SSL certificates and hostnames
     * without verification.
     */
    public static HttpClientBuilder unsafeHttpClientBuilder() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = newSSLContext(newUnsafeTrustManager());
        return HttpClientBuilder.create()
                .setSSLContext(sc)
                .setSSLHostnameVerifier(new NoopHostnameVerifier());
    }

}
