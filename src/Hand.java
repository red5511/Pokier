import java.util.Arrays;

/** Utility methods for evaluating or creating a hand of cards. */
public abstract class Hand {
    /**
     * Private constructor to disable instantiation of an abstract class.
     */
    private Hand() {

    }

    /**
     * Evaluates the given hand and returns its value as an integer.
     * Based on Kevin Suffecool's 5-card hand evaluator and with Paul Senzee's pre-computed hash.
     * @param cards a hand of cards to evaluate
     * @return the value of the hand as an integer between 1 and 7462
     */
    public static int evaluate(Card[] cards) {
        // Only 5-card hands are supported
        if (cards == null || cards.length != 7) {
            throw new IllegalArgumentException("Exactly 7 cards are required.");
        }

        // Binary representations of each card
        int c1 = cards[0].getValue();
        int c2 = cards[1].getValue();
        int c3 = cards[2].getValue();
        int c4 = cards[3].getValue();
        int c5 = cards[4].getValue();

        int c6 = cards[5].getValue();
        int c7 = cards[6].getValue();

        // No duplicate cards allowed
        if (hasDuplicates(new int[]{c1, c2, c3, c4, c5, c6, c7})) {
            throw new IllegalArgumentException("Illegal hand.");
        }

        int[][] combo = {{c1, c2, c3, c4, c5},
                {c1, c2, c3, c4, c6},
                {c1, c2, c3, c4, c7},
                {c1, c2, c3, c5, c6},
                {c1, c2, c3, c5, c7},
                {c1, c2, c3, c6, c7},
                {c1, c2, c4, c5, c6},
                {c1, c2, c4, c5, c7},
                {c1, c2, c4, c6, c7},
                {c1, c2, c5, c6, c7},
                {c1, c3, c4, c5, c6},
                {c1, c3, c4, c5, c7},
                {c1, c3, c4, c6, c7},
                {c1, c3, c5, c6, c7},
                {c1, c4, c5, c6, c7},
                {c2, c3, c4, c5, c6},
                {c2, c3, c4, c5, c7},
                {c2, c3, c4, c6, c7},
                {c2, c3, c5, c6, c7},
                {c2, c4, c5, c6, c7},
                {c3, c4, c5, c6, c7}};
        // Calculate index in the flushes/unique table

        int min_value = 10000;

        System.out.println();
        for (int i = 0; i < 21; i++) {
            System.out.println("FOr " + i);
            int index = (combo[i][0] | combo[i][1] | combo[i][2] | combo[i][3] | combo[i][4]) >> 16;


            // Flushes, including straight flushes
            if (i == 10) System.out.println(combo[i][0] + " " + combo[i][1]  + " " +  combo[i][2]  + " " +  combo[i][3]  + " " +  combo[i][4]);
            System.out.println("Flush " + (combo[i][0] & combo[i][1] & combo[i][2] & combo[i][3] & combo[i][4] & 0xF000));
            if ((combo[i][0] & combo[i][1] & combo[i][2] & combo[i][3] & combo[i][4] & 0xF000) != 0) {
                if (min_value > Tables.Flushes.TABLE[index] && Tables.Flushes.TABLE[index] != 0) min_value = Tables.Flushes.TABLE[index];
                continue;
            }
            System.out.println("1 " + min_value);
            // Straight and high card hands
            int value = Tables.Unique.TABLE[index];
            if (value != 0) {
                if (min_value > value) min_value = value;
                continue;
            }
            System.out.println("2 " + min_value);

            // Remaining cards
            int product = (combo[i][0] & 0xFF) * (combo[i][1] & 0xFF) * (combo[i][2] & 0xFF) * (combo[i][3] & 0xFF) * (combo[i][4] & 0xFF);
            System.out.println(" product " + product);
            if (min_value > Tables.Hash.Values.TABLE[hash(product)] && Tables.Hash.Values.TABLE[hash(product)] != 0) min_value = Tables.Hash.Values.TABLE[hash(product)];
            System.out.println("3 " + min_value);
        }
        return min_value;
    }

    /**
     * Creates a new 5-card hand from the given string.
     * @param string the string to create the hand from, such as "Kd 5s Jc Ah Qc"
     * @return a new hand as an array of cards
     * @see Card
     */
    public static Card[] fromString(String string) {
        final String[] parts = string.split(" ");
        final Card[] cards = new Card[parts.length];

        if (parts.length != 7)
            throw new IllegalArgumentException("Exactly 7 cards are required.");

        int index = 0;
        for (String part : parts)
            cards[index++] = Card.fromString(part);

        return cards;
    }

    /**
     * Converts the given hand into concatenation of their string representations
     * @param cards a hand of cards
     * @return a concatenation of the string representations of the given cards
     */
    public static String toString(Card[] cards) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < cards.length; i++) {
            builder.append(cards[i]);
            if (i < cards.length - 1)
                builder.append(" ");
        }

        return builder.toString();
    }

    /**
     * Checks if the given array of values has any duplicates.
     * @param values the values to check
     * @return true if the values contain duplicates, false otherwise
     */
    private static boolean hasDuplicates(int[] values) {
        Arrays.sort(values);
        for (int i = 1; i < values.length; i++) {
            if (values[i] == values[i - 1])
                return true;
        }
        return false;
    }

    private static int hash(int key) {
        key += 0xE91AAA35;
        key ^= key >>> 16;
        key += key << 8;
        key ^= key >>> 4;
        return ((key + (key << 2)) >>> 19) ^ Tables.Hash.Adjust.TABLE[(key >>> 8) & 0x1FF];
    }
}