import java.io.Serializable;

/**
 * Created by Jonas on 01/03/2015.
 */
public class PageRankDocument implements Serializable, Comparable<PageRankDocument>{
	private int docName;
	private double value;

	public PageRankDocument(String docName, double value) {
		this.docName = Integer.parseInt(docName);
		this.value = value;
	}

	public PageRankDocument(int docName, double value) {
		this.docName = docName;
		this.value = value;
	}

	public int getDocName() {
		return docName;
	}

	public double getValue() {
		return value;
	}

	@Override public int compareTo(PageRankDocument o) {
		if (o.getValue() > getValue()){
			return -1;
		}
		else if (o.getValue() == getValue()){
			return 0;
		}
		else{
			return 1;
		}
	}
}
