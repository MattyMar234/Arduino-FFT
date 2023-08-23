package application.frequencydisplayer;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


public class PacketListener implements SerialPortDataListener 
{
    private SerialInterface MySerialInterface;

    public PacketListener(SerialInterface s) {
        this.MySerialInterface = s;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) 
    {
        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) 
        {
            MySerialInterface.inputBuffer = new byte[MySerialInterface.Device.bytesAvailable()];
            MySerialInterface.byteCount = MySerialInterface.Device.readBytes(MySerialInterface.inputBuffer, MySerialInterface.inputBuffer.length);
            MySerialInterface.DataAvailable = true;
        }

        //System.out.println(event.getEventType());
        //SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
    }

    
}
