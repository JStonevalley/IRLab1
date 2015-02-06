import java.io.Serializable;

/**
 * Created by Jonas on 06/02/2015.
 */
public class Configuration implements Serializable {
	private boolean savedIndex;

	public Configuration(boolean savedIndex) {
		this.savedIndex = savedIndex;
	}

	public boolean isSavedIndex() {
		return savedIndex;
	}

	public void setSavedIndex(boolean savedIndex) {
		this.savedIndex = savedIndex;
	}
}
