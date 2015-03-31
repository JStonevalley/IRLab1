/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */

import java.util.*;

public class Query {

	private final double beta = 1d;
    private ArrayList<String> terms = new ArrayList<String>();
	private HashMap<String, Double> weights = new HashMap<String, Double>();
	private final double WEIGHT_THREASHOLD = 0d;
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }

    public int getTermsSize(){
        return terms.size();
    }

    public int getWeightsSize(){
        return weights.size();
    }

	public void addTerm(String term){
		terms.add(term);
		weights.put(term, 0d);
	}

	public String removeTerm(int i){
		return terms.remove(i);
	}

    public String getTerm(int i){
        return terms.get(i);
    }

    public Double getWeight(String key){
		return  weights.get(key);
    }

	public void setWeight(String key, double value){ weights.put(key, value);}

    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
		String term = tok.nextToken();
		terms.add(term);
		weights.put(term, 0d);
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (ArrayList<String>) terms.clone();
	queryCopy.weights = (HashMap<String, Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
		HashMap<String,PostingsList> index = indexer.getIndex().getIndexMap();
		ArrayList<Integer> relevantDocs = new ArrayList<Integer>(docIsRelevant.length);
		for (int i = 0; i < docIsRelevant.length; i++) {
			if(docIsRelevant[i]){
				relevantDocs.add(results.get(i).getDocID());
			}
		}
		Iterator<String> dictionary = index.keySet().iterator();
		while (dictionary.hasNext()){
			String term = dictionary.next();
			double weight = 0d;
			if (weights.containsKey(term)){
				weight = weights.get(term);
			}
			PostingsList postingsList = index.get(term);
//			if (postingsList.getiDF() < 1d){
//				continue;
//			}
			for (PostingsEntry entry : postingsList.getList()){
				if (relevantDocs.contains(entry.getDocID())){
					weight += beta * ((entry.getTf() * postingsList.getiDF() / indexer.getIndex().docLengths.get(entry.getDocID() + "")));
				}
			}
			if (weight > WEIGHT_THREASHOLD){
				if (!weights.containsKey(term)) {
					terms.add(term);
				}
				weights.put(term, weight);
			}
		}
		Iterator<String> queryTerms = weights.keySet().iterator();
		System.out.println(weights.size());
		int numTerms = weights.size();
		String term;
		while(queryTerms.hasNext()){
			term = queryTerms.next();
			weights.put(term, weights.get(term)/numTerms);
		}
    }
}

    
