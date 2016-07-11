import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

/* I pledge on my honor that I have not given or received any unauthorized assistance on this project.
 * Kevin Wood, kwood123@terpmail.umd.edu / kvn.wood@gmail.com
 */

/**
 * @author Kevin Wood
 * @uid 111235445
 * 
 * 
 */
public class FourInARow {

	/* these variables are just hashes and global bests and the time limiter */
	HashMap<Integer, HashMap<String, Integer>> adj = new HashMap<Integer, HashMap<String, Integer>>();
	HashMap<Integer, HashMap<Integer, Integer>> dist = new HashMap<Integer, HashMap<Integer, Integer>>();
	String[] dirs = new String[]{"NE", "E", "SE", "SW", "W", "NW"};
	long time_limit = 1000;
	long startTime;
	static ArrayList<Integer> original_Agent1;
	ArrayList<Integer> best = new ArrayList<Integer>();
	double best_v = Integer.MIN_VALUE;

	/* Initialize the class object and distance hash and adjacency hash, this is done in the constructor.
	 * The adjacency hash has 6 directions for each cell, listing which ones border it in the game table.
	 * The purpose of the hash is to allow instant access to the cells adjacent to any given cell, without having to do arithmetic or checks
	 * to see where the cell is located on the table.
	 * 
	 * The distance hash contains the distance from a given cell to every other cell in the game.
	 * 	example = cell 5 in the hash also contains a hash, in which every cell is mapped to an integer. h.get(5).get(25) = 2 
	 *  distance from 5 to 25 is 2.
	 * This is used in the evaluation of the game state, and the eval function is called many times which saves time in the long run.
	 */
	public FourInARow(){
		startTime = System.currentTimeMillis();

		/* all this stuff is just initialization and moving stuff into convenient data structures
		 * don't need to pay much attention to this part, it's just separated a lot for readability.
		 */
		int[] nw = new int[]{0,1,2,3,4,5,10,20,30,40,50};
		int[] ne = new int[]{0,1,2,3,4,5,16,27,38,49,60};
		int[] se = new int[]{60,70,80,90,100,105,106,107,108,109,110};
		int[] sw = new int[]{50,61,72,83,94,105,106,107,108,109,110};
		int[] n = new int[]{0,1,2,3,4,5};
		int[] s = new int[]{105,106,107,108,109,110};

		ArrayList<Integer> NW_Border = new ArrayList<Integer>();
		ArrayList<Integer> NE_Border = new ArrayList<Integer>();
		ArrayList<Integer> SE_Border = new ArrayList<Integer>();
		ArrayList<Integer> SW_Border = new ArrayList<Integer>();
		ArrayList<Integer> N_Border = new ArrayList<Integer>();
		ArrayList<Integer> S_Border = new ArrayList<Integer>();

		for(int i = 0; i < 11; i++){
			NW_Border.add(nw[i]);
			NE_Border.add(ne[i]);
			SE_Border.add(se[i]);
			SW_Border.add(sw[i]);
		}

		for(int i = 0; i < 6; i++){
			N_Border.add(n[i]);
			S_Border.add(s[i]);
		}

		String s1 = "NE";
		String s2 = "E";
		String s3 = "SE";
		String s4 = "SW";
		String s5 = "W";
		String s6 = "NW";

		int[] invalid_squares = new int[]{6,7,8,9,17,18,19,28,29,39,71,81,82,91,92,93,101,102,103,104};
		ArrayList<Integer> invalid = new ArrayList<Integer>();
		for(int i = 0;i < invalid_squares.length; i++ ){
			invalid.add(invalid_squares[i]);

		}
		for(int i = 0; i <= 110; i++){
			if(invalid.contains(i))
				continue;
			HashMap<String, Integer> h = new HashMap<String, Integer>();

			h.put(s1, null);
			if(!NE_Border.contains(i))
				h.put(s1, i-10);

			h.put(s2, null);
			if(!NE_Border.contains(i) && !SE_Border.contains(i) || i==0 || i==1 || i==2 || i==3 || i==4
					|| i==105 || i==106 || i==107 || i==108 || i==109)
				h.put(s2, i+1);

			h.put(s3, null);
			if(!SE_Border.contains(i))
				h.put(s3, i+11);

			h.put(s4, null);
			if(!SW_Border.contains(i))
				h.put(s4, i+10);

			h.put(s5, null);
			if(!NW_Border.contains(i) && !SW_Border.contains(i)|| i==1 || i==2 || i==3 || i==4 || i==5
					|| i==106 || i==107 || i==108 || i==109 || i==110)
				h.put(s5, i-1);

			h.put(s6, null);
			if(!NW_Border.contains(i))
				h.put(s6, i-11);

			adj.put(i, h);

		}

		/* distance hash... for every cell do a Breadth-first search east, southeast, southwest, and west. 
		 * Calculate distance from every cell to every other cell.
		 * This is used in the evaluation function, which is called many times. 
		 * 
		 * This distance hash construction takes about 40 ms, but it cuts down on time spent later when trying to evaluate game state
		 * to reduce time overall.
		 */
		for(Integer index = 0; index <= 110; index++){
			if(invalid.contains(index))
				continue;
			int distance = 1;
			Set<Integer> bfs = new HashSet<Integer>();
			bfs.add((Integer)index);

			while(!bfs.isEmpty()){
				ArrayList<Integer> to_add = new ArrayList<Integer>();
				ArrayList<Integer> to_remove = new ArrayList<Integer>();
				for(Integer e : bfs){

					ArrayList<Integer> bordering = new ArrayList<Integer>();
					bordering.add(e+1);
					bordering.add(e+10);
					bordering.add(e+11);
					bordering.add(e-1);

					for(Integer e2: bordering){
						if(invalid.contains(e2) || e2 > 110 || e2 < 0)
							continue;
						if(dist.get(index) == null)
							dist.put(index, new HashMap<Integer, Integer>());

						if(dist.get(index).get(e2) == null){
							dist.get(index).put(e2, distance);
							to_add.add(e2);
						}
					}

					to_remove.add(e);
				}
				bfs.removeAll(to_remove); 
				bfs.addAll(to_add);
				distance+=1;
			}

			/* getting all the cells before current cell index, to put in using already calculated values from BFS
			 * example = putting cell 0 into the hash for 55. This is so we don't have to do a BFS in every direction for every cell.
			 */
			for(Integer i2 = 0; i2 < index; i2++){
				if(invalid.contains((int)i2) || invalid.contains(i2))
					continue;
				if(dist.get(index) == null)
					dist.put(index, new HashMap<Integer, Integer>());
				dist.get(index).put(i2, dist.get(i2).get(index));
			}
		}

	}

	/* this class is used for construction of the game tree during iterative deepening search 
	 * Each node contains Agent 1 (player to move's game pieces_
	 * and Agent 2 (other player's game pieces)
	 * as well as the v (value) for the node
	 * and the descendants for the node
	 */
	public class Node {

		ArrayList<Integer> Agent1, Agent2;
		ArrayList<Node> descendants = new ArrayList<Node>();
		double v;

		public Node( ArrayList<Integer> A1, ArrayList<Integer> A2, double a){
			Agent1 = A1;
			Agent2 = A2;
			v = a;
		}
	}

	/** 
	 * 
	 * 
	 * @param green = pieces for Agent 1
	 * @param red = pieces for Agent 2
	 * @param player = 0 or 1, for current player 
	 * @return h = hashmap of all valid moves for player
	 * 
	 * Returns a  HashMap of all valid moves for the current player
	 * 	each key represents a piece, each element of the value (arraylist) 
	 * 	is a valid spot to move for that piece
	 * 	for example - h[2] = [13,24,35,12,22,32,1] means the piece at 2 can move
	 * 	to any of the game table positions listed in the arraylist.
	 * 
	 *
	 */
	public HashMap<Integer, ArrayList<Integer>> get_valid_moves(ArrayList<Integer> green, ArrayList<Integer> red){
		HashMap<Integer, ArrayList<Integer>> h = new HashMap<Integer, ArrayList<Integer>>();

		ArrayList<Integer> greens = new ArrayList<Integer>();
		ArrayList<Integer> reds = new ArrayList<Integer>();

		for(int tmp = 0; tmp < 4; tmp++){
			greens.add(green.get(tmp));
			reds.add(red.get(tmp));
		}

		for(int i = 0; i < 4; i++){
			Integer current_cell = green.get(i);
			for(int dir = 0; dir < 6; dir++){
				String current_dir = dirs[dir];
				Integer target_cell = adj.get(green.get(i)).get(current_dir);
				for(int j = 0; j < 3; j++){

					if(target_cell != null && !greens.contains(target_cell) && !reds.contains(target_cell)){
						if(h.get(current_cell) == null)
							h.put(current_cell,new ArrayList<Integer>());

						h.get(current_cell).add(target_cell);
					} else{
						break;
					}
					target_cell = adj.get(target_cell).get(current_dir);
				}	
			}
		}

		return h;

	}


	/**
	 * @param args = args[0] = name of input file
	 * Read in input from input file, put into ArrayLists and pass them to function that calculates best move. 
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

		FourInARow obj = new FourInARow();

		ArrayList<Integer> Agent1 = new ArrayList<Integer>(4);
		ArrayList<Integer> Agent2 = new ArrayList<Integer>(4);

		/* read in the input, need try catch block because java
		 * put important stuff in ArrayLists which are passed to move function.
		 */
		try {
			FileReader freader = new FileReader(args[0]);
			BufferedReader bReader = new BufferedReader(freader);
			String[] green_arr = bReader.readLine().split(" ");
			String[] red_arr = bReader.readLine().split(" ");
			for(int i = 0; i < 4; i++){
				Agent1.add(Integer.parseInt(green_arr[i]));
				Agent2.add(Integer.parseInt(red_arr[i]));
			}
			original_Agent1 = Agent1;
			freader.close();
			bReader.close();

		} catch (Exception e) {

			e.printStackTrace();
		}

		// find best move
		obj.move(Agent1, Agent2);

	}

	/**
	 * @param Agent1 = Agent 1's board pieces
	 * @param Agent2 = Agent 2's board pieces
	 * This function starts the process of iterative deepening, which continues until the time is nearly out.
	 * At that point, the exit function is called and global best is written to output file.
	 * 
	 */
	public void move(ArrayList<Integer> Agent1, ArrayList<Integer> Agent2) 
			throws FileNotFoundException, UnsupportedEncodingException{

		Collections.sort(Agent1);
		Collections.sort(Agent2);

		/* create game tree with depth limit iteratively incrementing by 1 */
		for(int depth_limit = 1; depth_limit < Integer.MAX_VALUE; depth_limit++){

			Node root = new Node(Agent1, Agent2, 0);
			alpha_beta(Agent1, Agent2, Integer.MIN_VALUE, Integer.MAX_VALUE, root,  0, 0, depth_limit);

		}

		// this should never be reached, but just in case it is then exit.
		exit_function(original_Agent1);

	}

	/**
	 * @param Agent1 = Agent 1's initial (input) game pieces
	 * @param Agent2 = Agent 2's initial (input) game pieces
	 * @param alpha = alpha value (for calculation of beta cutoffs)
	 * @param beta = beta value (for calculation of alpha cutoffs)
	 * @param root = root of current subtree, has current game state
	 * @param player = which player is to move (max = 0 or min = 1), alternates each level of tree
	 * @param depth = current depth of search in the tree
	 * @param limit = depth limit for current iteration of tree construction/search
	 * @return value of current node, calculated after pruning and looking at descendants. 
	 * 
	 * This function does the alpha beta pruning of the tree rooted at root(parameter) with total depth of limit(parameter),
	 * and current depth of depth(parameter).
	 *  
	 * alpha and beta pruning done with parameters, updated after searching all descendants of current node.
	 * 
	 */

	public double alpha_beta(ArrayList<Integer> Agent1, ArrayList<Integer> Agent2, double alpha, double beta, 
			Node root, int player, int depth, int limit) throws FileNotFoundException, UnsupportedEncodingException{

		/* if there are 15 milliseconds or less left (just for safety), call exit function */
		if(System.currentTimeMillis() - startTime > time_limit - 15){
			exit_function(original_Agent1);
		}

		HashMap<Integer, ArrayList<Integer>> h = get_valid_moves(root.Agent1, root.Agent2);
		if(player == 1)
			h = get_valid_moves(root.Agent2, root.Agent1);


		if(depth == limit || limit == 0){
			/* return the value of board state, don't construct any more subtrees from here (this is a leaf node) */
			return evaluate(root.Agent1, root.Agent2);
		}
		else if (player == 0){
			root.v = Integer.MIN_VALUE;

			root.descendants = create_and_sort_descendants(root.Agent1, root.Agent2, player, h, root);

			for(Node n: root.descendants){

				// the player^1 argument is just to flip the player, 0->1 or 1->0
				root.v = Math.max(root.v, alpha_beta(n.Agent1, n.Agent2, alpha, beta, n, player^1, depth+1, limit));

				// if current descendant's value is better than all previously calculated ones, this one is the best move
				if(depth == 0){
					if(best_v < root.v){
						best_v = root.v;
						best = n.Agent1;
					}
				}

				// beta cutoff..
				if(root.v >= beta){
					return root.v;
				} else{
					alpha = Math.max(alpha, root.v);
				}
			}
		}

		else if(player == 1){
			root.v = Integer.MAX_VALUE;

			root.descendants = create_and_sort_descendants(root.Agent1, root.Agent2, player, h, root);

			for(Node n: root.descendants){

				// the player^1 argument is just to flip the player, 0->1 or 1->0
				root.v = Math.min(root.v, alpha_beta(n.Agent1, n.Agent2, alpha, beta, n, player^1, depth+1, limit));

				// alpha cutoff..
				if(root.v <= alpha){
					return root.v;
				} else{
					beta = Math.min(beta, root.v);
				}
			}
		}

		return root.v;
	}


	/**
	 * @param Agent1 = Agent 1's game pieces
	 * @param Agent2 = Agent 2's game pieces
	 * @param player = player that is moving
	 * @param h = hash of valid moves for the player (Integer->Array of Integers)
	 * @param root = node that descendants are being created for
	 * @return = ArrayList of nodes that are descendants of the current node, in sorted (descending) order if P1
	 * 			 or ascending order if P2's move
	 * 
	 * This is used to construct the descendants of the current node, for use in construction and evaluation of the 
	 * game tree. Also sorts the descendants according to the current player to move, for the purposes of being able to quickly prune
	 * away the poor choices. (Move ordering)
	 * 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Node> create_and_sort_descendants(ArrayList<Integer> Agent1, ArrayList<Integer> Agent2, int player,
			HashMap<Integer, ArrayList<Integer>> h, Node root) throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<Node> ret = new ArrayList<Node>();

		/* if there are 15 milliseconds or less left (just for safety), call exit function */
		if(System.currentTimeMillis() - startTime > time_limit - 15){
			exit_function(original_Agent1);
		}

		for(Integer piece : h.keySet()){
			ArrayList<Integer> moves = h.get(piece);

			for(int i = 0; i < moves.size(); i++){

				/* need to clone to keep original arrays of board pieces unchanged.. */
				ArrayList<Integer> a1_clone =  (ArrayList<Integer>) root.Agent1.clone();
				ArrayList<Integer> a2_clone =  (ArrayList<Integer>) root.Agent2.clone();
				if(player == 0){
					a1_clone.remove(piece);
					a1_clone.add(moves.get(i));
				} else{
					a2_clone.remove(piece);
					a2_clone.add(moves.get(i));
				}

				Node tmp = new Node(a1_clone, a2_clone, player^1);
				ret.add(tmp);

			}
		}

		/* now sort them, based on their evaluation. if Agent 1's turn to move, in descending order (highest eval first) */
		if(player == 0)
			Collections.sort(ret, (a, b) -> evaluate(a.Agent1, a.Agent2) < evaluate(b.Agent1, b.Agent2) ? 1: -1);

		/* flip evaluation function if Agent 2 is currently moving ... because he wants to minimize Agent 1's payoff 
		 * so sorted list will be in ascending order
		 */
		if(player == 1)
			Collections.sort(ret, (a, b) -> evaluate(a.Agent2, a.Agent1) < evaluate(b.Agent2, b.Agent1) ? 1: -1);

		return ret;
	}

	/**
	 * @param Agent1 = Agent 1's game pieces as sorted ArrayList (player to move)
	 * @param Agent2 = Agent 2's game pieces as sorted ArrayList
	 * @return A number determined by how close P1's pieces are and how many are in a row, subtracted 
	 * 		   by that same calculation for P2 (zero-sum game)
	 * 
	 * 		   A positive number indicates advantage for player 1
	 * 		   A negative number indicates advantage for player 2
	 * 		   Any time a piece for a player moves out of the way of interrupting alignment for the other player
	 * 		   is penalized using this calculation.
	 * 
	 * 		   We also only have to check three directions for each cell (directions that make the cell # higher) 
	 * 		   because we have sorted the ArrayLists and know we won't encounter any we haven't
	 * 		   checked before the current cell.
	 * 
	 * 		   Function uses adjacency hash to check nearby cells faster, so we don't have to constantly calculate what cells surround it
	 * 		   and check whether a cell is actually valid or not.
	 * 		   
	 */
	public double evaluate(ArrayList<Integer> Agent1, ArrayList<Integer> Agent2){

		Collections.sort(Agent1);
		Collections.sort(Agent2);
		int p1_aligned = 1, p2_aligned = 1;
		double p1_distance = 0, p2_distance = 0;

		/* calculate average distance between cells for both players */
		for(Integer i = 0; i < 3; i++){
			for(Integer j = i+1; j < 4; j++){
				p1_distance += dist.get((Integer)Agent1.get(i)).get((Integer)Agent1.get(j));
				p2_distance += dist.get((Integer)Agent2.get(i)).get((Integer)Agent2.get(j));
			}
		}

		// averaging the distances, because 4 pieces.
		p1_distance /= 4.0;
		p2_distance /= 4.0;

		/* Agent 1 loop, each cell in each direction. Only 3 directions
		 * because the ArrayLists of pieces are sorted lowest to highest.
		 * Find max # in a row.
		 */
		for(int i = 0; i < 3; i++){
			int cell = Agent1.get(i);
			p1_aligned = Math.max(check_path(Agent1, Agent2, cell, "E"), p1_aligned);
			p1_aligned = Math.max(check_path(Agent1, Agent2, cell, "SE"), p1_aligned);
			p1_aligned = Math.max(check_path(Agent1, Agent2, cell, "SW"), p1_aligned);
		}

		/* same thing for player 2. just separated for readability */
		for(int i = 0; i < 3; i++){
			int cell = Agent2.get(i);
			p2_aligned = Math.max(check_path(Agent2, Agent1, cell, "E"), p2_aligned);
			p2_aligned = Math.max(check_path(Agent2, Agent1, cell, "SE"), p2_aligned);
			p2_aligned = Math.max(check_path(Agent2, Agent1, cell, "SW"), p2_aligned);
		}

		/* if either player is in a winning state, return max integer or minimum integer */
		if(p1_aligned == 4)
			return Integer.MAX_VALUE;

		if(p2_aligned == 4)
			return Integer.MIN_VALUE;

		/* value should get larger as p1 # in a row increases and player 2's pieces are farther away from each other
			and smaller as p2 # in a row increases and player 1's pieces are farther away from each other
			so p1 wants to maximize first half of calculation and minimize second half.
		 */
		return ((p1_aligned*(.2)+1) * p2_distance - ((p2_aligned*(.2)+1) * p1_distance));
	}

	/**
	 * @param Agent1 = Agent 1's board pieces
	 * @param Agent2 = Agent 2's board pieces
	 * @param cell = starting cell for search
	 * @param dir = direction that the current function call is searching
	 * @return = number of Agent 1's pieces in a row starting from cell in direction of string East, SouthEast, or SouthWest
	 * without any of Agent 2's pieces in between.
	 * 
	 * This is used in the evaluation function to check the max number of pieces for Agent1 that are in alignment.
	 */
	public int check_path(ArrayList<Integer> Agent1, ArrayList<Integer> Agent2, int cell, String dir){
		int num_aligned = 1;

		while(adj.get(cell).get(dir) != null){
			cell = adj.get(cell).get(dir);

			if(Agent1.contains(cell))
				num_aligned += 1;

			if(Agent2.contains(cell))
				break;
		}
		return num_aligned;
	}

	/**
	 * @param Agent1 = Agent 1's board pieces from starting input
	 * 
	 * This function is called when the time is nearly out.
	 * Puts the current best move found into the output file (FourInARow.out)
	 */
	public void exit_function(ArrayList<Integer> Agent1) throws FileNotFoundException, UnsupportedEncodingException{
		int[] ret = new int[2];

		for(int i = 0; i < 4; i++){

			// piece that was moved
			if(!best.contains(Agent1.get(i))){
				ret[0] = Agent1.get(i);
			}
		}
		for(int i = 0; i < 4; i++){

			// where it was moved
			if(!Agent1.contains(best.get(i))){
				ret[1] = best.get(i);
			}
		}

		// write output to file
		File out = new File("FourInARow.out");
		PrintWriter w = new PrintWriter(out, "UTF-8");
		w.write(ret[0] + " " + ret[1]);
		
		w.close();

		System.exit(0);
	}

}
