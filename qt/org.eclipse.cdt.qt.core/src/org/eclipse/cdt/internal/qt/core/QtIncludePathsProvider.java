/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This provider uses persistent cache to store the include paths for different
 * Qt installations.  A Qt installation is uniquely identified by the path to
 * the qmake binary within the installation.
 * <p>
 * This result is shared among all Build Configurations that use the provider
 * with the same value for the QMAKE environment variable.
 */
public class QtIncludePathsProvider extends LanguageSettingsSerializableProvider {

	/**
	 * The provider identifies Qt installations by the absolute path to the qmake binary.  The
	 * include paths relevant to the installations are computed and persisted in {@link QtIncludePaths}.
	 */
	private final Map<String, QtIncludePaths> qtInstallHeaders = new HashMap<>();

	/**
	 * The build configuration stores the path to the qmake binary as an environment variable.
	 */
	private static final String ENVVAR_QMAKE = "QMAKE";

	private static final String ELEMENT_QMAKE = "qmake";

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QtIncludePathsProvider))
			return super.equals(obj);

		/**
		 * Providers are equal when they have the same cached values.
		 */
		QtIncludePathsProvider other = (QtIncludePathsProvider) obj;
		if (qtInstallHeaders == null)
			return other.qtInstallHeaders == null;
		return qtInstallHeaders.equals(other.qtInstallHeaders);
	}

	@Override
	public int hashCode() {
		return qtInstallHeaders == null ? 0 : qtInstallHeaders.hashCode();
	}

	@Override
	public void loadEntries(Element providerNode) {
		super.loadEntries(providerNode);

		// Find and load all qmake child nodes.  There will be one node for each Qt
		// installation that has been used.  Qt installations that are no longer valid
		// are not loaded.  This means they will be removed from the file the next time
		// that the language setting providers are serialized.
		NodeList children = providerNode.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (ELEMENT_QMAKE.equals(child.getNodeName())) {
				QtIncludePaths qtIncludePaths = QtIncludePaths.loadFrom(child);
				if (qtIncludePaths != null && qtIncludePaths.isValid())
					qtInstallHeaders.put(qtIncludePaths.getQMakePath(), qtIncludePaths);
			}
		}
	}

	@Override
	public void serializeEntries(Element parent) {
		// NOTE: This creates its own XML structure where children of the provider node are qmake nodes.
		//       Within each qmake node is a list of include paths for that installation.  Calling the
		//       base #serializeEntries here would try to write this instance's (empty) list of settings
		//       to the file.

		// Each value is serialized into a new element in the XML document.
		Document document = parent instanceof Document ? (Document) parent : parent.getOwnerDocument();
		for (QtIncludePaths qtIncludePaths : qtInstallHeaders.values()) {
			Element child = document.createElement(ELEMENT_QMAKE);
			qtIncludePaths.serialize(child);
			parent.appendChild(child);
		}
	}

	/**
	 * The given build configuration's QMAKE environment variable is used to identify the appropriate
	 * Qt installation.  The language settings are then either returned from the previously persisted
	 * data or loaded, serialized, and returned.
	 */
	@Override
	public synchronized List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription configDesc,
			IResource rc, String languageId) {
		// Make sure the requested language is in scope for this provider.
		if (!getLanguageScope().contains(languageId))
			return null;

		// The value of the build configuration's QMAKE environment variable is used to select the
		// right version of qmake.
		IEnvironmentVariable qmake_var = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENVVAR_QMAKE,
				configDesc, true);
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

		return paths.getSettingEntries(configDesc, null, languageId);
	}
}
