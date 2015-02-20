import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Jonas on 03/02/2015.
 */
public class Search {
	public PostingsList intersect(PostingsList p1, PostingsList p2){
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

	public PostingsList phraseIntersect(PostingsList p1, PostingsList p2){
		PostingsList answer = new PostingsList();
		int i = 0, j = 0;
		while (i < p1.size() && j < p2.size()){
			if(p1.get(i).getDocID() == p2.get(j).getDocID()){
				Iterator<Integer> iterator = p1.get(i).getIterator();
				boolean entryCreated = false;
				while (iterator.hasNext()){
					int offset = iterator.next() + 1;
					if (p2.get(j).hasOffset(offset)){
						if (!entryCreated) {
							answer.addLast(new PostingsEntry(p2.get(j).getDocID(), offset));
							entryCreated = true;
						}
						else{
							answer.get(answer.size()-1).addOccurance(offset);
						}
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

	public PostingsList scoreUnion(PostingsList p1, PostingsList p2){
		PostingsList answer = new PostingsList();
		int i = 0, j = 0;
		while (i < p1.size() && j < p2.size()){
			PostingsEntry postingsEntry;
			if(p1.get(i).getDocID() == p2.get(j).getDocID()){
				postingsEntry = new PostingsEntry(p1.get(i).getDocID(), 0);
				postingsEntry.setScore(p1.get(i).getScore() + p2.get(j).getScore());
				answer.addLast(postingsEntry);
				i++;
				j++;
			}
			else if (p1.get(i).getDocID() < p2.get(j).getDocID()){
				answer.addLast(p1.get(i));
				i++;
			}
			else{
				answer.addLast(p2.get(j));
				j++;
			}
		}
		while (i < p1.size()) {
			answer.addLast(p1.get(i));
			i++;
		}
		while (j < p2.size()) {
			answer.addLast(p2.get(j));
			j++;
		}
		return answer;
	}
}
