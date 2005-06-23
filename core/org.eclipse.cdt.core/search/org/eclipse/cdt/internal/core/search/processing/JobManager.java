/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on May 30, 2003
 */
package org.eclipse.cdt.internal.core.search.processing;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMIndexRequest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
 
public abstract class JobManager implements Runnable {

	/* queue of jobs to execute */
	protected IIndexJob[] awaitingJobs = new IIndexJob[10];
	protected int jobStart = 0;
	protected int jobEnd = -1;
	protected boolean executing = false;

	/* background processing */
	protected Thread thread;

	/* flag indicating whether job execution is enabled or not */
	public static final int ENABLED = 1;
	public static final int DISABLED = 0;
	public static final int WAITING = 2;
	private int enabled = ENABLED;

	public static boolean VERBOSE = false;
	/* flag indicating that the activation has completed */
	public boolean activated = false;
	
	private int awaitingClients = 0;
	
	protected IndexingJob indexJob = null;
	
	static private final IStatus OK_STATUS = new Status( IStatus.OK, "org.eclipse.cdt.core", IStatus.OK, "", null );  //$NON-NLS-1$//$NON-NLS-2$
	static private final IStatus ERROR_STATUS = new Status( IStatus.ERROR, "org.eclipse.cdt.core", IStatus.ERROR, "", null );  //$NON-NLS-1$//$NON-NLS-2$
	
	public static void verbose(String log) {
		System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}

	public IProgressMonitor getIndexJobProgressGroup(){
		if( indexJob == null )
			return null;
		
		return indexJob.getProgressGroup();
	}
	
	/**
	 * Invoked exactly once, in background, before starting processing any job
	 */
	public void activateProcessing() {
		this.activated = true;
	}
	/**
	 * Answer the amount of awaiting jobs.
	 */
	public synchronized int awaitingJobsCount() {

		// pretend busy in case concurrent job attempts performing before activated
		if (!activated)
			return 1;

		return jobEnd - jobStart + 1;

	}
	/**
	 * Answers the first job in the queue, or null if there is no job available
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IIndexJob currentJob() {

		if ( enabled != ENABLED )
			return null;

		if (jobStart <= jobEnd) {
			return awaitingJobs[jobStart];
		}
		return null;
	}
	
	public synchronized void disable() {
		enabled = DISABLED;
		if (VERBOSE)
			JobManager.verbose("DISABLING background indexing"); //$NON-NLS-1$
	}
	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobs(String jobFamily) {

		if (VERBOSE)
			JobManager.verbose("DISCARD   background job family - " + jobFamily); //$NON-NLS-1$

		int oldEnabledState = 0;
		try {
			IIndexJob currentJob;
			// cancel current job if it belongs to the given family
			synchronized(this){
				currentJob = this.currentJob();
				oldEnabledState = enabledState();
				disable();
			}
			if (currentJob != null 
					&& (jobFamily == null || currentJob.belongsTo(jobFamily))) {
	
				currentJob.cancel();
			
				// wait until current active job has finished
				while (thread != null && executing){
					try {
						if (VERBOSE)
							JobManager.verbose("-> waiting end of current background job - " + currentJob); //$NON-NLS-1$ //$NON-NLS-2$
						Thread.sleep(50);
					} catch(InterruptedException e){
					}
				}
			}
	
			// flush and compact awaiting jobs
			int loc = -1;
			synchronized(this) {
				for (int i = jobStart; i <= jobEnd; i++) {
					currentJob = awaitingJobs[i];
					awaitingJobs[i] = null;
					if (!(jobFamily == null
						|| currentJob.belongsTo(jobFamily))) { // copy down, compacting
						awaitingJobs[++loc] = currentJob;
					} else {
						if (VERBOSE)
							JobManager.verbose("-> discarding background job  - " + currentJob); //$NON-NLS-1$
						currentJob.cancel();
						if( indexJob != null ){
							if( indexJob.tickDown( null ) <= 0 ){
								indexJob.done( OK_STATUS );
								indexJob = null;
							}
						}
					}
				}
				jobStart = 0;
				jobEnd = loc;
			}
		} finally {
			if ( oldEnabledState == ENABLED )
				enable();
			else if( oldEnabledState == WAITING )
				pause();
		}
		if (VERBOSE)
			JobManager.verbose("DISCARD   DONE with background job family - " + jobFamily); //$NON-NLS-1$
	}
	
	public synchronized void enable() {
		if( enabled == WAITING ){
			//stop waiting, restore the indexing Job for progress
			indexJob = new IndexingJob( thread, this );
			indexJob.setTicks(awaitingJobsCount());
		}
		enabled = ENABLED;
		if (VERBOSE)
			JobManager.verbose("ENABLING  background indexing"); //$NON-NLS-1$
	}
	
	public synchronized int enabledState() {
		return enabled;
	}
	
	public synchronized void pause(){
		enabled = WAITING;
		if( VERBOSE )
			JobManager.verbose("WAITING  pausing background indexing"); //$NON-NLS-1$
	}
	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {

		//if (!enabled) return;

		if (jobStart <= jobEnd) {
			awaitingJobs[jobStart++] = null;
			if (jobStart > jobEnd) {
				jobStart = 0;
				jobEnd = -1;
			}
		}
		if( indexJob != null ){
			String progressString = null;
			IIndexJob job = currentJob();
			if( job instanceof DOMIndexRequest ){
				progressString = " ("; //$NON-NLS-1$
				progressString += job.toString();
				progressString += ")"; //$NON-NLS-1$
			}
			if( indexJob.tickDown( progressString ) <= 0 ){
				indexJob.done( OK_STATUS );
				indexJob = null;
			}
		}
	}
	/**
	 * When idle, give chance to do something
	 */
	protected void notifyIdle(long idlingTime) {
	}
	/**
	 * This API is allowing to run one job in concurrence with background processing.
	 * Indeed since other jobs are performed in background, resource sharing might be 
	 * an issue.Therefore, this functionality allows a given job to be run without
	 * colliding with background ones.
	 * Note: multiple thread might attempt to perform concurrent jobs at the same time,
	 *            and should synchronize (it is deliberately left to clients to decide whether
	 *            concurrent jobs might interfere or not. In general, multiple read jobs are ok).
	 *
	 * Waiting policy can be:
	 * 		IJobConstants.ForceImmediateSearch
	 * 		IJobConstants.CancelIfNotReadyToSearch
	 * 		IJobConstants.WaitUntilReadyToSearch
	 *
	 */
	public boolean performConcurrentJob(
		IIndexJob searchJob,
		int waitingPolicy,
		IProgressMonitor progress, 
		IIndexJob jobToIgnore) {
			
		if (VERBOSE)
			JobManager.verbose("STARTING  concurrent job - " + searchJob); //$NON-NLS-1$
		if (!searchJob.isReadyToRun()) {
			if (VERBOSE)
				JobManager.verbose("ABORTED   concurrent job - " + searchJob); //$NON-NLS-1$
			return IIndexJob.FAILED;
		}

		int concurrentJobWork = 100;
		if (progress != null)
			progress.beginTask("", concurrentJobWork); //$NON-NLS-1$
		boolean status = IIndexJob.FAILED;
		if (awaitingJobsCount() > 0) {
			if( enabledState() == WAITING ){
				//the indexer is paused, resume now that we have been asked for something
				enable();
			}
			boolean attemptPolicy = true;
			policy: while( attemptPolicy ){
				attemptPolicy = false;
				switch (waitingPolicy) {
	
					case IIndexJob.ForceImmediate :
						if (VERBOSE)
							JobManager.verbose("-> NOT READY - forcing immediate - " + searchJob);//$NON-NLS-1$
						boolean wasEnabled = ( enabledState() == ENABLED );
						try {
							if( wasEnabled )
								disable(); // pause indexing
							status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
						} finally {
							if(wasEnabled)
								enable();
						}
						if (VERBOSE)
							JobManager.verbose("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
						return status;
						
					case IIndexJob.CancelIfNotReady :
						if (VERBOSE)
							JobManager.verbose("-> NOT READY - cancelling - " + searchJob); //$NON-NLS-1$
						if (progress != null) progress.setCanceled(true);
						if (VERBOSE)
							JobManager.verbose("CANCELED concurrent job - " + searchJob); //$NON-NLS-1$
						throw new OperationCanceledException();
	
					case IIndexJob.WaitUntilReady :
						int awaitingWork;
						IIndexJob previousJob = null;
						IIndexJob currentJob;
						IProgressMonitor subProgress = null;
						int totalWork = this.awaitingJobsCount();
						if (progress != null && totalWork > 0) {
							subProgress = new SubProgressMonitor(progress, concurrentJobWork / 2);
							subProgress.beginTask("", totalWork); //$NON-NLS-1$
							concurrentJobWork = concurrentJobWork / 2;
						}
						int originalPriority = this.thread.getPriority();
						try {
							synchronized(this) {
								
								// use local variable to avoid potential NPE (see Bug 20435 NPE when searching java method)
								Thread t = this.thread;
								if (t != null) {
									t.setPriority(Thread.currentThread().getPriority());
								}
								this.awaitingClients++;
							}
							while (((awaitingWork = awaitingJobsCount()) > 0)
								    && (!jobShouldBeIgnored(jobToIgnore))) {
								if (subProgress != null && subProgress.isCanceled())
									throw new OperationCanceledException();
								currentJob = currentJob();
								// currentJob can be null when jobs have been added to the queue but job manager is not enabled
								if (currentJob != null && currentJob != previousJob) {
									if (VERBOSE)
										JobManager.verbose("-> NOT READY - waiting until ready - " + searchJob);//$NON-NLS-1$
									if (subProgress != null) {
										subProgress.subTask(
											Util.bind("manager.filesToIndex", Integer.toString(awaitingWork))); //$NON-NLS-1$
										subProgress.worked(1);
									}
									previousJob = currentJob;
								}
								
								if( enabledState() == WAITING ){
									//user canceled the index we are waiting on, force immediate
									waitingPolicy = IIndexJob.ForceImmediate;
									attemptPolicy = true;
									continue policy;
								}
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
								}
							}
						} finally {
							synchronized(this) {
								this.awaitingClients--;
								
								// use local variable to avoid potential NPE (see Bug 20435 NPE when searching java method)
								Thread t = this.thread;
								if (t != null) {
									t.setPriority(originalPriority);
								}
							}
							if (subProgress != null) {
								subProgress.done();
							}
						}
				}//switch
			} //while
		} // if
		status = searchJob.execute(progress == null ? null : new SubProgressMonitor(progress, concurrentJobWork));
		if (progress != null) {
			progress.done();
		}
		if (VERBOSE)
			JobManager.verbose("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
		return status;
	}
	
	/**
	 * @param jobToIgnore
	 * @return
	 */
	private boolean jobShouldBeIgnored(IIndexJob jobToIgnore) {
		if (jobToIgnore == null)
			return false;
		
		if (currentJob() == jobToIgnore)
			return true;
		
		return false;
	}

	public abstract String processName();
	
	public synchronized void request(IIndexJob job) {
		if (!job.isReadyToRun()) {
			if (VERBOSE)
				JobManager.verbose("ABORTED request of background job - " + job); //$NON-NLS-1$
			return;
		}

		// append the job to the list of ones to process later on
		int size = awaitingJobs.length;
		if (++jobEnd == size) { // when growing, relocate jobs starting at position 0
			jobEnd -= jobStart;
			System.arraycopy(
				awaitingJobs,
				jobStart,
				(awaitingJobs = new IIndexJob[size * 2]),
				0,
				jobEnd);
			jobStart = 0;
		}
		awaitingJobs[jobEnd] = job;
		
		if (enabledState() ==WAITING){
			//Put back into enabled state
			enable();
		}
		else if( enabledState() == ENABLED ){
			if( indexJob == null ){
				indexJob = new IndexingJob( thread, this );
			} else {
				indexJob.tickUp();
			}
		}
		
		if (VERBOSE)
			JobManager.verbose("REQUEST   background job - " + job); //$NON-NLS-1$

	}
	/**
	 * Flush current state
	 */
	public void reset() {
		if (VERBOSE)
			JobManager.verbose("Reset"); //$NON-NLS-1$

		if (thread != null) {
			discardJobs(null); // discard all jobs
		} else {
			/* initiate background processing */
			thread = new Thread(this, this.processName());
			thread.setDaemon(true);
			// less prioritary by default, priority is raised if clients are actively waiting on it
			thread.setPriority(Thread.MIN_PRIORITY); 
			thread.start();
		}
		
	}

	/**
	 * Infinite loop performing resource indexing
	 */
	public void run() {

		long idlingStart = -1;
		activateProcessing();
		try {
			while (this.thread != null) {
				try {
					IIndexJob job;
					if ((job = currentJob()) == null) {
						if (idlingStart < 0)
							idlingStart = System.currentTimeMillis();
						notifyIdle(System.currentTimeMillis() - idlingStart);
						Thread.sleep(500);
						continue;
					} 
					
					idlingStart = -1;
					if (VERBOSE) {
						JobManager.verbose(awaitingJobsCount() + " awaiting jobs"); //$NON-NLS-1$
						JobManager.verbose("STARTING background job - " + job); //$NON-NLS-1$
					}
					try {
						executing = true;
						/*boolean status = */job.execute(null);
						//if (status == FAILED) request(job);
					} finally {
						executing = false;
						
						//Answer the job directly from the array; using currentJob()
						//results in no notification if indexing is disabled (Bug 78678)
						jobFinishedNotification(awaitingJobs[jobStart]);
						
						if (VERBOSE) {
							JobManager.verbose("FINISHED background job - " + job); //$NON-NLS-1$
						}
						moveToNextJob();
						if (this.awaitingClients == 0) {
							Thread.sleep(50);
						}
					}
				} catch (InterruptedException e) { // background indexing was interrupted
				}
			}
		} catch (RuntimeException e) {
			if( indexJob != null ){
				indexJob.done( ERROR_STATUS );
				indexJob = null;
			}
			if (this.thread != null) { // if not shutting down
				// log exception
				org.eclipse.cdt.internal.core.model.Util.log(e, "Background Indexer Crash Recovery", ICLogConstants.PDE); //$NON-NLS-1$
				
				// keep job manager alive
				this.discardJobs(null);
				this.thread = null;
				this.reset(); // this will fork a new thread with no waiting jobs, some indexes will be inconsistent
			}
			throw e;
		} catch (Error e) {
			if( indexJob != null ){
				indexJob.done( ERROR_STATUS );
				indexJob = null;
			}
			if (this.thread != null && !(e instanceof ThreadDeath)) {
				// log exception
				org.eclipse.cdt.internal.core.model.Util.log(e, "Background Indexer Crash Recovery", ICLogConstants.PDE); //$NON-NLS-1$
				
				// keep job manager alive
				this.discardJobs(null);
				this.thread = null;
				this.reset(); // this will fork a new thread with no waiting jobs, some indexes will be inconsistent
			}
			throw e;
		}
	}
	/**
	 * Stop background processing, and wait until the current job is completed before returning
	 */
	public void shutdown() {

		disable();
		discardJobs(null); // will wait until current executing job has completed
		Thread thread = this.thread;
		this.thread = null; // mark the job manager as shutting down so that the thread will stop by itself
		try {
			if (thread != null) { // see http://bugs.eclipse.org/bugs/show_bug.cgi?id=31858
				thread.join();
			}
		} catch (InterruptedException e) {
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append("Enabled:").append(this.enabled).append('\n'); //$NON-NLS-1$
		int numJobs = jobEnd - jobStart + 1;
		buffer.append("Jobs in queue:").append(numJobs).append('\n'); //$NON-NLS-1$
		for (int i = 0; i < numJobs && i < 15; i++) {
			buffer.append(i).append(" - job["+i+"]: ").append(awaitingJobs[jobStart+i]).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buffer.toString();
	}	
	
	protected abstract void jobFinishedNotification(IIndexJob job);

}
