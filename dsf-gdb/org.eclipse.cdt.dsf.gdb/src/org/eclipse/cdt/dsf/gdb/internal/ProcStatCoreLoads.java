/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		m_coreLoads = new HashMap<String,Float>();
	}
	
	public void put(String coreId, Float load) {
		m_coreLoads.put(coreId,load);
	}
//	public Float get(String coreId) {
//		return m_coreLoads.get(coreId);
//	}
	
	/**
	 * @param cpuId: the cpu/core id, as listed in /proc/cpuinfo
	 * @return The measured load for that core
	 */
	public Float getLoad(String cpuId) {
		return m_coreLoads.get("cpu"+cpuId); //$NON-NLS-1$
	}
}

