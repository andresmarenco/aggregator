package aggregator.beans;

import java.io.Serializable;

public class VerticalCategory implements Serializable {

	private static final long serialVersionUID = 201407090414L;
	
	private String id;
	private String name;
	private String description;
	private String fedWebCode;
	
	/**
	 * Default Constructor
	 */
	public VerticalCategory() {
	}

	/**
	 * Default Constructor
	 * @param id Category Id
	 */
	public VerticalCategory(String id) {
		super();
		this.id = id;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the fedWebCode
	 */
	public String getFedWebCode() {
		return fedWebCode;
	}

	/**
	 * @param fedWebCode the fedWebCode to set
	 */
	public void setFedWebCode(String fedWebCode) {
		this.fedWebCode = fedWebCode;
	}
	
}
