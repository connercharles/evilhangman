package hangman;

import java.io.File;

public class EvilHangman {
    private static EvilHangmanGame game = new EvilHangmanGame();

    // args: dictionary wordLength guesses
    public static void main(String[] args) {
        // use try's and catches for the exceptions

        try {
            // make sure args are valid
            // (wordLength >= 2 & guesses >= 1)
            if (args.length >= 3
                    && Integer.parseInt(args[1]) >= 2
                    && Integer.parseInt(args[2]) >= 1) {
                File dictionaryFile = new File(args[0]);
                // run game
                game.startGame(dictionaryFile, Integer.parseInt(args[1]));
                game.setGuesses(args[2]);
                game.runGame();
            } else {
                throw new IllegalArgumentException("Invalid input! Correct usage is: Usage: \"java EvilHangman dictionaryFile wordLength guesses\"");
            }
        // Tells user they're wrong
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
