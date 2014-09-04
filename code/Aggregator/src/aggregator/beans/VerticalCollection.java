package aggregator.beans;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

public class VerticalCollection implements Serializable {

	private static final long serialVersionUID = 201408120339L;
	
	private String id;
	private String name;
	private List<VerticalCollectionData> verticals;
	
	/**
	 * Default Constructor
	 */
	public VerticalCollection() {
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the verticals
	 */
	public List<VerticalCollectionData> getVerticals() {
		return verticals;
	}

	/**
	 * @param verticals the verticals to set
	 */
	public void setVerticals(List<VerticalCollectionData> verticals) {
		this.verticals = verticals;
	}
	
	public String getVerticalCode(Vertical vertical) {
		return this.getVerticalCode(vertical.getId());
	}
	
	public String getVerticalCode(String verticalId) {
		String result = null;
		if(this.verticals != null) {
			for(VerticalCollectionData data : this.verticals) {
				if(data.getVertical().getId().equalsIgnoreCase(verticalId)) {
					result = MessageFormat.format("{0}-{1}", this.id, data.getVerticalCollectionId());
					break;
				}
			}
		}
		return result;
	}
	
	public Vertical getVertical(String verticalId) {
		Vertical result = null;
		if(this.verticals != null) {
			for(VerticalCollectionData data : this.verticals) {
				if(data.getVertical().getId().equalsIgnoreCase(verticalId)) {
					result = data.getVertical();
					break;
				}
			}
		}
		return result;
	}
	
	public VerticalCollectionData getVerticalData(String verticalId) {
		VerticalCollectionData result = null;
		if(this.verticals != null) {
			for(VerticalCollectionData data : this.verticals) {
				if(data.getVertical().getId().equalsIgnoreCase(verticalId)) {
					result = data;
					break;
				}
			}
		}
		return result;
	}
	
	public Vertical getVerticalByCollectionId(String collectionId) {
		Vertical result = null;
		if(this.verticals != null) {
			for(VerticalCollectionData data : this.verticals) {
				if(data.getVerticalCollectionId().equalsIgnoreCase(collectionId)) {
					result = data.getVertical();
					break;
				}
			}
		}
		return result;
	}
	
	
	public static class VerticalCollectionData {
		private String verticalCollectionId;
		private Vertical vertical;
		private double sizeFactor;
		private int sampleSize;
		
		/**
		 * Default Constructor
		 */
		public VerticalCollectionData() {
		}
		
		/**
		 * Constructor with fields
		 * @param verticalCollectionId Vertical collection Id
		 * @param vertical Vertical
		 */
		public VerticalCollectionData(String verticalCollectionId, Vertical vertical) {
			super();
			this.verticalCollectionId = verticalCollectionId;
			this.vertical = vertical;
		}
		
		/**
		 * Constructor with fields
		 * @param verticalCollectionId Vertical collection Id
		 * @param vertical Vertical
		 * @param sizeFactor Size Factor
		 * @param sampleSize Sample Size
		 */
		public VerticalCollectionData(String verticalCollectionId, Vertical vertical, double sizeFactor, int sampleSize) {
			super();
			this.verticalCollectionId = verticalCollectionId;
			this.vertical = vertical;
			this.sizeFactor = sizeFactor;
			this.sampleSize = sampleSize;
		}



		/**
		 * @return the verticalCollectionId
		 */
		public String getVerticalCollectionId() {
			return verticalCollectionId;
		}
		
		/**
		 * @param verticalCollectionId the verticalCollectionId to set
		 */
		public void setVerticalCollectionId(String verticalCollectionId) {
			this.verticalCollectionId = verticalCollectionId;
		}
		
		/**
		 * @return the vertical
		 */
		public Vertical getVertical() {
			return vertical;
		}
		
		/**
		 * @param vertical the vertical to set
		 */
		public void setVertical(Vertical vertical) {
			this.vertical = vertical;
		}

		/**
		 * @return the sizeFactor
		 */
		public double getSizeFactor() {
			return sizeFactor;
		}

		/**
		 * @param sizeFactor the sizeFactor to set
		 */
		public void setSizeFactor(double sizeFactor) {
			this.sizeFactor = sizeFactor;
		}

		/**
		 * @return the sampleSize
		 */
		public int getSampleSize() {
			return sampleSize;
		}

		/**
		 * @param sampleSize the sampleSize to set
		 */
		public void setSampleSize(int sampleSize) {
			this.sampleSize = sampleSize;
		}
	}
}
