package com.richeton.apiproxy;

import java.util.concurrent.Callable;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetJsonCallable implements Callable<String> {
	private static Logger LOG = LoggerFactory.getLogger(GetJsonCallable.class);
	private WebDriver driver;
	private String loginUrl;
	private String url;

	public GetJsonCallable(WebDriver driver, String loginUrl, String url) {
		this.url = url;
		this.driver = driver;
		this.loginUrl = loginUrl;
	}

	@Override
	public String call() throws Exception {
		driver.get(url);

		waitForLogin(driver);

		return getContent();
	}

	private String getContent() {
		String content = driver.getPageSource();

		// Locate JSON
		// Browser may render JSON in an HTML document: just extract the JSON content.
		if (content.indexOf("{") >= 0 && content.indexOf("}") >= 0) {
			content = content.subSequence(content.indexOf("{"), content.lastIndexOf("}") + 1).toString();
		}

		return content;
	}

	/**
	 * We require the use to manually log in to access the API
	 *
	 * @param driver
	 * @throws InterruptedException
	 */
	private void waitForLogin(WebDriver driver) throws InterruptedException {

		while (driver.getCurrentUrl() == null || driver.getCurrentUrl().startsWith(loginUrl)) {
			LOG.info("Waiting for manual loggin...");
			Thread.sleep(500);
		}
	}
}
