package com.paypal.test.external.call;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class Http2Tls13Test {

    @Test
    public void testExternalCall() throws NoSuchAlgorithmException, KeyManagementException {
        final String url = "https://fonts.googleapis.com/icon?family=Material%20Icons";

        final ApplicationProtocolConfig protocolConfig = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1
        );

        final String[] protocols = new String[]{"TLSv1.3"};

        final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, null, null);

        final SslContext nettySslContext = new JdkSslContext(
                sslContext,
                true,
                null,
                IdentityCipherSuiteFilter.INSTANCE,
                protocolConfig,
                ClientAuth.NONE,
                protocols,
                false);

        final HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(nettySslContext))
                .protocol(HttpProtocol.H2);

        final String response = httpClient.get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        Assertions.assertTrue(response.contains(".material-icons"));
    }
}
