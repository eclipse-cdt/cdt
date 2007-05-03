/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.ui.newui.CDTListComparator;

/**
 * This class is intended to compare MBS-specific classes
 */
public class BuildListComparator extends CDTListComparator {
	private static BuildListComparator comparator = null;

	public static CDTListComparator getInstance() {
		if (comparator == null)
			comparator = new BuildListComparator();
		return comparator;
	}
	public int compare(Object a, Object b) {
		if (a == null || b == null) 
			return 0;
		if (a instanceof ITool) {
			ITool c1 = (ITool)a;
			ITool c2 = (ITool)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof IBuildPropertyValue) {
			IBuildPropertyValue c1 = (IBuildPropertyValue)a;
			IBuildPropertyValue c2 = (IBuildPropertyValue)b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		
		return super.compare(a, b);
	}
}
