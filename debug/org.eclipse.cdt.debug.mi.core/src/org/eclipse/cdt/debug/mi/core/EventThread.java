/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;
 
import java.io.IOException;

import org.eclipse.cdt.debug.mi.core.event.MIEvent;

/**
 * Transmission command thread blocks on the command Queue
 * and wake cmd are available and push them to gdb out channel.
 */
public class EventThread extends Thread {

	MISession session;

	public EventThread(MISession s) {
		super("MI Event Thread");
		session = s;
	}

	public void run () {
		try {
			while (true) {
				MIEvent event = null;
				Queue eventQueue = session.getEventQueue();
				// removeItem() will block until an item is available.
				try {
					event = (MIEvent)eventQueue.removeItem();
				} catch (InterruptedException e) {
					// signal by the session of time to die.
					if (session.getChannelOutputStream() == null) {
						throw new IOException();
					}
					//e.printStackTrace();
				}
				try {
					session.notifyObservers(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
