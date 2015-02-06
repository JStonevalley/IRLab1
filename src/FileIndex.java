import javafx.beans.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.Observable;

/**
 * Created by Jonas on 03/02/2015.
 */
public class FileIndex extends Observable implements Index {

	private String directory = "C:\\Users\\Jonas\\Documents\\IRLab1\\postings1\\";
	private HashSet<String> dictionary;
	private HashMap<String, PostingsList> tempDictionary;
	private Search search;
	private int writtenFiles;
	private int numFiles;
	private Configuration configuration;

	public FileIndex(int numFiles, Observer observer){
		File file = new File("C:\\Users\\Jonas\\Documents\\IRLab1\\configuration.txt");
		if (file.exists()){
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
				ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
				configuration = (Configuration)objectInputStream.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else{
			configuration = new Configuration(false);
		}
		if (!configuration.isSavedIndex()){
			try {
				FileUtils.deleteDirectory(new File(directory));
				new File(directory).mkdir();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dictionary = new HashSet<String>();
		Collection<File> files = FileUtils.listFiles(new File(directory), FileFilterUtils.trueFileFilter(), null);
		for (File term : files){
			dictionary.add(term.getName().substring(0, term.getName().length()-4));
		}
		search = new Search();
		writtenFiles = 0;
		tempDictionary = new HashMap<String, PostingsList>();;
		this.numFiles = numFiles;
		addObserver(observer);
	}

	@Override public void insert(String token, int docID, int offset) {
		if (configuration.isSavedIndex()){
			return;
		}
		int span = 1748;
		if (docID == numFiles - 2){
			span = 0;
		}
		if (docID > writtenFiles + span){
			int counter = 0;
			int total = tempDictionary.size();
			Iterator<String> iterator = tempDictionary.keySet().iterator();
			String key;
			PostingsList postingsList ;
			while (iterator.hasNext()){
				key = iterator.next();
				postingsList = tempDictionary.get(key);
				File file = new File(directory + key + ".txt");
				try {
					ObjectOutputStream objectStream;
					if (file.exists()){
						FileOutputStream fileOutputStream = new FileOutputStream(file, true);
						BufferedOutputStream outputBuffer = new BufferedOutputStream(fileOutputStream);
						objectStream = new AppendableObjectOutputStream(outputBuffer);
					}
					else {
						FileOutputStream fileOutputStream = new FileOutputStream(file, true);
						BufferedOutputStream outputBuffer = new BufferedOutputStream(fileOutputStream);
						objectStream = new ObjectOutputStream(outputBuffer);
					}
					objectStream.writeObject(postingsList);
					objectStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				counter++;
				setChanged();
				notifyObservers(counter * 100 / total);
			}
			tempDictionary = new HashMap<String, PostingsList>();
			writtenFiles = docID;
		}
		PostingsList postingsList = tempDictionary.get(token);
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
		tempDictionary.put(token, postingsList);
		dictionary.add(token);
	}

	@Override public Iterator<String> getDictionary() {
		return dictionary.iterator();
	}

	@Override public PostingsList getPostings(String token) {
		if (!dictionary.contains(token)){
			return null;
		}
		File file = new File(directory + token + ".txt");
		PostingsList postings = new PostingsList();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
			PostingsList tempPosting = null;
			while ((tempPosting = (PostingsList)objectInputStream.readObject()) != null) {
				postings.appendPostingsList(tempPosting);
			}
		} catch (EOFException e){
			return postings;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return postings;
	}

	@Override public PostingsList search(Query query, int queryType, int rankingType, int structureType) {
		if (query.getTermsSize() == 1){
			return getPostings(query.getTerm(0));
		}
		else if (query.getTermsSize() > 1 && queryType == Index.INTERSECTION_QUERY){
			ArrayList<PostingsList> postingLists = collectPostings(query);
			Collections.sort(postingLists);
			PostingsList intermediatePosting = postingLists.remove(postingLists.size() - 1);
			while (!postingLists.isEmpty()) {
				PostingsList shortestPosting = postingLists.remove(postingLists.size() - 1);
				intermediatePosting = search.intersect(intermediatePosting, shortestPosting);
			}
			return intermediatePosting;
		}
		else if (query.getTermsSize() > 1 && queryType == Index.PHRASE_QUERY){
			ArrayList<PostingsList> postingLists = collectPostings(query);
			PostingsList intermediatePosting;
			intermediatePosting = postingLists.get(0);
			for (int i = 1; i < postingLists.size(); i++) {
				intermediatePosting = search.phraseIntersect(intermediatePosting, postingLists.get(i));
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

	@Override public void cleanup(){
		configuration.setSavedIndex(true);
		File file = new File("C:\\Users\\Jonas\\Documents\\IRLab1\\configuration.txt");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			BufferedOutputStream outputBuffer = new BufferedOutputStream(fileOutputStream);
			ObjectOutputStream objectStream = new ObjectOutputStream(outputBuffer);
			objectStream.writeObject(configuration);
			objectStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
