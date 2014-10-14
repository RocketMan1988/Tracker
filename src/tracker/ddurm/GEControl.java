/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import static com.oracle.jrockit.jfr.ContentType.Timestamp;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Camera;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import de.micromata.opengis.kml.v_2_2_0.gx.Playlist;
import de.micromata.opengis.kml.v_2_2_0.gx.Tour;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.marineapi.nmea.util.Time;

/**
 *
 * @author Sardon
 */
public class GEControl {
    
    GUI window = null;
    
    static Kml kml = new Kml();
    static Kml networkkml = new Kml();
    static NetworkLink link = new NetworkLink();
    static Link linkToKML = new Link();
    static Document doc = new Document();
    static Folder folderPoints = new Folder();
    static Folder folderTrack = new Folder();
    static Folder folderLines = new Folder();
    static File file = new File("netWorkLink.kml");
    static Camera camera = new Camera();
    static File kmlFile = new File("MainKML.kml");
    static Playlist playlist = new Playlist();
    static Tour tour = new Tour();
    static double cntpoints = 1;
    static Date lastDate;
    static SimpleDateFormat format = new SimpleDateFormat ("hh:mm:ss");
    static SimpleDateFormat formatWhen = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static LineString linestring = new LineString();
    static LineStyle linestyle = new LineStyle();
    static Style style = new Style();
    
    GEControl(GUI window) {
        this.window = window;
        lastDate = new Date();
        lastDate.setTime(0);
    }
    
    public void startTracking(){
      
        //The code below checks to see if Google Earth is running. If it isn't then it will execute a set of routines
        //that launch Google Earth and create the files nessay for tracking.
        try {
            String line;
            String pidInfo ="";
            
            Process p = null;
            try {
                p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            } catch (IOException ex) {
                Logger.getLogger(GEControl.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    pidInfo+=line;
                }
            }
            
            if(pidInfo.contains("googleearth.exe"))
            {
                // do what you want
                //System.out.println("Google Earth is Running!");
            }
            else
            {
                this.createMainKML();
                this.createNetworkLinkKML();
                    this.startGoogleEarth();           
            }
        } catch (IOException ex) {
            Logger.getLogger(GEControl.class.getName()).log(Level.SEVERE, null, ex);
        }
}
    
    public static void clearPointsandTrack(){
        try {
            GEControl.createMainKML();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GEControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            GEControl.createNetworkLinkKML();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GEControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        GEControl.cntpoints = 1;
        
    }
    
    public static void createMainKML() throws FileNotFoundException{
     
        doc = kml.createAndSetDocument().withName("Tracker").withOpen(true);

        // create a Folder for Points
        folderPoints = doc.createAndAddFolder();
        folderPoints.withName("Tracker's Data Points").withOpen(true);

        // create a Folder for Track
        folderTrack = doc.createAndAddFolder();
        folderTrack.withName("Tracker's Track").withOpen(Boolean.TRUE);
        
        // create a Folder for Lines
        folderLines = doc.createAndAddFolder();
        folderLines.withName("Tracker's Lines").withOpen(Boolean.TRUE);
        
      
        //Create Track
        createTrack();
                
        // print and save
        kml.marshal(kmlFile);
    }   
    
    public static void createNetworkLinkKML() throws FileNotFoundException{
                
        link = networkkml.createAndSetNetworkLink().withName("Tacker").withOpen(true);
        linkToKML = link.createAndSetLink();

        link.setVisibility(Boolean.TRUE);
        link.setFlyToView(Boolean.TRUE);
                
        linkToKML.setViewRefreshMode(ViewRefreshMode.ON_STOP);
        linkToKML.setViewRefreshTime(2);
                
        linkToKML.setHref("C:\\Users\\Sardon\\Documents\\NetBeansProjects\\Tracker.ddurm\\MainKML.kml");
                
                
        // print and save
        networkkml.marshal(file);
    }
    
        /**
 * The createPlacemarkWithChart ()-method generates and set a placemark object, with the given statistical data . The Icon and Style
 * objects (color and size of the text and icon) are saved to the root element. The placemark is created and set to the given folder.
 * 
 * @param longitude of the continent
 * @param latitude of the continent
 * @param altitude of the continent
 * @throws java.io.FileNotFoundException
 */
public static void createPlacemark(double longitude, double latitude, double altitude) throws FileNotFoundException {

        camera.setAltitude(altitude + 100);
        camera.setLongitude(longitude);
        camera.setLatitude(latitude);
        
       //Placemark line = folderLines.createAndAddPlacemark(); 
       Placemark placemark = folderPoints.createAndAddPlacemark();
       
       LineString lines = folderLines.createAndAddPlacemark().createAndSetLineString().addToCoordinates(longitude, latitude, altitude);
    
        //Placemark placemark = kml.createAndSetPlacemark();
        // use the style for each continent
       
        placemark.withName("Point: " + String.valueOf(cntpoints))
            .withStyleUrl("#style_type")    
            // 3D chart imgae
            .withDescription(
                "Longitude: " + String.valueOf(longitude) + " Latitude: " + String.valueOf(latitude) + " Altitude: " + String.valueOf(altitude))
            // coordinates and distance (zoom level) of the viewer
            .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(altitude).withRange(altitude + 100)
                ;
       
        //line.withName("Line: " + String.valueOf(cntpoints)).createAndSetLineString().addToCoordinates(longitude, latitude, altitude);
        //linestring.addToCoordinates(longitude, latitude, altitude);
        placemark.createAndSetPoint().addToCoordinates(longitude, latitude); // set coordinates
        
        //line.createAndSetLineString().addToCoordinates(longitude, latitude, altitude);
        //createTrackPoint(1,longitude, latitude, altitude);
             
        kml.marshal(kmlFile);
        
        cntpoints = cntpoints + 1;
        
}

public static void createPlacemark(double longitude, double latitude, double altitude, double hour, double minute, double second, String fix, int satelliteCount) throws FileNotFoundException {

        camera.setAltitude(altitude + 100);
        camera.setLongitude(longitude);
        camera.setLatitude(latitude);
        
       
       Point point = null; 
       Placemark placemark = folderPoints.createAndAddPlacemark();
       
       Date date = new Date(69, 7, 20, (int)hour, (int)minute,(int)second);
       
        //Placemark placemark = kml.createAndSetPlacemark();
        // use the style for each continent
        placemark.withName("Point: " + String.valueOf(cntpoints))
            .withStyleUrl("#style_type")
            // 3D chart imgae
            .withDescription(
                "Longitude: " + String.valueOf(longitude) + "\nLatitude: " + String.valueOf(latitude) + "\nAltitude: " + String.valueOf(altitude) + "\nTime: " + format.format(date) + "\nFix: " + fix + "\nSatellite Count: " + String.valueOf(satelliteCount))
            // coordinates and distance (zoom level) of the viewer
            .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude).withAltitude(altitude).withRange(altitude + 100)
                ;
        
        point = placemark.createAndSetPoint();
        point.setAltitudeMode(AltitudeMode.RELATIVE_TO_SEA_FLOOR);
        point.addToCoordinates(longitude, latitude, altitude); // set coordinates
        placemark.createAndSetTimeStamp().withWhen(formatWhen.format(date));
      
      
        
        createTrackPoint(longitude, latitude, altitude, date);
        
        linestring.addToCoordinates(longitude, latitude, altitude);
        //createLineSegement(longitude,latitude,altitude);
             
        kml.marshal(kmlFile);
        
        cntpoints = cntpoints + 1;
        
}



public static void createTrack() throws FileNotFoundException {

  tour = folderTrack.createAndAddTour().withName("TourTest"); 
 
  playlist = tour.createAndSetPlaylist();
  
  style = folderLines.createAndAddStyle().withId("LineStyleMain");
  
  linestyle = style.createAndSetLineStyle();
  
  linestring = folderLines.createAndAddPlacemark().withName("Line Pathway").withStyleUrl("#LineStyleMain").createAndSetLineString();  
  
  linestyle.setColor("red");
  linestyle.setWidth(5);
  
  
  linestring.createAndSetCoordinates();
  
  
}

public static void createTrackPoint(double longitude, double latitude, double altitude, Date date) throws FileNotFoundException {

        Camera cameraTrack = folderTrack.createAndSetCamera();
        
        long duration = (date.getTime() - lastDate.getTime())/1000;
        
        playlist.createAndAddFlyTo().setDuration(duration);
        cameraTrack = playlist.createAndAddFlyTo().createAndSetCamera();
        cameraTrack.setLongitude(longitude);
        cameraTrack.setLatitude(latitude);
        cameraTrack.setAltitude(altitude);
        
        lastDate = date;
     
}


public static void createTrackPoint(double duration, double longitude, double latitude, double altitude) throws FileNotFoundException {

        Camera cameraTrack = folderTrack.createAndSetCamera();
           
        playlist.createAndAddFlyTo().setDuration(duration);
        cameraTrack = playlist.createAndAddFlyTo().createAndSetCamera();
        cameraTrack.setLongitude(longitude);
        cameraTrack.setLatitude(latitude);
        cameraTrack.setAltitude(altitude);
     
}
    
public static void createLineSegement(double longitude, double latitude, double altitude){
 
    //line.createAndSetLineString().addToCoordinates(longitude, latitude, altitude);
  
}

    public static void startGoogleEarth(){
    try {
            Process p = Runtime.getRuntime().exec("C:\\Program Files (x86)\\Google\\Google Earth\\client\\googleearth.exe \"C:\\Users\\Sardon\\Documents\\NetBeansProjects\\Tracker.ddurm\\netWorkLink.kml\" ");
        } catch (IOException ex) { 
            Logger.getLogger(GEControl.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void startGoogleEarthDelay(long timeDelay) throws InterruptedException{
  
        new Timer().schedule(new TimerTask() {          
            @Override
            public void run() {
                // this code will be executed after 2 seconds 
                tracker.ddurm.GEControl.startGoogleEarth();
            }
        }, timeDelay);
   }
    
    

}