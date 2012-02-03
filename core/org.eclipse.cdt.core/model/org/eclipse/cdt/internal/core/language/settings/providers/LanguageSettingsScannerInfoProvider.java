/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.settings.model.ACPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of {@link IScannerInfoProvider} backed by the list of
 * language settings providers of "default settings configuration"
 * (see {@link ICProjectDescription#getDefaultSettingConfiguration()}).
 *
 * @see IScannerInfo#getIncludePaths()
 *
 */
public class LanguageSettingsScannerInfoProvider implements IScannerInfoProvider, ILanguageSettingsChangeListener {
	private static final String FRAMEWORK_PRIVATE_HEADERS_INCLUDE = "/__framework__.framework/PrivateHeaders/__header__"; //$NON-NLS-1$
	private static final String FRAMEWORK_HEADERS_INCLUDE = "/__framework__.framework/Headers/__header__"; //$NON-NLS-1$
	private static final ExtendedScannerInfo DUMMY_SCANNER_INFO = new ExtendedScannerInfo();

	private Map<IResource, List<IScannerInfoChangeListener>> listenersMap = null;

	@Override
	public ExtendedScannerInfo getScannerInformation(IResource rc) {
		IProject project = rc.getProject();
		if (project==null)
			return DUMMY_SCANNER_INFO;

		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if (prjDescription==null)
			return DUMMY_SCANNER_INFO;

		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		if (cfgDescription==null)
			return DUMMY_SCANNER_INFO;

		List<String> languageIds = LanguageSettingsManager.getLanguages(rc, cfgDescription);
		if (languageIds.isEmpty()) {
			String msg = NLS.bind(SettingsModelMessages.getString("LanguageSettingsScannerInfoProvider.UnableToDetermineLanguage"), rc.toString()); //$NON-NLS-1$
			IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, new Exception());
			CCorePlugin.log(status);
			return DUMMY_SCANNER_INFO;
		}

		LinkedHashSet<ICLanguageSettingEntry> includePathEntries = new LinkedHashSet<ICLanguageSettingEntry>();
		LinkedHashSet<ICLanguageSettingEntry> includePathLocalEntries = new LinkedHashSet<ICLanguageSettingEntry>();
		LinkedHashSet<ICLanguageSettingEntry> includeFileEntries = new LinkedHashSet<ICLanguageSettingEntry>();
		LinkedHashSet<ICLanguageSettingEntry> macroFileEntries = new LinkedHashSet<ICLanguageSettingEntry>();
		LinkedHashSet<ICLanguageSettingEntry> macroEntries = new LinkedHashSet<ICLanguageSettingEntry>();

		for (String langId : languageIds) {
			List<ICLanguageSettingEntry> incSys = LanguageSettingsProvidersSerializer.getSystemSettingEntriesByKind(cfgDescription, rc, langId,
					ICSettingEntry.INCLUDE_PATH);
			includePathEntries.addAll(incSys);

			List<ICLanguageSettingEntry> incLocal = LanguageSettingsProvidersSerializer.getLocalSettingEntriesByKind(cfgDescription, rc, langId,
					ICSettingEntry.INCLUDE_PATH);
			includePathLocalEntries.addAll(incLocal);

			List<ICLanguageSettingEntry> incFiles = LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, rc, langId,
					ICSettingEntry.INCLUDE_FILE);
			includeFileEntries.addAll(incFiles);

			List<ICLanguageSettingEntry> macroFiles = LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, rc, langId,
					ICSettingEntry.MACRO_FILE);
			macroFileEntries.addAll(macroFiles);

			List<ICLanguageSettingEntry> macros = LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, rc, langId,
					ICSettingEntry.MACRO);
			macroEntries.addAll(macros);
		}

		String[] includePaths = convertToLocations(includePathEntries, cfgDescription);
		String[] includePathsLocal = convertToLocations(includePathLocalEntries, cfgDescription);
		String[] includeFiles = convertToLocations(includeFileEntries, cfgDescription);
		String[] macroFiles = convertToLocations(macroFileEntries, cfgDescription);

		Map<String, String> definedMacros = new HashMap<String, String>();
		for (ICLanguageSettingEntry entry : macroEntries) {
			ICMacroEntry macroEntry = (ICMacroEntry)entry;
			String name = macroEntry.getName();
			String value = macroEntry.getValue();
			definedMacros.put(name, value);
		}

		return new ExtendedScannerInfo(definedMacros, includePaths, macroFiles, includeFiles, includePathsLocal);
	}

	private IPath expandVariables(IPath path, ICConfigurationDescription cfgDescription) {
		ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
		String pathStr = path.toString();
		try {
			pathStr = varManager.resolveValue(pathStr, "", null, cfgDescription); //$NON-NLS-1$
		} catch (CdtVariableException e) {
			// Swallow exceptions but also log them
			CCorePlugin.log(e);
		}
		IPath resolvedLoc = new Path(pathStr);
		return resolvedLoc;
	}

	/**
	 * Get build working directory for the provided configuration. Returns
	 * project location if none defined.
	 */
	private static IPath getBuildCWD(ICConfigurationDescription cfgDescription) {
		IPath buildCWD = cfgDescription.getBuildSetting().getBuilderCWD();
		if (buildCWD==null) {
			IProject project = cfgDescription.getProjectDescription().getProject();
			buildCWD = project.getLocation();
		} else {
			ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();
			try {
				// Note that IPath buildCWD holding variables is mis-constructed,
				// i.e. ${workspace_loc:/path} gets split into 2 path segments
				// still, MBS does that and we need to handle that
				String buildPathString = buildCWD.toString();
				buildPathString = mngr.resolveValue(buildPathString, "", null, cfgDescription); //$NON-NLS-1$
				buildCWD = new Path(buildPathString);
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}

		}
		buildCWD = buildCWD.addTrailingSeparator();
		return buildCWD;
	}

	/**
	 * Resolve location to file system location in a configuration context.
	 * Resolving includes replacing build/environment variables with values, making relative path absolute etc.
	 *
	 * @param location - location to resolve. If relative, it is taken to be rooted in build working directory.
	 * @param cfgDescription - the configuration context.
	 * @return resolved file system location.
	 */
	private static String resolveEntry(String location, ICConfigurationDescription cfgDescription) {
		// Substitute build/environment variables
		ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
		try {
			location = varManager.resolveValue(location, "", null, cfgDescription); //$NON-NLS-1$
		} catch (CdtVariableException e) {
			// Swallow exceptions but also log them
			CCorePlugin.log(e);
		}
		// use OS file separators (i.e. '\' on Windows)
		if (java.io.File.separatorChar != '/') {
			location = location.replace('/', java.io.File.separatorChar);
		}

		// note that we avoid using org.eclipse.core.runtime.Path for manipulations being careful
		// to preserve "../" segments and not let collapsing them which is not correct for symbolic links.
		Path locPath = new Path(location);
		if (locPath.isAbsolute() && locPath.getDevice()==null) {
			// prepend device (C:) for Windows
			IPath buildCWD = getBuildCWD(cfgDescription);
			String device = buildCWD.getDevice();
			if (device!=null)
				location = device + location;
		}
		if (!locPath.isAbsolute()) {
			// consider relative path to be from build working directory
			IPath buildCWD = getBuildCWD(cfgDescription);
			location = buildCWD.toOSString() + locPath;
		}
		return location;
	}

	/**
	 * Convert the path entries to absolute file system locations represented as String array.
	 * Resolve the entries which are not resolved.
	 *
	 * @param entriesPath - language settings path entries.
	 * @param cfgDescription - configuration description for resolving entries.
	 * @return array of the locations.
	 */
	private String[] convertToLocations(LinkedHashSet<ICLanguageSettingEntry> entriesPath, ICConfigurationDescription cfgDescription){
		List<String> locations = new ArrayList<String>(entriesPath.size());
		for (ICLanguageSettingEntry entry : entriesPath) {
			ACPathEntry entryPath = (ACPathEntry)entry;
			if (entryPath.isValueWorkspacePath()) {
				IPath loc = entryPath.getLocation();
				if (loc!=null) {
					if (checkBit(entryPath.getFlags(), ICSettingEntry.FRAMEWORKS_MAC)) {
						// handle frameworks, see IScannerInfo.getIncludePaths()
						locations.add(loc.append(FRAMEWORK_HEADERS_INCLUDE).toOSString());
						locations.add(loc.append(FRAMEWORK_PRIVATE_HEADERS_INCLUDE).toOSString());
					} else {
						locations.add(loc.toOSString());
					}
				}
			} else {
				String locStr = entryPath.getName();
				if (entryPath.isResolved()) {
					locations.add(locStr);
				} else {
					locStr = resolveEntry(locStr, cfgDescription);
					if (locStr!=null) {
						if (checkBit(entryPath.getFlags(), ICSettingEntry.FRAMEWORKS_MAC)) {
							// handle frameworks, see IScannerInfo.getIncludePaths()
							locations.add(locStr+FRAMEWORK_HEADERS_INCLUDE);
							locations.add(locStr+FRAMEWORK_PRIVATE_HEADERS_INCLUDE);
						} else {
							locations.add(locStr);
							// add relative paths again for indexer to resolve from source file location
							IPath unresolvedPath = entryPath.getLocation();
							if (!unresolvedPath.isAbsolute()) {
								IPath expandedPath = expandVariables(unresolvedPath, cfgDescription);
								if (!expandedPath.isAbsolute()) {
									locations.add(expandedPath.toOSString());
								}
							}
						}
					}
				}
			}
		}

		return locations.toArray(new String[locations.size()]);
	}

	private static boolean checkBit(int flags, int bit) {
		return (flags & bit) == bit;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}

		if (listenersMap == null) {
			listenersMap = Collections.synchronizedMap(new HashMap<IResource, List<IScannerInfoChangeListener>>());
		}

		IProject project = resource.getProject();
		List<IScannerInfoChangeListener> list = listenersMap.get(project);
		if (list == null) {
			list = new Vector<IScannerInfoChangeListener>();
			listenersMap.put(project, list);
		}
		if (!list.contains(listener)) {
			list.add(listener);
		}
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		if (resource == null || listener == null) {
			return;
		}

		IProject project = resource.getProject();
		if (listenersMap != null) {
			List<IScannerInfoChangeListener> list = listenersMap.get(project);
			if (list != null) {
				list.remove(listener);
			}
		}
	}

	@Override
	public void handleEvent(ILanguageSettingsChangeEvent event) {
		if (listenersMap == null || listenersMap.isEmpty()) {
			return;
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(event.getProjectName());
		if (project != null) {
			ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project);
			if (prjDescription != null) {
				ICConfigurationDescription indexedCfgDescription = prjDescription.getDefaultSettingConfiguration();
				String indexedCfgId = indexedCfgDescription.getId();

				for (String cfgId : event.getConfigurationDescriptionIds()) {
					if (cfgId.equals(indexedCfgId)) {
						for (Entry<IResource, List<IScannerInfoChangeListener>> entry : listenersMap.entrySet()) {
							IResource rc = entry.getKey();
							List<IScannerInfoChangeListener> listeners = listenersMap.get(rc);
							if (listeners != null && !listeners.isEmpty()) {
								IScannerInfo info = getScannerInformation(rc);
								for (IScannerInfoChangeListener listener : listeners) {
									listener.changeNotification(rc, info);
								}
							}
						}
						break;
					}
				}

			}
		}
	}
}
