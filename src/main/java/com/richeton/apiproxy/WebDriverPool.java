/**
 * Manage a pool of WebDriver instances
 */
package com.richeton.apiproxy;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

public class WebDriverPool {

    // Known WebDriver instances (added on init)
    List<WebDriver> webDrivers = new ArrayList<WebDriver>();

    // Available WebDriver instances (changed )
    List<WebDriver> availableWebDrivers = new ArrayList<WebDriver>();

    /**
     * Add a WebDriver instance to the pool
     * 
     * @param driver WebDriver instance
     */
    public void add(WebDriver driver) {
        webDrivers.add(driver);
        availableWebDrivers.add(driver);
    }

    /**
     * Return an available WebDriver instance from the pool
     * 
     * @return WebDriver instance
     */
    public synchronized WebDriver get() {
        WebDriver driver = availableWebDrivers.get(0);
        availableWebDrivers.remove(0);
        return driver;
    }

    /**
     * Release a WebDriver instance back into the pool, making it available for
     * other threads
     * 
     * @param driver WebDriver instance
     */
    public synchronized void release(WebDriver driver) {
        // Check we know about this driver
        if (!webDrivers.contains(driver)) {
            throw new IllegalArgumentException("Unknown driver");
        }
        availableWebDrivers.add(driver);
    }

    /**
     * Close all WebDriver instances in the pool
     */
    public void close() {
        for (WebDriver driver : webDrivers) {
            driver.close();
        }
    }

    /**
     * Return the number of WebDriver instances in the pool
     * 
     * @return
     */

    public int size() {
        return webDrivers.size();
    }

}