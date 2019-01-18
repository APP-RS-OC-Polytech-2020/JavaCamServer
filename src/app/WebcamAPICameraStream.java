package app;

import app.HttpStreamServer;

import com.github.sarxos.webcam.*;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

import java.util.Timer;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
/**
 * Classe principale qui permet d'initialiser et lancer le serveur.
 * Aka: MainClass
 * @author prospere
 *
 */
public class WebcamAPICameraStream {

    public static BufferedImage frame = null;
    private static HttpStreamServer httpStreamService;
    static Webcam videoCapture;
    //static Timer tmrVideoProcess;
    static Timer timerPush;
    static Timer timerPoke;
    
    static InetSocketAddress serverAddress =  new InetSocketAddress("193.48.125.70", 50008);
    static int port = 50009;
    static String name = "cam1";
    static int fps = 10;
    static int width = 176;
    static int height = 144;
    
    
    public static void start() {
    	
    	if(System.getProperty("os.name").toLowerCase().contains("linux")){
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
			System.err.println("Could not open camera.");
            return;
		}
        
        //Demarrer le serveur
        
        httpStreamService = new HttpStreamServer(port);
        new Thread(httpStreamService).start();

        //Laisser un temps de process avant de push l'image
        long periodPush = 1000/fps; //FPS to delay between frames
        timerPush = new Timer();
        
        frame = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        
        timerPush.schedule(new PushImageTask(httpStreamService, videoCapture,frame,periodPush-20), 100, periodPush);
        System.out.println("PushImageTask Scheduled");
        
       //Schedule server poke, so as to have something to work with.
       long periodPoke = 5000;
       timerPoke = new Timer();
       timerPoke.schedule(new PokeServerTask(httpStreamService, port, serverAddress,name), 100, periodPoke);
       System.out.println("PokeServerTask Scheduled");
 
    }

    public static void main(String[] args) {
        start();
    }


}
