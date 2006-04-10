/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.files.core.subsystems;

import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResult;


/**
 * This interface represents a handle to a search result.
 */
public interface IRemoteSearchResult extends IRemoteLineReference, IHostSearchResult 
{

  
 

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
	 * Gets the search string that this result matches.
	 * @return the search string.
	 */
	public SystemSearchString getMatchingSearchString();
	
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
}