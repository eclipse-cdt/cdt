/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.search;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AcceptMatchOperation implements IWorkspaceRunnable {

	ICSearchResultCollector collector;
	ArrayList matches;
	/**
	 * @param collector
	 */
	public AcceptMatchOperation(ICSearchResultCollector collector, ArrayList matches) {
		this.collector = collector;
		this.matches = matches;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		Iterator i = matches.iterator();
		while (i.hasNext()){
		  IMatch match = (IMatch) i.next();	
		  collector.acceptMatch(match); 
		}
		collector.done();
	}

}
