package aggregator.verticalwrapper.srr;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aggregator.beans.QueryResult;
import aggregator.beans.Vertical;
import aggregator.util.IterableNodeList;

public class XMLSRRScript implements SRRScript<NodeList> {
	
	private List<QueryResult> srrResult;
	private XMLCommandParser xmlCommandParser;
	private Vertical vertical;
	
	/**
	 * Default Constructor
	 */
	public XMLSRRScript(Vertical vertical) {
		this.srrResult = new ArrayList<QueryResult>();
		this.xmlCommandParser = new XMLCommandParser();
		this.vertical = vertical;
	}

	@Override
	public List<QueryResult> execute(NodeList queryResult, List<String> script) {
		for(Node node : new IterableNodeList(queryResult)) {
			QueryResult nodeResult = new QueryResult(vertical);
			
			for(String command : script) {
				xmlCommandParser.parseAndExecute(node, nodeResult, command);
			}
			
			srrResult.add(nodeResult);
		}
		
		return srrResult;
	}

}
