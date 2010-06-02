/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.Platform;

/*
 * This class provides a way to wait for an asynchronous ServerEvent
 * to occur.  The user of this class specifies which event is of
 * interest . waitForEvent() can then be called to block until the event occurs or
 * the timeout elapses. It's important that this object be created <b>before</b> 
 * executing the debugger operation that will cause the expected event to occur, 
 * otherwise the caller stands to miss out on the event.
 * 
 * Note that if the event occurs after object construction but
 * before waitForEvent() is called, waitForEvent() will return immediately
 * since it will know the event has already occurred.
 */

public class ServiceEventWaitor<V> {
	/*
	 *  Indicates we will wait forever. Otherwise the time specified
	 *  is in milliseconds.
	 */
	public final static int WAIT_FOREVER = 0 ;

	/* The type of event to wait for */
	private Class<V> fEventTypeClass;
	private DsfSession fSession;
    private V fEvent;
    
    /**
     * Trace option for wait metrics
     */
    private static final boolean LOG = TestsPlugin.DEBUG && "true".equals(Platform.getDebugOption("org.eclipse.cdt.tests.dsf.gdb/debug/waitMetrics"));  //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Constructor
	 * 
	 * @param session
	 *            the DSF session we'll wait for an event to happen on
	 * @param eventClass
	 *            the event to expect
	 */
	public ServiceEventWaitor(DsfSession session, Class<V> eventClass)	{
		assert eventClass != null;
		fSession = session;
		fEventTypeClass = eventClass;
		fEvent = null;
		fSession.addServiceEventListener(this, null);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (fEventTypeClass != null) fSession.removeServiceEventListener(this);
	}

	/*
	 * Block until 'timeout' or the expected event occurs. The expected event is
	 * specified at construction time.
	 * 
	 * @param timeout the maximum time to wait in milliseconds.
	 */
	public synchronized V waitForEvent(int timeout) throws Exception {
		if (fEventTypeClass == null) {
			throw new Exception("Event to wait for has not been specified!");
		}
		
		long startMs = System.currentTimeMillis();
		
		// The event might have already been received
		if (fEvent == null) {
			wait(timeout);
			if (fEvent == null) {
				throw new Exception("Timed out waiting for ServiceEvent: " + fEventTypeClass.getName());
			}
		}

		long stopMs = System.currentTimeMillis();
		
		// Turning on trace during development gives you the following  
		// helpful analysis, which you can use to establish reasonable timeouts,
		// and detect poorly configured ones. The best way to use this it to 
		// set breakpoints on the WARNING println calls.
		if (LOG) {
			final long duration = stopMs - startMs;
			System.out.println("The following caller waited for " + (duration) + " milliseconds");
			boolean print = false;
			for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
				if (frame.toString().startsWith("sun.reflect.NativeMethodAccessorImpl")) {
					// ignore anything once we get into the reflection/junit portion of the stack
					System.out.println("\t... (junit)");
					break;
				}
				if (print) {
					System.out.println("\t" + frame);
				}
				if (!print && frame.toString().contains("ServiceEventWaitor.waitForEvent")) {
					// we're only interested in the call stack up to (and including) our caller					
					print = true;
				}
			}
			
			if (timeout != WAIT_FOREVER) {
				if (duration == 0) {
					if (timeout > 1000) {
						System.out.println("WARNING: Caller specified a timeout over a second but the operation was instantenous. The timeout is probably too loose.");
					}
					else if (timeout < 100) {
						System.out.println("WARNING: Caller specified a timeout less than 100 milliseconds. Even though the operation completed instantaneously, the timeout is probably too tight.");
					}
				}
				else {
					if (timeout/duration > 7.0 && timeout > 2000) {
						// don't bother for timeouts less than 2 seconds
						System.out.println("WARNING: Caller specified a timeout that was more than 7X what was necessary. The timeout is probably too loose.");
					}
					else if ((((float)(timeout - duration))/(float)duration) < 0.20) {
						System.out.println("WARNING: Caller specified a timeout that was less than 20% above actual time. The timeout is probably too tight.");
					}
				}
			}
			else {
				System.out.println("WARNING: Caller requested to wait forever. It should probably specify some reasonable value.");
			}
		}
		
		return fEvent;
	}

	/*
	 * Listen to all possible events by having the base class be the parameter.
	 * and then figure out if that event is the one we were waiting for.
	 */
	@DsfServiceEventHandler 
	public void eventDispatched(V event) {
		if (fEventTypeClass.isAssignableFrom(event.getClass())) {
			synchronized(this) {
				fEvent = event;
				notifyAll();
			}
		}
	}
}
