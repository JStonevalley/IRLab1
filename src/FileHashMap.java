import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Jonas on 29/01/2015.
 */
public class FileHashMap<K, V> extends HashMap<K, V>{

	private HashSet<K> keys;
	private String directory;
	private ObjectOutputStream writer;
	private ObjectInputStream reader;

	public FileHashMap(String directory){
		keys = new HashSet<K>();
		this.directory = directory;
	}
	@Override
	public V put(K key, V object){
		try {
			File objectFile = new File(directory + key.toString() + ".txt");
			if(!objectFile.exists()) {
				objectFile.createNewFile();
			}
			writer = new ObjectOutputStream(new BufferedOutputStream((new FileOutputStream(objectFile, false))));
			writer.writeObject(object);
			writer.close();
			keys.add(key);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

	@Override
	public V get(Object key){
		V object = null;
		if (keys.contains(key)) {
			try {
				reader = new ObjectInputStream(
						new BufferedInputStream((new FileInputStream((directory + key.toString() + ".txt")))));
				object = (V) reader.readObject();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	@Override
	public Set<K> keySet(){
		return keys;
	}

	@Override public int size() {
		return keys.size();
	}

	@Override public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	@Override public V remove(Object key) {
		V object = get(key);
		keys.remove(key);
		new File(directory + key.toString()).delete();
		return object;
	}

	@Override public void clear() {
		Iterator<K> iterator = keySet().iterator();
		while (iterator.hasNext()){
			remove(iterator.next());
		}
	}
}
