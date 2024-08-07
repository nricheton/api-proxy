package com.richeton.apiproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiProxy {
	private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
	private static String bind = "localhost";
	private static final String CONFIG_BIND = "bind";
	private static final String CONFIG_LOGIN_HOST = "loginHost";
	private static final String CONFIG_PORT = "port";
	private static final String CONFIG_TARGET_HOST = "targetHost";
	private static final String CONFIG_THREADS = "threads";

	private static Logger LOG = LoggerFactory.getLogger(ApiProxy.class);
	private static String loginHost = null;
	private static int port = 8080;
	private static String targetHost = null;
	private static int threads = 1;

	private static void loadConfig() throws IOException {
		FileInputStream fis = new FileInputStream(new File("config.properties"));

		Properties properties = new Properties();
		properties.load(fis);
		fis.close();

		if (properties.getProperty(CONFIG_PORT) != null) {
			port = Integer.parseInt(properties.getProperty(CONFIG_PORT));
		}

		if (properties.getProperty(CONFIG_THREADS) != null) {
			threads = Integer.parseInt(properties.getProperty(CONFIG_THREADS));
		}

		if (properties.getProperty(CONFIG_BIND) != null) {
			bind = properties.getProperty(CONFIG_BIND);
		}

		targetHost = properties.getProperty(CONFIG_TARGET_HOST);
		loginHost = properties.getProperty(CONFIG_LOGIN_HOST);

	}

	public static void main(String[] args) throws Exception {

		loadConfig();
		showBanner();

		final Proxy p = new Proxy();
		p.setLoginUrl(loginHost);
		p.setThreads(threads);
		p.init();

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("server");

		// Create a Server instance.
		Server server = new Server(threadPool);

		// Create a ServerConnector to accept connections from clients.
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		connector.setHost(bind);

		// Add the Connector to the Server
		server.addConnector(connector);

		// Set a simple Handler to handle requests/responses.
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, Request jettyRequest, HttpServletRequest request,
					HttpServletResponse response) {

				try {

					String url = targetHost + request.getRequestURI();

					if (request.getQueryString() != null) {
						url += "?" + request.getQueryString();
					}

					LOG.info("Getting url... {}", url);

					String result = p.get(url);

					LOG.debug("Response: {}", result);
					response.setContentType(CONTENT_TYPE_JSON);
					response.getWriter().println(result);
					response.getWriter().close();
					response.flushBuffer();
				} catch (InterruptedException e) {
					LOG.error("Error getting content", e);
				} catch (ExecutionException e) {
					LOG.error("Error getting content", e);
				} catch (IOException e) {
					LOG.error("Error getting content", e);

				}
				jettyRequest.setHandled(true);

			}
		});

		// Start the Server so it starts accepting connections from clients.
		server.start();

		try {
			while (true) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			LOG.warn("Shuting down Api Proxy");
		}

		p.close();
	}

	private static void showBanner() {
		LOG.info("    ___    ____  ____   ____                       ");
		LOG.info("   /   |  / __ \\/  _/  / __ \\_________  _  ____  __");
		LOG.info("  / /| | / /_/ // /   / /_/ / ___/ __ \\| |/_/ / / /");
		LOG.info(" / ___ |/ ____// /   / ____/ /  / /_/ />  </ /_/ / ");
		LOG.info("/_/  |_/_/   /___/  /_/   /_/   \\____/_/|_|\\__, /  ");
		LOG.info("                                          /____/   ");
		LOG.info("                     by Nicolas Richeton");
		LOG.info("Listening on " + bind + ":" + port);
		LOG.info("Target Host is " + targetHost);
		LOG.info("Login Host is " + loginHost);
		LOG.info("Threads is " + threads);
		LOG.info("");
	}

}
