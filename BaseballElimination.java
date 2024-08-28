import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stack;

public class BaseballElimination {
    private final int numTeams;
    private final String[] teams;
    private final int[][] games;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;

    public BaseballElimination(String filename) {
        In in = new In(filename);
        numTeams = in.readInt();
        teams = new String[numTeams];
        games = new int[numTeams][numTeams];
        wins = new int[numTeams];
        losses = new int[numTeams];
        remaining = new int[numTeams];

        for (int i = 0; i < numTeams; i++) {
            teams[i] = in.readString();
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remaining[i] = in.readInt();
            for (int j = 0; j < numTeams; j++) {
                games[i][j] = in.readInt();
            }
        }
        in.close();
    }

    public Iterable<String> teams() {
        Stack<String> teamStack = new Stack<>();
        for (String team : teams) {
            teamStack.push(team);
        }
        return teamStack;
    }

    public boolean isEliminated(String team) {
        int teamIndex = indexOf(team);
        if (teamIndex == -1) throw new IllegalArgumentException("Unknown team " + team);
        return isEliminated(teamIndex);
    }

    private boolean isEliminated(int teamIndex) {
        int maxWins = wins[teamIndex] + remaining[teamIndex];
        for (int i = 0; i < numTeams; i++) {
            if (wins[i] > maxWins) {
                return true;
            }
        }

        FlowNetwork network = createFlowNetwork(teamIndex, maxWins);
        FordFulkerson maxflow = new FordFulkerson(network, 0, network.V() - 1);

        for (FlowEdge edge : network.adj(0)) {
            if (edge.flow() < edge.capacity()) return true;
        }
        return false;
    }

    private FlowNetwork createFlowNetwork(int teamIndex, int maxWins) {
        int gameVertices = (numTeams * (numTeams - 1)) / 2;
        int totalVertices = 2 + gameVertices + numTeams; // source, sink, game vertices, team vertices
        FlowNetwork network = new FlowNetwork(totalVertices);

        int source = 0;
        int sink = totalVertices - 1;
        int gameVertex = 1;

        for (int i = 0; i < numTeams; i++) {
            if (i == teamIndex) continue;

            for (int j = i + 1; j < numTeams; j++) {
                if (j == teamIndex) continue;

                network.addEdge(new FlowEdge(source, gameVertex, games[i][j]));
                network.addEdge(new FlowEdge(gameVertex, teamVertex(i), Double.POSITIVE_INFINITY));
                network.addEdge(new FlowEdge(gameVertex, teamVertex(j), Double.POSITIVE_INFINITY));
                gameVertex++;
            }

            int capacity = maxWins - wins[i];
            if (capacity < 0) capacity = 0; // Ensure non-negative capacity
            network.addEdge(new FlowEdge(teamVertex(i), sink, capacity));
        }

        return network;
    }

    private int teamVertex(int team) {
        int gameVertices = (numTeams * (numTeams - 1)) / 2;
        return 1 + gameVertices + team;
    }

    public Iterable<String> certificateOfElimination(String team) {
        int teamIndex = indexOf(team);
        if (teamIndex == -1) throw new IllegalArgumentException("Unknown team " + team);
        return certificateOfElimination(teamIndex);
    }

    private Iterable<String> certificateOfElimination(int teamIndex) {
        Stack<String> certificate = new Stack<>();
        int maxWins = wins[teamIndex] + remaining[teamIndex];
        FlowNetwork network = createFlowNetwork(teamIndex, maxWins);
        FordFulkerson maxflow = new FordFulkerson(network, 0, network.V() - 1);

        for (int i = 0; i < numTeams; i++) {
            if (i == teamIndex) continue;
            if (maxflow.inCut(teamVertex(i))) {
                certificate.push(teams[i]);
            }
        }
        return certificate;
    }

    private int indexOf(String team) {
        for (int i = 0; i < numTeams; i++) {
            if (teams[i].equals(team)) return i;
        }
        return -1;
    }

    public int numberOfTeams() {
        return numTeams;
    }

    public int wins(String team) {
        int teamIndex = indexOf(team);
        if (teamIndex == -1) throw new IllegalArgumentException("Unknown team " + team);
        return wins[teamIndex];
    }

    public int losses(String team) {
        int teamIndex = indexOf(team);
        if (teamIndex == -1) throw new IllegalArgumentException("Unknown team " + team);
        return losses[teamIndex];
    }

    public int remaining(String team) {
        int teamIndex = indexOf(team);
        if (teamIndex == -1) throw new IllegalArgumentException("Unknown team " + team);
        return remaining[teamIndex];
    }

    public int against(String team1, String team2) {
        int index1 = indexOf(team1);
        int index2 = indexOf(team2);
        if (index1 == -1 || index2 == -1) throw new IllegalArgumentException("Unknown team");
        return games[index1][index2];
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            StdOut.println("Please provide the filename as a command-line argument.");
            return;
        }

        BaseballElimination division;
        try {
            division = new BaseballElimination(args[0]);
        } catch (IllegalArgumentException e) {
            StdOut.println("Error reading file: " + e.getMessage());
            return;
        }

        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}

