import java.io.*;
import java.net.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import com.github.sarxos.webcam.Webcam;

class LiveFeedServer
{
    public static void main(String argv[]) throws Exception
    {
        System.out.println(InetAddress.getLocalHost());
        //String portStr = "3991";
        
        boolean greeted = false;
        int port = Integer.parseInt(argv[0]);
        //int port = Integer.parseInt(portStr);
        ServerSocket welcomeSocket = new ServerSocket(port);
        
        // get default webcam and open it
        Webcam webcam = null;
        try
        {
            webcam = Webcam.getDefault();
		    webcam.open();
        }
        catch(Exception e)
        {
            System.out.println("ERROR: No webcam detected. Please run this file on a computer with a webcam.");
            welcomeSocket.close();
            return;
        }

        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            // Makes sure greeting isn't sent twice
            if (!greeted)
            {
                outToClient.writeBytes("Hello!\n");
                greeted = true;
            }

            try
            {
                // save image to PNG file
                BufferedImage webcamImage = webcam.getImage();
                ImageIO.write(webcamImage, "PNG", new File("test.png"));

                // First get image size, send it to client
                File jokeFile = new File("test.png");
                BufferedImage image = ImageIO.read(jokeFile);
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", byteStream);
                byteStream.flush();
                long imgSize = byteStream.toByteArray().length;
                ByteBuffer buff = ByteBuffer.allocate(Long.BYTES);
                buff.putLong(imgSize);
                buff.rewind();
                outToClient.write(buff.array());

                // Send actual image to client
                outToClient.write(byteStream.toByteArray()); 
            }
            catch(NullPointerException e) 
            {
                // In case client disconnects prematurely
                connectionSocket.close();
                welcomeSocket.close();
                greeted = false;
            }
        }
    }
}
