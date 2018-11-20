/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Discovers and persists the list of Qt include paths for a particular installation of
 * Qt.  The Qt installation is described by the path to qmake.
 * <p>
 * Qt uses a tool called qmake to generate makefiles for Qt projects.  The tool has a
 * query mode that can be used to discover information about the Qt installation.  Here
 * qmake is used to build a list of all installed Qt include paths.
 * <p>
 * These paths are persisted into a file called language-settings.xml in the workspace
 * metadata area.
 *
 * @see QtIncludePathsProvider
 */
public class QtIncludePaths extends LanguageSettingsSerializableProvider {

	/**
	 * The path to the qmake executable uniquely identifies this installation.
	 */
	private final String qmakePath;

	/**
	 * The cached data is reloaded when the qmake executable is modified.
	 */
	private long qmakeModTime;

	/**
	 * The cached data is reloaded when the folder holding the include paths
	 * is removed.
	 */
	private String qtInstallHeadersPath;

	/**
	 * The cached data is reloaded when the folder containing the include folders is
	 * modified.
	 */
	private long qtInstallHeadersModTime;

	private static final String ATTR_QMAKE = "qmake";
	private static final String ATTR_QMAKE_MOD = "qmakeModification";
	private static final String ATTR_QT_INSTALL_HEADERS = "QT_INSTALL_HEADERS";
	private static final String ATTR_QT_INSTALL_HEADERS_MOD = "qtInstallHeadersModification";

	/**
	 * Create a new instance of the include path wrapper for the Qt installation for
	 * the given qmake binary.
	 */
	public QtIncludePaths(String qmakePath) {
		this.qmakePath = qmakePath;
	}

	/**
	 * Create and load an instance of QtIncludePaths from data that was serialized into the
	 * given XML element.  Return null if an instance cannot be loaded or if the installation
	 * is no longer valid.
	 */
	public static QtIncludePaths loadFrom(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE)
			return null;

		Element element = (Element) node;
		String qmakePath = element.getAttribute(ATTR_QMAKE);
		if (qmakePath == null || qmakePath.isEmpty())
			return null;

		QtIncludePaths qtIncludePaths = new QtIncludePaths(qmakePath);
		qtIncludePaths.load(element);
		return qtIncludePaths;
	}

	public String getQMakePath() {
		return qmakePath;
	}

	/**
	 * Return true if the receiver points to a valid Qt installation and false otherwise.
	 * The installation is considered valid if an executable qmake binary exists at the
	 * expected location.
	 */
	public boolean isValid() {
		if (qmakePath == null || qmakePath.isEmpty())
			return false;

		File qmake = new File(qmakePath);
		return qmake.exists() && qmake.canExecute();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QtIncludePaths))
			return super.equals(obj);

		// Include paths are equivalent when they point to the same qmake binary.  All other
		// values are reloaded from that binary and do not need to be directly compared.
		QtIncludePaths other = (QtIncludePaths) obj;
		return qmakePath == null ? other.qmakePath == null : qmakePath.equals(other.qmakePath);
	}

	@Override
	public int hashCode() {
		return qmakePath == null ? 0 : qmakePath.hashCode();
	}

	/**
	 * Return a current list of the include paths for this Qt installation.  Return null if
	 * no such paths can be found.
	 * <p>
	 * Updates the cached results if needed.  If the settings are updated then the new list
	 * will be serialized into the workspace metadata area.
	 */
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription configDesc, IResource rc,
			String languageId) {
		List<ICLanguageSettingEntry> entries = null;

		File qmake = new File(qmakePath);
		if (!qmake.exists() || qmakeModTime != qmake.lastModified())
			entries = reload();
		else {
			File qtInstallHeadersDir = new File(qtInstallHeadersPath);
			if (!qtInstallHeadersDir.exists() || qtInstallHeadersModTime != qtInstallHeadersDir.lastModified())
				entries = reload();
		}

		// If the cache was not reloaded, then return the previously discovered entries.
		if (entries == null)
			return super.getSettingEntries(configDesc, rc, languageId);

		// Otherwise store, persist, and return the newly discovered values.
		setSettingEntries(configDesc, rc, languageId, entries);
		serializeLanguageSettingsInBackground(null);
		return entries;
	}

	@Override
	public Element serializeAttributes(Element parentElement) {
		parentElement.setAttribute(ATTR_QMAKE, qmakePath);
		parentElement.setAttribute(ATTR_QMAKE_MOD, Long.toString(qmakeModTime));
		parentElement.setAttribute(ATTR_QT_INSTALL_HEADERS, qtInstallHeadersPath);
		parentElement.setAttribute(ATTR_QT_INSTALL_HEADERS_MOD, Long.toString(qtInstallHeadersModTime));

		// The parent implementation tries to create a new child node (provider) that is used
		// as the part for later entries.  This isn't needed in this case, we just want to
		// use the part that serializes the languages.
		return parentElement;
	}

	@Override
	public void loadAttributes(Element element) {
		qmakeModTime = getLongAttribute(element, ATTR_QMAKE_MOD);
		qtInstallHeadersPath = element.getAttribute(ATTR_QT_INSTALL_HEADERS);
		qtInstallHeadersModTime = getLongAttribute(element, ATTR_QT_INSTALL_HEADERS_MOD);

		// The parent implementation tries to create a new child node (provider) that is used
		// as the part for later entries.  This isn't needed in this case, we just want to
		// use the part that serializes the languages.
	}

	/**
	 * Parse and return the given attribute as a long.  Return 0 if the attribute does
	 * not have a valid value.
	 */
	private static long getLongAttribute(Element element, String attr) {
		String value = element.getAttribute(attr);
		if (value == null || value.isEmpty())
			return 0;

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			Activator.log("attribute name:" + attr + " value:" + value, e);
			return 0;
		}
	}

	/**
	 * Reload and return the entries if possible, return null otherwise.
	 */
	private List<ICLanguageSettingEntry> reload() {
		// All keys are reset and then updated as their values are discovered.  This allows partial
		// success to skip over previously calculated values.
		qmakeModTime = 0;
		qtInstallHeadersPath = null;
		qtInstallHeadersModTime = 0;

		File qmake = new File(qmakePath);
		if (!qmake.exists() || !qmake.canExecute())
			return Collections.emptyList();

		qmakeModTime = qmake.lastModified();

		// Run `qmake -query QT_INSTALL_HEADERS` to get output like "/opt/qt-5.0.0/include".
		BufferedReader reader = null;
		Process process = null;
		try {
			process = ProcessFactory.getFactory().exec(new String[] { qmakePath, "-query", "QT_INSTALL_HEADERS" });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			qtInstallHeadersPath = reader.readLine();
		} catch (IOException e) {
			Activator.log(e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				/* ignore */
			} finally {
				if (process != null)
					process.destroy();
			}
		}

		if (qtInstallHeadersPath == null)
			return Collections.emptyList();

		File qtInstallHeadersDir = new File(qtInstallHeadersPath);

		qtInstallHeadersModTime = qtInstallHeadersDir.lastModified();
		if (!qtInstallHeadersDir.exists() || !qtInstallHeadersDir.canRead() || !qtInstallHeadersDir.isDirectory())
			return Collections.emptyList();

		// Create an include path entry for all sub-folders in the QT_INSTALL_HEADERS location, including
		// the QT_INSTALL_HEADERS folder itself.
		File[] files = qtInstallHeadersDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.exists() && pathname.isDirectory();
			}
		});

		List<ICLanguageSettingEntry> entries = new ArrayList<>(files.length + 1);
		safeAdd(entries, qtInstallHeadersDir);
		for (File file : files)
			safeAdd(entries, file);

		return entries;
	}

	private static void safeAdd(List<ICLanguageSettingEntry> entries, File file) {
		try {
			entries.add(
					new CIncludePathEntry(file.getCanonicalPath(), ICSettingEntry.READONLY | ICSettingEntry.RESOLVED));
		} catch (IOException e) {
			Activator.log(e);
		}
	}
}
