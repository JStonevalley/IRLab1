/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

import java.io.Serializable;
import java.util.ArrayList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public ArrayList<Integer> offsets;
    public double score;

    public PostingsEntry(int docID, int offset) {
        this.offsets = new ArrayList<Integer>();
        offsets.add(offset);
        this.docID = docID;
        this.score = 1;
    }

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */

    public void addOccurance(int offset){
        offsets.add(offset);
        score++;
    }

    public int getDocID() {
        return docID;
    }

    public ArrayList<Integer> getOffsets() {
        return offsets;
    }

    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PostingsEntry that = (PostingsEntry) o;

        if (docID != that.docID)
            return false;
        if (Double.compare(that.score, score) != 0)
            return false;
        if (!offsets.equals(that.offsets))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = docID;
        result = 31 * result + offsets.hashCode();
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }

}

    
