package tools;

public class WinTieCounter {
    public int[] wins;
    public int tie = 0;

    public WinTieCounter(int numberOfPlayers) {
        wins = new int[numberOfPlayers];
    }

    /**
     * Declares current winner or tie for the score and increments {@link #wins} and {@link #tie} for that matter.
     * Call after every episode to log wins and ties for compete.
     */
    public void declareWinner(ScoreTuple scoreTuple) {
        if (wins.length == 1) {
            if (scoreTuple.scTup[0] < 0) return;
            if (scoreTuple.scTup[0] > 0){
                wins[0] += 1;
                return;
            }
            if (scoreTuple.scTup[0] == 0) {
                tie +=1;
                return;
            }
        }

        double bestScore = scoreTuple.scTup[0];
        int winningPlayer = 0;
        boolean tie = false;

        for (int i=1; i<scoreTuple.scTup.length; i++) {
            if (scoreTuple.scTup[i] > bestScore) {
                bestScore = scoreTuple.scTup[i];
                winningPlayer = i;
                tie = false;
            }
            else if (scoreTuple.scTup[i] == bestScore) {
                tie = true;
            }
        }

        if (tie) {
            this.tie += 1;
        } else {
            this.wins[winningPlayer] +=1;
        }
    }

    public void shift(int k) {
        if (k % wins.length == 0) return;
        int[] newWins = new int[wins.length];
        for (int i = 0; i < wins.length; i++) {
            newWins[(i+k)%wins.length] = wins[i];
        }
        wins = newWins;
    }

    public void combine(WinTieCounter otherCounter) {
        for(int i=0; i<wins.length; i++) {
            wins[i] += otherCounter.wins[i];
        }
        tie += otherCounter.tie;
    }

    public int getLosses(int player) {
        int losses = 0;
        for(int i = 0; i < wins.length; i++) {
            if (i == player) continue;
            losses += wins[i];
        }
        return losses;
    }

    public String toString() {
        switch (wins.length) {
            case 1:
                return "X wins: " + wins[0];
            case 2:
                return "X wins: " + wins[0] + " | O wins: " + wins[1] + " | ties: " + tie;
            default:
                String string = "";
                for (int i = 0; i< wins.length; i++) {
                    string += "player " + i + " wins: " + wins[i] + " ";
                }
                string += "ties: " + tie;
                return string;
        }
    }
}
