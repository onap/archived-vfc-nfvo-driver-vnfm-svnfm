/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HttpTestServer {
    public Server _server;
    public volatile List<String> requests = new ArrayList<>();
    public volatile List<Integer> codes = new ArrayList<>();
    public volatile List<String> respones = new ArrayList<>();
    ExecutorService executorService = Executors.newCachedThreadPool();
    public void start() throws Exception {
        configureServer();
        startServer();
    }

    private void startServer() throws Exception {
        requests.clear();
        codes.clear();
        _server.start();
        Future<?> serverStarted = executorService.submit(() -> {
            while(true){
                try {
                    Thread.sleep(10);
                    if(_server.isStarted()){
                        return;
                    }
                } catch (InterruptedException e) {
                }
            }
        });
        serverStarted.get(30, TimeUnit.SECONDS);
    }

    protected void configureServer() throws Exception {
        Path jksPath = Paths.get(TestCbamTokenProvider.class.getResource("/unittests/localhost.jks").toURI());
        String path = jksPath.normalize().toAbsolutePath().toUri().toString();
        _server = new Server();
        SslContextFactory factory = new SslContextFactory(path);
        factory.setKeyStorePassword("changeit");
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(_server, factory);
        connector.setHost("127.0.0.1");
        _server.addConnector(connector);
        _server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                requests.add(new String(ByteStreams.toByteArray(request.getInputStream())));
                httpServletResponse.getWriter().write(respones.remove(0));
                httpServletResponse.setStatus(codes.remove(0));
                request.setHandled(true);
            }
        });
    }

    public void stop() throws Exception {
        _server.stop();
    }
}
