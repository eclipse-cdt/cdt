/*******************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/**
 * Property tester to test expressions in plugin.xml. Tests following expressions:
 * 1. Checks whether given object is a source file. Usage:
 *   <test property="org.eclipse.cdt.ui.isSource"/>
 * 2. Checks value of a preference. Usage:
 *   <test property="org.eclipse.cdt.ui.checkPreference" value="org.eclipse.cdt.ui:properties.export.page.enable=true"/>
 */
public class CPropertyTester extends PropertyTester {
	private static final String KEY_SRC = "isSource"; //$NON-NLS-1$
	private static final String KEY_PREF = "checkPreference"; //$NON-NLS-1$
	private static final Pattern PREFERENCE_PATTERN = Pattern.compile("(.*)[/:](.*)=(.*)"); //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (KEY_SRC.equals(property)) {
			if (receiver instanceof ITranslationUnit) {
				return ((ITranslationUnit) receiver).isSourceUnit();
			} else if (receiver instanceof IFile) {
				IFile file = (IFile) receiver;
				return CoreModel.isValidSourceUnitName(file.getProject(), file.getName());
			}
		} else if (KEY_PREF.equals(property) && expectedValue instanceof String) {
			Matcher matcher = PREFERENCE_PATTERN.matcher((String) expectedValue);
			if (matcher.matches()) {
				String pluginId = matcher.group(1);
				String preference = matcher.group(2);
				String wantedValue = matcher.group(3);

				IPreferencesService preferences = Platform.getPreferencesService();
				String actualValue = preferences.getString(pluginId, preference, null, null);
				if (wantedValue != null) {
					return wantedValue.equals(actualValue) || (actualValue == null && wantedValue.equals("false")); //$NON-NLS-1$
				} else {
					return actualValue != null;
				}
			}
		}
		return false;
	}
}
