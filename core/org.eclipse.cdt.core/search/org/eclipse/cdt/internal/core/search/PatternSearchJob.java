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
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search;

import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PatternSearchJob implements IJob {

	/**
	 * 
	 */
	public PatternSearchJob() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#belongsTo(java.lang.String)
	 */
	public boolean belongsTo(String jobFamily) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor progress) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#isReadyToRun()
	 */
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return false;
	}

}
