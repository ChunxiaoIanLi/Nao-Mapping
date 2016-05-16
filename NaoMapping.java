package com.aldebaran.nao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALSonar;
import com.aldebaran.qi.Application;
import com.aldebaran.qi.helper.proxies.ALMemory;

public class NaoMapping {

	private static Application application;
	private static ALSonar sonar;
	private static ALMemory alMemory;
	private static ALMotion motion;

	private static final int UP = 0;
	private static final int RIGHT = 1;
	private static final int DOWN = 2;
	private static final int LEFT = 3;
	public int FACING = UP;
	public static final float LEFT_COEFFICIENT = -0.400f;
	public static final float RIGHT_COEFFICIENT = 0.200f;
	public static final float WALKING_COEFFICIENT = -0.080f;
	public static final float LEFT_TURN = 1.5709f;
	public static final float RIGHT_TURN = -1.5709f;
	public int naoX = 0;
	public int naoY = 0;
	public int naoStartingX = 0;
	public int naoStartingY = 0;
	private final float THRESHOLD = 0.5f;
	private final float CONFIDENT = 0.3f;
	private final float MINDIST = 0.26f;
	private final float TOL = 0.01f;

	// initialize the map
	private List<List<Integer>> roommap = new ArrayList<List<Integer>>();

	public String printMap() {
		String result = "";
		for (int i = 0; i < this.roommap.size(); i++) {
			for (int j = 0; j < this.roommap.get(i).size(); j++) {
				if (i == this.naoX && j == this.naoY) {
					result += "N";
				} else {
					if (this.roommap.get(i).get(j) == -1) {
						result += "-";
					} else {
						result += this.roommap.get(i).get(j);
					}
				}
				result += " ";
			}
			// System.out.println();
			result += "\n";
		}
		return result;
	}

	public void naoMove(float x, float y, float d) throws CallError,
			InterruptedException {
		if (x == 0.3f) {
			if (this.FACING == UP) {
				this.naoX -= 1;
			} else if (this.FACING == RIGHT) {
				this.naoY += 1;
			} else if (this.FACING == DOWN) {
				this.naoX += 1;
			} else {
				this.naoY -= 1;
			}
		}
		if (d == LEFT_TURN) {
			this.FACING = (this.FACING + 4 - 1) % 4;
			d = d + this.LEFT_COEFFICIENT;
		}
		if (d == RIGHT_TURN) {
			this.FACING = (this.FACING + 4 + 1) % 4;
			d = d + this.RIGHT_COEFFICIENT;
		}
		try {
			// add turning coefficient to walking
			d = d + this.WALKING_COEFFICIENT;
			NaoMapping.motion.moveTo(x, y, d);
		} catch (CallError | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.extendMap();
		this.printStatus();
	}

	public void printStatus() throws CallError, InterruptedException {
		System.out.println("facing: " + this.FACING);
		System.out.println("x: " + this.naoX);
		System.out.println("y: " + this.naoY);
		System.out.println("starting x: " + this.naoStartingX);
		System.out.println("starting y: " + this.naoStartingY);
		float leftDist = (float) alMemory
				.getData("Device/SubDeviceList/US/Left/Sensor/Value");
		float rightDist = (float) alMemory
				.getData("Device/SubDeviceList/US/Right/Sensor/Value");
		System.out.println("left " + leftDist);
		System.out.println("right " + rightDist);
		System.out.println(this.printMap());
	}

	public void drawToMap(int val) {

		switch (this.FACING) {
		case UP:
			this.roommap.get(this.naoX - 1).set(this.naoY, val);
			break;
		case RIGHT:
			this.roommap.get(this.naoX).set(this.naoY + 1, val);
			break;
		case DOWN:
			this.roommap.get(this.naoX + 1).set(this.naoY, val);
			break;
		case LEFT:
			this.roommap.get(this.naoX).set(this.naoY - 1, val);
		}
		System.out.println(this.printMap());
	}

	public void extendMap() {
		// ------up
		// left [x,y] right
		// -----down
		switch (this.FACING) {
		case UP:
			// add new row at index 0
			if (this.naoX == 0) {
				this.roommap.add(
						0,
						new ArrayList<Integer>(Collections.nCopies(this.roommap
								.get(0).size(), -1)));
				this.naoX += 1;
				this.naoStartingX += 1;
			}
			break;
		case RIGHT:
			// add column at the right end
			if (this.naoY == this.roommap.get(0).size() - 1) {
				int i = 0;
				int j = this.roommap.size();
				while (i < j) {
					this.roommap.get(i).add(-1);
					i++;
				}
			}
			break;
		case DOWN:
			// add row to the bottom
			if (this.naoX == this.roommap.size() - 1) {
				this.roommap.add(new ArrayList<Integer>(Collections.nCopies(
						this.roommap.get(0).size(), -1)));
			}
			break;
		case LEFT:
			// add column to the left end
			if (this.naoY == 0) {
				int i = 0;
				int j = this.roommap.size();
				while (i < j) {
					this.roommap.get(i).add(0, -1);
					i++;
				}
				this.naoY++;
				this.naoStartingY += 1;
			}
			break;
		}
	}

	public void adjustNao(float leftDist, float rightDist) throws CallError,
			InterruptedException {

		while (Math.abs(leftDist - rightDist) > 1.5 * this.TOL
				|| Math.abs(leftDist - this.CONFIDENT) > this.TOL
				) {

			// if nao is not FACING the surface perpendicularly make it turn
			while (Math.abs(leftDist - rightDist) > 1.5 * this.TOL) {
				// if left is closer turn left
				if (leftDist < rightDist) {
					float d = 0.1f + 0.08f;
					this.naoMove(0.0f, 0.0f, d);
				}
				// if right is closer turn right
				else {
					float d = -0.1f + 0.08f;
					this.naoMove(0.0f, 0.0f, d);
				}
				leftDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Left/Sensor/Value");
				rightDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Right/Sensor/Value");
				System.out.println("left " + leftDist);
				System.out.println("right " + rightDist);
			}

			// adjust nao in x axis
			while (Math.abs(leftDist - this.CONFIDENT) > this.TOL) {
				// move back if too close
				if (leftDist < this.CONFIDENT ) {
					// big step
					if (Math.abs(leftDist - this.CONFIDENT) > 0.05f) {
						this.naoMove(-0.03f, 0.0f, 0.0f);
					}
					// small step
					else {
						this.naoMove(-0.01f, 0.0f, 0.0f);
					}

				}
				// move forward if too far
				else {
					if (Math.abs(leftDist - this.CONFIDENT) > 0.05f) {
						this.naoMove(0.03f, 0.0f, 0.0f);
					}
					// small step
					else {
						this.naoMove(0.01f, 0.0f, 0.0f);
					}
				}
				leftDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Left/Sensor/Value");
				rightDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Right/Sensor/Value");
			}
		}
		return;
	}

	public void moveNaoFromLeastPath(ArrayList<Integer> path) throws CallError,
			InterruptedException {
		for (int i = 0; i < path.size() - 1; i++) {
			int difference = path.get(i) - path.get(i + 1);
			if (difference == -1) { // move right
				System.out.print("R");
				if (this.FACING == 0) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				} else if (this.FACING == 2) {
					this.naoMove(0.0f, 0.0f, LEFT_TURN);
				} else if (this.FACING == 3) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				}

			}
			if (difference == 1) { // move left
				System.out.print("L");
				if (this.FACING == 0) {
					this.naoMove(0.0f, 0.0f, LEFT_TURN);
				} else if (this.FACING == 1) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				} else if (this.FACING == 2) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				}
			}
			if (difference < -1) { // move down
				System.out.print("D");
				if (this.FACING == 0) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				} else if (this.FACING == 1) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				} else if (this.FACING == 3) {
					this.naoMove(0.0f, 0.0f, LEFT_TURN);
				}
			}
			if (difference > 1) { // move up
				System.out.print("U");
				if (this.FACING == 1) {
					this.naoMove(0.0f, 0.0f, LEFT_TURN);
				} else if (this.FACING == 2) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				} else if (this.FACING == 3) {
					this.naoMove(0.0f, 0.0f, RIGHT_TURN);
				}
			}
			if (i != path.size() - 2) {
				this.naoMove(0.3f, 0.0f, 0.0f);
			} else {
				float leftDist;
				float rightDist;
				leftDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Left/Sensor/Value");
				rightDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Right/Sensor/Value");
				System.out.println("left " + leftDist);
				System.out.println("right " + rightDist);

				if (leftDist < this.CONFIDENT || rightDist < this.CONFIDENT) {
					// obstacal detected
					if (this.FACING == 0) {
						this.roommap.get(this.naoX - 1).set(this.naoY, 1);
					} else if (this.FACING == 1) {
						this.roommap.get(this.naoX).set(this.naoY + 1, 1);
					} else if (this.FACING == 2) {
						this.roommap.get(this.naoX + 1).set(this.naoY, 1);
					} else if (this.FACING == 3) {
						this.roommap.get(this.naoX).set(this.naoY - 1, 1);
					}
				}
				else{
					if (this.FACING == 0) {
						this.roommap.get(this.naoX - 1).set(this.naoY, 0);
					} else if (this.FACING == 1) {
						this.roommap.get(this.naoX).set(this.naoY + 1, 0);
					} else if (this.FACING == 2) {
						this.roommap.get(this.naoX + 1).set(this.naoY, 0);
					} else if (this.FACING == 3) {
						this.roommap.get(this.naoX).set(this.naoY - 1, 0);
					}
				}
			}
		}
		System.out.println("");
	}

	public static void main(String[] args) {
		boolean finished = false;
		boolean justturned = false;
		float leftDist;
		float rightDist;
		NaoMapping naoMapping = new NaoMapping();

		String s;
		Scanner in = new Scanner(System.in);
		System.out.println("Load Map?(Y/N) ");
		s = in.nextLine();
		if (s.equals("Y")) {
			System.out.println("external?(Y/N) ");
			s = in.nextLine();
			if (s.equals("Y")) {
				System.out.println("Nao starting x: ");
				s = in.nextLine();
				naoMapping.naoStartingX = Integer.parseInt(s);
				System.out.println("Nao starting y: ");
				s = in.nextLine();
				naoMapping.naoStartingY = Integer.parseInt(s);
			} else {
				finished = true;
			}
			System.out.println("Nao facing direction: ");
			s = in.nextLine();
			naoMapping.FACING = Integer.parseInt(s);
			System.out.println("Map: ");
			int rowNum = 0;
			while (!(s = in.nextLine()).isEmpty()) {
				String row[];
				row = s.split(" ");
				naoMapping.roommap.add(new ArrayList<Integer>());
				for (int i = 0; i < row.length; i++) {
					if (row[i].equals("-") || row[i].equals("N")) {
						naoMapping.roommap.get(rowNum).add(-1);
					} else {
						naoMapping.roommap.get(rowNum).add(
								Integer.parseInt(row[i]));
					}
					if (row[i].equals("N")) {
						naoMapping.naoX = rowNum;
						naoMapping.naoY = i;
						naoMapping.roommap.get(rowNum).set(i, 0);
					}
				}
				rowNum++;
			}
		}

		else {
			// put nao at [0,0]
			naoMapping.roommap.add(new ArrayList<Integer>());
			naoMapping.roommap.get(0).add(0);
			naoMapping.extendMap();
		}
		application = new Application(args, "tcp://192.168.1.135:9559");

		try {

			application.start();
			System.out.println("\nconnected to Nao");
			sonar = new ALSonar(application.session());
			alMemory = new ALMemory(application.session());
			motion = new ALMotion(application.session());

			sonar.subscribe("mapping");

			// main loop
			boolean initial = true;
			while (!finished) {
				// get distance from both sonars for each iteration of distance
				// check
				leftDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Left/Sensor/Value");
				rightDist = (float) alMemory
						.getData("Device/SubDeviceList/US/Right/Sensor/Value");
				System.out.println("left " + leftDist);
				System.out.println("right " + rightDist);
				/*
				 * obstacle detected
				 */

				// if both readings are within the threshold, then we know
				// there is an obstacle in front
				if (leftDist < naoMapping.THRESHOLD
						&& rightDist < naoMapping.THRESHOLD) {
					// if nao is TOO close to the wall, take a step back
					// adjust nao to confident distance
					naoMapping.adjustNao(leftDist, rightDist);
					// draw obstacle to map
					naoMapping.drawToMap(1);
					// continue traveling along the surface
					naoMapping.naoMove(0.0f, 0.0f, RIGHT_TURN);
					justturned = false;
				}
				// walk straight otherwise, but need to check if
				else {
					// test for exterior corner 
					// THIS NEEDS MORE WORK
					if (justturned == true) {
						
						// exterior corner detected, turn right and walk a
						// little to ensure safety
						
							naoMapping.naoMove(0.0f, 0.0f, RIGHT_TURN);
							naoMapping.naoMove(0.15f, 0.0f, 0.0f);
							naoMapping.naoMove(0.0f, 0.0f, LEFT_TURN);
					}
					naoMapping.drawToMap(0);
					naoMapping.naoMove(0.3f, 0.0f, 0.0f);
					naoMapping.naoMove(0.0f, 0.0f, LEFT_TURN);
					justturned = true;
				}
				if (naoMapping.naoX == naoMapping.naoStartingX
						&& naoMapping.naoY == naoMapping.naoStartingY
						&& !initial) {
					finished = true;
					break;
				}

				initial = false;
				Thread.sleep(1000);

			}

			// start internal mapping
			// choosing vertex to visit by computing distance with dijkstra

			// get unvisited vertices

			// for (int i = 0; i < 5; i++) {
			// ArrayList<Integer> row = new ArrayList<Integer>();
			// for (int j = 0; j < 10; j++) {
			// row.add(1);
			// }
			// naoMapping.roommap.add(row);
			// }
			// naoMapping.roommap.get(0).set(0, -1);
			// for (int i = 2; i < 9; i++) {
			// naoMapping.roommap.get(1).set(i, 0);
			// }
			// for (int i = 1; i < 9; i++) {
			// naoMapping.roommap.get(2).set(i, 0);
			// }
			// naoMapping.roommap.get(2).set(4, -1);
			// naoMapping.roommap.get(2).set(5, -1);
			// for (int i = 3; i < 7; i++) {
			// naoMapping.roommap.get(3).set(i, 0);
			// }
			// naoMapping.roommap.get(4).set(0, -1);
			// naoMapping.roommap.get(4).set(1, -1);
			// naoMapping.roommap.get(4).set(8, -1);
			// naoMapping.roommap.get(4).set(9, -1);
			// naoMapping.naoX = 2;
			// naoMapping.naoY = 1;
			int roomWidth = naoMapping.roommap.get(0).size();
			int roomHeight = naoMapping.roommap.size();
			ArrayList<Integer> unvisited = new ArrayList<Integer>();
			for (int i = 0; i < naoMapping.roommap.size(); i++) {
				for (int j = 0; j < naoMapping.roommap.get(i).size(); j++) {
					if (naoMapping.roommap.get(i).get(j) == -1) {
						unvisited.add(i * roomWidth + j);
					}
				}
			}

			while (unvisited.size() > 0) {
				System.out.println(naoMapping.printMap());
				/* call dijkstra */
				try {
					Dijkstra d = new Dijkstra(naoMapping.roommap,
							naoMapping.naoX, naoMapping.naoY);
					// System.out.println("Dijkstra adjacency matrix:");
					// System.out.println(d);
					Node temp[] = d.run();
					ArrayList<Node> nodes = new ArrayList<Node>(
							Arrays.asList(temp));
					// change unvisited to 1 if it's not reachable
					int nearestV = unvisited.get(0);
					Iterator<Integer> itr = unvisited.iterator();
					while (itr.hasNext()) {
						int i = itr.next();
						if (nodes.get(i).minDistance == Double.POSITIVE_INFINITY) {
							naoMapping.roommap.get(i / roomWidth).set(
									i % roomWidth, 1);
							itr.remove();
						} else if (nodes.get(i).minDistance < nodes
								.get(nearestV).minDistance) {
							//if less node in the direction
							nearestV = i;
						}
					}

					// if unvisited is still not empty
					if (unvisited.size() > 0) {
						List<Node> path = Dijkstra.getLeastPath(nodes
								.get(nearestV));
						System.out.println("Min distance to "
								+ nodes.get(nearestV) + ": "
								+ nodes.get(nearestV).minDistance);
						System.out.println("Least Path: " + path);
						unvisited.remove(new Integer(nearestV));

						ArrayList<Integer> intPath = Dijkstra.parse(path);
						System.out.println(intPath);
						naoMapping.moveNaoFromLeastPath((intPath));

						// System.out.println("test down left up:");
						// ArrayList<Integer> j = new ArrayList<Integer>();
						// j.add(21);
						// j.add(31);
						// j.add(30);
						// j.add(20);
						// System.out.println(j);
						// NaoMapping.moveNaoFromLeastPath(j);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println(naoMapping.printMap());
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
