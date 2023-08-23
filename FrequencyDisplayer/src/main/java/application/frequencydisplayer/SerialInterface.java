package application.frequencydisplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import com.fazecast.jSerialComm.*;

public class SerialInterface {

    protected SerialPort Device;
    protected InputStream SerialReader;
    protected OutputStream SerialWriter;

    public String PortName;
    public int Baudrate;
    
    protected volatile byte[] inputBuffer = new byte [0];
    protected volatile boolean DataAvailable = false;
    protected volatile int byteCount; 

    
    public SerialInterface(String PortName, int Baudrate) throws IOException, NotValidPortException
    {
        SerialPort ports[] = SerialPort.getCommPorts();
        boolean PortAvailable = false;
        this.Baudrate = Baudrate;
        this.PortName = PortName;

        for (SerialPort port : ports) {
            if(PortName.equals(port.getSystemPortName())) {
                PortAvailable = true;
                break;
            }
        }

        if(!PortAvailable) {
            throw new NotValidPortException();
        }
    }

    public void StartComunication()
    {
        Device = SerialPort.getCommPort(PortName);
        Device.setComPortParameters(this.Baudrate, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        Device.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        
        if (!Device.openPort()) {
            throw new IllegalStateException();
        }

        //System.out.println("---------------------------------------------------");
        //System.out.println("Connecting to (" + PortName + ")");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {Device.closePort();}));
        Device.addDataListener(new PacketListener(this));   
    }

    public void StopComunication()
    {
        if(Device != null) {
            Device.closePort();
        }
    }


    public String readString() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < inputBuffer.length; i++) {
            sb.append((char)(inputBuffer[i]));
        }
        DataAvailable = false;
        return sb.toString();
    }

    public byte[] readBytes() throws IOException {
        DataAvailable = false;
        return inputBuffer;
    }

    public void WriteBytes(byte [] buffer) {
        Device.writeBytes(buffer, buffer.length);
    }

    

    public boolean SerialAvailable()  {
        return DataAvailable;
    }

    public void clear()  {
        DataAvailable = false;
    }


    public class NotValidPortException extends Exception 
    {
        public NotValidPortException() {
            super();
        }
    }
    
}
