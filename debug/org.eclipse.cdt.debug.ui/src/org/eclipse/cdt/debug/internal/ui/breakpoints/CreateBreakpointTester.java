/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 *
 */
public class CreateBreakpointTester extends PropertyTester {

	private final static String PROP_CREATE_BREAKPOINT_ADAPT = "createBreakpointAdapt"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (PROP_CREATE_BREAKPOINT_ADAPT.equals(property) && receiver instanceof ICBreakpointContext
				&& expectedValue instanceof String) {
			try {
				Class<?> expectedClass = Class.forName((String) expectedValue);
				return expectedClass.isAssignableFrom(((ICBreakpointContext) receiver).getBreakpoint().getClass());
			} catch (ClassNotFoundException e) {
				CDebugUIPlugin.log(new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID,
						"Unable to create class: " + expectedValue, e)); //$NON-NLS-1$
			}
		}
		return false;
	}

}
