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
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultCollector implements ICSearchResultCollector {

	/**
	 * 
	 */
	public CSearchResultCollector() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#aboutToStart()
	 */
	public void aboutToStart() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#accept(org.eclipse.core.resources.IResource, int, int, java.lang.Object, int)
	 */
	public void accept(
		IResource resource,
		int start,
		int end,
		Object enclosingElement,
		int accuracy)
		throws CoreException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#done()
	 */
	public void done() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return _monitor;
	}

	/**
	 * @param monitor
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		this._monitor = monitor;
	}

	private IProgressMonitor _monitor;

	/**
	 * @param operation
	 */
	public void setOperation( CSearchOperation operation ) {
		_operation = operation;
	}
	
	
	private CSearchOperation _operation;
}
