/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package wseurekaclient;

import ec.edu.monster.vistas.MainView;

/**
 *
 * @author leito
 */
public class WSEurekaClient {

    /**
     * @param args the command line arguments
     */
   public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create and show the MainView
                MainView mainView = new MainView();
                mainView.setVisible(true);
            }
        });
    }
    
}
