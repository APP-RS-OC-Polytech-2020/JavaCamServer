package app;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TimerTask;

import com.github.sarxos.webcam.Webcam;

public class PushImageTask extends TimerTask {
	private HttpStreamServer httpStreamServer;
	private BufferedImage frame;
	private Webcam video;
	private long period;
	private static boolean  sendingFrame = true;
	

	public PushImageTask(HttpStreamServer httpStreamServer, Webcam video, BufferedImage frame,long period){
		this.httpStreamServer = httpStreamServer;
		this.video = video;
		this.frame = frame;
		this.period = period;
	}
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		if(sendingFrame){
			if(httpStreamServer.isConnected()){
				frame = video.getImage();
				try {
					httpStreamServer.pushImage(frame);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			sendingFrame = true;
		}
		long endTime = System.currentTimeMillis();
		if(endTime-time > period){
			sendingFrame = false;
		}
	}

}
