/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.event.MIEvent;

/**
 * Event Thread blocks on the event Queue, wakes up
 * when events are available and notify all the observers.
 */
public class EventThread extends Thread {

	MISession session;

	public EventThread(MISession s) {
		super("MI Event Thread");
		session = s;
	}

	public void run() {
		// Signal by the session of time to die.
		while (session.getChannelOutputStream() != null) {
			MIEvent event = null;
			Queue eventQueue = session.getEventQueue();
			// removeItem() will block until an item is available.
			try {
				event = (MIEvent) eventQueue.removeItem();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
			try {
				if (event != null) {
					session.notifyObservers(event);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
