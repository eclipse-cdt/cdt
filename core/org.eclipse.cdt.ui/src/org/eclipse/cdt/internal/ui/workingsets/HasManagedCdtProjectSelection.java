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

import java.util.Collection;

import org.eclipse.cdt.ui.newui.ManageConfigSelector;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

/**
 * Property tester for the enablement of the command handled by {@link org.eclipse.cdt.internal.ui.actions.ManageConfigsHandler}.
 * Will evaluate {@code true} if the current selection is a managed CDT project, contains a managed CDT project, or belongs to a managed CDT project.
 */
public class HasManagedCdtProjectSelection extends PropertyTester {

	private static final String PROPERTY_HAS_MANAGED_CDT_PROJECT_SELECTION = "hasManagedCdtProjectSelection"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (PROPERTY_HAS_MANAGED_CDT_PROJECT_SELECTION.equals(property)) {
			boolean hasNonEmptyWorksets = hasManagedCdtProjectSelection(receiver);
			return hasNonEmptyWorksets;
		}
		return false;
	}

	private static boolean hasManagedCdtProjectSelection(Object receiver) {
		if (receiver instanceof Collection) {
			Collection<?> selection = (Collection<?>) receiver;
			if (!selection.isEmpty()) {
				IProject[] obs = ManageConfigSelector.getProjects(selection.toArray());
				return ManageConfigSelector.getManager(obs) != null;
			}
		}
		return false;
	}

}
