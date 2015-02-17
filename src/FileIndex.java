import javafx.beans.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (configuration.isSavedIndex()){
			file = new File("C:\\Users\\Jonas\\Documents\\IRLab1\\dictionary.txt");
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
				ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
				dictionary = (HashSet<String>)objectInputStream.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		search = new Search();
		writtenFiles = 0;
		tempDictionary = new HashMap<String, PostingsList>();;
		this.numFiles = numFiles;
		addObserver(observer);
	}

	@Override public boolean hasSavedIndex(){
		return configuration.isSavedIndex();
	}

	@Override public void insert(String token, int docID, int offset) {
		if (configuration.isSavedIndex()){
			return;
		}
		int span = 1748;
		if (docID == numFiles - 2){
			span = 0;
		}
		// Write to file
		if (docID > writtenFiles + span){
			int counter = 0;
			int total = tempDictionary.size();
			Iterator<String> iterator = tempDictionary.keySet().iterator();
			String key;
			PostingsList postingsList ;
			ExecutorService pool = Executors.newFixedThreadPool(16);
			while (iterator.hasNext()) {
				key = iterator.next();
				if (isValidName(key)){
					postingsList = tempDictionary.get(key);
					pool.execute(new FileWriter(postingsList, directory + key + ".txt"));
				}
				counter++;
				setChanged();
				notifyObservers(counter * 100 / total);
			}
			tempDictionary = new HashMap<String, PostingsList>();
			writtenFiles = docID;
		}
		// Index term
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

	public void computeScore(){}

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
			file = new File("C:\\Users\\Jonas\\Documents\\IRLab1\\dictionary.txt");
			fileOutputStream = new java.io.FileOutputStream(file, false);
			outputBuffer = new BufferedOutputStream(fileOutputStream);
			objectStream = new ObjectOutputStream(outputBuffer);
			objectStream.writeObject(dictionary);
			objectStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isValidName(String text){
		if (text.length() > 200){
			return false;
		}
		Pattern pattern = Pattern.compile(
				"# Match a valid Windows filename (unspecified file system).          \n" +
						"^                                # Anchor to start of string.        \n" +
						"(?!                              # Assert filename is not: CON, PRN, \n" +
						"  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
						"    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
						"    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
						"  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
						"  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
						"  $                              # and end of string                 \n" +
						")                                # End negative lookahead assertion. \n" +
						"[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
						"[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
						"$                                # Anchor to end of string.            ",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
		Matcher matcher = pattern.matcher(text);
		boolean isMatch = matcher.matches();
		return isMatch;
	}
}
