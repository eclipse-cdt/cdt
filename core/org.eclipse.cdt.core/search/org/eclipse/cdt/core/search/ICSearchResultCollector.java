/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.core.search;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ICSearchResultCollector {
	/**
	 * The search result corresponds exactly to the search pattern.
	 */
	int EXACT_MATCH = 0;

	/**
	 * The search result is potentially a match for the search pattern,
	 * but a problem prevented the search engine from being more accurate
	 * (typically because of the classpath was not correctly set).
	 */
	 int POTENTIAL_MATCH = 1;

	/**
	 * Called before the actual search starts.
	 */
	public void aboutToStart();
	
	/**
	 * Called when the search has ended.
	 */
	public void done();
	
	/**
	 * Accepts the given search result.
	 *
	 * @param resource the resource in which the match has been found
	 * @param start the start position of the match, -1 if it is unknown
	 * @param end the end position of the match, -1 if it is unknown;
	 *  the ending offset is exclusive, meaning that the actual range of characters 
	 *  covered is <code>[start, end]</code>
	 * @param enclosingObject an object that contains the character range
	 *	<code>[start, end]</code>; the value can be <code>null</code> indicating that
	 *	no enclosing object has been found
	 * @param accuracy the level of accuracy the search result has; either
	 *  <code>EXACT_MATCH</code> or <code>POTENTIAL_MATCH</code>
	 * @exception CoreException if this collector had a problem accepting the search result
	 */
	public IMatch createMatch( Object fileResource, int start, int end, 
						ISourceElementCallbackDelegate node, IASTScope parent) throws CoreException;
	
	public void acceptMatch( IMatch match ) throws CoreException;
	
	/**
	 * Returns the progress monitor used to report progress.
	 *
	 * @return a progress monitor or null if no progress monitor is provided
	 */
	public IProgressMonitor getProgressMonitor();


	/**
	 * returns an IMatch object that contains any information the client cared
	 * to extract from the IAST node.  
	 * Note that clients should not reference information in the node itself so 
	 * that it can be garbage collected 
	 * @param node
	 * @return
	 */
	//public IMatch createMatch(ISourceElementCallbackDelegate node, IASTScope parent );
}
