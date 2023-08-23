package application.frequencydisplayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.fazecast.jSerialComm.SerialPort;

import application.frequencydisplayer.SerialInterface.NotValidPortException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class main_controller implements Initializable 
{
    private static final int [] baudrates = {9600, 19200, 38400, 56200, 76800, 115200};
    
    @FXML public MenuItem closeMenuItem;
    @FXML public Menu settingsMenu;
    @FXML public Menu COM_menu;
    @FXML public MenuItem disconenctDeviceItem;
    @FXML public MenuItem ConnectDeviceItem;
    @FXML public Menu baudrateMenu;
    @FXML public Label frequenzeLabel;

    private int currentBaudrate = 0;
    private String currentCOMPort = "";
    private SerialPortComunication serialPortComunication = null;


    
    public main_controller() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        frequenzeLabel.setText("0 Hz");
        baudrateMenu.getItems().clear();
        
    
        for (int value : baudrates) {
            MenuItem item = new MenuItem(Integer.toString(value));

            item.setOnAction(e -> {
                baudrateMenu.setText("Boudrate: " + value);
                currentBaudrate = value;
            });

            baudrateMenu.getItems().add(item);
        }

        baudrateMenu.setText("Boudrate: " + baudrates[5]);
        currentBaudrate = baudrates[5];
        
    }


    private String[] getAvailable_COM_PORTS()
    {
        String [] name;
        SerialPort[] portsAvailable = SerialPort.getCommPorts();

        name = new String [portsAvailable.length];

        for(int i = 0; i < name.length; i++) {
            name[i] = portsAvailable[i].getSystemPortName();

            System.out.println(portsAvailable[i].getPortDescription());
            System.out.println(portsAvailable[i].getDescriptivePortName());
       
        }
        return name;
    }


    
    
    @FXML
    public void closeApplication(Event event) {
        System.exit(0);
    }

    @FXML
    public void connectDevice(Event event) {
        if(serialPortComunication == null) {
            try {
                System.out.println("Opening " + currentCOMPort + "\nBoaudrate: " + currentBaudrate + " ");
                serialPortComunication = new SerialPortComunication(new SerialInterface(currentCOMPort, currentBaudrate), this);
                serialPortComunication.start();
            }  
            catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    

    @FXML
    public void disconnectDevice(Event event) {
        if(serialPortComunication != null && serialPortComunication.isAlive()) {
            serialPortComunication.CloseComunication();
            while(serialPortComunication.isAlive());
            serialPortComunication = null;
        }
        else {
            serialPortComunication = null;
        }
    }

    @FXML
    public void updateSettingsMenu(Event event) {

    }

    @FXML
    public void update_COM_PORTS(Event event) 
    {
        COM_menu.getItems().clear();

        for (SerialPort COM : SerialPort.getCommPorts()) {
            MenuItem item = new MenuItem(COM.getDescriptivePortName());
            
            item.setOnAction(e -> {
                currentCOMPort = COM.getSystemPortName();
                System.out.println("COM port selected: " + currentCOMPort);
            });
            
            COM_menu.getItems().add(item);
        }

        /*for (String str : getAvailable_COM_PORTS()) {
            MenuItem item = new MenuItem(str);
            COM_menu.getItems().add(item);
        }*/
    }

    @FXML
    public void updateBoudrateMenu(Event event) {

    }
}