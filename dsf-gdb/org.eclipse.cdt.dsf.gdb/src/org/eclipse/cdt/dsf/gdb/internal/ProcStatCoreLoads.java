/*******************************************************************************
 * Copyright (c) 2013 Ericsson
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
package org.eclipse.cdt.dsf.gdb.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a container to store the computed
 * loads for the various CPU cores.
 *
 */
public class ProcStatCoreLoads {
	private Map<String, Float> m_coreLoads;

	public ProcStatCoreLoads() {
		m_coreLoads = new HashMap<>();
	}

	public void put(String coreId, Float load) {
		m_coreLoads.put(coreId, load);
	}

	/**
	 * @param cpuId: the cpu/core id, as listed in /proc/cpuinfo.
	 * For example, for the core labelled "cpu0" in /proc/stat,
	 * use id "0".
	 * @return The measured load for that core
	 */
	public Float getLoad(String cpuId) {
		return m_coreLoads.get("cpu" + cpuId); //$NON-NLS-1$
	}
}
