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

package org.eclipse.rse.services.clientserver;

import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;

/**
 * @author mjberger
 *
 * Represents a search result in a virtual file.
 */
public final class VirtualSearchResult 
{
	private VirtualChild _virtualChild;
	private long _lineNumber;
	private String _matchingLine;
	
	public VirtualSearchResult(VirtualChild vc, long lineNumber, String matchingLine) 
	{
		_virtualChild = vc;
		_lineNumber = lineNumber;
		_matchingLine = matchingLine;
	}

	public long getLineNumber() 
	{
		return _lineNumber;
	}

	public String getMatchingLine() {
		return _matchingLine;
	}

	public VirtualChild getVirtualChild() {
		return _virtualChild;
	}
}