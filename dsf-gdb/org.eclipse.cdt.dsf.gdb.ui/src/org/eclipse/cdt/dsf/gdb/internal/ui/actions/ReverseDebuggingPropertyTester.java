/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester for reverse debugging information available through the given
 * object.  The object being tested is an {@link IDMVMContext}.
 * <p>
 * One property is supported:
 * <ul>
 * <li> "isReverseDebuggingEnabled" - Checks whether reverse debugging is currently
 * enabled given the receiver.</li>
 * </ul>
 * </p>
 */
public class ReverseDebuggingPropertyTester extends PropertyTester {

	private static final String ENABLED = "isReverseDebuggingEnabled"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (ENABLED.equals(property)) {
			if (receiver instanceof IDMVMContext) {
				return test((IDMVMContext) receiver);
			}
		}
		return false;
	}

	private boolean test(IDMVMContext context) {
		boolean result = false;
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(context.getDMContext(),
				ICommandControlDMContext.class);
		if (controlDmc != null) {
			IReverseToggleHandler toggle = (controlDmc.getAdapter(IReverseToggleHandler.class));
			if (toggle != null) {
				result = toggle.isReverseToggled(controlDmc);
			}
		}
		return result;
	}
}
