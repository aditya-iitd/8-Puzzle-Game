#!/usr/bin/env python3
"""
8-Puzzle Random Move Generator

This module provides functionality for generating random 8-puzzle configurations
and testing puzzle solutions. It can operate in two modes:
1. Random move generation: Creates random puzzles by performing random moves
2. Format checking: Validates puzzle solutions (same as formatChecker.py)

Usage:
    python randomMoves.py number_of_moves  (for random move generation)
    python randomMoves.py input_file output_file  (for format checking)
"""

import random
import sys

def build_random_grid():
    """
    Creates a random 8-puzzle configuration by shuffling tiles 0-8.
    
    Returns:
        list: A list of integers 0-8 representing a shuffled puzzle state,
              where 0 represents the blank tile.
    """
    grid = [0, 1, 2, 3, 4, 5, 6, 7, 8]
    random.shuffle(grid)
    return grid

def try_up(grid): 
    """Check if blank tile can move up (blank not in top row)."""
    return 0 not in grid[:3]
    
def try_down(grid): 
    """Check if blank tile can move down (blank not in bottom row)."""
    return 0 not in grid[6:]
    
def try_left(grid): 
    """Check if blank tile can move left (blank not in leftmost column)."""
    return 0 not in grid[0::3]
    
def try_right(grid): 
    """Check if blank tile can move right (blank not in rightmost column)."""
    return 0 not in grid[2::3]

def swap(grid, zpos, other, char):
    """
    Swaps the blank tile with another tile and returns move notation.
    
    Args:
        grid (list): Current puzzle state
        zpos (int): Position index of the blank tile
        other (int): Position index of the tile to swap with
        char (str): Direction character (U/D/L/R) for move notation
        
    Returns:
        str: Move notation combining the moved tile number and direction
    """ 
    grid[zpos], grid[other] = grid[other], grid[zpos] 
    return str(grid[zpos]) + char

def move_up(grid, zpos): 
    """Move blank tile up by swapping with tile above."""
    return swap(grid, zpos, zpos - 3, 'D')
    
def move_down(grid, zpos): 
    """Move blank tile down by swapping with tile below."""
    return swap(grid, zpos, zpos + 3, 'U')
    
def move_left(grid, zpos): 
    """Move blank tile left by swapping with tile to the left."""
    return swap(grid, zpos, zpos - 1, 'R')
    
def move_right(grid, zpos): 
    """Move blank tile right by swapping with tile to the right."""
    return swap(grid, zpos, zpos + 1, 'L')

def perform_random_move(grid, move=None):
    """
    Performs a random valid move on the puzzle grid.
    
    Args:
        grid (list): Current puzzle state (modified in-place)
        move: Unused parameter (kept for interface compatibility)
        
    Returns:
        tuple: (modified_grid, move_notation)
            - modified_grid: The grid after performing the move
            - move_notation: String describing the move (e.g., "5U")
    """
    zpos = grid.index(0)
    move_functions = [move_up, move_down, move_left, move_right]
    test_functions = [try_up, try_down, try_left, try_right]
    valid_functions = [move_functions[i] for i in [0, 1, 2, 3] if test_functions[i](grid)]
    randnum = random.randint(0, len(valid_functions) - 1)
    return grid, valid_functions[randnum](grid, zpos) 

def build_state_string(grid):
    """
    Converts a puzzle grid to its string representation.
    
    Args:
        grid (list): Puzzle state as list of integers
        
    Returns:
        str: String representation where numbers 1-8 remain as digits
             and 0 (blank) is represented as 'G'
    """
    return ''.join([str(key) if key > 0 else 'G' for key in grid])

def verify_path(start_str, end_str, moves):
    """
    Verifies that a sequence of moves correctly transforms start state to end state.
    
    Args:
        start_str (str): Initial puzzle configuration
        end_str (str): Target puzzle configuration  
        moves (list): List of move strings (e.g., ["5U", "3L"])
        
    Exits program with error message if path is invalid.
    """
    def build_board_from_string(string):
        """
        Converts string representation back to integer grid.
        
        Args:
            string (str): String representation of puzzle state
            
        Returns:
            list: Integer list where 'G' becomes 0, digits remain as integers
        """
        return [int(char) if char != 'G' else 0 for char in string]

    def perform_move(grid, move):
        """
        Applies a single move to the grid and validates its correctness.
        
        Args:
            grid (list): Current puzzle state (modified in-place)
            move (str): Move string (e.g., "5U" means tile 5 moves up)
            
        Exits program with error if move is invalid.
        """
        key, direction = int(move[0]), move[1]
        check_function = {'D': try_up, 'U': try_down, 'R': try_left, 'L': try_right}
        move_functions = {'D': move_up, 'U': move_down, 'R': move_left, 'L': move_right}
        allowed_functions = { motion: move_functions[motion] for motion in 'ULDR' if check_function[motion] } 
        state_string = build_state_string(grid)

        if direction not in allowed_functions:
            print("DIRECTION ERROR: Can't perform the move {} on {}".format(move, build_state_string(grid)))
            exit(0)
        if move != allowed_functions[direction](grid, grid.index(0)):
            print("POSITION ERROR: Can't perform the move {} on {}".format(move, state_string))
            exit(0)

    grid = build_board_from_string(start_str)
    for move in moves:
        perform_move(grid, move)
    final_str = build_state_string(grid)
    if final_str != end_str:
        print("FINAL DESTINATION ERROR: After performing the moves, you reach {}, instead of {}".format(final_str, end_str))
        exit(0)

def verify_cost(output_cost, path, cost_fn):
    """
    Verifies that the reported cost matches the sum of individual move costs.
    
    Args:
        output_cost (int): Cost reported in the solution output
        path (list): List of move strings
        cost_fn (dict): Mapping from tile numbers to their movement costs
        
    Exits program with error if costs don't match.
    """
    correct_cost = sum([cost_fn[int(move[0])] for move in path])
    if correct_cost != output_cost:
        print("COST ERROR: Cost of the output path is {}, but you reported {}".format(correct_cost, output_cost))
        exit(0)


def verify_pathlen(pathlen, path):
    """
    Verifies that the reported path length matches the actual number of moves.
    
    Args:
        pathlen (int): Reported number of moves
        path (list): Actual list of moves
        
    Exits program with error if lengths don't match.
    """
    if pathlen != len(path): 
        print("PATH LENGTH ERROR: reported #moves is {} but {} moves found.".format(pathlen, len(path))) 
        exit(0)

def handle_inconsistent(pathlen, cost, path):
    """
    Handles the case where a puzzle is marked as unsolvable.
    Verifies that all values are consistently set to -1 for unsolvable cases.
    
    Args:
        pathlen (int): Reported path length
        cost (int): Reported cost
        path (list): Reported move sequence
        
    Returns:
        bool: True if inconsistency handling is correct
        
    Exits program with error if inconsistent marking is found.
    """
    if not (pathlen == -1 and cost == -1 and path == []):
        print("NOT REACHABLE CASE: Presentation error in while marking it as not reachable.")
        exit(0)
    return True

def verify_solution(input_start_str, input_end_str, input_cost_fn, output_pathlen, output_cost, output_path):
    """
    Comprehensive verification of a puzzle solution.
    
    Args:
        input_start_str (str): Starting puzzle configuration
        input_end_str (str): Target puzzle configuration
        input_cost_fn (dict): Movement cost function for each tile
        output_pathlen (int): Reported number of moves in solution
        output_cost (int): Reported total cost of solution
        output_path (list): Sequence of moves in the solution
        
    Performs all validation checks and exits with error if any check fails.
    """
    if output_pathlen < 0 or output_cost < 0:
        if handle_inconsistent(output_pathlen, output_cost, output_path): return
    verify_pathlen(output_pathlen, output_path)
    verify_path(input_start_str, input_end_str, output_path)
    verify_cost(output_cost, output_path, input_cost_fn)


def parse_input_testcase(line1, line2, t):
    """
    Parses a single test case from input file format.
    
    Args:
        line1 (str): Line containing start and end puzzle configurations
        line2 (str): Line containing movement costs for tiles 1-8
        t (int): Test case number (for error reporting)
        
    Returns:
        tuple: (start_str, end_str, cost_function_dict)
    """
    start_str, end_str = [string.strip() for string in line1.split()]
    costfn = {position + 1: int(number.strip()) for position, number in enumerate(line2.split())}
    if len(costfn) != 8 or len(start_str) != 9 or len(end_str) != 9:
        print("INPUT ERROR : for testcase {} ".format(t))
    return start_str, end_str, costfn

def parse_inputfile(filename):
    """
    Parses the complete input file containing multiple test cases.
    
    Args:
        filename (str): Path to input file
        
    Returns:
        tuple: (number_of_testcases, list_of_parsed_testcases)
    """
    with open(filename) as f:
        inputs = [line.strip() for line in list(f)]
    T = int(inputs[0])
    inputs = [parse_input_testcase(inputs[2*i + 1], inputs[2*i + 2], 1 + i) for i in range(T)]
    return T, inputs

def parse_output_testcase(line1, line2, t):
    """
    Parses a single test case solution from output file format.
    
    Args:
        line1 (str): Line containing path length and total cost
        line2 (str): Line containing space-separated move sequence
        t (int): Test case number (for error reporting)
        
    Returns:
        tuple: (path_length, total_cost, list_of_moves)
    """
    pathlen, cost = [int(token.strip()) for token in line1.split()]
    moves = [token.strip() for token in line2.split()]
    return pathlen, cost, moves

def parse_outputfile(filename, T):
    """
    Parses the complete output file containing solutions for all test cases.
    
    Args:
        filename (str): Path to output file
        T (int): Expected number of test cases
        
    Returns:
        list: List of parsed output test cases
    """
    with open(filename) as f:
        lines = [line.strip() for line in list(f)]
    lines = lines[0: 2 * T]
    outputs = [parse_output_testcase(lines[2 * i], lines[2 * i + 1], i+1) for i in range(T)]
    return outputs

def format_checker(inputfile, outputfile):
    """
    Main function that performs format checking and solution validation.
    
    Args:
        inputfile (str): Path to input file with test cases
        outputfile (str): Path to output file with solutions
        
    Validates each test case solution against its input requirements.
    """
    T, inputs = parse_inputfile(inputfile)
    outputs = parse_outputfile(outputfile, T)

    for testcase in range(T):
        print("Starting to verify testcase {}".format(testcase))
        start_str, end_str, costfn = inputs[testcase]
        pathlen, cost, moves = outputs[testcase]
        verify_solution(start_str, end_str, costfn, pathlen, cost, moves)
        print("...Done")

def random_moves(moves):
    """
    Generates a random puzzle configuration by performing specified number of random moves.
    This is the primary function of this module when used in RANDOM_MOVES mode.
    
    Args:
        moves (int): Number of random moves to perform from initial random state
        
    Prints:
        - Line 1: start_state end_state (both as 9-character strings)
        - Line 2: sequence of moves performed (space-separated)
    """
    grid = build_random_grid()
    start_state = build_state_string(grid)
    path, move = [], None
    for _ in range(moves):
        grid, move = perform_random_move(grid, move)
        path.append(move)
    end_state = build_state_string(grid)
    print('{} {}'.format(start_state, end_state))
    print(' '.join(path))


# Initialize random seed for reproducible randomness
random.seed(None)

# Configuration: This module primarily focuses on random move generation
# but includes format checking functionality for completeness
# MODE = "FORMAT_CHECKER"
MODE = "RANDOM_MOVES"

if __name__ == '__main__':
    """
    Main execution block.
    
    Command line usage:
    - Random moves mode (default): python randomMoves.py number_of_moves
    - Format checking mode: python randomMoves.py input_file output_file
    
    The random moves mode generates puzzle configurations for testing purposes.
    """
    if MODE == "FORMAT_CHECKER":
        # Alternative format checking functionality
        format_checker(sys.argv[1], sys.argv[2])
    else:
        # Primary function: generate random puzzle configurations
        random_moves(int(sys.argv[1]))
