#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <utility/imumaths.h>
#include <DRV8835MotorShield.h>
#include <SoftwareSerial.h>

#define LED_PIN 13
char data = 0;
int Rm = 150; //M1, right motor
int Lm = 150; //M2, left motor

DRV8835MotorShield motors;

/* Set the delay between fresh samples */
#define BNO055_SAMPLERATE_DELAY_MS (100)
#define BNO055_SAMPLERATE_PERIOD_MS 10

unsigned long tnext, tnow;
Adafruit_BNO055 bno = Adafruit_BNO055(55);

void displaySensorDetails(void)
{
  sensor_t sensor;
  bno.getSensor(&sensor);
  Serial.println("------------------------------------");
  Serial.print  ("Sensor:       "); Serial.println(sensor.name);
  Serial.print  ("Driver Ver:   "); Serial.println(sensor.version);
  Serial.print  ("Unique ID:    "); Serial.println(sensor.sensor_id);
  Serial.print  ("Max Value:    "); Serial.print(sensor.max_value); Serial.println(" xxx");
  Serial.print  ("Min Value:    "); Serial.print(sensor.min_value); Serial.println(" xxx");
  Serial.print  ("Resolution:   "); Serial.print(sensor.resolution); Serial.println(" xxx");
  Serial.println("------------------------------------");
  Serial.println("");
  delay(500);
}

void setup(void)
{
  pinMode(LED_PIN, OUTPUT);
  Serial.begin(9600);
  Serial.println("Orientation Sensor Test");
  Serial.println("");

  /* Initialise the sensor */
  if(!bno.begin())
  {
    /* There was a problem detecting the BNO055 ... check your connections */
    Serial.print("Ooops, no BNO055 detected ... Check your wiring or I2C ADDR!");
    while(1);
  }
   
  delay(1000);

  /* Use external crystal for better accuracy */
  bno.setExtCrystalUse(true);
   
  /* Display some basic information on this sensor */
  displaySensorDetails();
}

void loop(void)
{
  if(Serial.available() > 0)      
   {
      data = Serial.read();   //read steering control data sent from laptop, keyboard input from user     
      if(data == '0'){ //increase right motor
         Rm = Rm + 10;
         motors.setM1Speed(-Rm);
       }
      else if(data == '1'){ //decrease right motor
        Rm = Rm - 10;
        motors.setM1Speed(-Rm);
      }
      else if(data == '2'){ //stop right motor
        Rm = 150;
        motors.setM1Speed(0);
      }
      else if(data == '3'){ //increase left motor
        Lm = Lm + 10;
        motors.setM2Speed(Lm);
      }
      else if(data == '4'){ //decrease left motor
        Lm = Lm - 10;
        motors.setM2Speed(Lm);
      }
      else if(data == '5'){ //stop left motor
        Lm = 150;
        motors.setM2Speed(0);
      }
      else if(data == '6'){ //increase both motors
        Rm = Rm + 10;
        Lm = Lm + 10;
        motors.setM1Speed(Rm);
        motors.setM2Speed(-Lm);
      }
      else if(data == '7'){ //decrease both motors
        Rm = Rm - 10;
        Lm = Lm - 10;
        motors.setM1Speed(-Rm);
        motors.setM2Speed(Lm);
      }
      else if(data == '8'){ //stop both motors
        Rm = 150;
        Lm = 150;
        motors.setM1Speed(0);
        motors.setM2Speed(0);
      }
      else if(data == '9'){ //move backward 'S'
        Rm = 150;
        Lm = 150;
        for(int speed = 0; speed <= 500; speed++)
        {
          motors.setM1Speed(speed);
          motors.setM2Speed(-speed);
        }
        motors.setM1Speed(Rm);
        motors.setM2Speed(-Lm);
      }
      else if(data == 'a'){ //move forward 'W'
        Rm = 150;
        Lm = 150;
        for(int speed = 0; speed <= 500; speed++)
        {
          motors.setM1Speed(-speed);
          motors.setM2Speed(speed);
        }
        motors.setM1Speed(-Rm);
        motors.setM2Speed(Lm);
      }
      else if(data == 'b'){ //turn right in-place 'D'
        Rm = 150;
        Lm = 150;
        for(int speed = 0; speed <= 500; speed++)
        {
          motors.setM1Speed(speed);
          motors.setM2Speed(speed);
        }
        motors.setM1Speed(Rm);
        motors.setM2Speed(Lm);
      }
      else if(data == 'c'){ //turn left in-place 'A'
        Rm = 150;
        Lm = 150;
        for(int speed = 0; speed <= 500; speed++)
        {
          motors.setM1Speed(-speed);
          motors.setM2Speed(-speed);
        }
        motors.setM1Speed(-Rm);
        motors.setM2Speed(-Lm);
      }
   }
  /* Get a new sensor event */
  sensors_event_t event;
  bno.getEvent(&event);

  /* The processing sketch expects data as roll, pitch, heading */
  Serial.print(F("Orientation: ")); //sends orientation data
  Serial.print((float)event.orientation.x);
  Serial.print(F(" "));
  Serial.print((float)event.orientation.y);
  Serial.print(F(" "));
  Serial.print((float)event.orientation.z);
  Serial.println(F(""));
  /* Also send calibration data for each sensor. */
  uint8_t sys, gyro, accel, mag = 0;
  bno.getCalibration(&sys, &gyro, &accel, &mag);
  Serial.print(F("Calibration: ")); //sends calibration data
  Serial.print(sys, DEC);
  Serial.print(F(" "));
  Serial.print(gyro, DEC);
  Serial.print(F(" "));
  Serial.print(accel, DEC);
  Serial.print(F(" "));
  Serial.print(mag, DEC);
  Serial.println(F(""));
  imu::Vector<3> linearaccel = bno.getVector(Adafruit_BNO055::VECTOR_LINEARACCEL);
  Serial.print(F("Acceleration: ")); //sends linear acceleration data, in the Y-direction (forwards/backwards)
  Serial.println(linearaccel.y());
 
  delay(BNO055_SAMPLERATE_DELAY_MS);
}
