package com.aldebaran.nao;

import java.util.*;

import static java.util.Arrays.copyOfRange;

class Node implements Comparable<Node> {
	public final String name;
	public Edge[] adjacencies;
	public double minDistance = Double.POSITIVE_INFINITY;
	public Node previous;

	public Node(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
	
	public int toInt(){
		return Integer.parseInt(this.name.substring(1, this.name.length()));
		
	}

	public int compareTo(Node other) {
		return Double.compare(minDistance, other.minDistance);
	}
}

class Edge {
	public final Node target;
	public final double weight;

	public Edge(Node target, double weight) {
		this.target = target;
		this.weight = weight;
	}

	public static Edge[] addEdge(String system[], Node[] nodes) {
		Edge e[] = new Edge[nodes.length];
		int counter = 0;
		for (int i = 0; i < e.length; i++) {
			if (Integer.parseInt(system[i]) > 0) {
				e[i] = new Edge(nodes[i], Integer.parseInt(system[i]));
				counter++;
			}
		}
		Edge s[] = new Edge[counter];
		int c = 0;
		for (int i = 0; i < e.length; i++) {
			if (e[i] != null) {
				s[c] = e[i];
				c++;
			}
		}
		return s;
	}
}

public class Dijkstra {
	private List<List<Integer>> roommap;
	private List<List<Integer>> adjMatrix;
	private int naoX = 0;
	private int naoY = 0;
	private int width = 0;
	private int height = 0;

	public Dijkstra(List<List<Integer>> roommap, int x, int y) {
		this.roommap = roommap;
		this.naoX = x;
		this.naoY = y;
		this.width = roommap.get(0).size();
		this.height = roommap.size();
		this.adjMatrix = new ArrayList<List<Integer>>();
		// 1 1 1 1
		// 0 0 0 0

		fill(roommap.size()*roommap.get(0).size());
		convert(roommap);
	}

	public String toString() {
		String map = "";
		for (List<Integer> col : adjMatrix) {
			for (int row : col) {
				map += row + " ";
			}
			map += "\n";
		}
		return map;
	}

	private void fill(int n) {

		for (int i = 0; i < n * n; i++) {
			ArrayList<Integer> row = new ArrayList<Integer>();
			for (int j = 0; j < n * n; j++) {
				row.add(0);
			}
			this.adjMatrix.add(row);
		}
	}


	private void convert(List<List<Integer>> roommap) {
		for (int i = 0; i < this.roommap.size(); i++) {
			for (int j = 0; j < this.roommap.get(i).size(); j++) {
				if (i == 0) {
					if (this.roommap.get(i + 1).get(j) == 0
							|| this.roommap.get(i + 1).get(j) == -1) {
						adjMatrix.get(i * this.width + j)
								.set((i + 1) * this.width + j, 1);
					}
					if (j != 0) {
						if (this.roommap.get(i).get(j - 1) == 0
								|| this.roommap.get(i).get(j - 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j - 1,
									1);
						}
					}
					if (j != this.width - 1) {
						if (this.roommap.get(i).get(j + 1) == 0
								|| this.roommap.get(i).get(j + 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j + 1,
									1);
						}
					}
				} else if (i == this.height - 1) {
					if (this.roommap.get(i - 1).get(j) == 0
							|| this.roommap.get(i - 1).get(j) == -1) {
						adjMatrix.get(i * this.width + j)
								.set((i - 1) * this.width + j, 1);
					}
					if (j != 0) {
						if (this.roommap.get(i).get(j - 1) == 0
								|| this.roommap.get(i).get(j - 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j - 1,
									1);
						}
					}
					if (j != this.width - 1) {
						if (this.roommap.get(i).get(j + 1) == 0
								|| this.roommap.get(i).get(j + 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j + 1,
									1);
						}
					}
				} else {
					if (this.roommap.get(i - 1).get(j) == 0
							|| this.roommap.get(i - 1).get(j) == -1) {
						adjMatrix.get(i * this.width + j)
								.set((i - 1) * this.width + j, 1);
					}
					if (this.roommap.get(i + 1).get(j) == 0
							|| this.roommap.get(i + 1).get(j) == -1) {
						adjMatrix.get(i * this.width + j)
								.set((i + 1) * this.width + j, 1);
					}
					if (j != 0) {
						if (this.roommap.get(i).get(j - 1) == 0
								|| this.roommap.get(i).get(j - 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j - 1,
									1);
						}
					}
					if (j != this.width - 1) {
						if (this.roommap.get(i).get(j + 1) == 0
								|| this.roommap.get(i).get(j + 1) == -1) {
							adjMatrix.get(i * this.width + j).set(i * this.width + j + 1,
									1);
						}
					}
				}

			}
		}

	}

	public static void computePaths(Node source) {
		source.minDistance = 0.;
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		queue.add(source);

		while (!queue.isEmpty()) {
			Node u = queue.poll();

			// traverse each edge
			for (Edge e : u.adjacencies) {
				Node v = e.target;
				double weight = e.weight;
				double vToU = u.minDistance + weight;
				if (vToU < v.minDistance) {
					queue.remove(v);
					v.minDistance = vToU;
					v.previous = u;
					queue.add(v);
				}
			}
		}
	}
	
	public static List<Node> getLeastPath(Node target) {
		List<Node> path = new ArrayList<Node>();
		for (Node vertex = target; vertex != null; vertex = vertex.previous)
			path.add(vertex);
		Collections.reverse(path);
		return path;
	}
	
	public static ArrayList<Integer> parse(List<Node> path) {
		ArrayList<Integer> intPath = new ArrayList<Integer>();
		for (Node v : path) {
			String s = v.toString();
			intPath.add(Integer.parseInt(s.substring(1, s.length())));
		}
		return intPath;
	}

	public Node[] run() {
		int numRouters = this.adjMatrix.size();

		String system[][] = new String[numRouters][numRouters]; // the whole
																// thing
		String matrix[] = new String[numRouters]; // each router
		Node nodes[] = new Node[numRouters];

		for (int i = 0; i < numRouters; i++) {
			for (int j = 0; j < numRouters; j++) {

				system[i][j] = this.adjMatrix.get(i).get(j).toString();
			}

		}

		for (int g = 0; g < numRouters; g++) {
			nodes[g] = new Node("v" + Integer.toString(g));
		}

		for (int h = 0; h < numRouters; h++) {
			nodes[h].adjacencies = Edge.addEdge(system[h], nodes);
		}

		System.out.println();
		System.out.println();
		computePaths(nodes[this.naoX * this.width + this.naoY]);
		// System.out.println("Source node "
		// + nodes[this.naoX * this.width + this.naoY]);
		// for (Node v : nodes) {
		// System.out.println("Min distance to " + v + ": " + v.minDistance);
		// List<Node> path = getLeastPath(v);
		// System.out.println("Least Path: " + path);
		// }

		return nodes;

	}
}