import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        char c;

        In in = new In(fileName);
        
        // Reads just enough characters to form the first window
        for (int i = 0; i < windowLength; i++) {
            window+=in.readChar();
        }

        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();

            // Checks if the window is already in the map
            List probs = CharDataMap.get(window);

            // If the window was not found in the map
            if (probs == null) {
                // Creates a new empty list, and adds (window,list) to the map
                probs = new List();
                CharDataMap.put(window, probs);
            }
             // Calculates the counts of the current character.
            probs.update(c);

            // Advances the window: adds c to the window’s end, and deletes the
            // window's first character.
            window+=c;
            window = window.substring(1);
        }
        // The entire file has been processed, and all the characters have been counted.
        // Proceeds to compute and set the p and cp fields of all the CharData objects
        // in each linked list in the map.
        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);
       
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    public void calculateProbabilities(List probs) {
        // first we count how many characters are in the list               
        int n = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            n+=probs.listIterator(i).current.cp.count;
        }

        for (int j = 0; j < probs.getSize(); j++) {
            CharData prevCd = (j == 0) ? null : probs.listIterator(j - 1).current.cp;
            CharData currentCd = probs.listIterator(j).current.cp;
            currentCd.p = (double) currentCd.count / n;
           if (j == 0) {
            currentCd.cp = currentCd.p;
           } 
           else {
            currentCd.cp = prevCd.cp + currentCd.p;
           }
        }
    }

    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        int i = 0;
        
        while (probs.listIterator(i).current.cp.cp < r) {
            i++;
        }
        return probs.get(i).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training. 
     * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
     * doesn't appear as a key in Map, we generate no text and return only the initial text. 
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }
        List last = CharDataMap.get(initialText.substring(initialText.length()-windowLength));
        if (last == null) {
            return initialText;
        }
            
        String window = initialText.substring(initialText.length() - windowLength);
        String generatedText = window;
        while (generatedText.length() < (textLength + windowLength)) {
            List probs = CharDataMap.get(window);
            if (probs == null) {
                break;
            }
            char c = getRandomChar(probs);
            generatedText += c;
            window = generatedText.substring(generatedText.length() - windowLength);
        }
        return generatedText;

    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));  
    }
}