/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh.files;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.ssh.Activator;

/**
 * A Mutual Exclusion Lock for Threads that need to access a resource
 * in a serialized manner.
 * 
 * Usage Example:
 * <code>
 *    private Mutex fooMutex;
 *    boolean doFooSerialized()(IProgressMonitor monitor) {
 *        if (fooMutex.waitForLock(monitor, 1000)) {
 *            try {
 *                return doFoo();
 *            } finally {
 *                fooMutex.release();
 *            }
 *        }
 *        return false;
 *    }
 * </code>
 */
public class Mutex {
	
	private boolean fLocked = false;
	private List fWaitQueue = new LinkedList();
	
	/**
	 * Try to acquire the lock maintained by this mutex.
	 *
	 * If the thread needs to wait before it can acquire the mutex, it
	 * will wait in a first-come-first-serve fashion. In case a progress 
	 * monitor was given, it will be updated and checked for cancel every 
	 * second.
	 * 
	 * @param monitor Eclipse Progress Monitor. May be <code>null</code>.
	 * @param timeout Maximum wait time given in milliseconds.
	 * @return <code>true</code> if the lock was obtained successfully.
	 */
	public boolean waitForLock(IProgressMonitor monitor, long timeout) {
        if (Thread.interrupted()) {
        	return false;
		}
    	final Thread myself = Thread.currentThread();
        synchronized(fWaitQueue) {
        	if (!fLocked) {
        		//acquire the lock immediately.
        		fLocked = true;
        		return true;
        	} else {
        		fWaitQueue.add(myself);
            	Activator.trace("Mutex: added "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }
    	//need to wait for the lock.
        boolean lockAcquired = false;
    	try {
            long start = System.currentTimeMillis();
            long timeLeft = timeout;
            long pollTime = (monitor!=null) ? 1000 : timeLeft;
            long nextProgressUpdate = start+500;
        	boolean canceled = false;
            while (timeLeft>0 && !canceled && !lockAcquired) {
            	//is it my turn yet? Check wait queue and wait
        		synchronized(fWaitQueue) {
                	if (!fLocked && fWaitQueue.get(0) == myself) {
                		fWaitQueue.remove(0);
                    	Activator.trace("Mutex: SUCCESS, removed "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
                		fLocked = true;
                		lockAcquired = true;
                	} else {
                    	fWaitQueue.wait(timeLeft > pollTime ? pollTime : timeLeft);
                    	if (!fLocked && fWaitQueue.get(0) == myself) {
                    		fWaitQueue.remove(0);
                        	Activator.trace("Mutex: SUCCESS, removed "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
                    		fLocked = true;
                    		lockAcquired = true;
                    	}
                	}
        		}
        		if (!lockAcquired) {
        			//Need to continue waiting
            		Activator.trace("Mutex: wakeup "+myself+" ?"); //$NON-NLS-1$ //$NON-NLS-2$
                	long curTime = System.currentTimeMillis();
                    timeLeft = start + timeout - curTime;
                	if (monitor!=null) {
                		canceled = monitor.isCanceled();
                		if (!canceled && (curTime>nextProgressUpdate)) {
                    		monitor.worked(1);
                			nextProgressUpdate+=1000;
                		}
                	}
        		}
            }
        } catch(InterruptedException e) {
          	//canceled waiting -> no lock aquired
    	} finally {
    		if (!lockAcquired) {
        		synchronized(fWaitQueue) {
                	fWaitQueue.remove(myself);
                	Activator.trace("Mutex: FAIL, removed "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
                }
    		}
    	}
        return lockAcquired;
	}

	/**
	 * Release this mutex's lock.
	 * 
	 * May only be called by the same thread that originally acquired 
	 * the Mutex.
	 */
	public void release() {
		synchronized(fWaitQueue) {
			fLocked=false;
			if (!fWaitQueue.isEmpty()) {
				Object nextOneInQueue = fWaitQueue.get(0);
				Activator.trace("Mutex: releasing "+nextOneInQueue); //$NON-NLS-1$
				fWaitQueue.notifyAll();
			}
		}
	}

	/**
	 * Return this Mutex's lock status.
	 * @return <code>true</code> if this mutex is currently acquired by a thread.
	 */
	public boolean isLocked() {
		synchronized(fWaitQueue) {
			return fLocked;
		}
	}
	
	/**
	 * Interrupt all threads waiting for the Lock, causing their
	 * {@link #waitForLock(IProgressMonitor, long)} method to return
	 * <code>false</code>.
	 * This should be called if the resource that the Threads are 
	 * contending for, becomes unavailable for some other reason.
	 */
	public void interruptAll() {
		synchronized(fWaitQueue) {
			Iterator it = fWaitQueue.iterator();
			while (it.hasNext()) {
				Thread aThread = (Thread)it.next();
				aThread.interrupt();
			}
		}
	}

}
