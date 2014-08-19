package aggregator.beans;

import java.io.Serializable;

public class VerticalCollection implements Serializable {

	private static final long serialVersionUID = 201408120339L;
	
	private String id;
	private String name;
	
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
	
}
