/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import net.sf.marineapi.nmea.event.SentenceListener;

/**
 *
 * @author Sardon
 */
public class Communicator implements SerialPortEventListener {
    //passed from main GUI
    GUI window = null;
    
    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
    final static int RETURN_ASCII = 13;
    final static int MONEY_SIGN = 36;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";
    String lastLine = "";

    public Communicator(GUI window) {
        this.window = window;
    }


    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                window.comPort.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect()
    {
        String selectedPort = (String)window.comPort.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;

            //for controlling GUI elements
            setConnected(true);

            //logging
            logText = selectedPort + " opened successfully.";
            window.txtLog.setForeground(Color.black);
            window.txtLog.append(logText);

            //CODE ON SETTING BAUD RATE ETC OMITTED
            //XBEE PAIR ASSUMED TO HAVE SAME SETTINGS ALREADY
        }
        catch (PortInUseException e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            
            window.txtLog.setForeground(Color.RED);
            window.txtLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            window.txtLog.append(logText + "\n");
            window.txtLog.setForeground(Color.RED);
        }
    }

    
    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            //writeData(0, 0);
            
            
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
            return successful;
        }
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        //System.out.println("initListener");
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }

    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            //writeData(0, 0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);

            logText = "Disconnected.";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append("\n" + logText + "\n\n");
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            
            try
            {
                byte singleData = (byte)input.read();

                if (singleData != NEW_LINE_ASCII)
                {
                    logText = new String(new byte[] {singleData});
                    window.txtLog.append(logText);
                }
                else
                {
                    window.txtLog.append("\n");
                }
                //if (singleData == RETURN_ASCII) {
                    
                //}
                if (singleData == MONEY_SIGN)
                {
                    int totalLines;
                    int startOffset;
                    int endOffset;
                    int lengthOffset;
                    //System.out.println("Money SIGN!!!");
                    window.txtLog.insert("\n", window.txtLog.getDocument().getLength() - 1);
                    totalLines = window.txtLog.getLineCount();
                    startOffset = window.txtLog.getLineStartOffset(totalLines - 2);
                    endOffset = window.txtLog.getLineEndOffset(totalLines - 2);
                    lengthOffset = endOffset - startOffset;
                    //System.out.println(totalLines);
                    //System.out.println(startOffset);
                    //System.out.println(endOffset);
                    //System.out.println(lengthOffset);
                    
                    lastLine = window.txtLog.getText(startOffset, lengthOffset);
                    //System.out.println(lastLine);
                    
                    
                    window.stringToGPS.readGPSfromString(lastLine);
                    
                    //GEControl.createPlacemark(cameraLong, cameraLat, cameraAlt, timeHour, timeMinute, timeSecond, fix, satelliteCount);
                    
                    //window.txtLog.setCaretPosition(window.txtLog.getDocument().getLength());
                }
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                window.txtLog.setForeground(Color.red);
                window.txtLog.append(logText + "\n");
                this.disconnect();
            }
           
        }
    }

    
    public void writeData(int leftThrottle)
    {
        try
        {
            //System.out.println(leftThrottle);
            output.write(leftThrottle);
            output.flush();
            //this is a delimiter for the data
            //output.write(NEW_LINE_ASCII);
            //output.flush();
           
        }
        catch (Exception e)
        {
            //System.out.println("Failed to write data...");
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }
    
    public void writeDataNewLine()
    {
                 try
        {
            //this is a delimiter for the data
            output.write(RETURN_ASCII);
            output.flush();
           
        }
        catch (Exception e)
        {
            //System.out.println("Failed to write data...");
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }
    
    public void writeData(byte leftThrottle)
    {
        try
        {
            //System.out.println(leftThrottle);
            output.write(leftThrottle);
            output.flush();
            //this is a delimiter for the data
            //output.write(NEW_LINE_ASCII);
            //output.flush();
           
        }
        catch (Exception e)
        {
            //System.out.println("Failed to write data...");
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }
    
        public void writeDataReboot()
    {
        try
        {
            output.write('!');
            output.flush();
            output.write(NEW_LINE_ASCII);
            output.flush();
            output.write('2');
            output.flush();
            //this is a delimiter for the data
            output.write(NEW_LINE_ASCII);
            output.flush();
            
            output.write('4');
            output.write('2');
            output.flush();
            //this is a delimiter for the data
            output.write(NEW_LINE_ASCII);
            output.flush();
            
            
           
        }
        catch (Exception e)
        {
            //System.out.println("Failed to write data...");
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "\n");
        }
    }
    
    
    
}
