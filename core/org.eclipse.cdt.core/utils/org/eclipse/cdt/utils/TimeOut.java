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
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TimeOut implements Runnable {
	
	
	protected Thread thread;
	protected boolean enabled;
	protected IProgressMonitor pm = null;
	private int timeout = 0; 
	private int threadPriority = Thread.MIN_PRIORITY + 1;
	boolean debug = false;
	private String threadName = null;
	boolean readyToRun = true;
	
	public TimeOut(){
		reset();
	}
	
	public TimeOut(String threadName){
		this.threadName = threadName;
		reset();
	}
	
	public void run() {
	 while (this.thread != null) {
	  try {
		synchronized(this){
			if (enabled){
			 	readyToRun = false;
				wait(timeout);
				 if (enabled){
				 	if(pm != null)
				 	 	pm.setCanceled(true);
					 enabled = false;
				 }
			 }
			 else{
				 while(!enabled){
				 	readyToRun = true;
				 	wait();
				 }
			 }
		   }
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	}
	
	public synchronized void startTimer(){
 	  enabled = true;
 	  notify();
	}
	
	public synchronized void stopTimer(){
		enabled= false;
		notify();
	}
	
	

	public void reset() {
		enabled=false;
		if (threadName!=null){
			thread = new Thread(this, threadName);
		}
		else{
			thread = new Thread(this, "Time Out Thread"); //$NON-NLS-1$
		}
		thread.setDaemon(true);
		thread.setPriority(threadPriority); 
		thread.start();
	}
	/**
	 * @return Returns the threadPriority.
	 */
	public int getThreadPriority() {
		return threadPriority;
	}
	/**
	 * @param threadPriority The threadPriority to set.
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}
	/**
	 * @return Returns the pm.
	 */
	public IProgressMonitor getProgressMonitor() {
		return pm;
	}
	/**
	 * @param pm The pm to set.
	 */
	public void setProgressMonitor(IProgressMonitor pm) {
		this.pm = pm;
	}
	/**
	 * @return Returns the timeout.
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isReadyToRun(){
		return readyToRun;
	}
}
