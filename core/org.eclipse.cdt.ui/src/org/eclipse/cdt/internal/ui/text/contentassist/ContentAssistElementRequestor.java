/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.utils.TimeOut;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 *
 * This class is the source element requestor used by the completion engine.
 */
public class ContentAssistElementRequestor extends NullSourceElementRequestor implements ITimeoutThreadOwner{
	// a static timer thread
	private static TimeOut timeoutThread = new TimeOut(); 
	private IProgressMonitor pm = new NullProgressMonitor();

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath, workingCopies	);
	}
	
	/**
	 * 
	 */
	public ContentAssistElementRequestor() {
		super();
		// set the timer thread to max priority for best performance
		timeoutThread.setThreadPriority(Thread.MAX_PRIORITY);	
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#setTimeout(int)
	 */
	public void setTimeout(int timeout) {
		timeoutThread.setTimeout(timeout);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#startTimer()
	 */
	public void startTimer() {
		createProgressMonitor();
		timeoutThread.startTimer();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ITimeoutThreadOwner#stopTimer()
	 */
	public void stopTimer() {
		timeoutThread.stopTimer();
		pm.setCanceled(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#parserTimeout()
	 */
	public boolean parserTimeout() {
		if ((pm != null) && (pm.isCanceled()))
			return true;
		return false;
	}
	/*
	 * Creates a new progress monitor with each start timer
	 */
	private void createProgressMonitor() {
		pm.setCanceled(false);
		timeoutThread.setProgressMonitor(pm);
	}

}
