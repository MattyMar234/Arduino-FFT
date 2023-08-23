package application.frequencydisplayer;

import java.io.IOException;
import java.util.Scanner;

import javafx.application.Platform;

public class SerialPortComunication extends Thread {
    
    public volatile boolean ExecuteReadRutine  = false;
    public volatile boolean ExecuteWriteRutine = false;
    public volatile boolean ExecuteEraseRutine = false;

    protected volatile byte [] buffer;
    protected volatile String response = "";
    protected volatile SerialInterface COM_DEVICE;
    protected volatile boolean running;
    
    protected long timeoutValue = 0; 
    protected long timeoutInterval = 6000; 
    protected long timeoutstart = 0;
    
    protected long PingValue = 0; 
    protected long PingInterval = 1000; 
    protected long Pingstart = 0;

    private main_controller controller;


    public SerialPortComunication(SerialInterface serialIntreface, main_controller controller) 
    {
        this.controller = controller;
        this.COM_DEVICE = serialIntreface;
        running = true;
    }


    @Override
    public void run()
    {
        StringBuffer sb = new StringBuffer();
        try {
            while(running) 
            {
                this.COM_DEVICE.StartComunication();

                while(COM_DEVICE != null && running)
                {
                    if(COM_DEVICE.DataAvailable) {
                        for(byte b : COM_DEVICE.readBytes()) 
                        {
                            if((char) b != '\r' && (char) b != '\n') {
                               sb.append((char)b);
                            }
                            else if((char) b == '\n') {
                                String str = sb.toString();
                                sb.setLength(0);

                                System.out.println(str);

                                Platform.runLater(() -> {
                                    if(str.contains("f:")) {
                                        String freq = str.split("f:")[1];
                                        controller.frequenzeLabel.setText(freq + (freq.contains("Hz") ? "" : " Hz"));
                                    }
                                }); 
                            }   
                        }   
                    }
                    else {
                        //try {sleep(20);} catch (InterruptedException e) {}
                    }   
                }
            }

            if(this.COM_DEVICE != null) {
                this.COM_DEVICE.StopComunication();
            }
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } 
    }

    public void CloseComunication() {
        COM_DEVICE.Device.closePort();
        running = false;
    }

    

    /*public void setSerialInterface(SerialInterface serialIntreface) {
        this.COM = serialIntreface;
    }

    public void removeSerialPORT() {
        PortConnected = false;
        this.COM = null;
    }*/

    protected byte [] String_to_byteBuffer(String str) {
        byte [] buffer;

        buffer = new byte[str.length()];

        for(int i = 0; i < str.length(); i++) {
            buffer[i] = (byte) str.charAt(i);
        }
            
        return buffer;
    }
}
