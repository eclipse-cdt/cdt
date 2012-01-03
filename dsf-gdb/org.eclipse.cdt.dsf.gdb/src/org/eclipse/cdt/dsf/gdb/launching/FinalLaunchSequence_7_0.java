/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation (Bug 365471)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Subclass for GDB >= 7.0.
 * This class currently sets non-stop mode, or makes
 * sure target-async is off for all-stop.
 * 
 * @since 4.1
 */
public class FinalLaunchSequence_7_0 extends FinalLaunchSequence {

	public FinalLaunchSequence_7_0(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence") + 1, "stepInitializeFinalLaunchSequence_7_0"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// Note that stepSetNonStop is defined in the base class for backwards-compatibility
			orderList.add(orderList.indexOf("stepSourceGDBInitFile") + 1, "stepSetNonStop"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the FinalLaunchSequence_7_0 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_0(RequestMonitor rm) {
		rm.done();
	}
}
