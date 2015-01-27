/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */

import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList postingsList = index.get(token);
        if (postingsList == null){
            postingsList = new PostingsList();
            index.put(token, postingsList);
        }
	    PostingsEntry postingsEntry = null;

        if (postingsList.size() < 0 && postingsList.get(postingsList.size()-1).getDocID() == docID){
            postingsEntry = postingsList.get(postingsList.size()-1);
        }

        if (postingsEntry != null){
            postingsEntry.addOccurance(offset);
        }
        else{
            postingsList.add(new PostingsEntry(docID, offset));
        }
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	if (query.size() == 1){
        return getPostings(query.terms.getFirst());
    }
	return null;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
