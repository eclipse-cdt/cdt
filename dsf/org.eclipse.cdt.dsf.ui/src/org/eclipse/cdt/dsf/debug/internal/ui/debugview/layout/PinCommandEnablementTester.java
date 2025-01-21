/*******************************************************************************
 * Copyright (c) 2025 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.debugview.layout;

import org.eclipse.cdt.debug.internal.ui.actions.PinViewHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;

/**
 * Tests if 'Pin to Debug Context' and 'Open New View' commands can be enabled.
 * This is tested in 2 steps.
 * <p>
 * 1. If any view is already pinned then command must be enabled in all the
 * views where it is contributed.
 * </p>
 * <p>
 * 2. A valid {@link DsfSession} is active.
 * </p>
 *
 *
 * @author Raghunandana Murthappa
 */
public class PinCommandEnablementTester extends PropertyTester {

	private static final String PIN_VIEW_COMMAND_PROP_TEST_NAME = "canPinViewEnabled"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if (PIN_VIEW_COMMAND_PROP_TEST_NAME.equals(property)) {

			// We enable Pin Command on all views if it is pinned in any View.
			if (!PinViewHandler.getPinnedViews().isEmpty()) {
				return true;
			}

			IAdaptable debugContext = DebugUITools.getDebugContext();
			IDMVMContext dmvmContext = Adapters.adapt(debugContext, IDMVMContext.class);
			return dmvmContext != null && DsfSession.isSessionActive(dmvmContext.getDMContext().getSessionId());
		}
		return false;
	}
}
