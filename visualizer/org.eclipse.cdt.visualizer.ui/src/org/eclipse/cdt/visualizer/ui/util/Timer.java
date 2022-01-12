/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// ---------------------------------------------------------------------------
// Timer
// ---------------------------------------------------------------------------

/**
 * Periodic event timer class.
 */
public class Timer implements ActionListener, Runnable {
	// --- members ---

	/** Actual timer */
	// NOTE: we delegate rather than subclassing, because
	// Timer class implements ISerializable, so every derived type
	// must either declare a serialization ID, or suppress the warning.
	protected javax.swing.Timer m_timer = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public Timer(int intervalMilliseconds) {
		m_timer = new javax.swing.Timer(intervalMilliseconds, null);
		m_timer.addActionListener(this);
		m_timer.setRepeats(true);
		m_timer.setCoalesce(true);
		m_timer.start();
	}

	/** Dispose method. */
	public void dispose() {
		if (m_timer != null) {
			if (m_timer.isRunning()) {
				m_timer.stop();
			}
			m_timer = null;
		}
	}

	// --- methods ---

	/** Gets whether timer repeatedly fires events. */
	public boolean isRepeating() {
		return m_timer.isRepeats();
	}

	/** Sets whether timer repeatedly fires events. */
	public void setRepeating(boolean repeats) {
		m_timer.setRepeats(repeats);
	}

	/** Starts/restarts timer.
	 *  Has no effect if timer is already running
	 */
	public void start() {
		if (!m_timer.isRunning())
			m_timer.start();
	}

	/** Stops timer.
	 *  Has no effect if timer is already stopped.
	 */
	public void stop() {
		if (m_timer.isRunning())
			m_timer.stop();
	}

	// --- ActionListener implementation ---

	/**
	 * Invoked each time the timer fires.
	 * Default implementation invokes the run() method on the UI thread.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		GUIUtils.exec(this);
	}

	/** Invoked each time the timer fires. */
	@Override
	public void run() {
	}

}
