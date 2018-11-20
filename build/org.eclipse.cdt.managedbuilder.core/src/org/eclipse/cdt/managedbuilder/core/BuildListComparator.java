/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.util.Comparator;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;

/**
 * This class is intended to compare MBS-specific classes
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BuildListComparator extends CDTListComparator {
	private static final String EMPTY = ""; //$NON-NLS-1$

	private static Comparator<Object> comparator = null;

	public static Comparator<Object> getInstance() {
		if (comparator == null)
			comparator = new BuildListComparator();
		return comparator;
	}

	@Override
	public int compare(Object a, Object b) {
		if (a == null || b == null)
			return 0;
		if (a instanceof ITool) {
			ITool c1 = (ITool) a;
			ITool c2 = (ITool) b;
			String s1 = c1.getName();
			if (s1 == null)
				s1 = EMPTY;
			String s2 = c2.getName();
			if (s2 == null)
				s2 = EMPTY;
			return s1.compareToIgnoreCase(s2);
		}
		if (a instanceof IToolChain) {
			IToolChain c1 = (IToolChain) a;
			IToolChain c2 = (IToolChain) b;
			String s1 = c1.getUniqueRealName();
			if (s1 == null)
				s1 = EMPTY;
			String s2 = c2.getUniqueRealName();
			if (s2 == null)
				s2 = EMPTY;
			return s1.compareToIgnoreCase(s2);
		}
		if (a instanceof IBuilder) {
			IBuilder c1 = (IBuilder) a;
			IBuilder c2 = (IBuilder) b;
			String s1 = c1.getUniqueRealName();
			if (s1 == null)
				s1 = EMPTY;
			String s2 = c2.getUniqueRealName();
			if (s2 == null)
				s2 = EMPTY;
			return s1.compareToIgnoreCase(s2);
		}
		if (a instanceof IBuildPropertyValue) {
			IBuildPropertyValue c1 = (IBuildPropertyValue) a;
			IBuildPropertyValue c2 = (IBuildPropertyValue) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}

		return super.compare(a, b);
	}
}
