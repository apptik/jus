/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.apptik.comm.jus.netty;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.stack.AbstractHttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class NettyHttpClientHandler extends SimpleChannelInboundHandler<HttpObject> implements
        Future<NetworkResponse> {

    private boolean resultReceived = false;
    private NetworkResponse result;
    private JusError exception;
    private final ByteArrayPool byteArrayPool;
    int len = -1;
    NetworkResponse.Builder builder = new NetworkResponse.Builder();

    public NettyHttpClientHandler(ByteArrayPool byteArrayPool) {
        this.byteArrayPool = byteArrayPool;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            System.err.println("STATUS: " + response.status());
            System.err.println("VERSION: " + response.protocolVersion());
            System.err.println();

            builder.setStatusCode(response.status().code());

            if (!response.headers().isEmpty()) {
                for (CharSequence name : response.headers().names()) {
                    for (CharSequence value : response.headers().getAll(name)) {
                        System.err.println("HEADER: " + name + " = " + value);
                        if (name.toString().equalsIgnoreCase("content-length")) {
                            len = Integer.parseInt(value.toString());
                            System.err.println("LEN: " + len);
                        }
                        builder.setHeader(name.toString(), value.toString());
                    }
                }
                System.err.println();
            }

            if (HttpHeaderUtil.isTransferEncodingChunked(response)) {
                System.err.println("CHUNKED CONTENT {");
            } else {
                System.err.println("CONTENT {");
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            if (!content.content().hasArray()) {
                try {
                    builder.setBody(AbstractHttpStack.getContentBytes(
                            new ByteBufInputStream(content.content(), len), byteArrayPool,
                            len));
                } catch (IOException e) {
                    e.printStackTrace();
                    exceptionCaught(ctx, e);
                }
            } else {
                builder.setBody(content.content().array());
            }

            content.content().resetReaderIndex();
            System.err.print(content.content().toString(CharsetUtil.UTF_8));
            System.err.flush();

            if (content instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
                result = builder.build();
                resultReceived = true;
                ctx.close();
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        exception = new JusError(cause);
        cause.printStackTrace();
        ctx.close();
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return resultReceived || exception != null || isCancelled();
    }

    @Override
    public NetworkResponse get() throws InterruptedException, ExecutionException {
        try {
            return doGet(null);
        } catch (TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public NetworkResponse get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException,
            TimeoutException {
        return doGet(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    private synchronized NetworkResponse doGet(Long timeoutMs)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (exception != null) {
            throw new ExecutionException(exception);
        }

        if (resultReceived) {
            return result;
        }

        if (timeoutMs == null) {
            wait(0);
        } else if (timeoutMs > 0) {
            wait(timeoutMs);
        }

        if (exception != null) {
            throw new ExecutionException(exception);
        }

        if (!resultReceived) {
            throw new TimeoutException();
        }

        return result;
    }

}

