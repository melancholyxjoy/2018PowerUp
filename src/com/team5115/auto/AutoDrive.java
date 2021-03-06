package com.team5115.auto;

import com.team5115.Konstanten;
import com.team5115.PID;
import com.team5115.robot.Robot;
import com.team5115.statemachines.StateMachineBase;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoDrive extends StateMachineBase {

	public static final int DRIVING = 1;
	public static final int FINISHED = 2;
	
	boolean line;

	double targetDist;
	double targetAngle;
	
	double time;

	PID forwardController;
	PID turnController;

	public void startLine(double dist, double maxSpeed) {
		line = true;
		//Takes some time to reset gyros + encoders
		Timer.delay(0.1);
		targetDist = Robot.drivetrain.distanceTraveled() + dist;
		targetAngle = Robot.drivetrain.getYaw();
		//System.out.println("Target " + targetAngle);

		
		//Change back to our constants, this one doesn't work
		forwardController = new PID(Konstanten.AUTO_FORWARD_KP, Konstanten.AUTO_FORWARD_KI, Konstanten.AUTO_FORWARD_KD ,maxSpeed);
		turnController = new PID(Konstanten.AUTO_TURN_KP, Konstanten.AUTO_TURN_KI ,Konstanten.AUTO_TURN_KD);
		time = Timer.getFPGATimestamp();
		setState(DRIVING);
	}

	public void startTurn(double angle, double maxSpeed) {
		line = false;
		targetDist = Robot.drivetrain.distanceTraveled();
		targetAngle = Robot.drivetrain.getYaw() + (angle);

		forwardController = new PID(Konstanten.AUTO_FORWARD_KP, Konstanten.AUTO_FORWARD_KI, Konstanten.AUTO_FORWARD_KD);
		turnController = new PID(Konstanten.TURN_KP, Konstanten.TURN_KI ,Konstanten.AUTO_TURN_KD, maxSpeed);
		
		setState(DRIVING);
	}
	
	public void startArc(double radius, double angle, double maxSpeed) {
		double distance = (angle * Math.PI / 180) * radius;
		//double time = angle / maxSpeed;
		double maxForwardSpeed = Konstanten.ARC_RATIO * maxSpeed;
		targetDist = Robot.drivetrain.distanceTraveled() + distance;
		targetAngle = Robot.drivetrain.getYaw() + (angle);

		forwardController = new PID(Konstanten.AUTO_FORWARD_KP, Konstanten.AUTO_FORWARD_KI, Konstanten.AUTO_FORWARD_KD, maxForwardSpeed);
		turnController = new PID(Konstanten.AUTO_TURN_KP, Konstanten.AUTO_TURN_KI ,Konstanten.AUTO_TURN_KD, maxSpeed);
		
		setState(DRIVING);
	}

	public void update() {
		SmartDashboard.putNumber("autodrive state: ", state);
		System.out.println("autodrive target: " + targetDist);
		switch (state) {
			case DRIVING:
				Robot.drivetrain.inuse = true;

				// run every Constants.getAsDouble()DELAY seconds while driving
				double vForward = forwardController.getPID(targetDist, Robot.drivetrain.distanceTraveled(), Robot.drivetrain.averageSpeed());

				double clearYaw = clearSteer(Robot.drivetrain.getYaw(), targetAngle);
				double vTurn = turnController.getPID(targetAngle, clearYaw, Robot.drivetrain.getTurnVelocity());
				
				if (!line && Math.abs(turnController.getError()) > 4 * Konstanten.TURN_TOLERANCE) {
					vTurn += 0.15 * Math.signum(vTurn);
				}

				Robot.drivetrain.drive(vForward, vTurn);
				//System.out.println("distance travelled:  " + Robot.drivetrain.distanceTraveled());
//				System.out.println("Yaw: " + Robot.drivetrain.getYaw());
//				System.out.println("Clear Yaw " + clearYaw);
//				System.out.println("Target" + targetAngle);

				SmartDashboard.putNumber("distance traveled left", Robot.drivetrain.leftDist());
				SmartDashboard.putNumber("distance traveled right", Robot.drivetrain.rightDist());
				SmartDashboard.putNumber("velocity", Robot.drivetrain.averageSpeed());

				SmartDashboard.putNumber("vTurn", vTurn);
				SmartDashboard.putNumber("vForward", vForward);
				
				if (forwardController.isFinished(Konstanten.FORWARD_TOLERANCE, Konstanten.FORWARD_DTOLERANCE) && turnController.isFinished(Konstanten.TURN_TOLERANCE, Konstanten.TURN_DTOLERANCE)) {
					Robot.drivetrain.drive(0, 0);
					setState(FINISHED);
				}
				
				double delay = Timer.getFPGATimestamp() - time;
				SmartDashboard.putNumber("delay", delay);
				time = Timer.getFPGATimestamp();

				break;
			case FINISHED:
				Robot.drivetrain.inuse = false;
				Robot.drivetrain.drive(0, 0);
				
				forwardController = null;
				turnController = null;
				break;
		}
	}

	private double clearSteer(double yaw, double target) {
		if (Math.abs(target - yaw) > 180) {
			if (target < 180) {
				yaw -= 360;
			} else {
				yaw += 360;
			}
		}

		return yaw;
	}

}
