
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        char character;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) {
            window = window + in.readChar();
        }
        while (!(in.isEmpty())) {
            character = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null){
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(character);
            window = window + character;
            window = window.substring(1);
        }
        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);
        // Your code goes here
    }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {               
        
        double numOfChr = 0.0;
        double p, cp = 0.0;
        Node current = probs.getNode();

        while (current != null) {
            numOfChr += current.cp.count;
            current = current.next;
        }

        current = probs.getNode();
        while (current != null) {
            p = current.cp.count / numOfChr;
            cp += p;
            current.cp.p = p;
            current.cp.cp = cp;
            current = current.next;
        }
    }



    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double rand = randomGenerator.nextDouble();
        int i = 0;
       
        while ((probs.listIterator(i).current.cp.cp < rand)) {
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
        if (initialText.length() >= windowLength) {
            for (int i = 0; i < textLength; i++) {
                initialText += getRandomChar(
                        CharDataMap.get(initialText.substring(initialText.length() - windowLength)));
            }
        }
        return initialText;
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
        // Your code goes here
    }
}
