package aggregator.beans;

import java.io.Serializable;
import java.util.List;

public class Vertical implements Serializable {

	private static final long serialVersionUID = 201407032117L;
	
	private String id;
	private String name;
	private String description;
	private List<VerticalCategory> categories;
	
	/**
	 * Default Constructor
	 */
	public Vertical() {
	}
	
	
	/**
	 * Constructor with Id
	 * @param id Vertical Id
	 */
	public Vertical(String id) {
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
	 * @return the categories
	 */
	public List<VerticalCategory> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(List<VerticalCategory> categories) {
		this.categories = categories;
	}
	
}
