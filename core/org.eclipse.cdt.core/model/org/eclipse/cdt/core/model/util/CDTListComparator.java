/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others
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
package org.eclipse.cdt.core.model.util;

import java.util.Comparator;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

public class CDTListComparator implements Comparator<Object> {
	private static Comparator<Object> comparator = null;

	public static Comparator<Object> getInstance() {
		if (comparator == null)
			comparator = new CDTListComparator();
		return comparator;
	}

	@Override
	public int compare(Object a, Object b) {
		if (a == null || b == null)
			return 0;
		if (a instanceof ICLanguageSetting) {
			ICLanguageSetting c1 = (ICLanguageSetting) a;
			ICLanguageSetting c2 = (ICLanguageSetting) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry c1 = (ICLanguageSettingEntry) a;
			ICLanguageSettingEntry c2 = (ICLanguageSettingEntry) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof ICConfigurationDescription) {
			ICConfigurationDescription c1 = (ICConfigurationDescription) a;
			ICConfigurationDescription c2 = (ICConfigurationDescription) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		if (a instanceof ICdtVariable) {
			ICdtVariable c1 = (ICdtVariable) a;
			ICdtVariable c2 = (ICdtVariable) b;
			return c1.getName().compareToIgnoreCase(c2.getName());
		}
		return a.toString().compareTo(b.toString());
	}

}
