import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BiGramIndex implements Index {

	private HashMap<String,PostingsList> index = new HashMap<String, PostingsList>(20000);
	private int prevDocId = -1;
	private String prevToken = null;

	@Override
	public void insert(String token, int docID, int offset) {
		if (docID == prevDocId){
			PostingsList postingsList = index.get(prevToken + " " + token);
			if (postingsList == null) {
				postingsList = new PostingsList();
				postingsList.addLast(new PostingsEntry(docID, offset - 1));
			} else {
				PostingsEntry postingsEntry = postingsList.getLast();
				if (postingsEntry.getDocID() != docID) {
					postingsList.addLast(new PostingsEntry(docID, offset - 1));
				} else {
					postingsEntry.addOccurance(offset);
				}
			}
			index.put(prevToken + " " + token, postingsList);
		}
		prevDocId = docID;
		prevToken = token;
	}

	@Override
	public void computeScore(){
		Iterator<String> dictionary = getDictionary();
		PostingsList term;
		while(dictionary.hasNext()){
			term = index.get(dictionary.next());
			term.computeIDF(docLengths.size()-1);
		}
	}

	@Override public HashMap<String, PostingsList> getIndexMap() {
		return index;
	}

	@Override public Iterator<String> getDictionary() {
		return index.keySet().iterator();
	}

	@Override public PostingsList getPostings(String token) {
		return index.get(token);
	}

	@Override public PostingsList search(Query query, int queryType, int rankingType, int structureType) {
		long startTime = System.nanoTime();
		Search search = new Search();
		if (queryType == Index.RANKED_QUERY){
			while(query.getTerm(1) != null && (query.getTerm(1).split(" ").length < 2)){
				query.addTerm(query.getTerm(0) + " " + query.getTerm(1));
				query.removeTerm(0);
			}
			query.removeTerm(0);
//			ArrayList<String> biGramQueryTerms = new ArrayList<String>(query.getTermsSize());
//			for (int i = 1; i < query.getTermsSize(); i++) {
//				biGramQueryTerms.add(query.getTerm(i-1) + " " + query.getTerm(i));
//			}

			ArrayList<PostingsList> postings = new ArrayList<PostingsList>(query.getTermsSize());
			for (int i = 0; i < query.getTermsSize(); i++) {
				PostingsList postingsList = new PostingsList(getPostings(query.getTerm(i)));
				if (query.getWeight(query.getTerm(i)) == null || query.getWeight(query.getTerm(i)) == 0d) {
					double termScore = Math.log(docLengths.size() / postingsList.getDF());
					query.setWeight(query.getTerm(i), termScore);
				}
				postings.add(postingsList);
				for (int j = 0; j < postingsList.size(); j++) {
					PostingsEntry entry = postingsList.get(j);
						entry.setScore(((entry.getTf() * postingsList.getiDF()) / docLengths.get(entry.getDocID() + ""))
								* query.getWeight(query.getTerm(i)));
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
		}
		return null;
	}

	@Override public boolean hasSavedIndex() {
		return false;
	}

	@Override public void cleanup() {

	}
}
