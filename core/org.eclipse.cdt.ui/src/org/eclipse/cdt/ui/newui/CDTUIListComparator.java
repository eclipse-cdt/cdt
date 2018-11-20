/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Comparator;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.ui.newui.AbstractExportTab.ExtData;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CDTUIListComparator extends CDTListComparator {
	private static Comparator<Object> comparator = null;

	public static Comparator<Object> getInstance() {
		if (comparator == null)
			comparator = new CDTUIListComparator();
		return comparator;
	}

	@Override
	public int compare(Object a, Object b) {
		if (a == null || b == null)
			return 0;
		if (a instanceof ExtData) {
			ExtData c1 = (ExtData) a;
			ExtData c2 = (ExtData) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof IConfigurationElement) {
			IConfigurationElement e1 = (IConfigurationElement) a;
			IConfigurationElement e2 = (IConfigurationElement) b;
			return AbstractPage.getWeight(e1).compareTo(AbstractPage.getWeight(e2));
		}
		if (a instanceof EntryDescriptor) {
			EntryDescriptor c1 = (EntryDescriptor) a;
			EntryDescriptor c2 = (EntryDescriptor) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		return super.compare(a, b);
	}

}
