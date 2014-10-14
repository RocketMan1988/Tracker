/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

/**
 *
 * @author Sardon
 */
public class StringToGPS implements SentenceListener {
    
    private SentenceReader reader;
    private GUI window;
    
    
    public StringToGPS(GUI window) {
        this.window = window;
        
    }
    
        public void readGPSfromString(String myString) throws IOException {

		// create sentence reader and provide input stream
		InputStream stream = new ByteArrayInputStream( myString.getBytes( Charset.defaultCharset() ) );
		reader = new SentenceReader(stream);

		// register self as a listener for GGA sentences
		reader.addSentenceListener(this, SentenceId.GGA);
		reader.start();
                
                //System.out.println("In ReadGPSfromString");
	}

    @Override
    public void readingPaused() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readingStarted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readingStopped() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sentenceRead(SentenceEvent event) {
        // Safe to cast as we are registered only for GGA updates. Could
	// also cast to PositionSentence if interested only in position data.
	// When receiving all sentences without filtering, you should check the
	// sentence type before casting (e.g. with Sentence.getSentenceId()).
        //System.out.println("In Sentence Read for StringToGPS");
	GGASentence ggaSentence = (GGASentence) event.getSentence();
        // RMCSentence rmcSentence = (RMCSentence) event.getSentence();
     
        double cameraAlt = ggaSentence.getPosition().getAltitude();
        double cameraLong = ggaSentence.getPosition().getLongitude();
        double cameraLat = ggaSentence.getPosition().getLatitude();
        double timeHour = ggaSentence.getTime().getHour();
        double timeMinute = ggaSentence.getTime().getMinutes();
        double timeSecond = ggaSentence.getTime().getSeconds();
        String fix = ggaSentence.getFixQuality().toString();
        int satelliteCount = ggaSentence.getSatelliteCount();
        
        //System.out.println(cameraLong);
        
        try {
            GEControl.createPlacemark(cameraLong, cameraLat, cameraAlt, timeHour, timeMinute, timeSecond, fix, satelliteCount);
            Object[] row = { cameraLong, cameraLat, cameraAlt, "", "", fix, satelliteCount,window.geoCalculator.bearing(window.localLatitude, window.localLongitude,cameraLat,cameraLong), window.geoCalculator.calculateElevationAngle(window.localAltitude,window.localLatitude,window.localLongitude,cameraAlt/1000,cameraLat,cameraLong), "" };
            window.model.insertRow(window.table.getRowCount(), row);
            
            window.table.changeSelection(window.table.getRowCount() - 1, 0, false, false);
        } catch (FileNotFoundException ex) {                              
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
	// Do something with sentence data..
	//System.out.println(ggaSentence.getPosition());        
    }
    
}
