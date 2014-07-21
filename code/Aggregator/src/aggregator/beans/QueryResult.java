package aggregator.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueryResult implements Serializable {

	private static final long serialVersionUID = 201407020752L;
	
	private String id;
	private String title;
	private String summary;
	private String info;
	private List<String> authors;
	private List<String> keywords;
	private Vertical vertical;
	
	
	/**
	 * Default Constructor
	 */
	public QueryResult() {
		this.authors = new ArrayList<String>();
	}
	
	

	/**
	 * Default Constructor
	 * @param vertical Source Vertical
	 */
	public QueryResult(Vertical vertical) {
		super();
		this.vertical = vertical;
		this.authors = new ArrayList<String>();
		this.keywords = new ArrayList<String>();
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}


	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}


	/**
	 * @return the vertical
	 */
	public Vertical getVertical() {
		return vertical;
	}



	/**
	 * @return the authors
	 */
	public List<String> getAuthors() {
		return authors;
	}



	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}



	/**
	 * @return the keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}



	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}



	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}



	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

}
