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
	private WebDriver driver;

	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	private String loginUrl;

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
				driver = new FirefoxDriver();
				break;

			case "chrome":
				driver = new ChromeDriver();
				break;
			}
		}
	}

	/**
	 * Shutdown Executor and Browser
	 */
	public void close() {
		exec.shutdown();
		driver.close();
	}

	public String get(String url) throws InterruptedException, ExecutionException {
		GetJsonCallable gj = new GetJsonCallable(driver, loginUrl, url);
		FutureTask<String> result = new FutureTask<String>(gj);

		try {
			exec.execute(result);
		} catch (Exception e) {
			LOG.error("Error retreiving content for {}", url, e);
		}

		return result.get();
	}

	public void init() throws Exception {
		checkAndLoadDriver("gecko", "geckodriver");
		checkAndLoadDriver("gecko", "geckodriver.exe");
		checkAndLoadDriver("chrome", "chromedriver");
		checkAndLoadDriver("chrome", "chromedriver.exe");

		if (driver == null) {
			throw new Exception("Driver file missing !  geckodriver of chromedriver.");
		}
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
}
