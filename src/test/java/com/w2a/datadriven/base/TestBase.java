package com.w2a.datadriven.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.w2a.utilities.ExtentManager;

public class TestBase {
	/*
	 * Initilization WebDriver logs properties excel db mail Exten Reports
	 */
	public static ThreadLocal<RemoteWebDriver> dr = new ThreadLocal<RemoteWebDriver>();
	public RemoteWebDriver driver = null;
	public Properties OR = new Properties();
	public Properties Config = new Properties();
	public FileInputStream fis;
	public Logger log = Logger.getLogger("devpinoyLogger");
	public WebDriverWait wait;
	public ExtentReports rep = ExtentManager.getInstance();
	public ExtentTest test;
	public static ThreadLocal<ExtentTest> exTest = new ThreadLocal<ExtentTest>();
	public String browser;

	public void addLog(String message) {

		log.debug("Thread : " + getThreadValue(dr.get()) + " Browser : " + browser + " " + message);
	}
	public static String screenshotPath;
	public static String screenshotName;

	public void captureScreenshot() {
		File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);

		Date d = new Date();
		screenshotName = d.toString().replace(":", "_").replace(" ", "_").concat(".jpg");

		try {
			FileUtils.copyFile(scrFile,
					new File(System.getProperty("user.dir") + "\\reports\\" + screenshotName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getExtentTest().log(LogStatus.INFO, "Screenshot -> " + test.addScreenCapture(screenshotName));
	}

	// @BeforeSuite
	public void setUp() {

		if (driver == null) {
			try {
				fis = new FileInputStream(
						System.getProperty("user.dir") + "\\src\\test\\resources\\properties\\Config.properties");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Config.load(fis);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fis = new FileInputStream(
						System.getProperty("user.dir") + "\\src\\test\\resources\\properties\\OR.properties");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				OR.load(fis);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public WebDriver getDriver() {
		return dr.get();
	}

	public void setWebDriver(RemoteWebDriver driver) {
		dr.set(driver);
	}

	public ExtentTest getExtentTest() {
		return exTest.get();
	}

	public void setExtentTest(ExtentTest et) {
		exTest.set(et);
	}
	
	public String getThreadValue(Object value) {
		String text = value.toString();
		String[] newText = text.split(" ");
		String text2 = newText[newText.length-1].replace("(", "").replace(")","");
		String[] lastText = text2.split("-");
		return lastText[lastText.length-1];
		
	}
	
	public void openBrowser(String browser) throws MalformedURLException {
		this.browser = browser;
		DesiredCapabilities cap = null;
		
		if (browser.equals("firefox")) {
			cap = DesiredCapabilities.firefox();
			cap.setBrowserName("firefox");
			cap.setPlatform(Platform.ANY);
		} else if (browser.equals("chrome")) {
			cap = DesiredCapabilities.chrome();
			cap.setBrowserName("chrome");
			cap.setPlatform(Platform.ANY);
		} else if (browser.equals("iexplore")) {
			cap = DesiredCapabilities.internetExplorer();
			cap.setBrowserName("iexplore");
			cap.setPlatform(Platform.WINDOWS);
		}

		driver = new RemoteWebDriver(new URL("http://192.168.99.100:4444/wd/hub"), cap);
		setWebDriver(driver);
		getDriver().manage().timeouts().implicitlyWait(Integer.parseInt(Config.getProperty("implicit.wait")),
				TimeUnit.SECONDS);
		getDriver().manage().window().maximize();
		getExtentTest().log(LogStatus.INFO, "Browser opened successfully " + browser);
	//	System.out.println(dr.get());
		System.out.println("Thread Value is : " + getThreadValue(dr.get()));
	}

	public void reportPass(String msg) {
		getExtentTest().log(LogStatus.PASS, msg);
	}

	public void reportFailure(String msg) {
		getExtentTest().log(LogStatus.FAIL, msg);
		captureScreenshot();
		Assert.fail(msg);
		//
	}

	public void navigate(String url) {

		getDriver().get(url);
		getExtentTest().log(LogStatus.INFO, "Navigating to " + url);
	}

	public void click(String locator) {
		try {
			if (locator.endsWith("_CSS")) {
				getDriver().findElement(By.cssSelector(OR.getProperty(locator))).click();
			} else if (locator.endsWith("_XPATH")) {
				getDriver().findElement(By.xpath(OR.getProperty(locator))).click();
			} else if (locator.endsWith("_ID")) {
				getDriver().findElement(By.id(OR.getProperty(locator))).click();
			}
			addLog("Clicking on an Element : " + locator);
		} catch (Throwable t) {
			reportFailure("Failing while clicking on an Element" + locator);
		}

	}

	public void type(String locator, String value) {
		try {
			if (locator.endsWith("_CSS")) {
				getDriver().findElement(By.cssSelector(OR.getProperty(locator))).sendKeys(value);
			} else if (locator.endsWith("_XPATH")) {
				getDriver().findElement(By.xpath(OR.getProperty(locator))).sendKeys(value);
			} else if (locator.endsWith("_ID")) {
				getDriver().findElement(By.id(OR.getProperty(locator))).sendKeys(value);
			}
			addLog("Typing on in an Element : " + locator);
			
		} catch (Throwable t) {
			reportFailure("Failing while typing in an Element" + locator);
		}
	}

	static WebElement dropdown;

	public void select(String locator, String value) {
		try {
			if (locator.endsWith("_CSS")) {
				dropdown = getDriver().findElement(By.cssSelector(OR.getProperty(locator)));
			} else if (locator.endsWith("_XPATH")) {
				dropdown = getDriver().findElement(By.xpath(OR.getProperty(locator)));
			} else if (locator.endsWith("_ID")) {
				dropdown = getDriver().findElement(By.id(OR.getProperty(locator)));
			}

			Select select = new Select(dropdown);
			select.selectByVisibleText(value);
		} catch (Throwable t) {
			reportFailure("Failing while selecting an Element" + locator);
		}
	}

	public boolean isElementPresent(By by) {
		try {
			getDriver().findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	@AfterSuite
	public void tearDown() {

		getDriver().quit();

	}

}
