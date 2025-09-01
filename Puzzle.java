
import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * Represents a position/coordinate in the 3x3 puzzle grid.
 * Used to track tile positions and the blank space location.
 */
class Tile{
	/** X coordinate (row) in the 3x3 grid */
	public int x;
	/** Y coordinate (column) in the 3x3 grid */
	public int y;

	/**
	 * Creates a new Tile with the specified coordinates.
	 * @param x Row position in the 3x3 grid (0-2)
	 * @param y Column position in the 3x3 grid (0-2)
	 */
	public Tile(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}

/**
 * Represents an 8-puzzle state and provides solving functionality.
 * The 8-puzzle is a sliding puzzle consisting of a 3x3 grid with 8 numbered tiles and one blank space.
 * The goal is to arrange the tiles in order by sliding them into the blank space.
 */
class puzzle8
{
    /** Dimension of the puzzle grid (3x3) */
    public  static int DIMS=3;
	/** 2D array representing the current puzzle state */
	public int[][] arr;
	/** Width for display formatting */
	public int width;
	/** Position of the blank tile (represented as 0) */
	public Tile blank;
    /** Hash map for tracking puzzle states and their costs */
    public static HashMap<puzzle8,Integer> hash;
    /** Array of movement costs for each numbered tile (1-8) */
    public static int d[];
	/** Target puzzle configuration (solved state) */
	public  static puzzle8 answer=new puzzle8();
    /** Total cost of moves to reach this state */
    public int total_cost = 0;


	/**
	 * Copy constructor - creates a deep copy of another puzzle8 instance.
	 * @param toClone The puzzle8 instance to copy
	 */
	public puzzle8(puzzle8 toClone) {
		this();
		for(Tile p: allTilePos()) {
			arr[p.x][p.y] = toClone.Tile(p);
		}
		blank = toClone.getBlank();
	}

	/**
	 * Returns a list of all possible tile positions in the 3x3 grid.
	 * @return List containing Tile objects for all 9 positions (0,0) to (2,2)
	 */
	public List<Tile> allTilePos() {
		ArrayList<Tile> out = new ArrayList<Tile>();
		for(int i=0; i<DIMS; i++) {
			for(int j=0; j<DIMS; j++) {
				out.add(new Tile(i,j));
			}
		}
		return out;
	}


    	/**
    	 * Default constructor - creates the solved puzzle state.
    	 * Numbers 1-8 are arranged in order with the blank (0) in the bottom-right corner.
    	 */
    	public puzzle8() {
    		arr = new int[DIMS][DIMS];
    		int cnt=1;
    		for(int i=0; i<DIMS; i++) {
    			for(int j=0; j<DIMS; j++) {
    				arr[i][j]=cnt;
    				cnt++;
    			}
    		}
    		width=Integer.toString(cnt).length();
    		blank = new Tile(DIMS-1,DIMS-1);
    		arr[blank.x][blank.y]=0;
    	}


	/**
	 * Gets the value at the specified tile position.
	 * @param p The tile position to query
	 * @return The number at that position (0 for blank, 1-8 for numbered tiles)
	 */
	public int Tile(Tile p) {
		return arr[p.x][p.y];
	}

	/**
	 * Checks if two puzzle states are identical.
	 * @param o Object to compare with
	 * @return true if both puzzles have the same tile arrangement
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof puzzle8) {
			for(Tile p: allTilePos()) {
				if( this.Tile(p) != ((puzzle8) o).Tile(p)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns all valid move positions for the blank tile.
	 * A move is valid if it stays within grid bounds and moves exactly one position (up/down/left/right).
	 * @return List of tile positions that the blank can move to
	 */
	public List<Tile> allValidMoves() {
		ArrayList<Tile> out = new ArrayList<Tile>();
		int[] dx = {-1, 1, 0, 0};
		int[] dy = {0, 0, -1, 1};
		for(int i = 0; i < 4; i++) {
			Tile tp = new Tile(blank.x + dx[i], blank.y + dy[i]);
			if( isValid(tp) ) {
				out.add(tp);
			}
		}
		return out;
	}


    	/**
    	 * Returns the current position of the blank tile.
    	 * @return Tile object representing the blank's coordinates
    	 */
    	public Tile getBlank() {
    		return blank;
    	}


	/**
	 * Checks if moving the blank tile to position p is a valid move.
	 * @param p Target position for the blank tile
	 * @return true if the move is valid (within bounds, adjacent to current blank position, not already blank)
	 */
	public boolean isValid(Tile p) {
		if( ( p.x < 0) || (p.x >= DIMS) ) {
			return false;
		}
		if( ( p.y < 0) || (p.y >= DIMS) ) {
			return false;
		}
		int dx = blank.x - p.x;
		int dy = blank.y - p.y;
		if( (Math.abs(dx) + Math.abs(dy) != 1 ) || (dx*dy != 0) ) {
			return false;
		}
        if(arr[p.x][p.y]==0){
            return false;
        }
		return true;
	}


	/**
	 * Creates a new puzzle state by moving the blank tile to position p.
	 * @param p Target position for the blank tile
	 * @return New puzzle8 instance with the move applied
	 */
	public puzzle8 clone(Tile p) {
		puzzle8 out = new puzzle8(this);
		out.arr[blank.x][blank.y] = out.arr[p.x][p.y];
		out.arr[p.x][p.y]=0;
		out.blank = p;

        return out;
	}



	/**
	 * Generates all possible adjacent puzzle states by making valid moves.
	 * Also updates the hash map with movement costs for each generated state.
	 * @return List of puzzle states reachable in one move
	 */
	public List<puzzle8> adjPuzzles() {
        hash = new HashMap<puzzle8,Integer>();
		ArrayList<puzzle8> out = new ArrayList<puzzle8>();
		for( Tile move: allValidMoves() ) {
            puzzle8 puz = clone(move);
            if(!hash.containsKey(puz)){
                hash.put(puz, d[arr[move.x][move.y]-1]);
            }
            else{
                int m = hash.get(puz);
                hash.put(puz,m+d[arr[move.x][move.y]-1]);
            }

			out.add(puz);
		}
		return out;
	}



    /**
     * Generates a hash code for this puzzle state.
     * Uses base-9 representation of the tile arrangement.
     * @return Integer hash code uniquely identifying this puzzle state
     */
    @Override
	public int hashCode() {
    	int out=0;
    	for(Tile p: allTilePos()) {
    		out= (out*DIMS*DIMS) + this.Tile(p);
    	}
    	return out;
    }


	/**
	 * Checks if the current puzzle state matches the target solution.
	 * @return true if all numbered tiles are in their correct positions
	 */
	public boolean isSolved() {
        int checker=0;
		for(int i=0; i<DIMS; i++) {
			for(int j=0; j<DIMS; j++) {
				if( (arr[i][j] >0) && ( arr[i][j] != answer.arr[i][j] ) ){
					checker++;
				}
			}
		}
        return checker==0;
	}



    /**
     * Solves the 8-puzzle using Dijkstra's algorithm with custom movement costs.
     * Uses a priority queue to explore states with lowest cost first.
     * @return List of puzzle states representing the solution path, or null if no solution exists
     */
    public List<puzzle8> Solve() {
	  	HashMap<puzzle8,puzzle8> prevList = new HashMap<puzzle8,puzzle8>();
	  	HashMap<puzzle8,Integer> score = new HashMap<puzzle8,Integer>();
	  	Comparator<puzzle8> comparator = new Comparator<puzzle8>() {
            @Override
            public int compare(puzzle8 a, puzzle8 b) {
				if(score.get(a)==score.get(b)){
					return 0;
				}
				else if(score.get(a)-score.get(b)>0){
					return 1;
				}
				else{
					return -1;
				}
	  		}
	  	};
	  	PriorityQueue<puzzle8> toVisit = new PriorityQueue<puzzle8>(10000,comparator);

	  	prevList.put(this, null);
	  	score.put(this, 0);
	  	toVisit.add(this);
	  	while( toVisit.size() > 0) {
	  		puzzle8 puzz = toVisit.remove();
	  		if( puzz.isSolved() ) {
	  			LinkedList<puzzle8> answer = new LinkedList<puzzle8>();
	  			puzzle8 prev=puzz;
	  			while( prev != null ) {
	  				answer.addFirst(prev);
	  				prev = prevList.get(prev);
	  			}
	  			return answer;
	  		}
	  		for(puzzle8 fp: puzz.adjPuzzles()) {
	  			if( !prevList.containsKey(fp) ) {
	  				prevList.put(fp,puzz);
					score.put(fp, puzzle8.hash.get(fp));
	  				toVisit.add(fp);
	  			}
	  		}
	  	}
	  	return null;
	}

    /**
     * Determines the move that transforms puzzle state 'a' into puzzle state 'b'.
     * @param a Starting puzzle state
     * @param b Ending puzzle state (after one move)
     * @return String representation of the move (e.g., "5U" means tile 5 moved up)
     */
    public static String getDif(puzzle8 a, puzzle8 b)
    {

        int ax = a.blank.x;
        int ay = a.blank.y;
        int bx = b.blank.x;
        int by = b.blank.y;

        if(ax-bx>0 && ay==by){
            return b.arr[ax][ay]+"U";
        }
        if(ax-bx<0 && ay==by){
            return b.arr[ax][ay]+"D";
        }
        if(ay-by>0 && ax==bx){
            return b.arr[ax][ay]+"L";
        }
        if(ay-by<0 && ax==bx){
            return b.arr[ax][ay]+"R";
        }

        else return "Error!";
    }

    /**
     * Converts a solution path into a sequence of move strings.
     * @param solution List of puzzle states representing the solution path
     * @return Vector of move strings describing how to solve the puzzle
     */
    public static Vector<String> getOutput(List<puzzle8> solution) {
        Vector<String> vec = new Vector<String>();
        int ch = 0;
        puzzle8 previous = null;
        if (solution != null ) {
            for( puzzle8 sp: solution) {
				if(ch>0){
                    vec.add(getDif(sp,previous));
                    previous = sp;
                }
                else{
                    previous = sp;
                    ch++;
                }
			}
		}
		return vec;
	}
}

/**
 * Main class that handles input/output and coordinates the puzzle solving process.
 * Reads puzzle configurations from input file and writes solutions to output file.
 */
public class Puzzle{

    /**
     * Converts a 9-character string representation of a puzzle state into an integer array.
     * Non-digit characters (representing the blank) are converted to 0.
     * @param str String representation of the puzzle (9 characters)
     * @return Integer array representing the puzzle state
     */
    public static int[] convert(String str)
    {
        int res[] = new int[9];
        char ch[] = str.toCharArray();
        for(int i=0; i<9; i++)
        {
            if(Character.isDigit(ch[i]))
            {
                res[i] = Character.getNumericValue(ch[i]);
            }
            else{
                res[i] = 0;
            }
        }
        return res;
    }

	/**
	 * Main method that processes input file and generates solution output.
	 * Expected input format: number of test cases, followed by start state, end state, and movement costs for each case.
	 * @param args Command line arguments: args[0] = input filename, args[1] = output filename
	 * @throws java.lang.Exception for file I/O errors
	 */
	public static void main (String[] args) throws java.lang.Exception
	{
        Scanner sc = new Scanner(new File(args[0]));
        FileWriter fw=new FileWriter(args[1]);
        int t = sc.nextInt();

        for(int q=0; q<t; q++){
		puzzle8 input = new puzzle8();

		int inmatrix[] = new int[9] ;
		int outmatrix[] = new int[9];
		int z[] = new int[8];
		int count = 0;
        int tot = 0;
        String majors = "";

        String str1 = sc.next();
        String str2 = sc.next();
        inmatrix = convert(str1);
        outmatrix = convert(str2);

        for(int i=0; i<8; i++){
            z[i] = sc.nextInt();
        }

		puzzle8.d = z;

		for(int i=0; i<3; i++)
		{
			for(int j=0; j<3; j++)
			{	input.arr[i][j] = inmatrix[count];
				puzzle8.answer.arr[i][j] = outmatrix[count];
				if(input.arr[i][j]==0)
				{
					input.blank = new Tile(i,j);
				}
				if(puzzle8.answer.arr[i][j]==0)
				{
					puzzle8.answer.blank = new Tile(i,j);
				}
				count++;
			}
		}

        List<puzzle8> puzzle_list = input.Solve();
        Vector<String> final_solution = puzzle8.getOutput(puzzle_list);
        for(int i=0; i<final_solution.size(); i++)
        {
            tot += z[Character.getNumericValue(final_solution.elementAt(i).charAt(0))-1];
            majors += final_solution.elementAt(i)+" ";
        }



        if(puzzle_list!=null){
        if(puzzle_list.size()>1){
            fw.write(final_solution.size()+" ");
            fw.write(tot+"\n");
            fw.write(majors+"\n");
        }
        else if(puzzle_list.size()==1){
            fw.write("0 0\n\n");
        }}
        else{
            fw.write("-1 -1\n\n");
        }

    }
fw.close();

	}

}
