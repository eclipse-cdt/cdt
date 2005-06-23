/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.core.search;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

	public IMatch createMatch( Object fileResource, int start, int end, 
						ISourceElementCallbackDelegate node, IPath referringElement) throws CoreException;
	
	//return whether or not the match was accepted
	public boolean acceptMatch( IMatch match ) throws CoreException;
	
	/**
	 * Returns the progress monitor used to report progress.
	 *
	 * @return a progress monitor or null if no progress monitor is provided
	 */
	public IProgressMonitor getProgressMonitor();
}
