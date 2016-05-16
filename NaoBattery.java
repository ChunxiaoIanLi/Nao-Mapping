package com.aldebaran.nao;


import com.aldebaran.qi.Application;

import com.aldebaran.qi.helper.proxies.ALBattery;
import com.aldebaran.qi.helper.proxies.ALMemory;

public class NaoBattery {
	private static Application application;
	private static ALBattery battery;
	private static ALMemory mmr;

	public static void main(String[] args) {
		application = new Application(args, "tcp://192.168.1.135:9559");
		try {
			application.start();
			System.out.println("\nconnected to Nao");

			battery = new ALBattery(application.session());
			mmr = new ALMemory(application.session());

			int batterylife = battery.getBatteryCharge();
			System.out.println(batterylife);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
