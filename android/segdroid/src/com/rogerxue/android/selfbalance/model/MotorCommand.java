package com.rogerxue.android.selfbalance.model;

/**
 * speed: -128  (Full Reverse)  0 (Stop)   127 (Full Forward)
 * turn: if the direction is forward
 * 		 motor speed1 = speed1 - turn
 *       motor speed2 = speed1 + turn
 *       else the direction is reverse so
 *       motor speed1 = speed1 + turn
 *       motor speed2 = speed1 - turn
 * 
 * @author rogerxue
 */
public class MotorCommand {
	public byte speed;
	public byte turn;

	public MotorCommand(byte speed, byte turn) {
		this.speed = speed;
		this.turn = turn;
	}
}
