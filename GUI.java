import processing.serial.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import processing.opengl.*;
import saito.objloader.*;
import g4p_controls.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Timer;
import java.text.DecimalFormat;
import java.util.Arrays;
import cc.arduino.*;
//values for data read from IMU
static float roll  = 0.0F;
static float pitch = 0.0F;
static float yaw   = 0.0F;
static String accel = "0.0"; //X direction of microcontroller NOT IMU
static String velX = "0.0";
float temp  = 0.0F;
float alt   = 0.0F;

// Serial port state.
Serial       port;
final String serialConfigFile = "serialconfig.txt";
boolean      printSerial = false;

// UI controls.
GDropList serialList;

//GUI variables
static JFrame frame;
static JButton button;
static JPanel tiltPanel;
static JLabel tilt;
static JPanel tPanel;
static JLabel elapsed;
static JPanel accelerationPanel;
static JLabel acceleration;
static JPanel tdPanel;
static JLabel totalDistance;
static JPanel statsPanel;
static JLabel tD;
static JPanel timePanel;
static JLabel time;
static JTextField input;
static JLabel postTilt;
static JLabel postAcceleration;
static JLabel max;
static JLabel min;
static double maxTilt = -360.0;
static double minTilt = 360.0;
static double dist = 0;
static double tempAccel;
static long init_time;
static long start_time;
static long end_time;
static int minute = 0;
static int second = 0;
static int count = 0;
static double avgAccel = 0.0;
static double[] accelArray= new double[600];
static double[] tiltArray = new double[600];
static double[] tdArray = new double[600];
static boolean screen = true;

void setup() //sets up connection with bluetooth module and creates GUI
{
  Arrays.fill(accelArray, 0.0);
  Arrays.fill(tiltArray, 0.0);
  Arrays.fill(tdArray, 0.0);
  port = new Serial(this, "COM4", 9600);
  port.bufferUntil('\n');
  SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GUI gui = new GUI();
      }
    });
    frame = new JFrame();
    trialScreen(frame);
    size(250, 250);
    loop();
}

void draw(){ //update GUI indefinitely, while on the 'Trial' screen
  if(screen){
    updateTrial();
  }
}

void keyPressed(){ //input for steering and controlling car
  if(key == '7'){
    port.write('0');
  }else if(key == '4'){
    port.write('1');  
  }else if(key == '1'){
    port.write('2');  
  }else if(key == '9'){
    port.write('3');  
  }else if(key == '6'){
    port.write('4');  
  }else if(key == '3'){
    port.write('5');  
  }else if(key == '8'){
    port.write('6');  
  }else if(key == '5'){
    port.write('7');  
  }else if(key == '2' || key == ' '){
    port.write('8');  
  }else if(key == 'S' || key == 's'){
    port.write('a');  
  }else if(key == 'W' || key == 'w'){
    port.write('9');  
  }else if(key == 'D' || key == 'd'){
    port.write('b');  
  }else if(key == 'A' || key == 'a'){
    port.write('c');  
  }
}

void serialEvent(Serial p) //function for reading and storing data sent from IMU via Bluetooth
{
  String incoming = p.readString();
  if (printSerial) {
    println(incoming);
  }
 
  if ((incoming.length() > 8))
  {
    String[] list = split(incoming, " ");
    if ( (list.length > 0) && (list[0].equals("Orientation:")) )
    {
      roll  = float(list[3]); // Roll = Z
      pitch = float(list[2]); // Pitch = Y
      yaw   = float(list[1]); // Yaw/Heading = X
    }
    if ( (list.length > 0) && (list[0].equals("Acceleration:")) )
    {
      accel = list[1];
    }
    if ( (list.length > 0) && (list[0].equals("Alt:")) )
    {
      alt  = float(list[1]);
    }
    if ( (list.length > 0) && (list[0].equals("Temp:")) )
    {
      temp  = float(list[1]);
    }
  }
}

// Set serial port to desired value.
void setSerialPort(String portName) {
  // Close the port if it's currently open.
  if (port != null) {
    port.stop();
  }
  try {
    // Open port.
    port = new Serial(this, portName, 115200);
    port.bufferUntil('\n');
    // Persist port in configuration.
    saveStrings(serialConfigFile, new String[] { portName });
  }
  catch (RuntimeException ex) {
    // Swallow error if port can't be opened, keep port closed.
    port = null;
  }
}

//GUI code

public static void trialScreen(final JFrame frame){ //screen to run while still running the course
            screen = true;
            String Tilt = "00°";
            String Accel = "0.0 m/s/s";
            String totalDist = "0.0 m";
            start_time = System.currentTimeMillis();
            init_time = System.currentTimeMillis();
            JPanel panel = new JPanel();
            panel.setLayout(null);
            
            button = new JButton("Complete Trial"); //switch screen button
            button.setBounds(800, 400, 300, 100);
            button.setBackground(Color.WHITE);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                  frame.getContentPane().removeAll();
                  postTrialScreen(frame);
                }
                });
            
            tiltPanel = new JPanel();
            tiltPanel.setBackground(Color.GRAY);
            tiltPanel.setBounds(1275, 50, 600, 300);
            tiltPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            tilt = new JLabel("Tilt: " + Tilt);
            tilt.setBounds(1500, 50, 300, 100);
            tilt.setBackground(Color.WHITE);
            tilt.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            panel.add(tiltPanel);
            
            tPanel = new JPanel();
            tPanel.setBackground(Color.GRAY);
            tPanel.setBounds(50, 50, 600, 300);
            tPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            elapsed = new JLabel("Time Elapsed: 0:00");
            elapsed.setBounds(50, 50, 300, 100);
            elapsed.setBackground(Color.WHITE);
            elapsed.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            panel.add(tPanel);

            accelerationPanel = new JPanel();
            accelerationPanel.setBackground(Color.GRAY);
            accelerationPanel.setBounds(50, 600, 600, 300);
            accelerationPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            acceleration = new JLabel("Acceleration: " + Accel);
            acceleration.setBounds(50, 50, 300, 100);
            acceleration.setBackground(Color.WHITE);
            acceleration.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            panel.add(accelerationPanel);
            
            tdPanel = new JPanel();
            tdPanel.setBackground(Color.GRAY);
            tdPanel.setBounds(1275, 600, 600, 300);
            tdPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            totalDistance = new JLabel("Total Distance: " + totalDist);
            totalDistance.setBounds(1500, 600, 300, 100);
            totalDistance.setBackground(Color.WHITE);
            totalDistance.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            panel.add(tdPanel);
            
            panel.add(button);
            tiltPanel.add(tilt);
            tPanel.add(elapsed);
            accelerationPanel.add(acceleration);
            tdPanel.add(totalDistance);
            frame.getContentPane().add(panel);
            
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //set size of frame, change if need be
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("RC Car GUI");
            frame.setVisible(true); //make last statement
             
        }
        
        private static void updateTrial() { //function that updates trial screen based on new input data from IMU
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                  count++;
                  DecimalFormat df = new DecimalFormat("#.0");
                  if(pitch > maxTilt){
                    maxTilt = pitch;  
                  }
                  if(pitch < minTilt){
                    minTilt = pitch;  
                  }
                  end_time = System.currentTimeMillis();
                  tempAccel = Math.abs(Double.parseDouble(accel));
                  avgAccel = (avgAccel + tempAccel) / count;
                  double sec = (end_time - init_time) / 1000;
                  if((int) sec >= second){
                    tiltArray[second] = pitch;
                    accelArray[second] = tempAccel;
                    tdArray[second] = dist;
                    second++;
                  }
                  if(second%60 == 0){
                    minute = second / 60;
                    dist += (avgAccel * (second * second) / 2) / 100;
                  }
                  if(second%60 < 10){
                    elapsed.setText("Time Elapsed: " + minute + ":0" + (second%60));  
                  }else{
                    elapsed.setText("Time Elapsed: " + minute + ":" + (second%60));  
                  }
                  tilt.setText("Tilt: " + pitch + "°");
                  acceleration.setText("Acceleration: " + df.format(tempAccel) + " m/s/s");
                  totalDistance.setText("Total Distance: " + df.format(dist) + " m.");
                  start_time = System.currentTimeMillis();
                }
              });
         }
        
public static void postTrialScreen(final JFrame frame){ //display data collected during trial, second screen
            screen = false;
            String Tilt = "00°";
            String Accel = "0.0 m/s/s";
            String totalDist = "0.0 m";
            init_time = 0;
            start_time = 0;
            end_time = 0;
            dist = 0;
            DecimalFormat df = new DecimalFormat("#.0");
            JPanel panel = new JPanel();
            panel.setLayout(null);
            
            button = new JButton("Start New Trial"); //switch back and reset to 'Trial' screen
            button.setBounds(1500, 800, 300, 100);
            button.setBackground(Color.WHITE);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                  frame.getContentPane().removeAll();
                  second = 0;
                  minute = 0;
                  trialScreen(frame);
                }
             });
            
            statsPanel = new JPanel();
            statsPanel.setBackground(Color.GRAY);
            statsPanel.setBounds(600, 300, 600, 400);
            statsPanel.setLayout(new BoxLayout(statsPanel,BoxLayout.PAGE_AXIS));
            statsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            postTilt = new JLabel("Tilt: " + Tilt);
            postTilt.setBounds(1500, 100, 300, 100);
            postTilt.setBackground(Color.WHITE);
            postTilt.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            statsPanel.add(postTilt);
            
            postAcceleration = new JLabel("Acceleration: " + Accel);
            postAcceleration.setBounds(1500, 100, 300, 100);
            postAcceleration.setBackground(Color.WHITE);
            postAcceleration.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            statsPanel.add(postAcceleration);
            
            max = new JLabel("Max Tilt: " + df.format(maxTilt) + "°");
            max.setBounds(1500, 100, 300, 100);
            max.setBackground(Color.WHITE);
            max.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            statsPanel.add(max);
            
            min = new JLabel("Min Tilt: " + df.format(minTilt) + "°");
            min.setBounds(1500, 100, 300, 100);
            min.setBackground(Color.WHITE);
            min.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            statsPanel.add(min);
            
            tD = new JLabel("Total Distance: " + totalDist);
            tD.setBounds(1500, 100, 300, 100);
            tD.setBackground(Color.WHITE);
            tD.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            statsPanel.add(tD);
            
            timePanel = new JPanel();
            timePanel.setBackground(Color.GRAY);
            timePanel.setBounds(600, 50, 600, 200);
            timePanel.setLayout(new BoxLayout(timePanel,BoxLayout.PAGE_AXIS));
            timePanel.setBorder(BorderFactory.createLineBorder(Color.black));
            
            time = new JLabel("Enter Time ");
            time.setBounds(400,400,400,400);
            time.setBackground(Color.WHITE);
            time.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            timePanel.add(time);
            
            input = new JTextField("");
            input.setFont(new Font("Sans Serif", Font.PLAIN, 54));
            Action action = new AbstractAction() //updates screen when user enters time
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                  String in = input.getText();
                  String[] arr = in.split(":");
                  int min = Integer.parseInt(arr[0]);
                  int sec = Integer.parseInt(arr[1]);
                  sec += (min * 60);
                  DecimalFormat df = new DecimalFormat("#.00");
                  tD.setText("Total Distance: " + df.format(tdArray[sec]) + " m.");
                  postAcceleration.setText("Acceleration: " + df.format(accelArray[sec]) + " m/s/s");
                  postTilt.setText("Tilt: " + df.format(tiltArray[sec]) + "°");
              }
            };
            input.addActionListener(action);
            timePanel.add(input);
            
            panel.add(statsPanel);
            panel.add(timePanel);
            panel.add(button);
            frame.getContentPane().add(panel);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);//set size of frame, change if need be
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("RC Car GUI");
            frame.setVisible(true); //make last statement
    }
