/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

import sun.util.PreHashedMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable, Comparable<PostingsList> {

	private int cF;

	private double iDF;

    /** The postings list as a linked list. */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
	return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }

	public ArrayList<PostingsEntry> getList() {
		return list;
	}

	public void sortList(){
		Collections.sort(list);
	}

	public PostingsEntry getFirst(){
        return get(0);
    }

    public PostingsEntry getLast(){
        return get(size() - 1);
    }

    public void addLast(PostingsEntry postingsEntry){
        list.add(postingsEntry);
    }

	public void appendPostingsList(PostingsList postingsList){
		getList().addAll(postingsList.getList());
	}

	public int getDF(){
		return list.size();
	}

	public double getiDF() {
		return iDF;
	}

	public int getcF() {
		return cF;
	}

	public void computeIDF(int N){
		iDF = Math.log(N / getDF());
	}

	public void computeCF() {
		for (PostingsEntry entry : list){
			cF += entry.getCount();
		}
	}

	public void normalizeScores(HashMap<String, Integer> docLengths){
		for (PostingsEntry entry : list){
			entry.normalizeScore(docLengths.get(entry.getDocID() + ""));
		}
	}

	public void computeScores(){
		for (PostingsEntry entry : list){
			entry.computeScore(iDF);
		}
	}

	@Override public int compareTo(PostingsList other) {
		if (other.size() < size()){
			return 1;
		}
		else if (other.size() > size()){
			return -1;
		}
		return 0;
	}
}
	

			   
