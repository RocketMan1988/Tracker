/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.GpsFixQuality;

/**
 *
 * @author Sardon
 */
public class TextReader implements SentenceListener{

    private SentenceReader reader;
    private GUI window;
    
    
    
    public TextReader(GUI window) {
        this.window = window;
        
    }


    public void readGPSfromText(File file) throws IOException {

		// create sentence reader and provide input stream
		InputStream stream = new FileInputStream(file);
		reader = new SentenceReader(stream);

		// register self as a listener for GGA sentences
		reader.addSentenceListener(this, SentenceId.GGA);
		reader.start();
                
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.marineapi.nmea.event.SentenceListener#readingPaused()
	 */
	public void readingPaused() {
		//System.out.println("-- Paused --");
                window.txtLog.append("GPS Data File has been Read\n\n");
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.marineapi.nmea.event.SentenceListener#readingStarted()
	 */
	public void readingStarted() {
		//System.out.println("-- Started --");
                window.txtLog.append("Reading GPS Data File:\n");
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.marineapi.nmea.event.SentenceListener#readingStopped()
	 */
	public void readingStopped() {
		//System.out.println("-- Stopped --");
                window.txtLog.append("GPS Data File has been Read\n\n");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * net.sf.marineapi.nmea.event.SentenceListener#sentenceRead(net.sf.marineapi
	 * .nmea.event.SentenceEvent)
	 */
	public void sentenceRead(SentenceEvent event) {

	// Safe to cast as we are registered only for GGA updates. Could
	// also cast to PositionSentence if interested only in position data.
	// When receiving all sentences without filtering, you should check the
	// sentence type before casting (e.g. with Sentence.getSentenceId()).
	GGASentence ggaSentence = (GGASentence) event.getSentence();
        // RMCSentence rmcSentence = (RMCSentence) event.getSentence();
        window.txtLog.append(event.getSentence().toString() + "\n");
        
        
        
        double cameraAlt = ggaSentence.getPosition().getAltitude();
        double cameraLong = ggaSentence.getPosition().getLongitude();
        double cameraLat = ggaSentence.getPosition().getLatitude();
        double timeHour = ggaSentence.getTime().getHour();
        double timeMinute = ggaSentence.getTime().getMinutes();
        double timeSecond = ggaSentence.getTime().getSeconds();
        String fix = ggaSentence.getFixQuality().toString();
        int satelliteCount = ggaSentence.getSatelliteCount();
        double distance;
        
        distance =  window.geoCalculator.distance(cameraLat,cameraLong,   29.632925, -95.18695,0,0);
        //System.out.println(distance);
        
        Object[] row = { cameraLong, cameraLat, cameraAlt, "", "", fix, satelliteCount, window.geoCalculator.bearing(window.localLatitude, window.localLongitude, cameraLat,cameraLong), window.geoCalculator.calculateElevationAngle(window.localAltitude,window.localLatitude,window.localLongitude,cameraAlt/1000,cameraLat,cameraLong),"" };
        window.model.insertRow(window.table.getRowCount(),row);
        
        window.table.changeSelection(window.table.getRowCount() - 1, 0, false, false);
       
        try {
            GEControl.createPlacemark(cameraLong, cameraLat, cameraAlt, timeHour, timeMinute, timeSecond, fix, satelliteCount);
        } catch (FileNotFoundException ex) {                              
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
	// Do something with sentence data..
	//System.out.println(ggaSentence.getPosition());        
        
      
      
      
      /*  double cameraAlt = rmcSentence.getPosition().getAltitude();
        double cameraLong = rmcSentence.getPosition().getLongitude();
        double cameraLat = rmcSentence.getPosition().getLatitude();
        double timeHour = rmcSentence.getTime().getHour();
        double timeMinute = rmcSentence.getTime().getMinutes();
        double timeSecond = rmcSentence.getTime().getSeconds();
        double timemillisecond = rmcSentence.getTime().getMilliseconds();
        double dateYear = rmcSentence.getDate().getYear();
        double dateMonth = rmcSentence.getDate().getMonth();
        double dateDay = rmcSentence.getDate().getDay();
        double course = rmcSentence.getCourse();
        double speed = rmcSentence.getSpeed();
        
                                try {
            // TODO add your handling code here:

             testingNetworkLink.createPlacemark(cameraLong, cameraLat, cameraAlt);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Calculator.class.getName()).log(Level.SEVERE, null, ex);
        }
                                
                              
                
		// Do something with sentence data..
		//System.out.println(rmcSentence.getPosition());  
          
          
      }
*/
	}
    
}
