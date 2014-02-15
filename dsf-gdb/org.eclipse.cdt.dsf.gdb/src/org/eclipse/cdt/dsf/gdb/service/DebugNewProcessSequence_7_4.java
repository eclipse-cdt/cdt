/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * With GDB 7.4, breakpoints are again global instead of per-process,
 * so we remove that step from the sequence.
 * 
 * @since 4.4
 */
public class DebugNewProcessSequence_7_4 extends DebugNewProcessSequence_7_2 {

	public DebugNewProcessSequence_7_4(
			DsfExecutor executor, 
			boolean isInitial, 
			IDMContext dmc, 
			String file, 
			Map<String, Object> attributes, 
			DataRequestMonitor<IDMContext> rm) {
		super(executor, isInitial, dmc, file, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Remove the step that tracks breakpoints for this process in particular.
			// The final launch sequence has started tracking breakpoints already
			orderList.remove("stepStartTrackingBreakpoints");   //$NON-NLS-1$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
}
