package com.aldebaran.nao;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALAutonomousLife;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSonar;
import com.aldebaran.qi.helper.proxies.ALRobotPosture;

public class AutolifeOff {
	private static Application application;
	private static ALAutonomousLife autolife;
	private static ALMotion motion;
	private static ALRobotPosture pose;

	public static void main(String[] args) {
		application = new Application(args, "tcp://192.168.1.135:9559");
		try {
			application.start();
			System.out.println("\nconnected to Nao");
			
			autolife = new ALAutonomousLife(application.session());
			motion = new ALMotion(application.session());
			pose=new ALRobotPosture(application.session());
			
			//autolife.setState("solitary");
			autolife.setState("disabled");
			motion.wakeUp();
			pose.goToPosture("StandInit", (float) 0.5);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
