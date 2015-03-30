/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index;
	private HashMap<String, Double> pageRank;
	private final int requiredNumberOfOccurances = 0;

    public HashedIndex() {
        this.index = new HashMap<String,PostingsList>(160000);

		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("C:\\Users\\Jonas\\IdeaProjects\\PageRank\\data\\pageRank.txt")));
			this.pageRank = (HashMap<String, Double>)objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList postingsList = index.get(token);
        if (postingsList == null){
            postingsList = new PostingsList();
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
		index.put(token, postingsList);
    }

	public void computeScore(){
		Iterator<String> dictionary = getDictionary();
		PostingsList term;
		while(dictionary.hasNext()){
			term = index.get(dictionary.next());
			term.computeIDF(docLengths.size());
		}
	}

	@Override public HashMap<String, PostingsList> getIndexMap() {
		return index;
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
		long startTime = System.nanoTime();
		Search search = new Search();
        if (queryType == Index.INTERSECTION_QUERY){
			ArrayList<PostingsList> postingLists = collectPostings(query);
			Collections.sort(postingLists);
			PostingsList intermediatePosting = new PostingsList(postingLists.get(0));
			for (int i = 1; i < postingLists.size(); i++) {
				intermediatePosting = search.intersect(intermediatePosting, postingLists.get(i));
			}
			return intermediatePosting;
        }
        else if (queryType == Index.PHRASE_QUERY){
			ArrayList<PostingsList> postingLists = collectPostings(query);
            PostingsList intermediatePosting = new PostingsList(postingLists.get(0));
            for (int i = 1; i < postingLists.size(); i++) {
                intermediatePosting = search.phraseIntersect(intermediatePosting, postingLists.get(i));
            }
            return intermediatePosting;
        }
		else if (queryType == Index.RANKED_QUERY){
			ArrayList<PostingsList> postings = new ArrayList<PostingsList>(query.getTermsSize());
			for (int i = 0; i < query.getTermsSize(); i++) {
				PostingsList postingsList = new PostingsList(getPostings(query.getTerm(i)));
				//Speedup
//				if (postingsList.getiDF() < 1d){
//					continue;
//				}
				if (query.getWeight(query.getTerm(i)) == 0d) {
					double termScore = Math.log((double)docLengths.size() / postingsList.getDF());
					query.setWeight(query.getTerm(i), termScore);
					//Debug
					System.out.print(query.getTerm(i) + ": " + termScore);
					System.out.println("#docs: " + docLengths.size() + " DF: " + postingsList.getDF() + " ");
				}
				postings.add(postingsList);
				for (int j = 0; j < postingsList.size(); j++) {
					PostingsEntry entry = postingsList.get(j);
					//Speedup Index Elimination
					if (entry.getTf() > requiredNumberOfOccurances) {
						// Calculate and normalize by square root
						entry.setScore(((entry.getTf() * postingsList.getiDF()) / Math
								.sqrt(docLengths.get(entry.getDocID() + "")))
								* query.getWeight(query.getTerm(i)));
					} else {
						postingsList.getList().remove(j);
						j--;
					}
				}
			}
			PostingsList intermediatePosting = new PostingsList(postings.get(0));
			for (int i = 1; i < postings.size(); i++) {
				intermediatePosting = search.scoreUnion(intermediatePosting, postings.get(i));
			}
			if (rankingType == Index.TF_IDF) {
				intermediatePosting.sortList();
				System.out.println((System.nanoTime() - startTime)/1000 + " ms");
				return intermediatePosting;
			}
			else if (rankingType == Index.COMBINATION){
				intermediatePosting.addPageRankToScore(pageRank, docIDs);
				intermediatePosting.sortList();
				return intermediatePosting;
			}
			else{
				intermediatePosting.useOnlyPageRank(pageRank, docIDs);
				intermediatePosting.sortList();
				return intermediatePosting;
			}
		}
        return null;
    }

	@Override public boolean hasSavedIndex() {
		return false;
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

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
