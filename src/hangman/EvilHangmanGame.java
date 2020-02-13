package hangman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    private SortedSet<String> dictionary = new TreeSet<>();
    private int guesses = 0;
    private SortedSet<Character> guessedLetters = new TreeSet<>();
    private String word = "";

    private char[] ALPHABET = {'a','b','c','d','e','f','g','h','i', 'j','k',
            'l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

    public void setGuesses(String guesses) {
        this.guesses = Integer.parseInt(guesses);
    }

    @Override
    public void startGame(File dictionaryFile, int wordLength) throws IOException, EmptyDictionaryException {
        // throw exceptions if it doesn't exist or if it's empty
        if (!dictionaryFile.exists() || wordLength < 2) {
             throw new EmptyDictionaryException();
        }
        // Reads in the words
        BufferedReader in = new BufferedReader(
                new FileReader(dictionaryFile));
        while (in.ready()) {
            String temp = in.readLine();
            temp = temp.toLowerCase();
            // as it reads in the words watch for \n, ',', ' '
            String[] tempArray = temp.split("\\W+"); // " \n,"
            dictionary.addAll(Arrays.asList(tempArray));
        }
        in.close();

        ArrayList<String> toBeDeleted = new ArrayList<>();
        for (String word : dictionary) {
            if (word.length() != wordLength) {
                toBeDeleted.add(word);
            }
        }
        dictionary.removeAll(toBeDeleted);
        // throw exception if no words with word length
        if (dictionary.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        // set up the word
        String hidden = "-";
        word = hidden.repeat(wordLength);
    }

    // this runs the game
    public void runGame(){
        boolean won = false;

        try {
            // loop until the end
            while (!won) {
                char userInput = promptUser();
                makeGuess(userInput);
                won = checkIfDone();
            }
        } catch (GuessAlreadyMadeException e) {
            System.out.print("You have already guessed that letter.\n\n");
            runGame();
        }
    }

    private boolean checkIfDone() {
        if (!word.contains("-")) {
            // they won!
            System.out.print("You win!\n");
            System.out.printf("The word was: %s\n", dictionary.first());
            return true;
        } else if (guesses == 0) {
            System.out.print("You lose!\n");
            System.out.printf("The word was: %s\n", dictionary.first());
            return true;
        }
        return false;
    }

    private String printUsedLetter() {
        String letters = "";
        for (Character letter: guessedLetters) {
            letters += letter.toString() + " ";
        }
        return letters;
    }

    // this is the prompt for the game - returns input char
    private char promptUser() {

        // change to printf()
        System.out.printf("You have %d guesses left\n", guesses);
        System.out.printf("Used letters: %s \n", printUsedLetter());
        System.out.printf("Word: %s\n", word);

        // if input is longer than a 1 say it's wrong!
        // invalid guesses do not decrement guesses
        String input = "";
        Scanner scan = new Scanner(System.in);
        scan.useDelimiter("\\n");
        do {
            // only worry about the first letter put in
            System.out.print("Enter guess: ");
            input = scan.next();
            // get rid of non chars
            input = input.replaceAll("[^A-Za-z]+", "");
            // check if they put nothing in
            if (input.length() < 1) {
                System.out.print("\nInvalid Input!\n");
                input = "";
            }
        } while (input.isEmpty());
        // make lowercase
        input = input.toLowerCase();
        return input.charAt(0);
    }

    // takes the given word and creates a key to be put into groups
    private Pair getPattern(String currentWord, char letter) {
        StringBuilder pattern = new StringBuilder(word);
        int letterCounter = 0;

        for (int i = 0; i < currentWord.length(); i++) {
            // if it's the chosen letter do things
            if (currentWord.charAt(i) == letter) {
                pattern.setCharAt(i, letter);
                letterCounter++;
            }
        }

        return new Pair(pattern.toString(), letterCounter);
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        guess = Character.toLowerCase(guess);
        int minCharFrequency = word.length();

        // check if already guessed
        if (guessedLetters.contains(guess)){
            throw new GuessAlreadyMadeException();
        }

        HashMap<String, SortedSet<String>> dictionaryGroups = new HashMap<>();

        // section each word into a grouping and add to the dictionaryGroups
        for (String currentWord : dictionary) {
            Pair pattern = getPattern(currentWord, guess);
            // get minimum amount of char's revealed
            if (pattern.letterCount < minCharFrequency){
                 minCharFrequency = pattern.letterCount;
            }
            // add to key to map or add to existing set
            if (dictionaryGroups.containsKey(pattern.key)) {
                dictionaryGroups.get(pattern.key).add(currentWord);
            } else {
                SortedSet<String> newGroup = new TreeSet<String>();
                newGroup.add(currentWord);
                dictionaryGroups.put(pattern.key, newGroup);
            }
        }

        int largest = 0;
        ArrayList<String> largestKey = new ArrayList<>();
        // find out largest group number
        for (Map.Entry<String, SortedSet<String>> group: dictionaryGroups.entrySet()) {
            if (group.getValue().size() >= largest) {
                largest  = group.getValue().size();
                largestKey.add(group.getKey());
            }
        }
        // choose largest group first
        for (Map.Entry<String, SortedSet<String>> group: dictionaryGroups.entrySet()) {
            if (group.getValue().size() != largest) {
                // take not largest out of key list
                largestKey.remove(group.getKey());
            }
        }
        if (largestKey.size() == 1) {
            dictionary = dictionaryGroups.get(largestKey.get(0));
            // check if key has the a letter and change for word printing
            int counter = 0;
            for (int i = 0; i < largestKey.get(0).length(); i++) {
                if (largestKey.get(0).charAt(i) == guess) {
                    counter++;
                }
            }
            minCharFrequency = counter;

            // tell user what happened
            if (largestKey.get(0).equals(word)) {
                System.out.printf("Sorry, there are no %c's\n\n", guess);
            } else {
                // for wording
                if (minCharFrequency > 1) {
                    System.out.printf("Yes, there are %d %c's\n\n", minCharFrequency, guess);
                } else if (minCharFrequency == 1) {
                    System.out.printf("Yes, there is %d %c\n\n", minCharFrequency, guess);
                } else {
                    System.out.printf("Sorry, there are no %c's\n\n", guess);
                }
            }
            word = largestKey.get(0);
            guessedLetters.add(guess);
            guesses--;
            return dictionary;
        } else {
            // if same number then do these:

            // First get the group with a blank key (no guessed letter in word)
            SortedSet<String> noLetterGroup = dictionaryGroups.get(word);
            if (noLetterGroup != null) {
                dictionary = noLetterGroup;
                // tell user they're wrong!
                System.out.printf("Sorry, there are no %c's\n\n", guess);
                guessedLetters.add(guess);
                guesses--;
                return dictionary;
            }

            // Second get group with the least about of guessed letters
            ArrayList<String> keys = new ArrayList<>();

            // get groups with least amount of char's that was guessed
            for (Map.Entry<String, SortedSet<String>> key : dictionaryGroups.entrySet()) {
                int counter = 0;
                String keyString = key.getKey();
                // count char's guessed in key
                for (int i = 0; i < keyString.length(); i++) {
                    if (keyString.charAt(i) == guess) {
                        counter++;
                    }
                }
                // add to group of candidate groups - to cut some out that won't win
                if (counter == minCharFrequency) {
                    keys.add(keyString);
                }
            }

            // get the group with the lowest char guessed count furthest to the right
            GetGroupLoop:
            // goes from the back of the word to the beginning
            for (int i = word.length() - 1; i >= 0; i--) {
                boolean[] charCurrent = new boolean[keys.size()]; // default is false
                boolean hasLetter = false;
                for (int j = 0; j < keys.size(); j++) {
                    // check if guessed character is in that position
                    if (keys.get(j).charAt(i) == guess) {
                        charCurrent[j] = true;
                        hasLetter = true;
                    }
                }

                if (hasLetter) {
                    // new Keys
                    ArrayList<String> updatedKeys = new ArrayList<>();

                    for (int k = 0; k < keys.size(); k++) {
                        if (!charCurrent[k]) { // if they don't have that letter there
                            updatedKeys.add(keys.get(k));
                        }
                    }

                    // remove them
                    keys.removeAll(updatedKeys);

                    // if there's only one
                    if (keys.size() == 1) {
                        dictionary = dictionaryGroups.get(keys.get(0));
                        //update word
                        word = keys.get(0);
                        break GetGroupLoop;
                    }
                }
            }
        }

        // for wording
        if (minCharFrequency > 1) {
            System.out.printf("Yes, there are %d %c's\n\n", minCharFrequency, guess);
        } else {
            System.out.printf("Yes, there is %d %c\n\n", minCharFrequency, guess);
        }
        // add to guessed and decrement num of guesses
        guessedLetters.add(guess);
        guesses--;
        return dictionary;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return guessedLetters;
    }
}
