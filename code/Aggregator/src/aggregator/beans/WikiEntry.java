package aggregator.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

public class WikiEntry {
	private long id;
	private int namespace;
	private String title;
	private String redirect;
	private List<WikiEntryRevision> revisions;
	private boolean contentPage;
	private final MediaWikiParserFactory wikiParserFactory = new MediaWikiParserFactory(Language.english);
	private final MediaWikiParser wikiParser = wikiParserFactory.createParser();
	
	public WikiEntry() {
		this.revisions = new ArrayList<WikiEntryRevision>();
		this.contentPage = true;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the namespace
	 */
	public int getNamespace() {
		return namespace;
	}

	/**
	 * @param ns the namespace to set
	 */
	public void setNamespace(int ns) {
		this.namespace = ns;
		if(ns == NAMESPACE_CATEGORY) {
			title = StringUtils.trim(StringUtils.substringAfter(title, ":"));
		}
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
		if(namespace == NAMESPACE_CATEGORY) {
			this.title = StringUtils.trim(StringUtils.substringAfter(title, ":"));
		}
	}

	/**
	 * @return the redirect
	 */
	public String getRedirect() {
		return redirect;
	}
	
	public boolean isRedirect() {
		return redirect != null;
	}

	/**
	 * @param redirect the redirect to set
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	/**
	 * @return the revisions
	 */
	public List<WikiEntryRevision> getRevisions() {
		return revisions;
	}

	/**
	 * @param revisions the revisions to set
	 */
	public void setRevisions(List<WikiEntryRevision> revisions) {
		this.revisions = revisions;
	}
	
	public WikiEntryRevision getLastRevision() {
		return this.getRevisions().get(this.getRevisions().size()-1);
	}

	/**
	 * @return the contentPage
	 */
	public boolean isContentPage() {
		return contentPage;
	}

	/**
	 * @param contentPage the contentPage to set
	 */
	public void setContentPage(boolean contentPage) {
		this.contentPage = contentPage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WikiEntry [id=" + id + ", ns=" + namespace + ", title=" + title
				+ ", redirect=" + redirect + ", revisions=" + revisions
				+ "]";
	}
	
	@Override
	public WikiEntry clone() throws CloneNotSupportedException {
		WikiEntry clone = new WikiEntry();
		clone.id = id;
		clone.namespace = namespace;
		clone.redirect = redirect;
		clone.title = title;
		clone.contentPage = contentPage;
		for(WikiEntryRevision revision : revisions) {
			WikiEntryRevision cloneRevision = clone.newEntryRevision();
			cloneRevision.id = revision.id;
			cloneRevision.text = revision.text;
			for(String category : revision.categories) {
				cloneRevision.categories.add(new String(category));
			}
			
			clone.revisions.add(cloneRevision);
		}
		return clone;
	}
	
	public WikiEntryRevision newEntryRevision() { return new WikiEntryRevision(); }
	

	public class WikiEntryRevision {
		private long id;
		private String text;
		private List<String> categories;
		
		public WikiEntryRevision() {
			this.categories = new ArrayList<String>();
		}
		
		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}



		/**
		 * @param id the id to set
		 */
		public void setId(long id) {
			this.id = id;
		}



		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}



		/**
		 * @return the categories
		 */
		public List<String> getCategories() {
			return categories;
		}

		/**
		 * @param categories the categories to set
		 */
		public void setCategories(List<String> categories) {
			this.categories = categories;
		}
		
//		public void parseText() {
//			ParsedPage parsedPage = wikiParser.parse(text);
//			for(Link category : parsedPage.getCategories()) {
//				String catText = StringUtils.substringBefore(StringUtils.substringAfter(category.getText(), ":"), "|").replaceAll("\\u00A0", " ").trim();
//				if(StringUtils.isNotEmpty(catText)) {
//					this.categories.add(catText);
//				}
//			}
//			
//			this.text = parsedPage.getText().replaceAll("\\u00A0", " ").replace("&nbsp;", "\n").trim();
//		}

		/**
		 * @param text the text to set
		 */
		public void setText(String text) {
//			this.text = text;
			if(!WikiEntry.this.isRedirect()) {
				switch(WikiEntry.this.getNamespace()) {
				case NAMESPACE_ARTICLE:
				case NAMESPACE_CATEGORY: {
					ParsedPage parsedPage = wikiParser.parse(text);
					for(Link category : parsedPage.getCategories()) {
						String catText = StringUtils.substringBefore(StringUtils.substringAfter(category.getText(), ":"), "|").replaceAll("\\u00A0", " ").trim();
						if(StringUtils.isNotEmpty(catText)) {
							this.categories.add(catText);
						}
					}
					
					this.text = parsedPage.getText().replaceAll("\\u00A0", " ").replace("&nbsp;", "\n").trim();
					break;
				}
				
				default: {
					this.text = text;
				}
				}
			} else {
				this.text = text;
			}
//			
//			
////			StringBuilder parsedText = new StringBuilder();
////			if(!WikiEntry.this.isRedirect()) {
////				switch(WikiEntry.this.getNamespace()) {
////				case NAMESPACE_CATEGORY: {
////					String[] lines = StringEscapeUtils.unescapeHtml4(text).split("\n");
////					for(String line : lines) {
////						if(line.startsWith("[[Category:")) {
////							this.categories.add(this.parseCategoryName(line));
////						}
////					}
////					break;
////				}
////				
////				
////				case NAMESPACE_ARTICLE: {
//////					boolean addMore = true;
//////					List<String> lines = Arrays.asList(StringEscapeUtils.unescapeHtml4(text
//////							.replaceAll("(\\=\\=(\\=)*)", "")
//////							.replaceAll("(\\<\\!\\-\\-)(.)+(\\-\\-\\>)", "")
//////							.replace("wikt:", " ")
//////							.replace("|", " ")
//////							.trim())
//////							.split("\n"));
//////					
//////					ListIterator<String> iterator = lines.listIterator();
//////					while(iterator.hasNext()) {
//////						String line = iterator.next().trim();
//////						
//////						if(line.startsWith("[[Category:")) {
//////							categories.add(this.parseCategoryName(line));
//////						} else if(line.equals("External links")) {
//////							addMore = false;
//////						} else {
//////							if(addMore) {
//////								int currentIndex = iterator.nextIndex();
//////								String removeMeta = this.removeMeta(iterator, line, "{{", "}}");
//////								if(removeMeta == null) {
//////									iterator = lines.listIterator(currentIndex);
//////								} else {
//////									line = removeMeta;
//////								}
//////								
//////								line = line
//////										.replaceAll("\\<ref(.*)\\>", " ")
//////										.replace("</ref>", " ");
//////								
//////								if(!((line.startsWith("[[File:")) || (line.startsWith("[[Image:")))) {
//////									line = line
//////											.replace('[', ' ')
//////											.replace(']', ' ')
//////											.replaceAll("\\s+", " ")
//////											.trim();
//////									
//////									if(StringUtils.isNotBlank(line)) {
//////										parsedText.append(line).append("\n");
//////									}
//////								}
//////							}
//////						}
//////					}
//////					
////					break;
////				}
////				
////				default: {
////					parsedText.append(text);
////					break;
////				}
////				}
////			} else {
////				parsedText.append(text);
////			}
////			
////			this.text = parsedText.toString();
		}
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "WikiEntryRevision [id=" + id + ", text=" + text
					+ ", categories=" + categories + "]";
		}
		
		
		
		private String removeMeta(Iterator<String> iterator, String line, String start, String end) {
			int beginIndex, endIndex;
			while((beginIndex = line.lastIndexOf(start)) > -1) {
				while((endIndex = line.indexOf(end, beginIndex)) == -1) {
					if(!iterator.hasNext()) {
//						System.out.println(WikiEntry.this.id);
						return null;
					}
					line += " " + iterator.next().trim();
					beginIndex = line.lastIndexOf(start);
				}
				
				line = line.substring(0, beginIndex) + " " + line.substring(endIndex + (end.length()));
			}
			
			return line;
		}
		
		
		
		private String parseCategoryName(String line) {
			line = StringUtils.removeStart(line, "[[Category:");
			line = StringUtils.removeEnd(line, "]]");
			line = StringUtils.substringBefore(line, "|");
			line = StringUtils.substringBefore(line, "*");
			
			return line;
		}
	}

	public static final int NAMESPACE_MEDIA = -2;
	public static final int NAMESPACE_SPECIAL= -1;
	public static final int NAMESPACE_ARTICLE= 0;
	public static final int NAMESPACE_TALK = 1;
	public static final int NAMESPACE_USER = 2;
	public static final int NAMESPACE_USER_TALK = 3;
	public static final int NAMESPACE_WIKIPEDIA = 4;
	public static final int NAMESPACE_WIKIPEDIA_TALK = 5;
	public static final int NAMESPACE_FILE = 6;
	public static final int NAMESPACE_FILE_TALK = 7;
	public static final int NAMESPACE_MEDIA_WIKI= 8;
	public static final int NAMESPACE_MEDIA_WIKI_TALK = 9;
	public static final int NAMESPACE_TEMPLATE = 10;
	public static final int NAMESPACE_TEMPLATE_TALK = 11;
	public static final int NAMESPACE_HELP = 12;
	public static final int NAMESPACE_HELP_TALK = 13;
	public static final int NAMESPACE_CATEGORY = 14;
	public static final int NAMESPACE_CATEGORY_TALK= 15;
	public static final int NAMESPACE_PORTAL = 100;
	public static final int NAMESPACE_PORTAL_TALK = 101;
	public static final int NAMESPACE_BOOK = 108;
	public static final int NAMESPACE_BOOK_TALK = 109;
	public static final int NAMESPACE_DRAFT = 118;
	public static final int NAMESPACE_DRAFT_TALK = 119;
	public static final int NAMESPACE_EDUCATION_PROGRAM = 446;
	public static final int NAMESPACE_EDUCATION_PROGRAM_TALK = 447;
	public static final int NAMESPACE_TIMED_TEXT = 710;
	public static final int NAMESPACE_TIMED_TEXT_TALK = 711;
	public static final int NAMESPACE_MODULE = 828;
	public static final int NAMESPACE_MODULE_TALK = 829;
	public static final int NAMESPACE_TOPIC = 2600;
}
