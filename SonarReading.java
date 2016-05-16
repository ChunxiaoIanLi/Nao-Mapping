package com.aldebaran.nao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSonar;
import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALMemory;

public class SonarReading {

	private static Application application;
	private static ALSonar sonar;
	private static ALMemory alMemory;

	public static void main(String[] args) {
		
		application = new Application(args, "tcp://192.168.1.135:9559");

		try {
			application.start();
			System.out.println("\nconnected to Nao");

			sonar = new ALSonar(application.session());
			alMemory = new ALMemory(application.session());

			sonar.subscribe("mapping");
			while(true){
				float leftDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Left/Sensor/Value");
				float rightDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Right/Sensor/Value");
				System.out.println("left " + leftDist);
				System.out.println("right " + rightDist);
				Thread.sleep(1000);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}