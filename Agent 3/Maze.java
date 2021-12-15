// AUTHOR: Daniel Ying (INSERT NETID) and Zachary Tarman (zpt2)

package agent3;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Point;

public class Maze {
	
	private ArrayList<ArrayList<CellInfo>> maze_structure; // THE BIG OBJECT THAT WILL STORE ALL THE CELL INFORMATION AT THE SPECIFIED COORDINATES
    private int rows; // THE NUMBER OF ROWS
    private int cols; // THE NUMBER OF COLUMNS


    // THIS RETRIEVES A CELL'S INFORMATION GIVEN THE COORDINATE
    public CellInfo getCell(int x, int y) {
    	return maze_structure.get(x).get(y);
    }

    //RANDOMLY ASSIGN A BLOCKED OR FREE STATUS TO THE CELL
    private boolean gen_block(double p) {
        double rand = Math.random();
        if (rand < p) { return true; }
        return false;
    }

    // SETTING UP ALL THE INDIVIDUAL CELLS FOR ALL ROWS AND COLUMNS
    private ArrayList<ArrayList<CellInfo>> maze_create(int rows, int cols, double prob) {
        
        maze_structure = new ArrayList<ArrayList<CellInfo>>();
        
        for (int col = 0; col < cols; col++){
            
            ArrayList<CellInfo> temp = new ArrayList<CellInfo>();
            
            for (int row = 0; row < rows; row++) {
                
                // STORE THE CELL'S POSITION WITHIN THE MAZE
            	Point pos = new Point(col, row);

                // VERIFYING HOW MANY NEIGHBORS THIS CURRENT CELL ACTUALLY HAS
                int neighbors;
                if ((row == 0 && col == 0) || (row == 0 && col == cols - 1) || (row == rows - 1 && col == 0) || (row == rows - 1 && col == cols - 1)) {
                	neighbors = 3;
                } else if (row == 0 || col == 0 || row == rows - 1 || col == cols - 1) {
                	neighbors = 5;
                } else {
                	neighbors = 8;
                }

                // VERIFYING HOW MUCH THE HEURISTIC ESTIMATE IS FROM THIS GIVEN CELL
                	// GIVEN THAT THE MANHATTAN HEURISTIC YIELDED THE FASTEST RUNTIME IN PROJECT 1,
                	// WE'RE GOING TO BE USING THAT AS THE HEURISTIC HERE AS WELL
                double one = Math.abs(row - (rows - 1));
                double two = Math.abs(col - (cols - 1));
                double d_one = row - (rows - 1);
                double d_two = col - (cols - 1);
                double d_row = 0 - (rows - 1);
                double d_col = 0 - (cols - 1);
                double cross = Math.abs(d_one*d_col - d_two*d_row);
                double new_h = one + two;
                new_h += cross * 0.001;

                // NEW CELL BEING INSERTED INTO THE MAZE WITH THE CORRECT NUMBER OF NEIGHBORS, THE RIGHT HEURISTIC ESTIMATE, AND RANDOMIZED "BLOCKED" STATUS
                CellInfo temp2 = new CellInfo(pos, neighbors, new_h, gen_block(prob));
                temp.add(temp2);
            }

            maze_structure.add(temp);
        }

        // MAKING SURE THAT THE START AND GOAL CELLS ARE UNBLOCKED FOR US
        getCell(0, 0).setActualBlockStatus(false);
        getCell(cols - 1, rows - 1).setActualBlockStatus(false);

        return maze_structure;
    }

    // CONSTRUCTOR 
    public Maze(int rows, int cols, double p) {
        this.rows = rows;
        this.cols = cols;
        this.maze_structure = maze_create(rows, cols, p);
    }


    public String toStringGen(Point agent) {
        
        StringBuilder builder = new StringBuilder("\n");

        for (int i = 0; i < rows; i++) {
        	
        	for (int j = 0; j < cols; j++) {

        		if (i == 0 && j == 0) {
        			builder.append("0"); // MARK START CELL
        			continue;
        		} else if (i == rows - 1 && j == cols - 1) { // MARK GOAL CELL
        			builder.append("2");
        			continue;
        		}

        		if (getCell(j, i).isActuallyBlocked() && getCell(j, i).isVisited()) {
        			builder.append("X");
        		}else if (getCell(j, i).isActuallyBlocked()) {
        			builder.append("0");
        		} 
                 /*else if (getCell(j, i).isOnShortestPath()) {
        			builder.append("1");
        		}*/ else if (getCell(j,i).getPos() == agent){//getCell(j, i).isVisited() ) {
        			builder.append("1");
        		} else {
        			builder.append("0");
        		}

        	}

        	builder.append("\n");
        }

        return builder.toString();
    }
    
    
    public String toStringInfer(Point agent) {
        
        StringBuilder builder = new StringBuilder("\n");

        for (int i = 0; i < rows; i++) {
        	
        	for (int j = 0; j < cols; j++) {

        		if (getCell(j, i).isVisited()) {
        			int sensed = getCell(j, i).getBlocksSensed();
        			builder.append("" + sensed);
        		} else {
        			builder.append("X");
        		}

        	}

        	builder.append("\n");
        }

        return builder.toString();
    }
}