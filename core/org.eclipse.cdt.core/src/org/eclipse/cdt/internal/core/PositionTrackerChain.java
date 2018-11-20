/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

class PositionTrackerChain implements IDocumentListener {
	public static final int LINKED_LIST_SIZE = 64;
	public static final int LINKED_LIST_ENTRY_SIZE = 32;
	public static int MEMORY_SIZE = 32 + LINKED_LIST_SIZE;

	private static final int MAX_DEPTH = 100; // 100 saves
	private static final long MAX_AGE = 24 * 60 * 60 * 1000; // one day

	private Deque<PositionTracker> fTrackers = new ArrayDeque<>();
	private PositionTracker fActiveTracker;
	private IDocument fDocument;

	public PositionTrackerChain(long timestamp) {
		createCheckpoint(timestamp);
	}

	public int createCheckpoint(long timestamp) {
		// Travel in time.
		while (fActiveTracker != null && fActiveTracker.getTimeStamp() >= timestamp) {
			fTrackers.removeLast();
			if (fTrackers.isEmpty()) {
				fActiveTracker = null;
			} else {
				fActiveTracker = fTrackers.getLast();
				fActiveTracker.revive();
			}
		}

		int retiredMemsize = 0;
		PositionTracker newTracker = new PositionTracker();
		newTracker.setTimeStamp(timestamp);
		fTrackers.add(newTracker);

		if (fActiveTracker != null) {
			fActiveTracker.retire(newTracker);
			retiredMemsize = fActiveTracker.getMemorySize() + LINKED_LIST_ENTRY_SIZE;
		}
		fActiveTracker = newTracker;
		checkTrackerLimits();
		return retiredMemsize;
	}

	private void checkTrackerLimits() {
		while (fTrackers.size() >= MAX_DEPTH) {
			fTrackers.removeFirst();
		}
		long minTimeStamp = fActiveTracker.getTimeStamp() - MAX_AGE;
		for (Iterator<PositionTracker> iter = fTrackers.iterator(); iter.hasNext();) {
			PositionTracker tracker = iter.next();
			if (tracker.getRetiredTimeStamp() >= minTimeStamp) {
				break;
			}
			iter.remove();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IPositionUpdater#update(DocumentEvent)
	 */
	private void update(DocumentEvent event) {
		String text = event.getText();
		int insertLen = text != null ? text.length() : 0;
		update(event.getOffset(), event.getLength(), insertLen);
	}

	void update(int offset, int deleteLen, int insertLen) {
		if (insertLen > deleteLen) {
			fActiveTracker.insert(offset + deleteLen, insertLen - deleteLen);
		} else if (insertLen < deleteLen) {
			fActiveTracker.delete(offset + insertLen, deleteLen - insertLen);
		}
	}

	/**
	 * Finds the nearest tracker created at or after the given time.
	 *
	 * @param timestamp in milliseconds.
	 * @return the tracker nearest to the timestamp, <code>null</code> if all were created before.
	 */
	public PositionTracker findTrackerAtOrAfter(long timestamp) {
		PositionTracker candidate = null;
		for (Iterator<PositionTracker> iter = fTrackers.descendingIterator(); iter.hasNext();) {
			PositionTracker tracker = iter.next();
			long trackerTimestamp = tracker.getTimeStamp();
			if (trackerTimestamp >= timestamp) {
				candidate = tracker;
			} else {
				break;
			}
		}
		return candidate;
	}

	/**
	 * Finds the tracker created at the given time.
	 *
	 * @param timestamp in milliseconds.
	 * @return the tracker at the timestamp, <code>null</code> if none created at the given time.
	 */
	public PositionTracker findTrackerAt(long timestamp) {
		for (Iterator<PositionTracker> iter = fTrackers.descendingIterator(); iter.hasNext();) {
			PositionTracker tracker = iter.next();
			long trackerTimestamp = tracker.getTimeStamp();
			if (trackerTimestamp == timestamp) {
				return tracker;
			}
			if (trackerTimestamp < timestamp) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Destroys the tracker.
	 */
	public void dispose() {
		stopTracking();
		fTrackers = null;
		fActiveTracker = null;
	}

	public void startTracking(IDocument doc) {
		stopTracking();
		fDocument = doc;
		if (fDocument != null) {
			fDocument.addDocumentListener(this);
		}
	}

	public void stopTracking() {
		if (fDocument != null) {
			fDocument.removeDocumentListener(this);
			fDocument = null;
		}
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		update(event);
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		// React before updating the document.
	}

	public IDocument getCurrentDocument() {
		return fDocument;
	}

	public PositionTracker getActiveTracker() {
		return fActiveTracker;
	}

	public boolean isModified() {
		return fTrackers.size() > 1 || fActiveTracker.isModified();
	}

	public int getMemorySize() {
		int size = MEMORY_SIZE;
		for (PositionTracker tracker : fTrackers) {
			size += LINKED_LIST_ENTRY_SIZE;
			size += tracker.getMemorySize();
		}
		return size;
	}

	public int removeOldest() {
		int memdiff = 0;
		if (fTrackers.size() > 1) {
			PositionTracker tracker = fTrackers.removeFirst();
			memdiff = tracker.getMemorySize() + LINKED_LIST_ENTRY_SIZE;
			tracker.clear();
		}
		return -memdiff;
	}

	public long getOldestRetirement() {
		if (fTrackers.size() > 1) {
			PositionTracker tracker = fTrackers.getFirst();
			return tracker.getRetiredTimeStamp();
		}
		return Long.MAX_VALUE;
	}
}
