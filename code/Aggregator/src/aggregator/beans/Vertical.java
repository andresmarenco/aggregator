package aggregator.beans;

import java.io.Serializable;

public class Vertical implements Serializable {

	private static final long serialVersionUID = 201407032117L;
	
	private String id;
	private String name;
	private String description;
	private VerticalCategory category;
	private String fedWebCode;
	
	/**
	 * Default Constructor
	 */
	public Vertical() {
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
	 * @return the category
	 */
	public VerticalCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(VerticalCategory category) {
		this.category = category;
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
