/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Sardon
 */
public class TextWriter {
    
    private GUI window;
    String filename= "MyTestFile.txt";
    static FileWriter fileToRecord; //the true will append the new data
    static BufferedWriter bufferWritter; 
    static int beforeLength = 0;
    static int afterLength = 0;
            
    
    TextWriter(GUI window) {
        
     this.window = window;
        try
            {   
            this.fileToRecord = new FileWriter(filename,true);
            TextWriter.bufferWritter = new BufferedWriter(fileToRecord); 
            }
        catch(IOException ioe)
            {
            System.err.println("IOException: " + ioe.getMessage());
            }
        textAreaListener();
    }
    
    public void textAreaListener() {
        window.txtLog.getDocument().addDocumentListener(new MyDocumentListener());
    }

    private class MyDocumentListener implements DocumentListener {

        public MyDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            int i = 0;
            //System.out.println("In Insert");
            beforeLength = e.getDocument().getLength();
            try {
                if (e.getDocument().getText(afterLength, beforeLength - afterLength).equals("$"))
                {
                //System.out.println("Found $");
                //Write to text file and the read text file
                //Take the read text file and create a new point
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(TextWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            afterLength = beforeLength;
        }
         

        @Override
        public void removeUpdate(DocumentEvent e) {
            //System.out.println("In Remove");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            //System.out.println("In Changed");
       }
    }
 

}