/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

//----------------------------------------------------------------------------
//VisualizerLoadInfo
//----------------------------------------------------------------------------

/**
 * Object that represents the load of a CPU or core
 */
public class VisualizerLoadInfo {

	// --- members ---

	/** load */
	protected Integer m_load = null;

	/** the high load water-mark */
	protected Integer m_highLoadWatermark = null;

	// --- constructors/destructors ---

	/** constructor */
	public VisualizerLoadInfo(Integer load) {
		m_load = load;
	}

	public VisualizerLoadInfo(Integer load, Integer highLoadWatermark) {
		this(load);
		m_highLoadWatermark = highLoadWatermark;
	}

	// --- Object methods ---

	/** Returns string representation. */
	@Override
	public String toString() {
		if (m_highLoadWatermark != null) {
			return "Load:" + m_load + ", high water-mark:" + m_highLoadWatermark; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return "Load:" + m_load + ", high water-mark: not defined"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// --- accessors ---

	/** Gets the CPU usage load of this core. */
	public Integer getLoad() {
		return m_load;
	}

	/** get the high load water-mark */
	public Integer getHighLoadWaterMark() {
		return m_highLoadWatermark;
	}

}
