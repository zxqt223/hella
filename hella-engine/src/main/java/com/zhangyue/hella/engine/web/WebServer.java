package com.zhangyue.hella.engine.web;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

import com.zhangyue.hella.common.conf.Configuration;

public class WebServer {

    private static final Logger LOG = Logger.getLogger(WebServer.class);
    private Server server = null;

    public void initialize(Configuration conf, String host, int httpPort) {
        server = new Server();
        String applicationHome = System.getProperty("user.dir");
        Connector conn = new SelectChannelConnector();

        conn.setHost(host);
        conn.setPort(httpPort);
        server.setConnectors(new Connector[] { conn });

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setWar(applicationHome + "/webapp");
        server.setHandler(context);

        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMaxThreads(conf.getInt("webserver.max.thread.count", 10));
        pool.setMinThreads(conf.getInt("webserver.min.thread.count", 5));
        server.setThreadPool(pool);
    }

    public void start() throws Exception {
        server.start();
        LOG.info("Success to start web server.");
    }

    public void stop() throws Exception {
        server.stop();
    }
}
