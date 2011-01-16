package webbit.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import webbit.HttpHandler;
import webbit.WebServer;
import webbit.netty.NettyHttpChannelHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class NettyWebServer implements WebServer {
    private final ServerBootstrap bootstrap;
    private final InetSocketAddress socketAddress;

    public NettyWebServer(int port, final HttpHandler httpHandler) {
        this.socketAddress = new InetSocketAddress(port);

        // Configure the server.
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new NettyHttpChannelHandler(httpHandler));
                return pipeline;
            }
        });

    }

    @Override
    public void start() {
        bootstrap.bind(socketAddress);
    }

    @Override
    public void stop() {
        // TODO
    }

    @Override
    public URI getUri() {
        return URI.create("http://" + socketAddress.getHostName() +
                (socketAddress.getPort() == 80 ? "" : (":" + socketAddress.getPort())) + "/");
    }
}