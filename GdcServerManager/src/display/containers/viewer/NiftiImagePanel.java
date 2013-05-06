package display.containers.viewer;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
 
 
/**
 * Display image in a panel
 * @author Jérémy DEVERDUN
 *
 */
public class NiftiImagePanel extends JPanel {
    Image image = null;
   
    /**
     * 
     * @param image
     */
    public NiftiImagePanel(Image image) {
        this.image = image; 
    }
    
    /**
     * 
     */
    public NiftiImagePanel() {
    	image = null;
    }
    
    /**
     * 
     * @param image
     */
    public void setImage(Image image){
        this.image = image;
        this.repaint();
    }
   
    /**
     * 
     * @param image
     * @return
     */
    public Image getImage(Image image){
        return image;
    }
   
    /**
     * 
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
        if (image != null) { //there is a picture: draw it
            int height = this.getSize().height;
            int width = this.getSize().width;      
            g.drawImage(image,0,0, width, height, this);
        }
    }
}