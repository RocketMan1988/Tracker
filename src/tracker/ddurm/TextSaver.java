/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sardon
 */
public class TextSaver {

    GUI window = null;
    
    TextSaver(GUI window){
        this.window = window;
    }
    
    public void saveTextFile(File f){
        FileWriter fw = null;      
        try {
            fw = new FileWriter(f, false);
            window.txtLog.write(fw);
        } catch (IOException ex) {
            Logger.getLogger(TextSaver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(TextSaver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
