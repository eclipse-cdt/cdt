/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver.search;

/**
 * This class holds information about a particular match.
 */
public class SystemSearchMatch {
	
	private int startOffset;
	private int endOffset;

	/**
	 * Constructor for system search match.
	 * @param startOffset the offset from the beginning of the file where the match begins.
	 * @param endOffset the offset from the beginning of the file where the match ends.
	 */
	public SystemSearchMatch(int startOffset, int endOffset) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}
	
	/**
	 * Gets offset from the beginning of the file where the match begins.
	 * @return the offset where the match begins.
	 */
	public int getStartOffset() {
		return startOffset;
	}
	
	/**
	 * Gets offset from the beginning of the file where the match ends.
	 * @return the offset where the match ends.
	 */
	public int getEndOffset() {
		return endOffset;
	}
	
	/**
	 * Gets the length of the match. Start offset subtracted from end offset plus 1.
	 * @return the length of the match.
	 */
	public int getLength() {
		//TODO: check this
		return (endOffset - startOffset + 1);
	}
}