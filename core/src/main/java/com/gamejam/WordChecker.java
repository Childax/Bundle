package com.gamejam;

public class WordChecker {
    public static TileState[] checkWord(String guess, String solution) {
        int length = guess.length();
        TileState[] result = new TileState[length];
        boolean[] solutionUsed = new boolean[length];

        // Pass 1: exact matches
        for (int i = 0; i < length; i++) {
            if (guess.charAt(i) == solution.charAt(i)) {
                result[i] = TileState.CORRECT;
                solutionUsed[i] = true;
            }
        }

        // Pass 2: present vs absent
        for (int i = 0; i < length; i++) {
            if (result[i] == null) {
                boolean found = false;
                for (int j = 0; j < length; j++) {
                    if (!solutionUsed[j] && guess.charAt(i) == solution.charAt(j)) {
                        found = true;
                        solutionUsed[j] = true;
                        break;
                    }
                }
                result[i] = found ? TileState.PRESENT : TileState.ABSENT;
            }
        }

        return result;
    }
}
