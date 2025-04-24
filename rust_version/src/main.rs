mod puzzle;
mod checker;
mod random_moves;

use clap::{Parser, Subcommand};

#[derive(Parser)]
#[command(author, version, about)]
struct Args {
    #[command(subcommand)]
    command: Command,
}

#[derive(Subcommand)]
enum Command {
    /// Solve puzzle from input file and write to output file
    Solve {
        input: String,
        output: String,
    },
    /// Check solution output is correct (format checker)
    Check {
        input: String,
        output: String,
    },
    /// Generate a random puzzle and moves
    Random {
        moves: usize,
    },
}

fn main() {
    let args = Args::parse();
    match args.command {
        Command::Solve { input, output } => {
            puzzle::solve_file(&input, &output);
        },
        Command::Check { input, output } => {
            checker::format_check(&input, &output);
        },
        Command::Random { moves } => {
            random_moves::random_moves(moves);
        },
    }
}
