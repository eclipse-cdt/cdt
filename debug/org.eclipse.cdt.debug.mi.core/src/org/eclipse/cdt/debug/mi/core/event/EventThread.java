package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
public class EventThread extends Thread {

	final MISession session;
	final MIEvent[] events;

	public EventThread(MISession s, MIEvent[] evts) {
		super("MI Event Thread");
		session = s;
		events = evts;
		setDaemon(true);
	}

	/*
	 */
	public void run () {
		session.setDirty();
		session.notifyObservers(events);
	}
}
