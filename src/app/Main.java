package app;

import picocli.CommandLine;

/**
 * The "real" main class. This is the entry point of app, and should be launched
 * any time you use it packaged.
 * @author prospere
 *
 */
public class Main {

	public static void main(String[] args) {
		System.setProperty("log4j.configuration", "log4j.properties");
		
		CommandLine.run(new WebcamAPICameraStream(),args);

	}

}
