/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Property tester to test expressions in plugin.xml. Tests following expressions:
 * 1. Checks whether given object is a source file. Usage:
 *   <test property="org.eclipse.cdt.ui.isSource"/>
 * 2. Checks value of a preference. Usage:
 *   <test property="org.eclipse.cdt.ui.checkPreference" value="org.eclipse.cdt.ui:properties.export.page.enable=true"/>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	private static final String KEY_SRC = "isSource"; //$NON-NLS-1$
	private static final String KEY_PREF = "checkPreference"; //$NON-NLS-1$

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
			boolean result = false;
			final Pattern pat = Pattern.compile("(.*):(.*)=(.*)"); //$NON-NLS-1$
			Matcher matcher = pat.matcher((String) expectedValue);
			if (matcher.matches()) {
				String pluginId = matcher.group(1);
				String preference = matcher.group(2);
				String wantedValue = matcher.group(3);

				IEclipsePreferences node = InstanceScope.INSTANCE.getNode(pluginId);
				if (wantedValue != null) {
					String actualValue = node.get(preference, ""); //$NON-NLS-1$
					result = wantedValue.equals(actualValue) || (wantedValue.equals("false") && actualValue.isEmpty()); //$NON-NLS-1$
				} else {
					try {
						result = Arrays.asList(node.keys()).contains(preference);
					} catch (BackingStoreException e) {
						CUIPlugin.log(e);
					}
				}
			}
			return result;
		}
		return false;
	}

}
