package app;

import app.HttpStreamServer;

import com.github.sarxos.webcam.*;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

import java.util.Timer;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;

import picocli.CommandLine.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class which initialize and launch the server.
 * @author prospere
 *
 */
public class WebcamAPICameraStream implements Runnable {
	
	final Logger logger = LoggerFactory.getLogger(WebcamAPICameraStream.class);

    public static BufferedImage frame = null;
    private static HttpStreamServer httpStreamService;
    static Webcam videoCapture;
    static Timer timerPush;
    static Timer timerPoke;
    
    @Option(names = {"-a","--address","--addr"}, description = "Adress of the server to poke (default: ${DEFAULT-VALUE})")
    static String STRserverAddress =  "193.48.125.70";
    @Option(names = {"-p","--port"}, description = "Port of the server to poke (default: ${DEFAULT-VALUE})")
    static int serverPort = 50008;
    @Option(names = {"-cp","--clientport","--cliport"}, description = "Port we will be using to send our images (default: ${DEFAULT-VALUE})")
    static int port = 50009;
    @Option(names = {"-n","--name"}, description = "Name of our sender, will figure in JSON")
    static String name = "cam";
    @Option(names = {"-f","--fps"}, description = "Number of frames per seconds, camera-dependant (default: ${DEFAULT-VALUE})")
    static int fps = 10;
    static int width = 640;
    static int height = 480;
    @Option(names = "-v" , description = "Makes the program say more things (useless rn)")
    static boolean verbose;
    
    static InetSocketAddress serverAddress = new InetSocketAddress(STRserverAddress,serverPort);
    
    
    public void run() {
    	if(System.getProperty("os.name").toLowerCase().contains("linux")){
    			logger.info("Oh, we're on Linux, using V4l4jDriver.");
            	Webcam.setDriver(new V4l4jDriver()); // this is important for new cam on Linux
    	}
    	
    	//Ouvrir la camera
        videoCapture = Webcam.getDefault();
        videoCapture.setViewSize(new Dimension(width,height));
        
        //Si la camera  ne fonctionne pas bien, on ne fait rien
        //Sinon, on commence a stream, et on notifie notre serveur.
        try {
			videoCapture.open();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Could not open camera. Aborting.");
			return;
		}
        
	    logger.info("Camera opened. My name is:"
	        		+ name
	        		+ " and I'll stream in: "
	        		+ width
	        		+ "x"
	        		+ height
	        		+ "@"
	        		+ fps
	        		+ "fps.");
        
        //Demarrer le serveur
        
        httpStreamService = new HttpStreamServer(port,this);
        new Thread(httpStreamService).start();

        //Laisser un temps de process avant de push l'image
        long periodPush = 1000/fps; //FPS to delay between frames
        timerPush = new Timer();
        
        frame = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        
        timerPush.schedule(new PushImageTask(httpStreamService, videoCapture,frame,periodPush-20), 100, periodPush);
        logger.info("PushImageTask Scheduled");
    }
    /**
     * Schedule the poke Task. This will "poke" our server, 
     * sending him a JSON message that says who we are and where we are.
     * @param periodPoke
     */
    public void startPokeTask(long periodPoke){
        //Schedule server poke, so as to have something to work with.
        timerPoke = new Timer();
        timerPoke.schedule(new PokeServerTask(httpStreamService, port, serverAddress,name), 100, periodPoke);
        logger.info("PokeServerTask Scheduled");
    }

    /**
     * Main to test quickly stuff. To be run from IDE, usually.
     * @param args
     */
    public static void main(String[] args) {
    	WebcamAPICameraStream cam = new WebcamAPICameraStream();
        cam.run();
    }

}
