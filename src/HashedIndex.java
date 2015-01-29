/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */

import java.util.*;

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
			ArrayList<PostingsList> postingLists = collectPostings(query);
			Collections.sort(postingLists);
            PostingsList intermediatePosting = postingLists.remove(postingLists.size() - 1);
            while (!postingLists.isEmpty()) {
                PostingsList shortestPosting = postingLists.remove(postingLists.size() - 1);
                intermediatePosting = intersect(intermediatePosting, shortestPosting);
            }
            return intermediatePosting;
        }
        else if (query.getTermsSize() > 1 && queryType == Index.PHRASE_QUERY){
			ArrayList<PostingsList> postingLists = collectPostings(query);
            PostingsList intermediatePosting;
            intermediatePosting = postingLists.get(0);
            for (int i = 1; i < postingLists.size(); i++) {
                intermediatePosting = phraseIntersect(intermediatePosting, postingLists.get(i));
            }
            return intermediatePosting;
        }
        return null;
    }

	private ArrayList<PostingsList> collectPostings(Query query){
		ArrayList<PostingsList> postingLists = new ArrayList<PostingsList>();
		for (int i = 0; i < query.getTermsSize(); i++) {
			if (getPostings(query.getTerm(i)) == null){
				return null;
			}
			postingLists.add(getPostings(query.getTerm(i)));
		}
		return postingLists;
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

    private PostingsList phraseIntersect(PostingsList p1, PostingsList p2){
        PostingsList answer = new PostingsList();
        int i = 0, j = 0;
        while (i < p1.size() && j < p2.size()){
            if(p1.get(i).getDocID() == p2.get(j).getDocID()){
                Iterator<Integer> iterator = p1.get(i).getIterator();
                while (iterator.hasNext()){
                    if (p2.get(j).hasOffset(iterator.next() + 1)){
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
