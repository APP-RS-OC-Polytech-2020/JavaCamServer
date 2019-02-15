package app;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import picocli.CommandLine;

/**
 * The "real" main class. This is the entry point of app, and should be launched
 * any time you use it packaged.
 * @author prospere
 *
 */
public class Main {

	public static void main(String[] args) {
		
		String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";
		
		//Check if file exist
		File config = new File(log4jConfigFile);
		if(config.exists()){
			PropertyConfigurator.configure(log4jConfigFile);
		}else{
			System.err.println("WARNING - no config file found, defaulting to BasicConfigurator");
			BasicConfigurator.configure();
		}
		
		CommandLine.run(new WebcamAPICameraStream(),args);

	}

}
