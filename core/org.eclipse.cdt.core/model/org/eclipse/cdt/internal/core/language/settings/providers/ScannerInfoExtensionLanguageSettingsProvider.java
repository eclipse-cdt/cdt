/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Wrapper class intended to provide backward compatibility with ScannerInfoProvider defined by org.eclipse.cdt.core.ScannerInfoProvider extension point
 */
public class ScannerInfoExtensionLanguageSettingsProvider extends LanguageSettingsBaseProvider {
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId) {
		List<ICLanguageSettingEntry> entries = new ArrayList<>();
		IScannerInfoProvider scannerInfoProvider = getScannerInfoProvider(cfgDescription);
		if (scannerInfoProvider != null) {
			IScannerInfo si = scannerInfoProvider.getScannerInformation(rc);
			if (si != null) {
				if (si instanceof IExtendedScannerInfo) {
					addLocalIncludePaths(entries, (IExtendedScannerInfo) si);
				}

				addSystemIncludePaths(entries, si);
				addDefinedSymbols(entries, si);

				if (si instanceof IExtendedScannerInfo) {
					addIncludeFiles(entries, (IExtendedScannerInfo) si);
					addMacroFiles(entries, (IExtendedScannerInfo) si);
				}

				if (!entries.isEmpty()) {
					return LanguageSettingsSerializableStorage.getPooledList(entries);
				}
			}
		}
		return null;
	}

	/**
	 * Return ScannerInfoProvider defined in configuration metadata in .cproject.
	 *
	 * @param cfgDescription - configuration description.
	 * @return an instance of ScannerInfoProvider or {@code null}.
	 */
	public IScannerInfoProvider getScannerInfoProvider(ICConfigurationDescription cfgDescription) {
		if (cfgDescription == null || cfgDescription.isPreferenceConfiguration()) {
			return null;
		}

		IScannerInfoProvider scannerInfoProvider = null;
		ICConfigExtensionReference[] refs = cfgDescription.get(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		if (refs != null && refs.length > 0) {
			ICConfigExtensionReference ref = refs[0];
			try {
				AbstractCExtension cExtension = null;
				IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(ref, "cextension", false); //$NON-NLS-1$
				cExtension = (AbstractCExtension) el.createExecutableExtension("run"); //$NON-NLS-1$
				cExtension.setExtensionReference(ref);
				cExtension.setProject(ref.getConfiguration().getProjectDescription().getProject());
				if (cExtension instanceof IScannerInfoProvider) {
					scannerInfoProvider = (IScannerInfoProvider) cExtension;
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return scannerInfoProvider;
	}

	/**
	 * Add local include paths to the list of entries.
	 */
	private void addLocalIncludePaths(List<ICLanguageSettingEntry> entries, IExtendedScannerInfo esi) {
		String[] localIncludePaths = esi.getLocalIncludePath();
		if (localIncludePaths != null) {
			for (String path : localIncludePaths) {
				entries.add(CDataUtil.createCIncludePathEntry(path, ICSettingEntry.LOCAL));
			}
		}
	}

	/**
	 * Add system include paths to the list of entries.
	 */
	private void addSystemIncludePaths(List<ICLanguageSettingEntry> entries, IScannerInfo si) {
		String[] includePaths = si.getIncludePaths();
		if (includePaths != null) {
			for (String path : includePaths) {
				entries.add(CDataUtil.createCIncludePathEntry(path, 0));
			}
		}
	}

	/**
	 * Add defined macros to the list of entries.
	 */
	private void addDefinedSymbols(List<ICLanguageSettingEntry> entries, IScannerInfo si) {
		Map<String, String> definedSymbols = si.getDefinedSymbols();
		if (definedSymbols != null) {
			for (Entry<String, String> entry : new TreeMap<>(definedSymbols).entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				entries.add(CDataUtil.createCMacroEntry(name, value, 0));
			}
		}
	}

	/**
	 * Add include files to the list of entries.
	 */
	private void addIncludeFiles(List<ICLanguageSettingEntry> entries, IExtendedScannerInfo esi) {
		String[] includeFiles = esi.getIncludeFiles();
		if (includeFiles != null) {
			for (String path : includeFiles) {
				entries.add(CDataUtil.createCIncludeFileEntry(path, 0));
			}
		}
	}

	/**
	 * Add macro files to the list of entries.
	 */
	private void addMacroFiles(List<ICLanguageSettingEntry> entries, IExtendedScannerInfo esi) {
		String[] macroFiles = esi.getMacroFiles();
		if (macroFiles != null) {
			for (String path : macroFiles) {
				entries.add(CDataUtil.createCMacroFileEntry(path, 0));
			}
		}
	}
}
