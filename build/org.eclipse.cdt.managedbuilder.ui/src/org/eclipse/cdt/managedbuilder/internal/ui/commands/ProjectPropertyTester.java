/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

public class ProjectPropertyTester extends PropertyTester {
	private static final String PROP_HAS_TARGET_CONVERTERS = "hasTargetConverters"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IProject) || args.length != 0)
			return false;
		if (property.equals(PROP_HAS_TARGET_CONVERTERS)) {
			return ConvertTargetHandler.hasTargetConverters((IProject) receiver);
		}
		return false;
	}
}
