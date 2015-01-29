/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

import java.io.Serializable;
import java.util.ArrayList;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable, Comparable<PostingsList> {
    
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

    public PostingsEntry getFirst(){
        return get(0);
    }

    public PostingsEntry getLast(){
        return get(size() - 1);
    }

    public void addLast(PostingsEntry postingsEntry){
        list.add(postingsEntry);
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
	

			   
