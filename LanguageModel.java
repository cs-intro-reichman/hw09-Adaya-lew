
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
        // Your code goes here
        String window = "";
        char c;
        In in = new In(fileName);

        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
            ;
        }

        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window = window.substring(1) + c;
        }

        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {
        // Your code goes here
        ListIterator itr = probs.listIterator(0);
        int totalChr = 0;
        while (itr.hasNext()) {
            CharData current = itr.next();
            totalChr += current.count;
        }

        CharData current = probs.getFirst();
        current.p = (double) current.count / (double) totalChr;
        current.cp = current.p;

        ListIterator itr2 = probs.listIterator(1);
        while (itr2.hasNext()) {
            CharData prev = probs.get(probs.indexOf(current.chr));
            current = itr2.next();
            prev.p = (double) prev.count / (double) totalChr;
            current.p = (double) current.count / (double) totalChr;
            current.cp = prev.cp + current.p;
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
        // Your code goes here
    }
}
