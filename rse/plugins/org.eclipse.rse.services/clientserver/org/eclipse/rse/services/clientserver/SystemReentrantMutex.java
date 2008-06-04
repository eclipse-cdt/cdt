/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Xuan Chen        (IBM)        - [160775] Derive from org.eclipse.rse.services.Mutex
 * Xuan Chen        (IBM)        - [209825] add some info of printing the lock status
 *******************************************************************************/

package org.eclipse.rse.services.clientserver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A reentrant Exclusion Lock for Threads that need to access a resource in a
 * serialized manner. If the request for the lock is running on the same thread
 * who is currently holding the lock, it will "borrow" the lock, and the call to
 * waitForLock() will go through. A SystemOperationMonitor is accepted in order
 * to support cancellation when waiting for the Mutex. This is a clone of
 * {@link org.eclipse.rse.services.Mutex} with some modification to make sure the
 * sequential calls to waitForLock() method in the same thread will not be
 * blocked.
 * 
 * Usage Example: <code>
 *    private SystemReentrantMutex fooMutex = new SystemReentrantMutex();
 *    boolean doFooSerialized()(ISystemOperationMonitor monitor) {
 *    	  int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
 *        mutexLockStatus = fooMutex.waitForLock(monitor, 1000);
 *        if (mutexLockStatus != SystemReentrantMutex.LOCK_STATUS_NOLOCK) {
 *            try {
 *                return doFoo();
 *            } finally {
 *                //We only release the mutex if we acquire it, not borrowed it.
 *   	          if (mutexLockStatus == SystemReentrantMutex.LOCK_STATUS_AQUIRED)
 * 		          {
 * 			          fooMutex.release();
 * 		          }
 *            }
 *        }
 *        return false;
 *    }
 * </code>
 * 
 * @since 3.0
 */
public class SystemReentrantMutex {

	private boolean fLocked = false;
	private List fWaitQueue = new LinkedList();
	private Thread threadLockThisMutex = null;
	public static final int LOCK_STATUS_NOLOCK = 0;   //No lock acquired or borrowed
	public static final int LOCK_STATUS_AQUIRED = 1;  //Lock is acquired
	public static final int LOCK_STATUS_BORROWED = 2; //Lock is borrowed, since it is running
	                                                  //on the same thread as the one holding the lock

    /**
     * Creates an instance of <tt>SystemMutex</tt>.
     */
	public SystemReentrantMutex() {
	}

	/**
	 * Try to acquire the lock maintained by this mutex.
	 *
	 * If the thread needs to wait before it can acquire the mutex, it
	 * will wait in a first-come-first-serve fashion. In case a progress
	 * monitor was given, it will be updated and checked for cancel every
	 * second.
	 *
	 * @param monitor SystemOperationMonitor. May be <code>null</code>.
	 * @param timeout Maximum wait time given in milliseconds.
	 * @return <code>LOCK_STATUS_AQUIRED</code> if the lock was acquired successfully.
	 *         <code>LOCK_STATUS_BORROWED</code> if the lock was borrowed.
	 *         <code>LOCK_STATUS_NOLOCK</code> if otherwise.
	 */
	public int waitForLock(ISystemOperationMonitor monitor, long timeout) {
        if (Thread.interrupted()) {
        	return LOCK_STATUS_NOLOCK;
		}
        if (monitor!=null && monitor.isCancelled()) {
        	return LOCK_STATUS_NOLOCK;
        }
    	final Thread myself = Thread.currentThread();
        synchronized(fWaitQueue) {
        	if (!fLocked) {
        		//acquire the lock immediately.
        		fLocked = true;
        		threadLockThisMutex = myself;
        		return LOCK_STATUS_AQUIRED;
        	} else {
        		fWaitQueue.add(myself);
        	}
        }
    	//need to wait for the lock.
        int lockStatus = LOCK_STATUS_NOLOCK;
    	try {
            long start = System.currentTimeMillis();
            long timeLeft = timeout;
            //It could be possible this function is called with null as monitor
            //And we don't want to wait forever here
            long pollTime = (timeLeft > 1000) ? 1000 : timeLeft;
            long nextProgressUpdate = start+500;
        	boolean cancelled = false;
			while (timeLeft > 0 && !cancelled && lockStatus == LOCK_STATUS_NOLOCK) {
            	//is it my turn yet? Check wait queue and wait
        		synchronized(fWaitQueue) {
                	if (!fLocked && fWaitQueue.get(0) == myself) {
                		fWaitQueue.remove(0);
                		fLocked = true;
                		lockStatus = LOCK_STATUS_AQUIRED;
                		threadLockThisMutex = myself;
                	} else
                	{
                		if (threadLockThisMutex == myself && fWaitQueue.contains(myself))
                		{
                			fWaitQueue.remove(myself);
                			fLocked = true;
                			lockStatus = LOCK_STATUS_BORROWED;
                		}
                		else
                		{
                			long waitTime = timeLeft > pollTime ? pollTime : timeLeft;
	                    	fWaitQueue.wait(waitTime);
	                    	Object firstInQueue = fWaitQueue.get(0);
	                    	boolean amIFirstInQueue = false;
	                    	if (firstInQueue == null || firstInQueue == myself)
	                    	{
	                    		amIFirstInQueue = true;
	                    	}
	                    	if (!fLocked && amIFirstInQueue) {
	                    		fWaitQueue.remove(0);
	                    		fLocked = true;
	                    		lockStatus = LOCK_STATUS_AQUIRED;
	                    		threadLockThisMutex = myself;
	                    	}
                		}
                	}
        		}
        		if (lockStatus == LOCK_STATUS_NOLOCK) {
        			//Need to continue waiting
                	long curTime = System.currentTimeMillis();
                    timeLeft = start + timeout - curTime;
                	if (monitor!=null) {
                		cancelled = monitor.isCancelled();
						if (!cancelled && (curTime > nextProgressUpdate)) {
                			nextProgressUpdate+=1000;
                		}
                	}
        		}
            }
        } catch(InterruptedException e) {
          	//cancelled waiting -> no lock acquired
    	} finally {
    		if (lockStatus == LOCK_STATUS_NOLOCK) {
        		synchronized(fWaitQueue) {
                	fWaitQueue.remove(myself);
                }
    		}
    	}
        return lockStatus;
	}

	/**
	 * Release this mutex's lock.
	 *
	 * May only be called by the same thread that originally acquired
	 * the SystemMutex.
	 */
	public void release() {
		synchronized(fWaitQueue) {
			fLocked=false;
			if (!fWaitQueue.isEmpty()) {
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
	 * {@link #waitForLock(ISystemOperationMonitor, long)} method to return
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

	/*
	 * Method used to debug this mutex
	 * uncomment it when needed
	 *
	private void printLockMessage(int status, Thread myself)
	{
		if (status == LOCK_STATUS_AQUIRED)
		{
			System.out.println("Lock is AQUIRED by thread " + myself.getId());
		}
		else if (status == LOCK_STATUS_BORROWED)
		{
			System.out.println("Lock is BORROWED by thread " + myself.getId());
		}
	}
	*/

}
