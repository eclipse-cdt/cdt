/*
 * Created on Apr 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.search;

import java.util.ArrayList;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IMatchLocator
		extends
			ISourceElementRequestor,
			ICSearchConstants {
	
	public void locateMatches( String [] paths, IWorkspace workspace, IWorkingCopy[] workingCopies,ArrayList matches ) throws InterruptedException;
	
	public void setProgressMonitor(IProgressMonitor progressMonitor);
}
