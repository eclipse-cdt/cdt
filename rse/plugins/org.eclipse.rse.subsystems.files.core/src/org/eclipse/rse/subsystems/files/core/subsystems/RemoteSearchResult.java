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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;


/**
 * The RemoteSearchResultImpl class is an implementation of IRemoteSearchResult.
 * It is used for storing information about a particular search result.
 */
public class RemoteSearchResult implements IAdaptable, IRemoteSearchResult {
	

	private String _text;
	private int _line = 0;
	private int _index = -1;

	private Object _parent;
	private SystemSearchString _matchingSearchString;
	private IHostSearchResultConfiguration _configuration;
	private List _matches;
	
	protected class RemoteSearchResultMatch {
		
		private int _startOffset = -1;
		private int _endOffset = -1;
		
		private RemoteSearchResultMatch(int startOffset, int endOffset) {
			this._startOffset = startOffset;
			this._endOffset = endOffset;
		}
	}

	/**
	 * Constructor to create a result.
	 * @param parent container of the result.
	 * @param searchString the search string for which the result was produced.
	 */
	public RemoteSearchResult(IHostSearchResultConfiguration configuration, Object parent, SystemSearchString searchString) {
		_parent = parent;
		_matchingSearchString = searchString;
		_matches = new ArrayList();
		_configuration = configuration;
	}

	/**
	 * Sets the displayable text for the search result.
	 * @param text the displayable text.
	 */
	public void setText(String text) {
		_text = text;
	}

	/**
	 * Sets the associated line number for the result.
	 * @param line the line number.
	 */
	public void setLine(int line) {
		_line = line;
	}
	
	/**
	 * Sets the index of the search result in the context of its parent.
	 * @param index the index.
	 */
	public void setIndex(int index) {
	    _index = index;
	}
	
    /**
     * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult#getText()
     */
    public String getText() {
        return _text;
    }
    
    /**
     * @see org.eclipse.rse.core.subsystems.IRemoteLineReference#getLine()
     */
    public int getLine() {
        return _line;
    }

    /**
     * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult#getIndex()
     */
    public int getIndex() {
        return _index;
    }
    
	/**
	 * Gets the absolute path of the file for which the result was found.
	 * @return the absolute path of the file.
	 */
	public String getAbsolutePath() {
		
		if (_parent instanceof IRemoteFile) {
			return ((IRemoteFile)_parent).getAbsolutePath();
		} 
		else {
			return null;
		}
	}

	/**
	 * Gets the containing object for the search result.
	 * @return the containing parent.
	 */
	public Object getParent() {
		return _parent;
	}

	/**
	 * Gets the associated adapter for this search result.
	 * @param adapterType the adapter type.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Returns children of the search result, if applicable.
	 * @return <code>null</code> for this implementation.
	 */
	public Object[] getChildren() {
		return null;
	}

	/**
	 * Indicates whether this search result has children
	 * @return <code>false</code> since there are no children.
	 */
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Gets the displayable label for this output. Calls getText().
	 * @return the label.
	 */
	public String getLabel() {
		return getText();
	}
	
	/**
	 * Gets the search string that this result matches.
	 * @return the search string.
	 */
	public SystemSearchString getMatchingSearchString() {
		return _matchingSearchString;
	}
	
	/**
	 * Add a match to the result. A match comprises a char start offset and a char end offset, both
	 * relative to the beginning of the file. The matches are added in order.
	 * @param startOffset the char start offset, from the beginning of the file.
	 * @param endOffset the char end offset, from the beginning of the file.
	 */
	public void addMatch(int startOffset, int endOffset) {
		_matches.add(new RemoteSearchResultMatch(startOffset, endOffset));
	}
	
	/**
	 * Gets the number of matches in this line.
	 * @return the number of matches.
	 */
	public int numOfMatches() {
		return _matches.size();
	}

	/**
	 * Gets the char start offset for the given match index.
	 * @param matchIndex the match index. For example, to get the start offset for the first match, specify 0.
	 * @return the char start offset, or -1 if there is no match corresponding to the given index. 
	 */
	public int getCharStart(int matchIndex) {
		RemoteSearchResultMatch match = (RemoteSearchResultMatch)(_matches.get(matchIndex));
		
		if (match != null) {
			return match._startOffset;
		}
		else {
			return -1;
		}
	}

	/**
	 * Gets the char end offset for the given match index.
	 * @param matchIndex the match index. For example, to get the end offset for the first match, specify 0.
	 * @return the char end offset, or -1 if there is no match corresponding to the given index. 
	 */
	public int getCharEnd(int matchIndex) {
		RemoteSearchResultMatch match = (RemoteSearchResultMatch)(_matches.get(matchIndex));
		
		if (match != null) {
			return match._endOffset;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * Returns the char start offset of the first match. 
	 * @see org.eclipse.rse.core.subsystems.IRemoteLineReference#getCharEnd()
	 */
	public int getCharEnd() {
		return getCharEnd(0);
	}

	/**
	 * Returns the char end offset of the first match.
	 * @see org.eclipse.rse.core.subsystems.IRemoteLineReference#getCharStart()
	 */
	public int getCharStart() {
		return getCharStart(0);
	}
	
	public IHostSearchResultConfiguration getConfiguration()
	{
		return _configuration;
	}
	
	public void setParent(Object parent)
	{
		_parent = parent;
	}
}