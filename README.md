# EGEN-310 Project

This code was written entirely by me, with some additional code supplied by Arduino in the GUI.ino file, so credit to them for those few lines.

The EGEN 310 project was simply to build an RC Car and make it work. My portion was to put an Adafruit IMU on board the car, have it deliver information to a microcontroller, and from there to a Bluetooth module on the car, and send and receive information with the laptop running the programs I wrote.

The GUI.java code was made to run in the Processing v3.3.6 IDE, and its job was to simultaneously display a GUI that displays real-time measurements collected from the IMU, and send steering controls sent via key strokes by the user, also in real-time. A button could be pressed, and this would take the user to a second screen, where the measurements were collected, and the user could enter a time and see what the measurements at that point in time for the car were, along with the maximum and minimum tilt.

The GUI.ino file would run on the microcontroller on the RC car, and would gather information from the IMU and send it to the Bluetooth module, while also receiving input from the laptop through the Bluetooth module, and sending the appropriate signals to the motors in order to correctly steer the car.

All of it worked correctly and as intended.
