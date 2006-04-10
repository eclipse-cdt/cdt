/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.search;

import org.eclipse.rse.services.clientserver.SystemSearchString;


public interface IHostSearchResult
{
	  public static final String SEARCH_RESULT_DELIMITER = ":SEARCH";
	   public static final String SEARCH_RESULT_OPEN_DELIMITER = "<";
	   public static final String SEARCH_RESULT_CLOSE_DELIMITER = ">";
	   public static final String SEARCH_RESULT_INDEX_DELIMITER = ":";
	   
	public void setParent(Object parent);   
	   
	public Object getParent();
	
	
	public int getLine();
	
	/**
	 * Gets the search string that this result matches.
	 * @return the search string.
	 */
	public SystemSearchString getMatchingSearchString();
	
	/**
	 * Gets the path to the file that this output references if it references any.  It may return null if
	 * 	no such association exists.  This may be used to jump to an editor from a view which displays 
	 * 	this
	 * 
	 * @return the path of the referenced file if there is one
	 */
	public String getAbsolutePath();
	
	/**
	 * Gets the text to display for a search result.
	 * @return the text.
	 */
	public String getText();
	
	/**
	 * Gets the index of this search result in the context of its parent.
	 * @return the index.
	 */
	public int getIndex();

	
	/**
	 * Add a match to the result. A match comprises a char start offset and a char end offset, both
	 * relative to the beginning of the file. The matches are added in order.
	 * @param startOffset the char start offset, from the beginning of the file.
	 * @param endOffset the char end offset, from the beginning of the file.
	 */
	public void addMatch(int startOffset, int endOffset);
	
	/**
	 * Gets the number of matches in this line.
	 * @return the number of matches.
	 */
	public int numOfMatches();
	
	/**
	 * Gets the char start offset for the given match index.
	 * @param matchIndex the match index. For example, to get the start offset for the first match, specify 0.
	 * @return the char start offset. 
	 */
	public int getCharStart(int matchIndex);
	
	/**
	 * Gets the char end offset for the given match index.
	 * @param matchIndex the match index. For example, to get the end offset for the first match, specify 0.
	 * @return the char end offset. 
	 */
	public int getCharEnd(int matchIndex);
	
	
	public IHostSearchResultConfiguration getConfiguration();
}