import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;

public class CertChainWriter {
  public static void main(String args[]) throws Exception {

    final String beginCert = "-----BEGIN CERTIFICATE-----";
    final String endCert = "-----END CERTIFICATE-----";
    final String certFilenameFormat = "cert%d.pem";
    final String protocol = "TLS";

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
           public java.security.cert.X509Certificate[] getAcceptedIssuers() {
             return null;
           }
           public void checkClientTrusted(X509Certificate[] certs, String authType) {}
           public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
     };
 
    SSLContext sc = SSLContext.getInstance(protocol);
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
    SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
    socket.startHandshake();
    SSLSession session = socket.getSession();
    java.security.cert.Certificate[] servercerts = session.getPeerCertificates();

    for (int i = 1; i < servercerts.length; i++) {
      Encoder encoder = Base64.getEncoder();
      System.out.println(beginCert);
      System.out.println(encoder.encodeToString(servercerts[i].getEncoded()));
      System.out.println(endCert);
      BufferedWriter writer = new BufferedWriter(new FileWriter(String.format(certFilenameFormat, i)));
      writer.write(beginCert);
      writer.newLine();
      writer.write(encoder.encodeToString(servercerts[i].getEncoded()));
      writer.newLine();
      writer.write(endCert);
      writer.newLine();
      writer.close();
    }
  }
}