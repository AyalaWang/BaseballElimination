import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BaseballElimination {
	private int numberOfTeams; 
	//private List<String> teams;
	private int[][] games;
 	private int[] wins;
    	private int[] losses;
    	private int[] remaining;
	private Map<String, Integer> teams; 

	public BaseballElimination(String filename) { 
		// create a baseball division from given filename in format specified below
		try {
			teams = new HashMap<>();	
			Scanner scanner = new Scanner(new File(filename));
			numberOfTeams = scanner.nextInt(); 
			int i = 0;
			while (scanner.hasNextLine()) { 
				String line = scanner.nextLine(); 
				String[] parts = line.split("\\s+");		
				String teamName = parts[0];
				teams.put(teamName, i);
				wins[i] = Integer.parseInt(parts[1]); 
				losses[i] = Integer.parseInt(parts[2]);
				remaining[i] = Integer.parseInt(parts[3]);
				for (int j = 0; j < numberOfTeams; j++) {
                    			games[i][j] = Integer.parseInt(parts[4 + j]);
                		}
				i++;	
			}
			scanner.close();
		}catch (FileNotFoundException e) {
            		e.printStackTrace();
        	}
	}
	public int numberOfTeams() {
        	// number of teams
		return numberOfTeams; 
	}
	public Iterable<String> teams() {
                             // all teams
		return teams.keySet(); 
	}

	public int wins(String team) {
                     // number of wins for given team
		return wins[teams.get(team)];
	}
	public int losses(String team) {
                  // number of losses for given team
                return losses[teams.get(team)];
	}
	public int remaining(String team) {
                // number of remaining games for given team
 		return remaining[teams.get(team)];
	}
	public int against(String team1, String team2) {
    		// number of remaining games between team1 and team2
		return games[teams.get(team1)][teams.get(team2)];
	}
	public boolean isEliminated(String team) {
             // is given team eliminated?
		int x = teams.get(team);

        	// Trivial elimination
        	int maxWins = wins[x] + remaining[x];
        	for (int i = 0; i < numberOfTeams; i++) {
            		if (wins[i] > maxWins) {
                		return true;
            		}
        	}	
		// Non-trivial elimination
        	FlowNetwork network = createFlowNetwork(x, maxWins);
        	FordFulkerson maxflow = new FordFulkerson(network, source, sink);

        	for (FlowEdge edge : network.adj(source)) {
            		if (edge.flow() < edge.capacity()) {
                		return true;
           		}
        	}
		return false;
	}
	public Iterable<String> certificateOfElimination(String team) {
 		// subset R of teams that eliminates given team; null if not eliminated
		List<String> subset = new ArrayList<>();
        	if (!isEliminated(team)) {
            		return null;
        	}

        	int x = teams.get(team);
        	int maxWins = wins[x] + remaining[x];

        	// Trivial elimination
        	/*for (int i = 0; i < numberOfTeams; i++) {
            		if (wins[i] > maxWins) {
                		subset.add(teams[i]);
                		return subset;
            		}
        	}*/
		for (String teamName : teams.keySet()) {
			if (wins[teams.get(teamName)] > maxWins) { 
			        subset.add(teamName);
				return subSet;
			}
		}

        	// Non-trivial elimination
        	FlowNetwork network = createFlowNetwork(x, maxWins);
        	FordFulkerson maxflow = new FordFulkerson(network, source, sink);

        	for (int i = 0; i < numberOfTeams; i++) {
            		if (i != x && maxflow.inCut(i)) {
                		subset.add(teamNames[i]);
            		}
        	}

        	return subset;
	}

    private FlowNetwork createFlowNetwork(int x, int maxWins) {
        int gameVertices = (numberOfTeams - 1) * (numberOfTeams - 2) / 2;
        int teamVertices = numberOfTeams - 1;
        int totalVertices = 1 + gameVertices + teamVertices + 1;

        FlowNetwork network = new FlowNetwork(totalVertices);
        int gameVertexIndex = 1;

        for (int i = 0; i < numberOfTeams; i++) {
            if (i == x) continue;
            for (int j = i + 1; j < numberOfTeams; j++) {
                if (j == x) continue;

                int gameIndex = gameVertexIndex++;
                network.addEdge(new FlowEdge(source, gameIndex, against[i][j]));
                network.addEdge(new FlowEdge(gameIndex, teamVertex(i), Double.POSITIVE_INFINITY));
                network.addEdge(new FlowEdge(gameIndex, teamVertex(j), Double.POSITIVE_INFINITY));
            }
        }

        for (int i = 0; i < numberOfTeams; i++) {
            if (i == x) continue;
            network.addEdge(new FlowEdge(teamVertex(i), sink, maxWins - wins[i]));
        }

        return network;
    }

    private int teamVertex(int teamIndex) {
        if (teamIndex > source) return teamIndex - 1;
        return teamIndex;
    }

    private int source = 0;
    private int sink = totalVertices - 1;

	public static void main(String[] args) {
    		BaseballElimination division = new BaseballElimination(args[0]);
    		for (String team : division.teams()) {
        		if (division.isEliminated(team)) {
            			StdOut.print(team + " is eliminated by the subset R = { ");
            			for (String t : division.certificateOfElimination(team)) {
                			StdOut.print(t + " ");
            			}
            			StdOut.println("}");
        		}
        		else {
            			StdOut.println(team + " is not eliminated");
        		}
    		}
	}
};
