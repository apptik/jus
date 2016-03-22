package io.apptik.comm.jus.netty;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.AbstractHttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class NettyHttpStack extends AbstractHttpStack {

    @Override
    public NetworkResponse performRequest(Request<?> request, Headers additionalHeaders,
                                          ByteArrayPool byteArrayPool) throws IOException {
        URI uri = null;
        NettyHttpClientHandler nettyHttpClientHandler = new NettyHttpClientHandler(byteArrayPool);
        try {
            uri = new URI(request.getUrlString());
        } catch (URISyntaxException e) {
            //should not happen but wth
            throw new RuntimeException(e);
        }
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new RuntimeException("Only HTTP(S) is supported.");
        }

        // Configure SSL context if necessary.
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslCtx = null;
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new NettyClientInit(sslCtx, nettyHttpClientHandler));

            // Make the connection attempt.
            Channel ch = null;
            ch = b.connect(host, port).sync().channel();


            // Prepare the HTTP request.
            HttpRequest nettyRequest = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, new HttpMethod(request.getMethod()), uri.getRawPath());

            nettyRequest.headers().set(HttpHeaderNames.HOST, host);
            nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            //nettyRequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

            addHeaders(nettyRequest, additionalHeaders.toMap());
            if (request.getHeaders() != null) {
                addHeaders(nettyRequest, request.getHeaders().toMap());
            }


            // Send the HTTP request.
            ch.writeAndFlush(nettyRequest);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
            return nettyHttpClientHandler.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }

    }


    private static void addHeaders(HttpRequest httpRequest, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpRequest.headers().set(entry.getKey(), entry.getValue());
        }
    }
}
