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
	public synchronized boolean waitForLock(IProgressMonitor monitor, long timeout) {
        if (Thread.interrupted()) {
        	return false;
		}
        if (fLocked) {
        	//need to wait for the lock.
        	boolean canceled = false;
        	final Thread myself = Thread.currentThread();
        	try {
            	fWaitQueue.add(myself);
            	Activator.trace("Mutex: added "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
                long start = System.currentTimeMillis();
                long timeLeft = timeout;
                long pollTime = (monitor!=null) ? 1000 : timeLeft;
                long nextProgressUpdate = start+500;
                while (timeLeft>0 && !canceled) {
                    try {
                    	wait(timeLeft > pollTime ? pollTime : timeLeft);
                		Activator.trace("Mutex: wakeup "+myself+" ?"); //$NON-NLS-1$ //$NON-NLS-2$
                    	//I'm still in the list, nobody is allowed to take me out!
                    	assert !fWaitQueue.isEmpty(); 
                    	if (!fLocked && fWaitQueue.get(0) == myself) {
                    		break; //gee it's my turn!
                    	}
                    	long curTime = System.currentTimeMillis();
                        timeLeft = start + timeout - curTime;
                    	if (monitor!=null) {
                    		canceled = monitor.isCanceled();
                    		if (!canceled && (curTime>nextProgressUpdate)) {
                        		monitor.worked(1);
                    			nextProgressUpdate+=1000;
                    		}
                    	}
                    } catch(InterruptedException e) {
                    	canceled = true;
                    }
                }
        	} finally {
            	fWaitQueue.remove(myself);
            	Activator.trace("Mutex: removed "+myself+", size="+fWaitQueue.size()); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        	if (fLocked || canceled) {
        		//we were not able to acquire the lock due to an exception,
        		//or because the wait was canceled.
        		return false;
        	}
        }
        //acquire the lock myself now.
        fLocked = true;
        return true;
	}

	/**
	 * Release this mutex's lock.
	 * 
	 * May only be called by the same thread that originally acquired 
	 * the Mutex.
	 */
	public synchronized void release() {
		fLocked=false;
		if (!fWaitQueue.isEmpty()) {
			Object nextOneInQueue = fWaitQueue.get(0);
			Activator.trace("Mutex: releasing "+nextOneInQueue); //$NON-NLS-1$
			notifyAll();
		}
	}

	/**
	 * Return this Mutex's lock status.
	 * @return <code>true</code> if this mutex is currently acquired by a thread.
	 */
	public synchronized boolean isLocked() {
		return fLocked;
	}
	
	/**
	 * Interrupt all threads waiting for the Lock, causing their
	 * {@link #waitForLock(IProgressMonitor, long)} method to return
	 * <code>false</code>.
	 * This should be called if the resource that the Threads are 
	 * contending for, becomes unavailable for some other reason.
	 */
	public void interruptAll() {
		Iterator it = fWaitQueue.iterator();
		while (it.hasNext()) {
			Thread aThread = (Thread)it.next();
			aThread.interrupt();
		}
	}

}
