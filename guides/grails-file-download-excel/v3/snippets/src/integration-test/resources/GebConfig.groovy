import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver

ChromeOptions options = new ChromeOptions()
if ( System.getProperty('download.folder') ) {
    options.setExperimentalOption("prefs", [
            "profile.default_content_settings.popups":  0, // <1>
            "download.default_directory": System.getProperty('download.folder') // <2>
    ])
}

environments {

    // run via “./gradlew -Dgeb.env=chrome iT”
    chrome {
        driver = { new ChromeDriver(options) }
    }

    // run via “./gradlew -Dgeb.env=chromeHeadless iT”
    chromeHeadless {
        driver = {
            options.addArguments('headless')
            new ChromeDriver(options)
        }
    }

    // run via “./gradlew -Dgeb.env=firefox iT”
    firefox {
        driver = { new FirefoxDriver() }
    }
}
