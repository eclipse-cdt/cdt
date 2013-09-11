/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * This provider uses an in-memory cache to store the include paths for different
 * versions of qmake.  The results are shared among all Build Configurations that
 * use the provider with the same value for the QMAKE environment variable.
 */
public class QtIncludePathsProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {

	private static final Map<String, QtIncludePaths> qtInstallHeaders = new HashMap<String, QtIncludePaths>();

	private static final String QMAKE_VARNAME = "QMAKE";

	@Override
	public synchronized List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription configDesc, IResource rc, String languageId) {

		// The value of the build configuration's QMAKE environment variable is used to select the
		// right version of qmake.
		IEnvironmentVariable qmake_var = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(QMAKE_VARNAME, configDesc, true);
		if (qmake_var == null)
			return null;

		String qmake = qmake_var.getValue();
		if (qmake == null)
			return null;

		// The path to qmake is used as the key into the in-memory cache of header paths.
		QtIncludePaths paths = qtInstallHeaders.get(qmake);
		if (paths == null) {
			paths = new QtIncludePaths(qmake);
			qtInstallHeaders.put(qmake, paths);
		}

		return paths.getEntries();
	}
}
