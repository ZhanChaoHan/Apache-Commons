package jachs.commons.httpcomponents.examples;

import java.util.concurrent.Future;

import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;

/**
 * This example demonstrates how to avoid the illegal reflective access operation warning
 * when running with Oracle JRE 9 or newer.
 * 此示例演示如何避免非法反射访问操作警告,运行Oracle JRE 9或更新版本。
 */
public class AsyncClientTlsAlpn {

    public final static void main(final String[] args) throws Exception {
        final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                .useSystemProperties()
                // IMPORTANT uncomment the following method when running Java 9 or older
                // in order for ALPN support to work and avoid the illegal reflective
                // access operation warning
                /*
                .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {

                    @Override
                    public TlsDetails create(final SSLEngine sslEngine) {
                        return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                    }
                })
                */
                .build();
        final PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();
        try (final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .setConnectionManager(cm)
                .build()) {

            client.start();

            final HttpHost target = new HttpHost("https", "nghttp2.org", 443);
            final String requestUri = "/httpbin/";
            final HttpClientContext clientContext = HttpClientContext.create();

            final SimpleHttpRequest request = SimpleHttpRequests.get(target, requestUri);
            final Future<SimpleHttpResponse> future = client.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    clientContext,
                    new FutureCallback<SimpleHttpResponse>() {

                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            System.out.println(requestUri + "->" + response.getCode());
                            System.out.println(response.getBody());
                            final SSLSession sslSession = clientContext.getSSLSession();
                            if (sslSession != null) {
                                System.out.println("SSL protocol " + sslSession.getProtocol());
                                System.out.println("SSL cipher suite " + sslSession.getCipherSuite());
                            }
                        }

                        @Override
                        public void failed(final Exception ex) {
                            System.out.println(requestUri + "->" + ex);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println(requestUri + " cancelled");
                        }

                    });
            future.get();

            System.out.println("Shutting down");
            client.close(CloseMode.GRACEFUL);
        }
    }

}
