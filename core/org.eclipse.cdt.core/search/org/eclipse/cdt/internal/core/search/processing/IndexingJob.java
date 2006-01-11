/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author aniefer
 */
public class IndexingJob extends Job {
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	IProgressMonitor progressMonitor = null;
	IProgressMonitor group = null;
	JobManager jobManager = null;
	Thread indexThread = null;
	
	static final String JOB_NAME = Util.bind( "indexerJob" ); //$NON-NLS-1$
	
	int ticks = 0;
	int maxTicks = 0;
	int workDone = 0;
	
	public IndexingJob( Thread thread, JobManager manager )
	{
		super( JOB_NAME ); //$NON-NLS-1$
		
		group = Platform.getJobManager().createProgressGroup();
		group.beginTask( JOB_NAME, 100 );
		
		jobManager = manager;
		indexThread = thread;
		
		setPriority( LONG );
		setProgressGroup( group, 100 );
		
		tickUp();
		schedule();
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		progressMonitor = monitor;
		setThread( indexThread );
		if (progressMonitor != null)
			progressMonitor.beginTask( "", 100 ); //$NON-NLS-1$
		return ASYNC_FINISH;
	}
	
	synchronized public void tickUp(){
		ticks++;
		if( ticks > maxTicks )
			maxTicks = ticks;
		updateRemainingCount( null );
	}
	
	synchronized public void setTicks( int n ){
		ticks = n;
		if( maxTicks < ticks )
			maxTicks = ticks;
		
		updatePercentage();
		updateRemainingCount( null );
	}
	
	synchronized public int tickDown( String str ){
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
		
		workDone += work;
		progressMonitor.worked( work );
		if( workDone < 0 ) workDone = 0;
	}
	
	public IProgressMonitor getProgressGroup(){
		return group;
	}
}
