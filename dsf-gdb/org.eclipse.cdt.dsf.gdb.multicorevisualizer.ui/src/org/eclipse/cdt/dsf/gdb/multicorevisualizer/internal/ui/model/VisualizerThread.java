/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Marc Khouzam (Ericsson)                 - Added knowledge about execution 
 *                                               state and os/gdb thread ids
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

/** Represents single thread. */
public class VisualizerThread
	implements Comparable<VisualizerThread>
{
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

	
	// --- constructors/destructors ---

	/** Constructor. */
	public VisualizerThread(VisualizerCore core, int pid, int tid, int gdbtid, VisualizerExecutionState state) {
		m_core = core;
		m_pid = pid;
		m_tid = tid;
		m_gdbtid = gdbtid;
		m_threadState = state;
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
			result = (
				v.m_pid == m_pid &&
				v.m_tid == m_tid &&
				v.m_gdbtid == m_gdbtid
			);
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
		StringBuffer output = new StringBuffer();
		output.append(m_core).append(",Proc:").append(m_pid) //$NON-NLS-1$
		      .append(",Thread:(").append(m_tid).append(",").append(m_gdbtid).append(")");  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		return output.toString();
	}

	
	// --- accessors ---
	
	/** Gets core. */
	public VisualizerCore getCore()	{
		return m_core;
	}
	
	public void setCore(VisualizerCore core) {
		m_core = core;
	}

	/** Returns true if this is the "process" thread, i.e.
	 *  its PID and OS TID are the same.
	 */
	public boolean isProcessThread()
	{
		return m_pid == m_tid;
	}
	
	/** Gets process id (pid). */
	public int getPID() {
		return m_pid;
	}
	
	/** Gets thread id (tid). */
	public int getTID()	{
		return m_tid;
	}

	/** Gets gdb thread id. */
	public int getGDBTID()	{
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
			}
			else if (m_pid > o.m_pid) {
				result = 1;
			}
			else if (m_tid < o.m_tid) {
				result = -1;
			}
			else if (m_tid > o.m_tid) {
				result = 1;
			}
		}
		return result;
	}
}
