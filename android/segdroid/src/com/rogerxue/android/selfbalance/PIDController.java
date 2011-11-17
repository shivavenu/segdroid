package com.rogerxue.android.selfbalance;

import com.rogerxue.android.selfbalance.model.MotorCommand;

/**
 * PID control loop calculator.
 *
 * @author rogerxue
 */
public class PIDController {
	private MotorCommand command;
	private double paramP = 0;
	private double paramI = 0;
	private double paramD = 0;

	public PIDController(double p, double i, double d, MotorCommand command) {
		this.command = command;
		paramP = p;
		paramI = i;
		paramD = d;
	}

	// TODO: calculate speed only for now, add turn support later.
	public void calculateMotorCommand(double error, double errorInte, double errorDri) {
		double loopFeedback = paramP * error + paramI * errorInte + paramD * errorDri;
		byte speed = regulate(loopFeedback);
		command.speed = speed;
		command.turn = (byte)0;
	}
	
	private byte regulate(double input) {
		if (input >= 127) {
			return (byte) 127;
		} else if (input < -127) {
			return (byte) -127;
		} else {
			return (byte) input;
		}
	}
	
	public void setParamP(double paramP) {
		this.paramP = paramP;
	}
	
	public void setParamI(double paramI) {
		this.paramI = paramI;
	}

	public void setParamD(double paramD) {
		this.paramD = paramD;
	}
	
	public double getParamP() {
		return paramP;
	}
	
	public double getParamI() {
		return paramI;
	}
	
	public double getParamD() {
		return paramD;
	}
}
