package SneakyPath;

import java.util.*;
import javax.swing.*;
import java.io.*;

public class FloydWarshall
{
    /*
    -------------------------------------------------------------------------------
    Matrix Variables
    -------------------------------------------------------------------------------
     */
    private static int[][] EdgeMatrix; //original edge matrix for storage and recall
    private static int[][] FlowMatrix;
    private static int[][] AllPairsMatrix; //All-pairs matrix for finding Shortest Path
    private static int[][] PRMatrix; //matrix for Path Reconstruction
    private static int[][] LoadTrafficMatrix; //Matrix with actual traffic on all roads
    private static int[][][] ShortestPaths; //Contains shortest path for all nodes to all other nodes
    private static int[][] CalculationsMatrix; //Used to store each of the min/max calculations before output
    private static double[][] AverageMatrix; //Used to store average traffic
    private static int[][] HopCount; //for storing number of hops when finding shortest path

    /*
    -------------------------------------------------------------------------------
    Constants
    -------------------------------------------------------------------------------
     */
    private static int numberOfVertices;
    public static final int INF = 999999999;


    /*
    -------------------------------------------------------------------------------
    Constructor
    -------------------------------------------------------------------------------
     */
    public FloydWarshall(int numberOfVertices, int[][] flowMatrix)
    {
        this.numberOfVertices = numberOfVertices;
        FlowMatrix = flowMatrix;
        ShortestPaths = new int[numberOfVertices + 1][numberOfVertices +1][];
        LoadTrafficMatrix = new int[numberOfVertices + 1][numberOfVertices +1];
        PRMatrix = new int[numberOfVertices + 1][numberOfVertices +1];
    }

    /*
    -------------------------------------------------------------------------------
    Matrix (Re)Initialization
    -------------------------------------------------------------------------------
     */
    public void initializePRMatrix(){
        for (int i  = 1; i < PRMatrix.length; i++){
            for (int j = 1; j < PRMatrix.length; j++){
                //initialize NextMatrix such that each node came from itself
                PRMatrix[i][j] = j;
            }
        }
    }

    public static void setEdgeMatrix(int[][] edgeMatrix) {
        EdgeMatrix = new int[edgeMatrix.length][edgeMatrix.length];
        for(int i = 0; i < edgeMatrix.length; i ++){
            for(int j = 0; j< edgeMatrix[i].length; j++){
                EdgeMatrix[i][j] = edgeMatrix[i][j];
            }
        }
    }

    /*
    -------------------------------------------------------------------------------
    Main Algorithm Functions
    -------------------------------------------------------------------------------
     */
    public void implementFW(int edgeMatrix[][])
    {
        // Initialize the solution matrix same as input graph matrix.
        int[][] dist = Arrays.copyOf(edgeMatrix, edgeMatrix.length);
        initializePRMatrix();

        // Add all vertices one by one to the set of intermediate vertices.
        for (int k = 1; k <= numberOfVertices; k++)
        {
            // Pick all vertices as source one by one
            for (int i = 1; i <= numberOfVertices; i++)
            {
                // Pick all vertices as destination for the above picked source
                for (int j = 1; j <= numberOfVertices; j++)
                {
                    // If vertex k is on the shortest path from i to j, then update the value of dist[i][j]
                    if (dist[i][k] + dist[k][j] < dist[i][j]){
                        dist[i][j] = dist[i][k] + dist[k][j];
                        PRMatrix[i][j] = PRMatrix[i][k];
                    }
                }
            }
        }

        this.AllPairsMatrix = dist;
    }

    public static ArrayList<Integer> getPath(int i, int j){
        if (PRMatrix[i][j] == INF) {
            return null;
        }
        ArrayList<Integer> path = new ArrayList<>();
        //add current node to pathway
        path.add(i);

        //ensure that we haven't reached the original node
        while (i != j) {
            //find the node necessary to reach the current node
            //set i to previous node and add to path
            i = PRMatrix[i][j];
            path.add(i);
        }
        return path;
    }

    public static void getAllPaths(){
        ShortestPaths = new int[AllPairsMatrix.length][AllPairsMatrix.length][];
        for(int i = 1; i < AllPairsMatrix.length; i++) {
            for (int j = 1; j < AllPairsMatrix.length; j++) {
                ArrayList<Integer> shortestPath = getPath(i, j);
                //change from ArrayList to array
                if (!(shortestPath.equals(null))) {
                    int[] shortPath = new int[shortestPath.size()];
                    for (int k = 0; k < shortestPath.size(); k++) {
                        shortPath[k] = shortestPath.get(k);
                    }
                    //put path in matrix
                    ShortestPaths[i][j] = shortPath;
                }
            }
        }
    }

    public static void buildLoadMatrix(){

        //Use getPath function to get shortest path between vertices and add each path to matrix
        for(int i = 1; i < ShortestPaths.length; i++) {
            for (int j = 1; j < ShortestPaths[i].length; j++) {
                    for (int k = 1; k < ShortestPaths[i][j].length; k++){
                        //store values of present and next nodes in order to add flow
                        int a = ShortestPaths[i][j][k-1];
                        int b = ShortestPaths[i][j][k];

                        //add flow along path into LoadMatrix
                        LoadTrafficMatrix[a][b] += FlowMatrix[i][j];
                    }
            }
        }

        //Get rid of 0's in Load Matrix for paths that don't exist
        for(int i = 1; i < LoadTrafficMatrix.length; i++) {
            for (int j = 1; j < LoadTrafficMatrix.length; j++) {
                if (EdgeMatrix[i][j] == INF){
                        LoadTrafficMatrix[i][j] = INF;
                }
            }
        }
    }

    /*
    -------------------------------------------------------------------------------
    Calculations
    -------------------------------------------------------------------------------
     */
    public static void calculateMinTraffic(){
        CalculationsMatrix = new int[numberOfVertices + 1][numberOfVertices +1];
        int min;

        for(int i = 1; i < ShortestPaths.length; i++) {
            for (int j = 1; j < ShortestPaths[i].length; j++) {
                min = INF;
                for (int k = 1; k < ShortestPaths[i][j].length; k++){
                    //store values of present and next nodes
                    int a = ShortestPaths[i][j][k-1];
                    int b = ShortestPaths[i][j][k];

                    if (LoadTrafficMatrix[a][b] < min){
                        min = LoadTrafficMatrix[a][b];
                    }
                }
                if (i == j){
                    CalculationsMatrix[i][j] = 0;
                }
                else {
                    CalculationsMatrix[i][j] = min;
                }
            }
        }
    }

    public static void calculateMaxTraffic(){
        CalculationsMatrix = new int[numberOfVertices + 1][numberOfVertices +1];
        int max;

        for(int i = 1; i < ShortestPaths.length; i++) {
            for (int j = 1; j < ShortestPaths[i].length; j++) {
                max = 0;
                for (int k = 1; k < ShortestPaths[i][j].length; k++){
                    //store values of present and next nodes
                    int a = ShortestPaths[i][j][k-1];
                    int b = ShortestPaths[i][j][k];

                    if (LoadTrafficMatrix[a][b] > max){
                        max = LoadTrafficMatrix[a][b];
                    }
                }
                if (i == j){
                    CalculationsMatrix[i][j] = 0;
                }
                else {
                    CalculationsMatrix[i][j] = max;
                }
            }
        }
    }

    public static void calculateAvgTraffic(){
        AverageMatrix = new double[numberOfVertices + 1][numberOfVertices +1];
        double sum, count;
        double avg;

        for(int i = 1; i < ShortestPaths.length; i++) {
            for (int j = 1; j < ShortestPaths[i].length; j++) {
                sum = count = 0;
                avg = 0;
                for (int k = 1; k < ShortestPaths[i][j].length; k++){
                    //store values of present and next nodes in order to add flow
                    int a = ShortestPaths[i][j][k-1];
                    int b = ShortestPaths[i][j][k];

                    sum += LoadTrafficMatrix[a][b];
                    count++;
                }
                if (count != 0) {
                    avg = sum / count;
                }
                else {
                    avg = 0;
                }

                if (i == j){
                    AverageMatrix[i][j] = 0;
                }
                else {
                    AverageMatrix[i][j] = avg;
                }
            }
        }
    }

    /*
    -------------------------------------------------------------------------------
    Printing
    -------------------------------------------------------------------------------
     */
    public static void printShortestPaths(BufferedWriter writer) throws IOException{
        for(int i = 1; i < ShortestPaths.length; i++) {
            writer.write(i + " -> ");
            for (int j = 1; j < ShortestPaths[i].length; j++) {
                writer.write("\t" + j + ": [ ");
                for (int k = 0; k < ShortestPaths[i][j].length; k++){
                    writer.write(ShortestPaths[i][j][k] + " ");
                }
                writer.write("]; ");
            }
            writer.newLine();
        }
    }

    public static void printMatrix(int[][] matrix, BufferedWriter writer)throws IOException{
        //print out values of any given 2d matrix, with row and column headers
        for (int i = 1; i < matrix.length; i++){
            writer.write("\t" + i);
        }
        writer.newLine();
        for (int i = 1; i < matrix.length; i++){
            writer.write((i) + "\t");
            for (int j = 1; j < matrix[i].length; j++){
                if (matrix[i][j] == INF){writer.write("INF\t");}
                else {writer.write(matrix[i][j] + "\t");}
            }
            writer.newLine();
        }
    }

    public static void printDoubleMatrix(double[][] matrix, BufferedWriter writer)throws IOException{
        //print out values of any given 2d matrix, with row and column headers
        String formattedString;
        for (int i = 1; i < matrix.length; i++){
            writer.write("\t" + i +"\t");
        }
        writer.newLine();
        for (int i = 1; i < matrix.length; i++){
            writer.write((i) + "\t");
            for (int j = 1; j < matrix[i].length; j++){
                if (matrix[i][j] == INF){writer.write("INF\t\t");}
                else {
                    formattedString = String.format("%.1f\t\t", matrix[i][j]);
                    writer.write(formattedString);
                }
            }
            writer.newLine();
        }
    }

    /*
    -------------------------------------------------------------------------------
    Program Entry Point
    -------------------------------------------------------------------------------
     */
    public static void main(String[] args) {
        //initialize variables
        long startTime, endTime;
        double runningTime;
        double totalRunningTime = 0;

        MatrixCreator mc = new MatrixCreator();
        mc.createMatrices();
        int[][] edge = mc.getEdgeMatrix();
        int[][] flow = mc.getFlowMatrix();
        int a = mc.getStartingPoint();
        int b = mc.getEndingPoint();
        FloydWarshall fw = new FloydWarshall(edge.length - 1, flow);

        //Get directory to save files to from user
        JFileChooser f = new JFileChooser();
        f.setDialogTitle("Select Folder To Save Output To");
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.showSaveDialog(null);

        //Create(or overwrite) file for output
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f.getSelectedFile() + "\\Output_CS404FS16SneakyPathInput_" + mc.getFilename()), "utf-8"))) {

            //print off initial variables
            writer.write("Number of Cities: " + numberOfVertices ); writer.newLine();
            writer.write("Number of Cities: " + numberOfVertices);
            writer.write("Starting point: " + a ); writer.newLine();
            writer.write("Ending point: " + b ); writer.newLine();
            writer.newLine();
            writer.write("EdgeMatrix: " ); writer.newLine();
            printMatrix(edge, writer);
            writer.newLine();
            writer.write("FlowMatrix: " ); writer.newLine();
            printMatrix(flow, writer);

            //All-Pairs shortest path
            writer.newLine();
            writer.write("All-Pairs Shortest Path Matrix:" ); writer.newLine();
            fw.setEdgeMatrix(edge);
            startTime = System.nanoTime();
            fw.implementFW(edge);
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printMatrix(AllPairsMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //Actual Shortest Paths
            writer.newLine();
            writer.write("Actual Shortest-Paths:" ); writer.newLine();
            startTime = System.nanoTime();
            fw.getAllPaths();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printShortestPaths(writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //Build Load Matrix
            startTime = System.nanoTime();
            fw.buildLoadMatrix();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            writer.newLine();
            writer.write("LoadMatrix:" ); writer.newLine();
            printMatrix(LoadTrafficMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //All-Pairs Sneaky Matrix
            startTime = System.nanoTime();
            fw.implementFW(LoadTrafficMatrix);
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            writer.newLine();
            writer.write("All-Pairs Sneaky-Path Matrix:" ); writer.newLine();
            printMatrix(AllPairsMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //Actual Sneaky Paths
            writer.newLine();
            writer.write("Actual Shortest Sneaky-Paths:" ); writer.newLine();
            startTime = System.nanoTime();
            fw.getAllPaths();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printShortestPaths(writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();
            writer.newLine();
            writer.write("Sneaky Path from " + a + " to " + b + ": " + fw.getPath(a, b) ); writer.newLine();


            //Minimum Traffic Matrix
            writer.newLine();
            writer.write("Minimum Cars Seen:" ); writer.newLine();
            startTime = System.nanoTime();
            calculateMinTraffic();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printMatrix(CalculationsMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //Maximum Traffic Matrix
            writer.newLine();
            writer.write("Maximum Cars Seen:" ); writer.newLine();
            startTime = System.nanoTime();
            calculateMaxTraffic();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printMatrix(CalculationsMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();

            //Average Traffic Matrix
            writer.newLine();
            writer.write("Average Cars Seen:" ); writer.newLine();
            startTime = System.nanoTime();
            calculateAvgTraffic();
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;
            printDoubleMatrix(AverageMatrix, writer);
            writer.write("Computation took " + (runningTime / 1000000) + " milliseconds" ); writer.newLine();
            writer.newLine();
            writer.write("Total running time for program: " + (totalRunningTime / 1000000) + " milliseconds" ); writer.newLine();
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}