package display.containers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import display.MainWindow;

public class ImagePanel extends JPanel{
	private BufferedImage image;

    public ImagePanel(String img) {
       try {                
          image = ImageIO.read(new File("/images/"));
       } catch (IOException ex) {
            // handle exception...
       }
       ImageIcon icon2=new ImageIcon(MainWindow.class.getResource("/images/"+img));
       image = new BufferedImage(100, 175, Image.SCALE_SMOOTH);
       image.getGraphics().drawImage(icon2.getImage(), 0, 0 , null);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
    }
}
