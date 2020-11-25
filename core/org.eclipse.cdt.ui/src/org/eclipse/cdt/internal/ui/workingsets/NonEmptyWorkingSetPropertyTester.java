/*******************************************************************************
 * Copyright (c) 2020 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Property tester for the enablement of the command handled by {@link org.eclipse.cdt.internal.ui.actions.WorkingSetConfigHandler}.
 * Will evaluate {@code true} if there is a non-empty working set in the workspace.
 */
public class NonEmptyWorkingSetPropertyTester extends PropertyTester {

	private static final String PROPERTY_HAS_NON_EMPTY_WORKING_SET = "hasNonEmptyWorkingSet"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (PROPERTY_HAS_NON_EMPTY_WORKING_SET.equals(property)) {
			boolean hasNonEmptyWorksets = hasNonEmptyWorksets();
			return hasNonEmptyWorksets;
		}
		return false;
	}

	private static boolean hasNonEmptyWorksets() {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
		if (workingSets != null) {
			for (IWorkingSet workingSet : workingSets) {
				if (!workingSet.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
}
