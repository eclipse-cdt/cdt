/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 12, 2004
 */
package org.eclipse.cdt.internal.core.search.processing;

import org.eclipse.cdt.internal.core.Util;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author aniefer
 */
public class IndexingJob extends Job {
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	IProgressMonitor progressMonitor = null;
	JobManager jobManager = null;
	Thread indexThread = null;
	
	int ticks = 0;
	int maxTicks = 0;
	int workDone = 0;
	
	public IndexingJob( Thread thread, JobManager manager )
	{
		super( "C/C++ Indexer" ); //$NON-NLS-1$
		jobManager = manager;
		indexThread = thread;
		setPriority( LONG );
		tickUp();
		schedule();
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		progressMonitor = monitor;
		setThread( indexThread );
		progressMonitor.beginTask( "", 100 ); //$NON-NLS-1$
		return ASYNC_FINISH;
	}
	
	public void tickUp(){
		if( progressMonitor != null && progressMonitor.isCanceled() ){
			jobManager.pause();
			return;
		}
		ticks++;
		if( ticks > maxTicks )
			maxTicks = ticks;
		updateRemainingCount( null );
	}
	
	public void setTicks( int n ){
		ticks = n;
		if( maxTicks < ticks )
			maxTicks = ticks;
		
		updatePercentage();
		updateRemainingCount( null );
	}
	
	public int tickDown( String str ){
		if( progressMonitor != null && progressMonitor.isCanceled() ){
			jobManager.pause();
			return 0;
		}
		ticks--;
		
		updatePercentage();
		updateRemainingCount( str );
		return ticks;
	}
	
	private void updateRemainingCount( String str ){
		if( progressMonitor == null )
			return;
		
		String taskString = Util.bind("manager.filesToIndex", Integer.toString(ticks)); //$NON-NLS-1$
		if( str != null )
			taskString += str;
		progressMonitor.subTask( taskString );
	}
	
	private void updatePercentage(){
		if( progressMonitor == null )
			return;
		
		int work = (( maxTicks - ticks ) * 100 / maxTicks ) - workDone;
		if( work > 0 ){
			workDone += work;
			progressMonitor.worked( work );
		}
	}
}
