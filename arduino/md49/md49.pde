#include <Wire.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>


#define CMD        0x00
#define GET_VER    0x29
#define GET_ENC1   0x23
#define GET_ENC2   0x24
#define GET_VI     0x2c
#define GET_ERROR  0x2d
#define SET_ACCEL  0x33
#define SET_MODE   0x34
#define SET_SPEED1 0x31
#define SET_SPEED2 0x32

#define SPEED_TURN_COMMAND 0

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");



// helper function
inline void sendCommand(byte command, byte value) {
 Serial1.write((byte)CMD);
 Serial1.write(command);
 Serial1.write(value);
}

void setup() {
   Serial1.begin(38400);
   Serial1.print("\r\nStart");
   acc.powerOn();
   sendCommand(SET_ACCEL, 5);   // Set accelleration to 5
   sendCommand(SET_MODE, 3);
}

void loop() {
  byte msg[3];
  if (acc.isConnected()) {
    int len = acc.read(msg, sizeof(msg), 1);
    if (len > 0) {
      if (msg[0] == SPEED_TURN_COMMAND) {
        sendCommand(SET_SPEED1, msg[1]);
        sendCommand(SET_SPEED2, msg[2]);
      }
    }
  } else {
    sendCommand(SET_SPEED1, 30);
    sendCommand(SET_SPEED2, 20); //turn value
  }

}
