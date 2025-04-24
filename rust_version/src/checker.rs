use std::fs::File;
use std::io::{BufRead, BufReader};
use std::collections::HashMap;

fn build_state_string(grid: &[u8; 9]) -> String {
    grid.iter().map(|&key| if key > 0 { char::from_digit(key as u32, 10).unwrap() } else { 'G' }).collect()
}

fn build_board_from_string(string: &str) -> [u8; 9] {
    let mut grid = [0u8; 9];
    for (i, c) in string.chars().enumerate() {
        grid[i] = if c == 'G' { 0 } else { c.to_digit(10).unwrap() as u8 };
    }
    grid
}

fn try_up(grid: &[u8;9]) -> bool { ![0,1,2].iter().any(|&i| grid[i]==0) }
fn try_down(grid: &[u8;9]) -> bool { ![6,7,8].iter().any(|&i| grid[i]==0) }
fn try_left(grid: &[u8;9]) -> bool { ![0,3,6].iter().any(|&i| grid[i]==0) }
fn try_right(grid: &[u8;9]) -> bool { ![2,5,8].iter().any(|&i| grid[i]==0) }

fn swap(grid: &mut [u8;9], zpos: usize, other: usize, ch: char) -> String {
    grid.swap(zpos, other);
    format!("{}{}", grid[zpos], ch)
}

fn move_up(grid: &mut [u8;9], zpos: usize) -> String { swap(grid, zpos, zpos-3, 'D') }
fn move_down(grid: &mut [u8;9], zpos: usize) -> String { swap(grid, zpos, zpos+3, 'U') }
fn move_left(grid: &mut [u8;9], zpos: usize) -> String { swap(grid, zpos, zpos-1, 'R') }
fn move_right(grid: &mut [u8;9], zpos: usize) -> String { swap(grid, zpos, zpos+1, 'L') }

fn perform_move(grid: &mut [u8;9], move_ : &str) -> Result<(), String> {
    if move_.len() != 2 { return Err(format!("Malformed move: {}", move_)); }
    let key = move_.chars().next().unwrap();
    let direction = move_.chars().nth(1).unwrap();
    let zpos = grid.iter().position(|&x| x == 0).unwrap();

    let check_function = match direction {
        'D' => try_up(grid),
        'U' => try_down(grid),
        'R' => try_left(grid),
        'L' => try_right(grid),
        _ => false,
    };
    if !check_function {
        return Err(format!("DIRECTION ERROR: Can't perform the move {} on {}", move_, build_state_string(grid)));
    }

    // Make a copy to check
    let mut tmp_grid = *grid;
    let valid_fn_tuple = match direction {
        'D' => move_up(&mut tmp_grid, zpos),
        'U' => move_down(&mut tmp_grid, zpos),
        'R' => move_left(&mut tmp_grid, zpos),
        'L' => move_right(&mut tmp_grid, zpos),
        _ => return Err(format!("BAD DIRECTION {}", direction))
    };
    if move_ != valid_fn_tuple {
        return Err(format!("POSITION ERROR: Can't perform the move {} on {}", move_, build_state_string(grid)));
    }
    // Now apply to main grid
    match direction {
        'D' => { move_up(grid, zpos); },
        'U' => { move_down(grid, zpos); },
        'R' => { move_left(grid, zpos); },
        'L' => { move_right(grid, zpos); },
        _ => return Err(format!("BAD DIRECTION 2 {}", direction))
    };
    Ok(())
}

fn verify_path(start_str: &str, end_str: &str, moves: &[String]) -> Result<(), String> {
    let mut grid = build_board_from_string(start_str);
    for mv in moves {
        perform_move(&mut grid, mv)?;
    }
    let final_str = build_state_string(&grid);
    if final_str != end_str {
        return Err(format!("FINAL DESTINATION ERROR: After performing the moves, you reach {}, instead of {}", final_str, end_str));
    }
    Ok(())
}

fn verify_cost(output_cost: i32, path: &[String], cost_fn: &HashMap<u8, i32>) -> Result<(), String> {
    let correct_cost: i32 = path.iter()
        .filter_map(|mv| mv.chars().next().and_then(|c| c.to_digit(10)).map(|d| cost_fn.get(&(d as u8)).copied().unwrap_or(0)))
        .sum();
    if correct_cost != output_cost {
        return Err(format!("COST ERROR: Cost of the output path is {}, but you reported {}", correct_cost, output_cost));
    }
    Ok(())
}

fn verify_pathlen(pathlen: i32, path: &[String]) -> Result<(), String> {
    if pathlen != path.len() as i32 {
        return Err(format!("PATH LENGTH ERROR: reported #moves is {} but {} moves found.", pathlen, path.len()));
    }
    Ok(())
}

fn handle_inconsistent(pathlen: i32, cost: i32, path: &[String]) -> Result<(), String> {
    if !(pathlen == -1 && cost == -1 && path.is_empty()) {
        return Err("NOT REACHABLE CASE: Presentation error in while marking it as not reachable.".to_string());
    }
    Ok(())
}

fn verify_solution(
    input_start_str: &str, input_end_str: &str, input_cost_fn: &HashMap<u8, i32>,
    output_pathlen: i32, output_cost: i32, output_path: &[String]
) -> Result<(), String> {
    if output_pathlen < 0 || output_cost < 0 {
        return handle_inconsistent(output_pathlen, output_cost, output_path);
    }
    verify_pathlen(output_pathlen, output_path)?;
    verify_path(input_start_str, input_end_str, output_path)?;
    verify_cost(output_cost, output_path, input_cost_fn)?;
    Ok(())
}

fn parse_input_testcase(line1: &str, line2: &str, t: usize) -> Result<(String, String, HashMap<u8,i32>), String> {
    let mut split1 = line1.split_whitespace();
    let start_str = split1.next().ok_or(format!("INPUT ERROR : for testcase {} ", t))?.to_string();
    let end_str = split1.next().ok_or(format!("INPUT ERROR : for testcase {} ", t))?.to_string();
    let costfn: HashMap<u8, i32> =
        line2
        .split_whitespace()
        .enumerate()
        .map(|(i, x)| ((i as u8) + 1, x.parse().unwrap_or(0)))
        .collect();
    if costfn.len() != 8 || start_str.len() != 9 || end_str.len() != 9 {
        Err(format!("INPUT ERROR : for testcase {} ", t))
    } else {
        Ok((start_str, end_str, costfn))
    }
}

fn parse_output_testcase(line1: &str, line2: &str, _t: usize) -> (i32, i32, Vec<String>) {
    let mut s1 = line1.trim().split_whitespace();
    let pathlen = s1.next().unwrap_or("-1").parse().unwrap_or(-1);
    let cost = s1.next().unwrap_or("-1").parse().unwrap_or(-1);
    let moves: Vec<String> = line2.trim().split_whitespace().map(|x| x.to_string()).collect();
    (pathlen, cost, moves)
}

pub fn format_check(inputfile: &str, outputfile: &str) {
    let finput = File::open(inputfile).expect("Input file error");
    let mut rdr = BufReader::new(finput);
    let mut buf = String::new();

    rdr.read_line(&mut buf).expect("Read testcase count");
    let t: usize = buf.trim().parse().expect("Testcase count parse");
    let mut input_cases = vec![];
    for _ in 0..t {
        let mut l1 = String::new();
        let mut l2 = String::new();
        rdr.read_line(&mut l1).expect("Read case1");
        rdr.read_line(&mut l2).expect("Read case2");
        input_cases.push((l1.trim().to_string(), l2.trim().to_string()));
    }
    let foutput = File::open(outputfile).expect("Output file error");
    let mut rdr2 = BufReader::new(foutput);

    let mut output_lines = vec![];
    while let Some(Ok(line)) = rdr2.by_ref().lines().next() {
        output_lines.push(line);
    }
    let T = t;
    if output_lines.len() != 2*T {
        panic!("Output file should have {} lines, has {}", 2*T, output_lines.len());
    }
    for testcase in 0..T {
        println!("Starting to verify testcase {}", testcase);
        let (start_str, end_str, costfn) = parse_input_testcase(&input_cases[testcase].0, &input_cases[testcase].1, testcase+1)
            .expect("Input parse error");
        let (pathlen, cost, moves) = parse_output_testcase(&output_lines[2*testcase], &output_lines[2*testcase+1], testcase+1);

        match verify_solution(&start_str, &end_str, &costfn, pathlen, cost, &moves) {
            Ok(_) => {
                println!("...Done");
            }
            Err(e) => {
                println!("FAILED: {}", e);
                std::process::exit(1);
            }
        }
    }
}
