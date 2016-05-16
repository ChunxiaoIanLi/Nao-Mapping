package com.aldebaran.nao;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALAutonomousLife;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSonar;
import com.aldebaran.qi.helper.proxies.ALRobotPosture;

public class MoveTo {
	private static Application application;
	private static ALAutonomousLife autolife; 
	private static ALMotion motion;
	
	public static void main(String[] args) {
		application = new Application(args, "tcp://192.168.1.135:9559");
		try {
			application.start();
			System.out.println("\nconnected to Nao");
			
			autolife = new ALAutonomousLife(application.session());
			motion = new ALMotion(application.session());
			ALRobotPosture pos = new ALRobotPosture(application.session());
			
			/*
			List<String> leg = new ArrayList<String>();
			leg.add("LLeg");
			Float[][] footstep = new Float[1][3];
			footstep[0][0] =0.4f;
			footstep[0][1] =0.0f;
			footstep[0][2] =0.0f;
			List<Float> timeList = new ArrayList<Float>();
			timeList.add(0.6f);
			motion.setFootSteps(leg, footstep, timeList, false);
			*/
			//pos.goToPosture("Stand", 0.5f);
			
			final float LEFT_COEFFICIENT = -0.400f;
			final float RIGHT_COEFFICIENT = 0.200f;
			final float LEFT_TURN = 1.5709f;
			final float RIGHT_TURN = -1.5709f;
			
			motion.moveTo(0.0f, 0.0f, RIGHT_TURN+RIGHT_COEFFICIENT);
			motion.moveTo(0.0f, 0.0f, LEFT_TURN+LEFT_COEFFICIENT);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
