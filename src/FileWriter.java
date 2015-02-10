import java.io.*;

/**
 * Created by Jonas on 09/02/2015.
 */
public class FileWriter implements Runnable {

	PostingsList postingsList;
	String fileName;

	public FileWriter(PostingsList postingsList, String fileName){
		this.postingsList = postingsList;
		this.fileName = fileName;
	}

	@Override public void run() {
		writeToFile(postingsList, fileName);
	}

	private void writeToFile(PostingsList postingsList, String fileName){
		File file = new File(fileName);
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
	}
}
