package SneakyPath;
import javax.swing.*;
import java.io.*;


public class MatrixCreator {
    private int[][] EdgeMatrix;
    private int[][] FlowMatrix;
    private int startingPoint;
    private int endingPoint;
    private String filename;
    private final static int INF = 999999999;

    public MatrixCreator(){

    }
    public int getStartingPoint(){
        return startingPoint;
    }

    public int getEndingPoint(){
        return endingPoint;
    }

    public int[][] getEdgeMatrix(){
        return EdgeMatrix;
    }

    public int[][] getFlowMatrix(){
        return FlowMatrix;
    }

    public String getFilename(){
        return filename;
    }

    public void createMatrices(){
        //Ask user to open file to read from

        File file = null;
        try {
            final JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Input File For SneakyPath");
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                filename = file.getName();
            }
            else{
                System.exit(0);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //Parse file into Edge and Flow matrices
        //Program assumes files will be in same format as was given in class examples

        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line = br.readLine();

            //get rid of all whitespace
            line = line.replaceAll("\\s", "");

            //initialize matrices, starting and ending points
            String[] values = line.split(",");
            int dimension = Integer.parseInt(values[0]);
            EdgeMatrix = new int[dimension + 1][dimension + 1];
            FlowMatrix = new int[dimension + 1][dimension + 1];
            //NextMatrix = new int[dimension + 1][dimension + 1];
            startingPoint = Integer.parseInt(values[1]);
            endingPoint = Integer.parseInt(values[2]);

            //Read in rest of data and fill matrices
            String matrix;
            int from;
            int to;
            int weight;

            while ( !(line = br.readLine()).equals(null)) {
                if (!line.equals("")) {
                    //parse values from each line into proper location
                    line = line.replaceAll("\\s", "");
                    values = line.split(",");
                    matrix = values[0];
                    from = Integer.parseInt(values[1]);
                    to = Integer.parseInt(values[2]);
                    weight = Integer.parseInt(values[3]);

                    //read each value into proper matrix
                    if (matrix.equals("E")) {
                        EdgeMatrix[from][to] = weight;
                    } else {
                        FlowMatrix[from][to] = weight;
                    }

                }
            }

        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //replace all zero's with INF (unless it is the distance between the node and itself, in which case leave at 0)
        for (int i  = 1; i < EdgeMatrix.length; i++){
            for (int j = 1; j < EdgeMatrix[i].length; j++){
                if (j != i){
                    if (EdgeMatrix[i][j] == 0){
                        EdgeMatrix[i][j] = INF;
                    }
                }
            }
        }
    }
}
