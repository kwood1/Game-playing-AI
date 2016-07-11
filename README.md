# Game-playing-AI

This AI plays a game that is similar to checkers. Board state is 110 octagonal tiles. Any piece can move any of the 8 directions,
up to three spaces, but not over any other piece. Game ends when one player gets four aligned in the same direction without any of the
opponents pieces in between, no matter how far apart the players pieces are.

game is analyzed using a game tree, alpha-beta pruning is performed to eliminate game states that the opponent would never let come to pass. Move ordering and iterative deepening also used.
