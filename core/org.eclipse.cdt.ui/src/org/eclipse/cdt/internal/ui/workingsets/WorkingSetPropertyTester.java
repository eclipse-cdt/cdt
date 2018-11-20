/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;

/**
 * Property tester for working sets that CDT can manipulate in cool ways, such
 * as managing build configurations.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public class WorkingSetPropertyTester extends PropertyTester {

	private static final String P_HAS_C_PROJECTS = "hasCProjects"; //$NON-NLS-1$

	/**
	 * Initializes me.
	 */
	public WorkingSetPropertyTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (P_HAS_C_PROJECTS.equals(property)) {
			return hasCProjects(getWorkingSet(receiver));
		}

		return false;
	}

	private IWorkingSet getWorkingSet(Object object) {
		IWorkingSet result = null;

		if (object instanceof IWorkingSet) {
			result = (IWorkingSet) object;
		} else if (object instanceof IAdaptable) {
			result = ((IAdaptable) object).getAdapter(IWorkingSet.class);
		}

		return result;
	}

	private boolean hasCProjects(IWorkingSet workingSet) {
		boolean result = false;

		if (workingSet != null) {
			IAdaptable[] members = workingSet.getElements();

			for (IAdaptable next : members) {
				IProject project = next.getAdapter(IProject.class);

				if ((project != null) && CoreModel.hasCNature(project)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}
}
