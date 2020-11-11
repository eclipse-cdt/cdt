/*******************************************************************************
 * Copyright (c) 2012, 2016 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Marc Khouzam (Ericsson)                 - Added knowledge about execution
 *                                               state and os/gdb thread ids
 *     Marc Dumais (Ericsson) -  Bug 405390
 *     Marc Dumais (Ericsson) -  Bug 409965
 *     Xavier Raynaud (Kalray) - Add tooltip support (Bug 431935)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;

/** Represents single thread. */
public class VisualizerThread implements Comparable<VisualizerThread>, IVisualizerModelObject {
	// --- members ---

	/** Current core this thread is on. */
	protected VisualizerCore m_core;

	/** Process ID (pid). */
	protected int m_pid;

	/** OS Thread ID (tid). */
	protected int m_tid;

	/** Thread ID as chosen by GDB. */
	protected int m_gdbtid;

	/** Thread execution state. */
	protected VisualizerExecutionState m_threadState;

	/** Location of this Thread, if any, based on his MIFrame */
	protected String m_locInfo;

	// --- constructors/destructors ---

	/** Constructor. */
	public VisualizerThread(VisualizerCore core, int pid, int tid, int gdbtid, VisualizerExecutionState state) {
		this(core, pid, tid, gdbtid, state, null);
	}

	/** Constructor. */
	public VisualizerThread(VisualizerCore core, int pid, int tid, int gdbtid, VisualizerExecutionState state,
			IFrameDMData frame) {
		m_core = core;
		m_pid = pid;
		m_tid = tid;
		m_gdbtid = gdbtid;
		m_threadState = state;
		setLocationInfo(frame);
	}

	/** Dispose method */
	public void dispose() {
		m_core = null;
	}

	// --- Object methods ---

	/** Equality comparison. */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof VisualizerThread) {
			VisualizerThread v = (VisualizerThread) obj;
			result = (v.m_pid == m_pid && v.m_tid == m_tid && v.m_gdbtid == m_gdbtid);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return m_pid ^ m_tid ^ m_gdbtid;
	}

	/** Returns string representation. */
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append(m_core).append(",Proc:").append(m_pid) //$NON-NLS-1$
				.append(",Thread:(").append(m_tid).append(",").append(m_gdbtid).append(")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		return output.toString();
	}

	// --- accessors ---

	/** Gets core. */
	public VisualizerCore getCore() {
		return m_core;
	}

	public void setCore(VisualizerCore core) {
		m_core = core;
	}

	/** Returns true if this is the "process" thread, i.e.
	 *  its PID and OS TID are the same.
	 */
	public boolean isProcessThread() {
		return m_pid == m_tid;
	}

	/** Gets process id (pid). */
	public int getPID() {
		return m_pid;
	}

	/** Gets thread id (tid). */
	public int getTID() {
		return m_tid;
	}

	/** Sets thread id (tid). */
	public void setTID(int tid) {
		m_tid = tid;
	}

	/** Gets thread id (gdbtid). */
	@Override
	public int getID() {
		return getGDBTID();
	}

	/** Return core the thread is on */
	@Override
	public IVisualizerModelObject getParent() {
		return getCore();
	}

	/** Gets gdb thread id. */
	public int getGDBTID() {
		return m_gdbtid;
	}

	/** Gets thread execution state. */
	public VisualizerExecutionState getState() {
		return m_threadState;
	}

	/** Sets thread execution state. */
	public void setState(VisualizerExecutionState state) {
		m_threadState = state;
	}

	// --- methods ---

	// --- Comparable implementation ---

	/** Compares this item to the specified item. */
	@Override
	public int compareTo(VisualizerThread o) {
		int result = 0;
		if (o != null) {
			if (m_pid < o.m_pid) {
				result = -1;
			} else if (m_pid > o.m_pid) {
				result = 1;
			} else if (getID() < o.getID()) {
				result = -1;
			} else if (getID() > o.getID()) {
				result = 1;
			}
		}
		return result;
	}

	/** IVisualizerModelObject version of compareTo() */
	@Override
	public int compareTo(IVisualizerModelObject o) {
		if (o != null) {
			if (o.getClass() == this.getClass()) {
				return compareTo((VisualizerThread) o);
			}
		}
		return 1;
	}

	/**
	 * Sets the location info of this thread
	 * @param s a string, displayinf location information of this thread.
	 */
	public void setLocationInfo(String s) {
		this.m_locInfo = s;
	}

	/**
	 * Sets the location info of this thread, based on given
	 * {@link IFrameDMData}
	 *
	 * @param dmData
	 *            a {@link IFrameDMData} (can be <code>null</code>)
	 */
	public void setLocationInfo(IFrameDMData dmData) {
		if (dmData == null) {
			this.m_locInfo = null;
		} else {
			StringBuilder label = new StringBuilder();
			// Add the function name
			if (dmData.getFunction() != null && dmData.getFunction().length() != 0) {
				label.append(" "); //$NON-NLS-1$
				label.append(dmData.getFunction());
				label.append("()"); //$NON-NLS-1$
			}

			boolean hasFileName = dmData.getFile() != null && dmData.getFile().length() != 0;

			// Add full file name
			if (hasFileName) {
				label.append(" at "); //$NON-NLS-1$
				label.append(dmData.getFile());

				// Add line number
				if (dmData.getLine() >= 0) {
					label.append(":"); //$NON-NLS-1$
					label.append(dmData.getLine());
					label.append(" "); //$NON-NLS-1$
				}
			}

			// Add module
			if (!hasFileName && (dmData.getModule() != null && dmData.getModule().length() != 0)) {
				label.append(" "); //$NON-NLS-1$
				label.append(dmData.getModule());
				label.append(" "); //$NON-NLS-1$
			}

			// Add the address
			if (dmData.getAddress() != null) {
				label.append("- 0x").append(dmData.getAddress().toString(16)); //$NON-NLS-1$
			}
			this.m_locInfo = label.toString();
		}
	}

	/**
	 * Gets the location of this thread or <code>null</code> if none.
	 *
	 * @return a String, or <code>null</code>
	 * @since 3.0
	 */
	public String getLocationInfo() {
		if (m_threadState == VisualizerExecutionState.RUNNING || m_threadState == VisualizerExecutionState.EXITED) {
			return null;
		}
		return m_locInfo;
	}

}
