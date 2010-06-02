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
package org.eclipse.cdt.tests.dsf;

import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;

/*
 * This class provides a way to wait for an asynchronous ServerEvent
 * to occur.  The user of this class specifies which event is of
 * interest using the proper constructor or the registerForEvent() method.
 * waitForEvent() can then be called to block until the event occurs or
 * the timeout elapses.
 * 
 * Note that if the event occurs after regsiterForEvent() is called but
 * before waitForEvent() is called, waitForEvent() will return immediatly
 * since it will know the event has already occured.
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
	

	/* Empty contructor.  registerForEvent() should be called when
	 * this constructor is used.
	 */
	public ServiceEventWaitor(DsfSession session) {
		fSession = session;
	}
	
	/* Contructor that takes the eventClass as parameter.  This is a shortcut
	 * that avoids calling registerForEvent()
	 */
	public ServiceEventWaitor(DsfSession session, Class<V> eventClass)	{
		this(session);
		registerForEvent(eventClass);
	}
	
	/* Specify which event to wait for, and add ourselves as
	 * a listener with the session 
	 */
	public void registerForEvent(Class<V> eventClass) {
		fEventTypeClass = eventClass;
		fEvent = null;
		fSession.addServiceEventListener(this, null);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (fEventTypeClass != null) fSession.removeServiceEventListener(this);
	}

	/* Block until 'timeout' or the previously specified event has been
	 * received.  The reason we don's specify the event as a parameter
	 * is that we must be ready for the event to occur event before
	 * this method is called.
	 */
	public synchronized V waitForEvent(int timeout) throws Exception {
		if (fEventTypeClass == null) {
			throw new Exception("Event to wait for has not been specified!");
		}
		// The event might have already been received
		if (fEvent != null) return fEvent;
		
		wait(timeout);
		
		if (fEvent == null) {
			throw new Exception("Timed out waiting for ServiceEvent: " + fEventTypeClass.getName());
		}
		return fEvent;
	}

	/*
	 * Listen to all possible events by having the base class be the parameter.
	 * and then igure out if that event is the one we were waiting for.
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
