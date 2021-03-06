/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    private int docID;
    private HashSet<Integer> offsets;
    private double score;

    public PostingsEntry(int docID, int offset) {
        this.docID = docID;
        this.score = 1d;
        this.offsets = new HashSet<Integer>();
        this.offsets.add(offset);
    }

	public PostingsEntry(PostingsEntry postingsEntry){
		this.docID = postingsEntry.getDocID();
		this.offsets = new HashSet<Integer>();
		this.offsets.addAll(postingsEntry.getOffsets());
		this.score = postingsEntry.getScore();
	}

    public int getDocID() {
        return docID;
    }

    public double getScore() {
        return score;
    }

    public void addOccurance(int offset){
        offsets.add(offset);
    }

    public boolean hasOffset(int offset){
        return offsets.contains(offset);
    }

	public HashSet<Integer> getOffsets() {
		return offsets;
	}

	public Iterator<Integer> getIterator(){
        return offsets.iterator();
    }

	public int getTf() {
		return offsets.size();
	}

	public void normalizeScore(double norm){
		score = score/norm;
	}

	public void computeScore(double iDF){
		score = getTf() * iDF;
	}

	public void setScore(double score){
		this.score = score;
	}

	/**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare(other.score, score );
    }
}

    
