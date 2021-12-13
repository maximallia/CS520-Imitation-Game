// AUTHOR: Zachary Tarman (zpt2)

import java.util.ArrayList;
import java.util.LinkedList;
import java.awt.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Agent3 {

	public Maze maze;
	public int rows; // ROW DIMENSION OF MAZE
	public int cols; // COLUMN DIMENSION OF MAZE

	public int collisions = 0; // THE NUMBER OF BLOCKS THE AGENT PHYSICALLY HITS
    public int cellsProcessed = 0; // THE NUMBER OF CELLS THAT WE "EXPLORE" OR POP OFF THE PLANNED PATH
	public int trajectoryLength = 0; // THE TRAJECTORY LENGTH OF THE AGENT
    public int shortestPathFound = 0; // THE LENGTH OF THE SHORTEST PATH FOUND BY THE AGENT WHILE TRAVERSING THE MAZE
    public long runtime = 0; // THE RUNTIME OF THE PROGRAM TO FIND A PATH TO THE GOAL

	public int direction = -1;

	String return_str = "";

    // USED TO PRINT THE ABOVE STATS FOR THE PROJECT
    public void printStats() {
    	System.out.println("Statistics for Maze Solution");
    	System.out.println("Trajectory Length: " + trajectoryLength);
    	System.out.println("Cells Popped: " + cellsProcessed);
    	System.out.println("Runtime: " + runtime);
    	System.out.println("Collisions: " + collisions);
    	System.out.println("Shortest Path Found: " + shortestPathFound);
    	System.out.println();
    	return;
    }




	// PLANNING METHOD (AKA A SINGLE ITERATION OF A* WITHOUT PHYSICALLY MOVING THE AGENT THROUGH THE MAZE)
	public LinkedList<CellInfo> plan(CellInfo start) {

		LinkedList<CellInfo> plannedPath = new LinkedList<CellInfo>(); // TO STORE THE NEW PLANNED PATH
		ArrayList<CellInfo> toExplore = new ArrayList<CellInfo>(); // TO STORE THE CELLS TO BE EXPLORED
		ArrayList<CellInfo> doneWith = new ArrayList<CellInfo>();
		
		CellInfo curr = start; // PTR TO THE CURRENT CELL WE'RE EVALUATING TO MOVE ON FROM IN OUR PLAN
		Point curr_position; // THE COORDINATE OF THE CURRENT CELL THAT WE'RE LOOKING AT
		int x, y; // THE X AND Y VALUES OF THE COORDINATE FOR THE CURRENT CELL THAT WE'RE LOOKING AT (FOR FINDING NEIGHBORING COORDINATES)
		boolean addUp, addDown, addLeft, addRight; // TO INDICATE IF WE CAN PLAN TO GO IN THAT DIRECTION FROM CURRENT CELL

		Point up = new Point(); // COORDINATE OF NORTH NEIGHBOR
		Point down = new Point(); // COORDINATE OF SOUTH NEIGHBOR
		Point left = new Point(); // COORDINATE OF WEST NEIGHBOR
		Point right = new Point(); // COORDINATE OF EAST NEIGHBOR
		
		// DEBUGGING STATEMENT
		// System.out.println("We're in a new planning phase.");
		
		CellInfo first = curr; // THIS IS TO MARK WHERE THE REST OF PLANNING WILL CONTINUE FROM
		toExplore.add(first);

		// BEGIN LOOP UNTIL PLAN REACHES GOAL
		while (toExplore.size() > 0) {

			curr = toExplore.remove(0); // CURRENT CELL THAT WE'RE LOOKING AT
			
			if (contains(curr, doneWith)) { // WE DON'T WANT TO EXPAND THE SAME CELL AGAIN (THIS IS HERE JUST IN CASE)
				// System.out.println("We've already seen this cell and its directions: " + curr.getPos().toString());
				continue;
			}

			// DEBUGGING STATEMENT
			// System.out.println("We're currently figuring out where to plan to go to next from " + curr.getPos().toString());
			
			curr_position = curr.getPos(); // COORDINATE OF THE CELL WE'RE CURRENTLY EXPLORING
			x = (int) curr_position.getX(); // X COORDINATE
			y = (int) curr_position.getY(); // Y COORDINATE
			doneWith.add(curr); // WE DON'T WANT TO EXPAND / LOOK AT THIS CELL AGAIN IN THIS PLANNING PHASE
						
			// IS THIS CELL THE GOAL??
			// IF SO, LET'S TRACE BACK TO OUR STARTING POSITION
			if (x == cols - 1 && y == rows - 1) {
				CellInfo goal = maze.getCell(cols - 1, rows - 1);
				CellInfo ptr = goal;
				while (ptr.getPos().getX() != first.getPos().getX() || ptr.getPos().getY() != first.getPos().getY()) {
					// DEBUGGING STATEMENT
					// System.out.print("(" + ptr.getPos().getX() + "," + ptr.getPos().getY() + "), ");
					plannedPath.addFirst(ptr);
					ptr = ptr.getParent();
				}
				plannedPath.addFirst(ptr); // ADDING START CELL TO THE PATH
				return plannedPath;
			}

			// DETERMINE POSSIBLE PLACES TO MOVE FROM CURRENT POSITION
			up.setLocation(x, y - 1); // NORTH
			down.setLocation(x, y + 1); // SOUTH
			left.setLocation(x - 1, y); // WEST
			right.setLocation(x + 1, y); // EAST
			addUp = true;
			addDown = true; 
			addLeft = true;
			addRight = true;

			// CHECK FOR CELLS WE CAN'T / SHOULDN'T EXPLORE OR MOVE INTO ON OUR WAY TO THE GOAL
			// CHECKS FOR NORTHBOUND NEIGHBOR
			if (!inBounds(up)) { 
				addUp = false; 
			} else if (maze.getCell((int) up.getX(), (int) up.getY()).isBlocked()) {
				addUp = false;
				// System.out.println("North is blocked.");
			} else if (maze.getCell((int) up.getX(), (int) up.getY()).isVisited()) {
				addUp = false;
				// System.out.println("North is visited.");
			} else if (contains(maze.getCell((int) up.getX(), (int) up.getY()), doneWith)) {
				addUp = false;
			}
			
			// CHECKS FOR SOUTHBOUND NEIGHBOR
			if (!inBounds(down)) { 
				addDown = false; 
			} else if (maze.getCell((int) down.getX(), (int) down.getY()).isBlocked()) {
				addDown = false;
				// System.out.println("South is blocked.");
			} else if (maze.getCell((int) down.getX(), (int) down.getY()).isVisited()) {
				addDown = false;
				// System.out.println("South is visited.");
			} else if (contains(maze.getCell((int) down.getX(), (int) down.getY()), doneWith)) {
				addDown = false;
			}
			
			// CHECKS FOR WESTBOUND NEIGHBOR
			if (!inBounds(left)) { 
				addLeft = false; 
			} else if (maze.getCell((int) left.getX(), (int) left.getY()).isBlocked()) {
				addLeft = false;
				// System.out.println("West is blocked.");
			} else if (maze.getCell((int) left.getX(), (int) left.getY()).isVisited()) {
				addLeft = false;
				// System.out.println("West is visited.");
			} else if (contains(maze.getCell((int) left.getX(), (int) left.getY()), doneWith)) {
				addLeft = false;
			}
			
			// CHECKS FOR EASTBOUND NEIGHBOR
			if (!inBounds(right)) { 
				addRight = false; 
			} else if (maze.getCell((int) right.getX(), (int) right.getY()).isBlocked()) {
				addRight = false;
				// System.out.println("East is blocked.");
			} else if (maze.getCell((int) right.getX(), (int) right.getY()).isVisited()) {
				addRight = false;
				// System.out.println("East is visited.");
			} else if (contains(maze.getCell((int) right.getX(), (int) right.getY()), doneWith)) {
				addRight = false;
			}

			// ADD ALL UNVISITED, UNBLOCKED AND NOT-LOOKED-AT-ALREADY CELLS TO PRIORITY QUEUE + SET PARENTS AND G-VALUES
			CellInfo temp;
			double curr_g = curr.getG(); // THE G_VALUE OF THE CURRENT CELL IN THE PLANNING PROCESS
			if (addUp) { // THE CELL TO OUR NORTH IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) up.getX(), (int) up.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the north cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addLeft) { // THE CELL TO OUR WEST IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) left.getX(), (int) left.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the west cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			if (addDown) { // THE CELL TO OUR SOUTH IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) down.getX(), (int) down.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the south cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
				
			}
			if (addRight) { // THE CELL TO OUR EAST IS A CELL WE CAN EXPLORE
				temp = maze.getCell((int) right.getX(), (int) right.getY());
				temp.setG(curr_g + 1);
				temp.setParent(curr);
				toExplore = insertCell(temp, toExplore);
				// DEBUGGING STATEMENT
				/* System.out.println("Inserting the east cell " + temp.getPos().toString() + 
						" into the priority queue. Its parent is " + temp.getParent().getPos().toString() +
						" and its f / g values are: " + temp.getF() + ", " + temp.getG()); */
			}
			
			
			// IF WE HAVE NO CELLS LEFT TO EXPLORE, WE NEED TRY AGAIN FROM A DIFFERENT POINT IN OUR PARENTAGE
			// IF THERE ARE NO OTHER VIABLE OPTIONS, WE'LL TERMINATE
			if (toExplore.size() == 0) {
				// System.out.println("We have to backtrack.");
				CellInfo lastChance = backtrack(first, doneWith);
				if (lastChance != null) {
					// System.out.println("The new beginning point for this planning phase is " + lastChance.getPos().toString());
					first = lastChance;
					toExplore.add(lastChance);
				}
			}

		}

		// System.out.println("We've reached here for some reason.");
		return null; // MAZE IS UNSOLVABLE ):
	}

	public void addMatrix(Maze maze, int direction, Point agent){
        
		return_str += maze.toString(agent);
		return_str+= String.valueOf(direction);
		return_str += "\n";
    }

	
	//public void writeMatrix(File filename, Maze maze, int direction){
	public void writeMatrix(File filename){
		try{
            FileWriter buffwrite = new FileWriter(filename, true);

			//buffwrite.write(maze.toString());
			//buffwrite.write("\n");
			buffwrite.write(return_str);
			//buffwrite.write(String.valueOf(direction));
			//buffwrite.write("\n");
			buffwrite.close();
			//System.out.println("Write Matrix SUCCESS!\n");

        } catch( IOException e){
            System.out.println("Write Matrix Error!\n");
        }
    }


	// HELPER METHODS FOR THE DIFFERENT STEPS OF THE AGENT'S ALGORITHM
	public CellInfo backtrack(CellInfo pos, ArrayList<CellInfo> doneWith) { // USED TO BACKTRACK IF AGENT IS STUCK
		
		Point curr_position; // THE COORDINATE OF THE CURRENT CELL THAT WE'RE LOOKING AT
		int x, y; // THE X AND Y VALUES OF THE COORDINATE FOR THE CURRENT CELL THAT WE'RE LOOKING AT (FOR FINDING NEIGHBORING COORDINATES)

		Point up = new Point(); // COORDINATE OF NORTH NEIGHBOR
		Point down = new Point(); // COORDINATE OF SOUTH NEIGHBOR
		Point left = new Point(); // COORDINATE OF WEST NEIGHBOR
		Point right = new Point(); // COORDINATE OF EAST NEIGHBOR
		
		// LOOP TO TAKE CARE OF BACKTRACKING IF NO VIABLE MOVES "FORWARD" (I.E. AT END OF LONG HALLWAY)
		do {
			curr_position = pos.getPos();
			x = (int) curr_position.getX();
			y = (int) curr_position.getY();
			
			// DEBUGGING STATEMENT
			// System.out.println("We're evaluating " + pos.getPos().toString() + " for viable neighbors.");

			up.setLocation(x, y - 1); // NORTH
			down.setLocation(x, y + 1); // SOUTH
			left.setLocation(x - 1, y); // WEST
			right.setLocation(x + 1, y); // EAST

			// FOR EACH POSSIBLE NEIGHBOR, WE CHECK IF IT'S IN BOUNDS,
				// IF IT'S INFERRED / OBSERVED TO BE BLOCKED,
				// AND IF IT HAS ALREADY BEEN VISITED
					// IF IT'S ALREADY BEEN VISITED, THEN WE KNOW THAT
					// IT'S NOT WORTH EXPLORING THAT AREA AGAIN
					// THE ONLY EXCEPTION TO THIS WILL BE IF WE NEED TO BACKTRACK
					// WHICH IS EXPLAINED BELOW
			if (inBounds(up)) {  
				if (!(maze.getCell((int) up.getX(), (int) up.getY()).isBlocked())) {
					if (!(maze.getCell((int) up.getX(), (int) up.getY()).isVisited())) {
						if (!(contains(maze.getCell((int)up.getX(), (int)up.getY()), doneWith))) {
							// DEBUGGING STATEMENT
							// System.out.println("We've hit a break north condition in the backtracking.");
							break;
						}
					}
				}
			}
			
			if (inBounds(down)) {  
				if (!(maze.getCell((int) down.getX(), (int) down.getY()).isBlocked())) {
					if (!(maze.getCell((int) down.getX(), (int) down.getY()).isVisited())) {
						if (!(contains(maze.getCell((int)down.getX(), (int)down.getY()), doneWith))) {
							// DEBUGGING STATEMENT
							// System.out.println("We've hit a break south condition in the backtracking.");
							break;
						}
					}
				}
			}
			
			if (inBounds(left)) {  
				if (!(maze.getCell((int) left.getX(), (int) left.getY()).isBlocked())) {
					if (!(maze.getCell((int) left.getX(), (int) left.getY()).isVisited())) {
						if (!(contains(maze.getCell((int)left.getX(), (int)left.getY()), doneWith))) {
							// DEBUGGING STATEMENT
							// System.out.println("We've hit a break west condition in the backtracking.");
							break;
						}
					}
				}
			}
			
			if (inBounds(right)) {  
				if (!(maze.getCell((int) right.getX(), (int) right.getY()).isBlocked())) {
					if (!(maze.getCell((int) right.getX(), (int) right.getY()).isVisited())) {
						if (!(contains(maze.getCell((int)right.getX(), (int)right.getY()), doneWith))) {
							// DEBUGGING STATEMENT
							// System.out.println("We've hit a break east condition in the backtracking.");
							break;
						}
					}
				}
			}

			// IF WE'RE STILL HERE, THEN WE KNOW THAT THERE ARE NO VIABLE NEIGHBORS THAT WE HAVEN'T ALREADY VISITED
			// ARE WE AT THE START NODE RIGHT NOW?
			if (x == 0 && y == 0) { // WE'RE STILL AT THE START NODE HERE, AND THERE'S NOWHERE TO GO
				return null; // MAZE ISN'T SOLVABLE :(
			}

			// THE ONLY HOPE FOR FINDING A VIABLE PATH AT THIS POINT WOULD BE TO BACKTRACK
			pos = pos.getParent(); // WE'VE BACKTRACKED AND WE WILL START THIS PROCESS AGAIN
			trajectoryLength++; // WE INCLUDE THIS BACKTRACKING IN THE TRAJECTORY LENGTH

		} while (true);
		
		
		return pos;
	}
	
	
	
	public boolean canMove(CellInfo pos, LinkedList<CellInfo> path) { // CHECK IF A CELL CAN ACTUALLY MOVE TO THE NEXT CELL OR NOT
				
		if (path.peekFirst().isActuallyBlocked()) { 
			// System.out.println("Cell " + path.peekFirst().getPos().getX() + ", " + path.peekFirst().getPos().getY() + " is blocked. We cannot move here.");
			return false; 
		}
		return true;
	}

	
	
	public boolean inBounds(Point coor) { // CHECK IF A NEIGHBORING CELL'S COORDINATES ARE IN BOUNDS (DON'T CHECK NON-EXISTENT CELLS)
		if (coor.getX() < 0 || coor.getX() >= cols || coor.getY() < 0 || coor.getY() >= rows) {
			return false;
		}
		return true;
	}
	
	
	
	public boolean contains(CellInfo newCell, ArrayList<CellInfo> doneWith) { // TO USE FOR THE "CLOSED LIST" IN THE PLANNING PHASE
		for (int i = 0; i < doneWith.size(); i++) {
			if (newCell.getPos().getX() == doneWith.get(i).getPos().getX() 
					&& newCell.getPos().getY() == doneWith.get(i).getPos().getY()) {
				return true;
			}
		}
		return false;
	}

	
	
	public ArrayList<CellInfo> insertCell(CellInfo newCell, ArrayList<CellInfo> toExplore) { // TO USE FOR IMPLEMENTING A PRIORITY QUEUE IN THE PLANNING PHASE
		
		// FIRST CELL TO BE ADDED TO AN EMPTY LIST
		if (toExplore.isEmpty()) {
			toExplore.add(newCell);
			return toExplore;
		}
		
		double cell_f = newCell.getF();
		for (int i = 0; i < toExplore.size(); i++) { // WE WANT TO CHECK IF IT'S ALREADY IN THE LIST
			if (newCell.getPos().getX() == toExplore.get(i).getPos().getX() && newCell.getPos().getY() == toExplore.get(i).getPos().getY()) {
				if (cell_f <= toExplore.get(i).getF()) { // IF THE EXISTING F-VALUE IS HIGHER THAN WE JUST FOUND, WE WANT TO UPDATE THAT
					toExplore.remove(i);
				} else { // OTHERWISE, WE JUST RETURN THE LIST AS IS
					return toExplore;
				}
			}
		}
		
		if (toExplore.isEmpty()) {
			toExplore.add(newCell);
			return toExplore;
		}
		
		// IF OUR CELL HAS A BETTER F-VALUE OR THE CELL ISN'T IN THE LIST ALREADY, THEN WE ADD IT HERE
		for (int i = 0; i < toExplore.size(); i++) {
			if (cell_f < toExplore.get(i).getF()) {
				toExplore.add(i, newCell);
				return toExplore;
			} else if (cell_f == toExplore.get(i).getF()) {
				double cell_g = newCell.getG();
				if (cell_g <= toExplore.get(i).getG()) {
					toExplore.add(i, newCell);
					return toExplore;
				}
			}
		}
		
		// THE CELL WE FOUND HAS THE HIGHEST F-VALUE OF ANY WE FOUND SO FAR
		toExplore.add(toExplore.size() - 1, newCell); // ADDING IT TO THE END OF THE LIST
		return toExplore;
		
	}

	
	
	



	// THIS IS THE METHOD THAT POWERS THE ENTIRE ALGORITHM, RUNNING THE AGENT UNTIL GOAL CELL OR IT DETERMINES THERE'S NO PATH TO GOAL
	public static char run(int rowNum, int colNum, double prob, File filename) {

		Agent3 mazeRunner = new Agent3(); // KEEPS TRACK OF ALL OF OUR DATA AND STRUCTURES
		
		// READING FROM INPUT
		mazeRunner.rows = rowNum; // THE NUMBER OF ROWS THAT WE WANT IN THE CONSTRUCTED MAZE
		mazeRunner.cols = colNum; // THE NUMBER OF COLUMNS THAT WE WANT IN THE CONSTRUCTED MAZE
		double p = prob; // VALUE BETWEEN 0.0 AND 1.0

		// SET UP MAZE
		mazeRunner.maze = new Maze(mazeRunner.rows, mazeRunner.cols, p);
		boolean badPath = false; // USED FOR DETERMINING WITH WE'VE FOUND A BAD PATH BY INFERENCE
		
		long begin = System.nanoTime();
		CellInfo start = mazeRunner.maze.getCell(0, 0);
		LinkedList<CellInfo> plannedPath = mazeRunner.plan(start); // STORES OUR BEST PATH THROUGH THE MAZE
		
		double prev_y = 0.0;
		double prev_x = 0.0;

		// MAIN LOOP FOR AGENT TO FOLLOW AFTER FIRST PLANNING PHASE
		while (true) {

			// EXTRACT THE NEXT CELL IN THE PLANNED PATH
			CellInfo currCell = plannedPath.poll();

			//0=up, 1=down, 2=left, 3=right
			if(currCell.getPos().getY() == prev_y-1 ) mazeRunner.direction = 0;
			else if(currCell.getPos().getY() == prev_y+1) mazeRunner.direction = 1;
			else if(currCell.getPos().getX() == prev_x-1) mazeRunner.direction = 2;
			else if(currCell.getPos().getX() == prev_x+1) mazeRunner.direction = 3;
			
			
			// DEBUGGING STATEMENT
			// System.out.println("Agent is currently in " + currCell.getPos().getX() + ", " + currCell.getPos().getY());
			
			// HAVE WE HIT THE GOAL CELL YET?
			if (currCell.getPos().getX() == mazeRunner.cols - 1 && currCell.getPos().getY() == mazeRunner.rows - 1) { // WE'VE HIT THE GOAL CELL
				break;
			}

			// WE HAVEN'T HIT THE GOAL CELL, SO WE CONTINUE ONWARD
			currCell.setEmpty(); // THE CELL WE ARE CURRENTLY IS EMPTY / UNBLOCKED
			if (!currCell.isVisited()) { // IF THE CELL HAS ALREADY BEEN VISITED THEN WE DON'T NEED TO SENSE (SAVING SOME COMPUTATIONAL TIME)
				//mazeRunner.sense(currCell); // SENSE HOW MANY NEIGHBORS ARE BLOCKED (BUT NOT WHERE THE BLOCKS ARE)
			}
			currCell.setVisited(); // WE HAVE NOW OFFICIALLY VISITED THIS CELL
			//mazeRunner.infer(currCell); // INFER WHAT WE CAN ABOUT OUR SURROUNDINGS AND UPDATE KNOWLEDGE BASE

			// HAVE WE HAD AN UPDATE THAT REQUIRES US TO REPLAN?
			// CHECK IF WE'VE HAD AN UPDATE (BLOCK) IN THE PATH THAT REQUIRES US TO REPLAN
			for (int i = 0; i < plannedPath.size(); i++) {
				CellInfo temp = plannedPath.poll();
				if (temp.isBlocked()) { // THIS IS CHECKING THE INFERRED / OBSERVED BLOCKS, NOT SNEAKING A PEEK AT THE LEGITIMATE STATUS
					badPath = true;
					break;
				}
				plannedPath.addLast(temp); // CYCLE THIS CELL TO THE BACK
				// IF ALL CELLS ARE STILL THOUGHT TO BE UNBLOCKED, THEY'LL END UP BACK IN THEIR CORRECT POSITIONS
			}

			// WE'VE FOUND A BLOCK IN OUR PATH WITHOUT ACTUALLY RUNNING INTO IT VIA INFERENCE
			if (badPath) {
				
				// DEBUGGING STATEMENT
				// System.out.println("WE'VE DISCOVERED A BAD PATH WITHOUT COLLISION.");
				
				plannedPath = mazeRunner.plan(currCell);
				if (plannedPath == null) {
					System.out.println("Maze is unsolvable.\n");
					// System.out.println(mazeRunner.maze.toString());
					return 'F';
				}

				mazeRunner.return_str= "";

				badPath = false;
				continue;
			}

			// OUR PLANNED PATH IS STILL OKAY AS FAR AS WE KNOW IF WE'RE HERE
			// ATTEMPT TO EXECUTE EXACTLY ONE CELL MOVEMENT
			mazeRunner.cellsProcessed++;
			if (!mazeRunner.canMove(currCell, plannedPath)) {
				// WE'VE FOUND / HIT A BLOCK
				CellInfo obstruction = plannedPath.peekFirst();
				obstruction.setBlocked();
				obstruction.setVisited();
				mazeRunner.collisions++;
				
				// DEBUGGING STATEMENT
				// System.out.println("We've hit a block at coordinate " + obstruction.getPos().toString());
				
				//mazeRunner.infer(obstruction); // WE CAN UPDATE OUR KNOWLEDGE BASE 
				// WE CAN'T SENSE THOUGH SINCE WE CAN'T OCCUPY THAT CELL

				// AND WE NEED TO REPLAN AS WELL
				plannedPath = mazeRunner.plan(currCell);
				if (plannedPath == null) {
					System.out.println("Maze is unsolvable.\n");
					// System.out.println(mazeRunner.maze.toString());
					return 'F';
				}
				continue;
			}

			//mazeRunner.writeMatrix(filename, mazeRunner.maze, mazeRunner.direction);//, mazeRunner.maze);

			mazeRunner.addMatrix(mazeRunner.maze, mazeRunner.direction, currCell.getPos());

			//mazeRunner.writeAction(filename, mazeRunner.direction);

			prev_y = currCell.getPos().getY();
			prev_x = currCell.getPos().getX();

			
			
			// IF WE HAVE SUCCESSFULLY MOVED TO ANOTHER CELL, WE UPDATE THE TRAJECTORY LENGTH
			mazeRunner.trajectoryLength++;


		}
		
		// IF WE BREAK FROM THE LOOP (AKA WE'RE HERE AND HAVEN'T RETURNED YET), WE KNOW WE FOUND THE GOAL.
		long end = System.nanoTime();
		mazeRunner.runtime = end - begin;		
		
		CellInfo ptr = mazeRunner.maze.getCell(mazeRunner.cols - 1, mazeRunner.rows - 1);
		while (ptr.getPos().getX() != 0 || ptr.getPos().getY() != 0) {
			
			// DEBUGGING STATEMENT
			// System.out.println("Currently backtracking and at " + ptr.getPos().toString());
			
			// THE WHOLE PURPOSE OF THE FOLLOWING SEVERAL LINES IS TO MAKE SURE WE'RE COMPUTING THE SHORTEST PATH CORRECTLY
				// WE MAY HAVE SITUATIONS WHERE WE RUN INTO A BLOCK AND SWERVE AROUND IT WITHOUT REALIZING THAT
				// IN THE COMPUTATION OF THE SHORTEST PATH, WE COULD'VE AVOIDED THAT DETOUR
			// THIS JUST LOOKS FOR IMMEDIATE NEIGHBORS FARTHER BACK IN THE PARENT CHAIN
			// AND IF WE HAVE A HIT, THEN WE KNOW WE HAVE AN EVEN SHORTER PATH THAN WE THOUGHT AND WE CHANGE THE POINTER
			// OF THE CURRENT CELL WE'RE AT DURING THE BACKTRACKING
			CellInfo temp = ptr.getParent();
			if (temp.getPos().getX() == 0 && temp.getPos().getY() == 0) {
				ptr.setOnShortestPath(); // FOR PRINTING PURPOSES DURING DEBUGGING
				mazeRunner.shortestPathFound++;
				break;
			}
			temp = temp.getParent(); // THIS IS TO INSURE WE ARE PAST THE MOST IMMEDIATE NEIGHBOR OF THE CURRENT CELL
			// OTHERWISE THIS WOULD BE A FRUITLESS VENTURE
			
			int x = (int)ptr.getPos().getX();
			int y = (int)ptr.getPos().getY();
			while (temp.getPos().getX() != 0 || temp.getPos().getY() != 0) {
				int x2 = (int)temp.getPos().getX();
				int y2 = (int)temp.getPos().getY();
				
				if ((Math.abs(x - x2) == 1 && y - y2 == 0) || (Math.abs(y - y2) == 1) && x - x2 == 0) {
					// DEBUGGING STATEMENT
					// System.out.println("We've determined that " + temp.getPos().toString() + " is a closer neighbor.");
					ptr.setParent(temp);
					break;
				} else {
					temp = temp.getParent();
				}
			}

			if (temp.getPos().getX() == 0 && temp.getPos().getY() == 0) {
				if ((x == 1 && y == 0) || (y == 1) && x == 0) {
					ptr.setParent(temp);
				}
			}
			
			ptr.setOnShortestPath(); // FOR PRINTING PURPOSES DURING DEBUGGING
			mazeRunner.shortestPathFound++;
			ptr = ptr.getParent(); // FOLLOW THE PARENT CHAIN BACK UP UNTIL THE START CELL
		}

		System.out.println("Path Found!");

		mazeRunner.writeMatrix(filename);

		// System.out.println(mazeRunner.maze.toString());
		mazeRunner.printStats();


		return 'S';
	}
	
	// DRIVER METHOD
	public static void main(String args[]) {
		
		// ROWS, COLUMNS, DENSITY OF BLOCKED CELLS AND THE NUMBER OF SUCCESSFUL PATHS FOUND
			// ALL READ IN AS COMMAND LINE ARGUMENTS
		int rowNum = Integer.parseInt(args[0]);
		int colNum = Integer.parseInt(args[1]);
		double prob = Double.parseDouble(args[2]);
		int successfulTrials = Integer.parseInt(args[3]);

		int maze_num = 1;

		File filename = new File("proto50.txt");

		

		try{
			//File filename = new File("matrix.txt");
			if(filename.createNewFile()) System.out.println("File create: " + filename.getName());
			else System.out.println("File already exists.");
		} catch (IOException e){
			System.out.println("ERROR.");
			e.printStackTrace();
		}

		while (successfulTrials > 0) {
			char result = run(rowNum, colNum, prob, filename);
			if (result == 'S') {
				System.out.println("Maze num: "+ maze_num);
				maze_num += 1;
				successfulTrials--;
			}
		}
		
		return;
		
	}

}