/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index;

    public HashedIndex() {
        this.index = new HashMap<String,PostingsList>();
    }

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList postingsList = index.get(token);
        if (postingsList == null){
            postingsList = new PostingsList();
            index.put(token, postingsList);
            postingsList.addLast(new PostingsEntry(docID, offset));
        }
        else{
            PostingsEntry postingsEntry = postingsList.getLast();
            if (postingsEntry.getDocID() != docID){
                postingsList.addLast(new PostingsEntry(docID, offset));
            }
            else{
                postingsEntry.addOccurance(offset);
            }
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
        if (query.getTermsSize() == 1){
            return getPostings(query.getTerm(0));
        }
        else if (query.getTermsSize() > 1 && queryType == Index.INTERSECTION_QUERY){
            ArrayList<PostingsList> postingLists = new ArrayList<PostingsList>();
            PostingsList intermediatePosting = new PostingsList();
            for (int i = 0; i < query.getTermsSize(); i++) {
				if (getPostings(query.getTerm(i)) == null){
					return null;
				}
                postingLists.add(getPostings(query.getTerm(i)));
            }
            int intermediateIndex = 0;
            for (int i = 0; i < postingLists.size(); i++) {
                if (postingLists.get(i).size() > intermediatePosting.size()){
                    intermediateIndex = i;
                    intermediatePosting = postingLists.get(i);
                }
            }
            postingLists.remove(intermediateIndex);
            while (!postingLists.isEmpty()) {
                PostingsList shortestPosting = new PostingsList();
                int shortestIndex = 0;
                for (int i = 0; i < postingLists.size(); i++) {
                    if (postingLists.get(i).size() > shortestPosting.size()){
                        shortestIndex = i;
                        shortestPosting = postingLists.get(i);
                    }
                }
                postingLists.remove(shortestIndex);
                intermediatePosting = intersect(intermediatePosting, shortestPosting);
            }
            return intermediatePosting;
        }
        else if (query.getTermsSize() > 1 && queryType == Index.PHRASE_QUERY){
            ArrayList<PostingsList> postingLists = new ArrayList<PostingsList>();
            PostingsList intermediatePosting;
            for (int i = 0; i < query.getTermsSize(); i++) {
				if (getPostings(query.getTerm(i)) == null){
					return null;
				}
                postingLists.add(getPostings(query.getTerm(i)));
            }
            intermediatePosting = postingLists.get(0);
            for (int i = 1; i < postingLists.size(); i++) {
                intermediatePosting = positionalIntersect(intermediatePosting, postingLists.get(i), 1);
            }
            return intermediatePosting;
        }
        return null;
    }

    private PostingsList intersect(PostingsList p1, PostingsList p2){
        PostingsList answer = new PostingsList();
        int i = 0, j = 0;
        while (i < p1.size() && j < p2.size()){
            if(p1.get(i).getDocID() == p2.get(j).getDocID()){
                answer.addLast(p1.get(i));
                i++;
                j++;
            }
            else if (p1.get(i).getDocID() < p2.get(j).getDocID()){
                i++;
            }
            else{
                j++;
            }
        }
        return answer;
    }

    private PostingsList positionalIntersect(PostingsList p1, PostingsList p2, int k){
        PostingsList answer = new PostingsList();
        int i = 0, j = 0;
        while (i < p1.size() && j < p2.size()){
            if(p1.get(i).getDocID() == p2.get(j).getDocID()){
                Iterator<Integer> iterator = p1.get(i).getIterator();
                while (iterator.hasNext()){
                    if (p2.get(j).hasOffset(iterator.next() + k)){
                        answer.addLast(p2.get(j));
						break;
                    }
                }
                i++;
                j++;
            }
            else if (p1.get(i).getDocID() < p2.get(j).getDocID()){
                i++;
            }
            else{
                j++;
            }
        }
        return answer;
    }
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
