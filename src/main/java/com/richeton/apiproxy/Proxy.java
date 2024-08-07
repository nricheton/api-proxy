package com.richeton.apiproxy;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy {
	private static Logger LOG = LoggerFactory.getLogger(Proxy.class);

	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	private String loginUrl;
	private int threads = 1;
	private WebDriverPool webDriverPool;

	/**
	 * Load driver if found in the current path
	 *
	 * @param type
	 * @param file
	 */
	private void checkAndLoadDriver(String type, String file) {
		File f = new File(file);
		if (f.exists()) {
			LOG.info("Found driver: {}", file);

			System.setProperty("webdriver." + type + ".driver", f.getAbsolutePath());

			switch (type) {
				case "gecko":
					for (int i = 0; i < threads; i++) {
						webDriverPool.add(new FirefoxDriver());
					}
					break;

				case "chrome":
					for (int i = 0; i < threads; i++) {
						webDriverPool.add(new ChromeDriver());
					}
					break;
			}
		}
	}

	/**
	 * Shutdown Executor and Browser
	 */
	public void close() {
		exec.shutdown();
		webDriverPool.close();
	}

	public String get(String url) throws InterruptedException, ExecutionException {
		WebDriver driver = webDriverPool.get();

		GetJsonCallable gj = new GetJsonCallable(driver, loginUrl, url);
		FutureTask<String> result = new FutureTask<String>(gj);

		try {
			exec.execute(result);
		} catch (Exception e) {
			LOG.error("Error retreiving content for {}", url, e);
		}

		webDriverPool.release(driver);
		return result.get();
	}

	public void init() throws Exception {

		webDriverPool = new WebDriverPool();

		checkAndLoadDriver("gecko", "geckodriver");
		checkAndLoadDriver("gecko", "geckodriver.exe");
		checkAndLoadDriver("chrome", "chromedriver");
		checkAndLoadDriver("chrome", "chromedriver.exe");

		if (webDriverPool.size() == 0) {
			throw new Exception("Driver file missing !  geckodriver of chromedriver.");
		}
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}
}
