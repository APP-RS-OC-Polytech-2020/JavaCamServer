package app;

import java.awt.image.BufferedImage;
import java.util.TimerTask;

import com.github.sarxos.webcam.Webcam;

public class PushImageTask extends TimerTask {
	private HttpStreamServer httpStreamServer;
	private BufferedImage frame;
	private Webcam video;
	private static boolean  sendingFrame = false;
	

	public PushImageTask(HttpStreamServer httpStreamServer, Webcam video, BufferedImage frame,long period){
		this.httpStreamServer = httpStreamServer;
		this.video = video;
		this.frame = frame;
	}
	@Override
	public void run() {
		if(!sendingFrame){
			if(httpStreamServer.isConnected()){
				sendingFrame = true;
				frame = video.getImage();
				httpStreamServer.pushImage(frame);
				sendingFrame = false;
			}
		}
	}

}
