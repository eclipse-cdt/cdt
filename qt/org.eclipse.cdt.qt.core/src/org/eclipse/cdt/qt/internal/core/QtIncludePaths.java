package org.eclipse.cdt.qt.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

/**
 * Qt uses a tool called qmake to generate makefiles for Qt projects.  The tool has a
 * query mode that can be used to discover information about the Qt installation.  This
 * class uses qmake to build a list of all installed Qt include paths.  This is
 * used by an implementation of {@link ILanguageSettingsProvider} to contribute the
 * include paths to the CDT project model.
 *
 * NOTE: This implementation is not thread-safe.
 *
 * @see QtIncludePathsProvider
 */
public class QtIncludePaths {

	private final String qmakePath;

	private long qmakeModTime;
	private long qtInstallHeadersModTime;
	private String qtInstallHeadersPath;
	private List<ICLanguageSettingEntry> entries;

	public QtIncludePaths(String qmakePath) {
		this.qmakePath = qmakePath;
	}

	/**
	 * Return a current list of the include paths for the receiver's instance of qmake.  Return
	 * null if no such paths can be found.
	 *
	 * Updates the cached results if the timestamp of either the qmake executable or the
	 * QT_INSTALL_HEADERS folder has changed.
	 */
	public List<ICLanguageSettingEntry> getEntries() {
		if (!valid())
			reload();

		return entries;
	}

	private boolean valid() {
		File qmake = new File(qmakePath);
		if (!qmake.exists()
		 || qmakeModTime != qmake.lastModified())
			return false;

		File qtInstallHeadersDir = new File(qtInstallHeadersPath);
		if (!qtInstallHeadersDir.exists()
		 || qtInstallHeadersModTime != qtInstallHeadersDir.lastModified())
			return false;

		return true;
	}

	private void reload() {
		qmakeModTime = 0;
		qtInstallHeadersPath = null;
		qtInstallHeadersModTime = 0;
		entries = null;

		File qmake = new File(qmakePath);
		if (!qmake.exists()
		 || !qmake.canExecute())
			return;

		qmakeModTime = qmake.lastModified();

		// Run `qmake -query QT_INSTALL_HEADERS` to get output like "/opt/qt-5.0.0/include".
		BufferedReader reader = null;
		Process process = null;
		try {
			process = ProcessFactory.getFactory().exec(new String[]{ qmakePath, "-query", "QT_INSTALL_HEADERS" });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			qtInstallHeadersPath = reader.readLine();

		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					/* ignore */
				}
			if (process != null)
				process.destroy();
		}

		if (qtInstallHeadersPath == null)
			return;

		File qtInstallHeadersDir = new File(qtInstallHeadersPath);

		// Check if the QT_INSTALL_HEADERS folder exists and get the timestamp of the last modification.
		qtInstallHeadersModTime = qtInstallHeadersDir.lastModified();
		if (!qtInstallHeadersDir.exists()
		 || !qtInstallHeadersDir.canRead()
		 || !qtInstallHeadersDir.isDirectory())
			entries = Collections.emptyList();
		else {
			// Create an include path entry for all sub-folders in the QT_INSTALL_HEADERS location.
			File[] files = qtInstallHeadersDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.exists() && pathname.isDirectory();
				}
			});

			entries = new ArrayList<ICLanguageSettingEntry>(files.length);
			for(File file : files)
				try {
					entries.add(new CIncludePathEntry(file.getCanonicalPath(), ICSettingEntry.READONLY | ICSettingEntry.RESOLVED));
				} catch(IOException e) {
					QtPlugin.log(e);
				}
		}
	}
}
