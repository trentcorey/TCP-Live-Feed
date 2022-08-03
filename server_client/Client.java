import java.io.*;
import java.net.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

class LiveFeedClient
{
    public static void main(String argv[]) throws Exception
    {
        boolean greeted = false;

        // Display window to display image
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(1200,1800);
        JLabel lbl=new JLabel();
        frame.setVisible(true);          

        while(true)
        {
            String addr = argv[0];
            //String addr= "localhost";
            int port = Integer.parseInt(argv[1]);
            //int port = 3991;
            Socket clientSocket = new Socket(addr, port);
            // hostname 10.242.94.34 found by running InetAddress.getLocalHost() from server machine

            BufferedReader inFromServer  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataInputStream imgFromServer = new DataInputStream(clientSocket.getInputStream());

            // Whether or not to display welcome msg
            if(!greeted)
            {
                System.out.println(inFromServer.readLine());
                greeted = true;
            }

            // First, receive image size
            byte[] receiveData = new byte[Long.BYTES];
            imgFromServer.readFully(receiveData);

            // Allocate enough space in the buffer for the image
            ByteBuffer buff = ByteBuffer.allocate(Long.BYTES);
            buff.put(receiveData);
            buff.flip();
            buff.rewind();
            long imgSize = buff.getLong();

            // Receive image file
            receiveData = new byte[(int)imgSize];
            imgFromServer.readFully(receiveData);

            // Write image file to system
            File writer = new File("output/test.png");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(receiveData));
            ImageIO.write(image, "png", writer);

            BufferedImage img;
            try
            {
                img=ImageIO.read(new File("output/test.png"));
                ImageIcon icon=new ImageIcon(img);
                lbl.setIcon(icon);
                frame.add(lbl);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            catch(Exception e)
            {
                //System.out.println("dropped frame");
            }

            clientSocket.close();  
        }
    }
}