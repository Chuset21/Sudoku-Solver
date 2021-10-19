# Sudoku Solver

A project that started as an idea to visualise a sudoku solver backtracking algorithm.  
This is still the main feature. Features include:

- Sudoku Generator, with difficulty choices.
- Sudoku Solver (visualised).
- Slider to change the speed of the visualisation of the solving algorithm.
- Hint button which gives you the right value for a random cell.
- Ability to input numbers yourself and have the application approve or deny them.

### Issues

The solving algorithm when visualised uses a new thread, which to my knowledge in JavaFX is not a good idea.  
This can cause the application to crash.  
This is quite rare but possible, if this happens, close the application and open it again, best I can do without
wrecking my head :)

### Application Showcase

![Sudoku Application Picture](https://github.com/Chuset21/Sudoku-Solver/blob/main/showcase/sudoku.png?raw=true)
