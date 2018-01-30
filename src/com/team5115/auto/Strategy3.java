package com.team5115.auto;

import com.cruzsbrian.robolog.Log;
import com.team5115.Constantos;
import com.team5115.PID;
import com.team5115.robot.Robot;
import com.team5115.auto.AutoDrive;
import com.team5115.systems.DriveTrain;
import com.team5115.statemachines.StateMachineBase;

//cross auto line
public class Strategy3 extends StateMachineBase {
	public static final int INIT = 0;
	public static final int DRIVE = 1;	//12 ft
	public static final int FINISHED = 2;

	AutoDrive drive;
	
	public Strategy3() {
		drive = new AutoDrive();
		
	}
	public void update() {
		switch(state){
		case INIT:
			drive.startLine(11.5, .25);
			setState(DRIVE);
			break;
		case DRIVE:
			drive.update();
			if(drive.state == AutoDrive.FINISHED){
        		setState(FINISHED);
        	}
			break;
		case FINISHED:
        	break;
		}
	}
}