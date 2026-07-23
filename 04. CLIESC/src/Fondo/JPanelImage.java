/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Fondo;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Custom JPanel for displaying a background image.
 */
public class JPanelImage extends JPanel {

    private final String path;

    public JPanelImage(JPanel pnlImagen, String path) {
        this.path = path;
        // Set the panel to preferred size if needed
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Load the image from the path
        ImageIcon img = new ImageIcon(getClass().getResource(path));
        // Draw the image
        g.drawImage(img.getImage(), 0, 0, getWidth(), getHeight(), this);
    }
}
