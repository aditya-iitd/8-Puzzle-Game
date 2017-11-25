
import java.util.*;
import java.lang.*;
import java.io.*;

class Tile{
	public int x;
	public int y;

	public Tile(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}

class puzzle8
{
    public  static int DIMS=3;
	public int[][] arr;
	public int width;
	public Tile blank;
    public static HashMap<puzzle8,Integer> hash;
    public static int d[];
	public  static puzzle8 answer=new puzzle8();
    public int total_cost = 0;


	public puzzle8(puzzle8 toClone) {
		this();
		for(Tile p: allTilePos()) {
			arr[p.x][p.y] = toClone.Tile(p);
		}
		blank = toClone.getBlank();
	}

	public List<Tile> allTilePos() {
		ArrayList<Tile> out = new ArrayList<Tile>();
		for(int i=0; i<DIMS; i++) {
			for(int j=0; j<DIMS; j++) {
				out.add(new Tile(i,j));
			}
		}
		return out;
	}


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


	public int Tile(Tile p) {
		return arr[p.x][p.y];
	}

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

	public List<Tile> allValidMoves() {
		ArrayList<Tile> out = new ArrayList<Tile>();
		for(int dx=-1; dx<2; dx++) {
			for(int dy=-1; dy<2; dy++) {
				Tile tp = new Tile(blank.x + dx, blank.y + dy);
				if( isValid(tp) ) {
					out.add(tp);
				}
			}
		}
		return out;
	}


    	public Tile getBlank() {
    		return blank;
    	}


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


	public puzzle8 clone(Tile p) {
		puzzle8 out = new puzzle8(this);
		out.arr[blank.x][blank.y] = out.arr[p.x][p.y];
		out.arr[p.x][p.y]=0;
		out.blank = p;

        return out;
	}



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



    @Override
	public int hashCode() {
    	int out=0;
    	for(Tile p: allTilePos()) {
    		out= (out*DIMS*DIMS) + this.Tile(p);
    	}
    	return out;
    }


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
					return -1;
				}
				else{
					return 1;
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

public class Puzzle{

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
