use std::fs::File;
use std::io::{BufRead, BufReader, Write};
use std::collections::{HashSet, HashMap, VecDeque};
use std::cmp::Ordering;

#[derive(Clone, Eq, PartialEq, Hash, Debug)]
struct Tile {
    x: usize,
    y: usize,
}

#[derive(Clone, Eq, PartialEq, Hash, Debug)]
struct Puzzle8 {
    arr: [[u8; 3]; 3],
    blank: Tile,
}

impl Puzzle8 {
    fn from_flat(flat: &[u8; 9]) -> Self {
        let mut arr = [[0; 3]; 3];
        let mut blank = Tile { x: 2, y: 2 };
        for i in 0..3 {
            for j in 0..3 {
                let v = flat[i * 3 + j];
                arr[i][j] = v;
                if v == 0 {
                    blank = Tile { x: i, y: j };
                }
            }
        }
        Puzzle8 { arr, blank }
    }

    fn to_flat(&self) -> [u8; 9] {
        let mut flat = [0u8; 9];
        for i in 0..3 {
            for j in 0..3 {
                flat[i * 3 + j] = self.arr[i][j];
            }
        }
        flat
    }

    fn all_tile_pos() -> Vec<Tile> {
        let mut v = Vec::with_capacity(9);
        for i in 0..3 {
            for j in 0..3 {
                v.push(Tile { x: i, y: j });
            }
        }
        v
    }

    fn is_valid_move(&self, to: &Tile) -> bool {
        if to.x >= 3 || to.y >= 3 {
            return false;
        }
        let dx = (self.blank.x as isize - to.x as isize).abs();
        let dy = (self.blank.y as isize - to.y as isize).abs();
        if (dx + dy) != 1 {
            return false;
        }
        if self.arr[to.x][to.y] == 0 {
            return false;
        }
        true
    }

    fn valid_moves(&self) -> Vec<Tile> {
        let mut moves = Vec::new();
        let dirs = [(-1, 0), (1, 0), (0, -1), (0, 1)];
        for &(dx, dy) in &dirs {
            let nx = self.blank.x as isize + dx;
            let ny = self.blank.y as isize + dy;
            if nx >= 0 && ny >= 0 && nx < 3 && ny < 3 {
                let tile = Tile { x: nx as usize, y: ny as usize };
                if self.is_valid_move(&tile) {
                    moves.push(tile);
                }
            }
        }
        moves
    }

    fn clone_and_move(&self, tile: &Tile) -> Self {
        let mut new_puz = self.clone();
        new_puz.arr[new_puz.blank.x][new_puz.blank.y] = new_puz.arr[tile.x][tile.y];
        new_puz.arr[tile.x][tile.y] = 0;
        new_puz.blank = Tile { x: tile.x, y: tile.y };
        new_puz
    }

    fn is_solved(&self, answer: &Puzzle8) -> bool {
        self.arr == answer.arr
    }

    fn get_diff(a: &Puzzle8, b: &Puzzle8) -> String {
        let ax = a.blank.x;
        let ay = a.blank.y;
        let bx = b.blank.x;
        let by = b.blank.y;
        let moved_val = b.arr[ax][ay];

        if ax > bx && ay == by {
            format!("{}U", moved_val)
        } else if ax < bx && ay == by {
            format!("{}D", moved_val)
        } else if ay > by && ax == bx {
            format!("{}L", moved_val)
        } else if ay < by && ax == bx {
            format!("{}R", moved_val)
        } else {
            "Error!".to_string()
        }
    }
}

// Custom A* puzzle state for priority queue
#[derive(Clone)]
struct State {
    puzzle: Puzzle8,
    cost: i32,
    total_cost: i32,
    parent: Option<Box<State>>,
}

impl State {
    fn new(puzzle: Puzzle8, cost: i32, total_cost: i32, parent: Option<Box<State>>) -> Self {
        Self { puzzle, cost, total_cost, parent }
    }
}

// Reverse ordering for min-heap
impl Ord for State {
    fn cmp(&self, other: &Self) -> Ordering {
        other.total_cost.cmp(&self.total_cost)
            .then_with(|| other.cost.cmp(&self.cost))
    }
}
impl PartialOrd for State {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}
impl PartialEq for State {
    fn eq(&self, other: &Self) -> bool {
        self.puzzle == other.puzzle && self.total_cost == other.total_cost && self.cost == other.cost
    }
}
impl Eq for State {}

pub fn solve_file(input_filename: &str, output_filename: &str) {
    // Parse input
    let file = File::open(input_filename).expect("Cannot open input file");
    let mut rdr = BufReader::new(file);
    let mut line = String::new();

    // First line: number of testcases
    rdr.read_line(&mut line).expect("Failed to read t");
    let t: usize = line.trim().parse().expect("Failed to parse t");
    let mut lines = Vec::new();
    for _ in 0..t {
        let mut l1 = String::new();
        let mut l2 = String::new();
        rdr.read_line(&mut l1).expect("Failed to read line1");
        rdr.read_line(&mut l2).expect("Failed to read line2");
        lines.push(l1.trim().to_string());
        lines.push(l2.trim().to_string());
    }

    let mut fout = File::create(output_filename).expect("Cannot open output file");
    for i in 0..t {
        let idx = 2 * i;
        let grids = lines[idx]
            .split_whitespace()
            .map(|s| s.trim())
            .collect::<Vec<_>>();
        let z: Vec<i32> = lines[idx + 1]
            .split_whitespace()
            .filter_map(|s| s.trim().parse().ok())
            .collect();
        if grids.len() != 2 || z.len() != 8 {
            // Write output consistent with Java: -1 -1\n\n
            writeln!(fout, "-1 -1\n").unwrap();
            continue;
        }
        let in_grid = grids[0];
        let out_grid = grids[1];

        let grid_to_arr = |s: &str| {
            s.chars()
                .map(|c| if c == 'G' { 0 } else { c.to_digit(10).unwrap() as u8 })
                .collect::<Vec<_>>()
                .try_into()
                .unwrap_or([0u8; 9])
        };

        let input = Puzzle8::from_flat(&grid_to_arr(in_grid));
        let answer = Puzzle8::from_flat(&grid_to_arr(out_grid));

        let res = solve_puzzle(&input, &answer, &z);
        match res {
            Some((moves, totcost, majors)) => {
                if moves > 0 {
                    writeln!(fout, "{} {}", moves, totcost).unwrap();
                    writeln!(fout, "{}", majors.trim()).unwrap();
                } else {
                    writeln!(fout, "0 0").unwrap();
                    writeln!(fout).unwrap();
                }
            }
            None => {
                writeln!(fout, "-1 -1").unwrap();
                writeln!(fout).unwrap();
            }
        }
    }
}

fn solve_puzzle(input: &Puzzle8, answer: &Puzzle8, costs: &Vec<i32>) -> Option<(usize, i32, String)> {
    use std::collections::BinaryHeap;

    let mut heap = BinaryHeap::new();
    let mut seen = HashSet::new();
    let mut g_cost = HashMap::new(); // State -> g

    heap.push(State::new(input.clone(), 0, 0, None));
    g_cost.insert(input.clone(), 0);

    while let Some(state) = heap.pop() {
        if seen.contains(&state.puzzle) {
            continue;
        }
        seen.insert(state.puzzle.clone());

        if state.puzzle.is_solved(answer) {
            // Reconstruct moves
            let mut path = vec![state.puzzle.clone()];
            let mut cur = &state;
            let mut back = &cur.parent;
            while let Some(ref b) = back {
                path.push(b.puzzle.clone());
                back = &b.parent;
            }
            path.reverse();
            let mut majors = String::new();
            let mut total_cost = 0;
            for w in path.windows(2) {
                let mv = Puzzle8::get_diff(&w[1], &w[0]);
                if let Some(digit) = mv.chars().next().and_then(|c| c.to_digit(10)) {
                    total_cost += costs[digit as usize - 1];
                }
                majors.push_str(&format!("{} ", mv));
            }
            let moves = if path.len() > 1 { path.len() - 1 } else { 0 };
            return Some((moves, total_cost, majors));
        }
        for tile in state.puzzle.valid_moves() {
            let mut new_puzzle = state.puzzle.clone_and_move(&tile);
            if seen.contains(&new_puzzle) {
                continue;
            }
            let moved_val = state.puzzle.arr[tile.x][tile.y];
            let move_cost = if moved_val == 0 { 1000000 } else { costs[moved_val as usize - 1] };
            let new_cost = state.cost + move_cost;
            let est_remain =
                heuristic(&new_puzzle, answer); // You can customize a better heuristic here
            let total_cost = new_cost + est_remain;
            let s = State::new(
                new_puzzle.clone(),
                new_cost,
                total_cost,
                Some(Box::new(state.clone())),
            );
            let should_update = g_cost.get(&new_puzzle).map_or(true, |&existing| new_cost < existing);
            if should_update {
                g_cost.insert(new_puzzle, new_cost);
                heap.push(s);
            }
        }
    }
    None
}

// Manhattan distance as heuristic
fn heuristic(puzzle: &Puzzle8, goal: &Puzzle8) -> i32 {
    let mut dist = 0;
    for i in 0..3 {
        for j in 0..3 {
            let val = puzzle.arr[i][j];
            if val == 0 {
                continue;
            }
            let mut gx = 0;
            let mut gy = 0;
            'outer: for x in 0..3 {
                for y in 0..3 {
                    if goal.arr[x][y] == val {
                        gx = x as i32;
                        gy = y as i32;
                        break 'outer;
                    }
                }
            }
            dist += (gx - i as i32).abs() + (gy - j as i32).abs();
        }
    }
    dist
}
