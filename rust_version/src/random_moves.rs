use rand::seq::SliceRandom;
use rand::thread_rng;

fn build_state_string(grid: &[u8; 9]) -> String {
    grid.iter().map(|&key| if key > 0 { char::from_digit(key as u32, 10).unwrap() } else { 'G' }).collect()
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

fn perform_random_move(grid: &mut [u8;9]) -> String {
    let zpos = grid.iter().position(|&x| x==0).unwrap();
    let mut valid: Vec<fn(&mut [u8;9], usize) -> String> = vec![];
    if try_up(grid)   { valid.push(move_up); }
    if try_down(grid) { valid.push(move_down); }
    if try_left(grid) { valid.push(move_left); }
    if try_right(grid){ valid.push(move_right); }
    let mut rng = thread_rng();
    let f = valid.choose(&mut rng).expect("No valid moves");
    f(grid, zpos)
}

pub fn random_moves(moves: usize) {
    let mut grid = [0u8, 1,2,3,4,5,6,7,8];
    let mut rng = thread_rng();
    grid[1..].shuffle(&mut rng);
    let start_state = build_state_string(&grid);
    let mut path = vec![];
    for _ in 0..moves {
        let mv = perform_random_move(&mut grid);
        path.push(mv);
    }
    let end_state = build_state_string(&grid);
    println!("{} {}", start_state, end_state);
    println!("{}", path.join(" "));
}
