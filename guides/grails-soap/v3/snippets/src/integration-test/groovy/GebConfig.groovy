import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

environments {

    htmlUnit {
        driver = { new HtmlUnitDriver() }
    }

    firefox {
        driver = { new FirefoxDriver() }
    }
}