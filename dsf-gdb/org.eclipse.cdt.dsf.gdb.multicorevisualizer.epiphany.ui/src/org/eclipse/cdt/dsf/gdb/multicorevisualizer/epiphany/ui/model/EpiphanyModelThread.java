/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;

@SuppressWarnings("restriction")
public class EpiphanyModelThread extends VisualizerThread {

	// --- members ---
	
	String m_programName = null;
	
	// --- constructors/destructors ---
	
	public EpiphanyModelThread(VisualizerCore core, String programName, int pid, int tid,	int gdbtid, VisualizerExecutionState state) {
		super(core, pid, tid, gdbtid, state);
		m_programName = programName;
		// TODO Auto-generated constructor stub
	}

	
	// --- accessors ---
	
	/**  */
	public void setProgramName(String progName) {
		m_programName = progName;
	}
	
	/**  */
	public String getProgramName() {
		return m_programName;
	}
	
}
