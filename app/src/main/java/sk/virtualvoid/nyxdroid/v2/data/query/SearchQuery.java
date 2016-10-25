package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author Juraj
 *
 */
public class SearchQuery implements ITaskQuery {
	public String Nick;
	public String Phrase;
	public int Position;
	
	public SearchQuery() {
		this.Position = 1;
	}
}
