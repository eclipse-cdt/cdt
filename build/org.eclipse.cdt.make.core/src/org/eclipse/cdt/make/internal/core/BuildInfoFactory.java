/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.service.environment.*;

public class BuildInfoFactory {

	private static final String PREFIX = MakeCorePlugin.getUniqueIdentifier();

	static final String BUILD_COMMAND = PREFIX + ".buildCommand"; //$NON-NLS-1$
	static final String BUILD_LOCATION = PREFIX + ".buildLocation"; //$NON-NLS-1$
	static final String STOP_ON_ERROR = PREFIX + ".stopOnError"; //$NON-NLS-1$
	static final String USE_DEFAULT_BUILD_CMD = PREFIX + ".useDefaultBuildCmd"; //$NON-NLS-1$
	static final String BUILD_TARGET_AUTO = PREFIX + ".autoBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_INCREMENTAL = PREFIX + ".incrementalBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_FULL = PREFIX + ".fullBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_CLEAN = PREFIX + ".cleanBuildTarget"; //$NON-NLS-1$
	static final String BUILD_FULL_ENABLED = PREFIX + ".enableFullBuild"; //$NON-NLS-1$
	static final String BUILD_CLEAN_ENABLED = PREFIX + ".enableCleanBuild"; //$NON-NLS-1$
	static final String BUILD_INCREMENTAL_ENABLED = PREFIX + ".enabledIncrementalBuild"; //$NON-NLS-1$
	static final String BUILD_AUTO_ENABLED = PREFIX + ".enableAutoBuild"; //$NON-NLS-1$
	static final String BUILD_ARGUMENTS = PREFIX + ".buildArguments"; //$NON-NLS-1$
	static final String ENVIRONMENT = PREFIX + ".environment"; //$NON-NLS-1$
	static final String BUILD_APPEND_ENVIRONMENT = PREFIX + ".append_environment"; //$NON-NLS-1$ 

	private abstract static class AbstractBuildInfo implements IMakeBuilderInfo {

		public void setUseDefaultBuildCmd(boolean on) throws CoreException {
			putString(USE_DEFAULT_BUILD_CMD, new Boolean(on).toString());
		}

		public boolean isDefaultBuildCmd() {
			if (getString(USE_DEFAULT_BUILD_CMD) == null) { // if no property
				// then default to
				// true
				return true;
			}
			return getBoolean(USE_DEFAULT_BUILD_CMD);
		}

		public String getBuildAttribute(String name, String defaultValue) {
			String value = getString(name);
			if (value == null ) {
				if (IMakeCommonBuildInfo.BUILD_COMMAND.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_COMMAND);
				} else if (IMakeCommonBuildInfo.BUILD_ARGUMENTS.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_ARGUMENTS);
				} else if (IMakeCommonBuildInfo.BUILD_LOCATION.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_LOCATION);
				} else if (IMakeBuilderInfo.BUILD_TARGET_AUTO.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_TARGET_AUTO);
				} else if (IMakeBuilderInfo.BUILD_TARGET_CLEAN.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_TARGET_CLEAN);
				} else if (IMakeBuilderInfo.BUILD_TARGET_FULL.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_TARGET_FULL);
				} else if (IMakeBuilderInfo.BUILD_TARGET_INCREAMENTAL.equals(name)) {
					value = getString(BuildInfoFactory.BUILD_TARGET_INCREMENTAL);
				} 
			} 
			return value != null ? value : defaultValue != null ? defaultValue : ""; //$NON-NLS-1$
		}

		public void setBuildAttribute(String name, String value) throws CoreException {
			putString(name, value);
		}

		public Map getExpandedEnvironment() {
			Map env = getEnvironment();
			HashMap envMap = new HashMap(env.entrySet().size());
			Iterator iter = env.entrySet().iterator();
			boolean win32 = Platform.getOS().equals(Constants.OS_WIN32);
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry)iter.next();
				String key = (String)entry.getKey();
				if (win32) {
					// Win32 vars are case insensitive. Uppercase everything so
					// that (for example) "pAtH" will correctly replace "PATH"
					key = key.toUpperCase();
				}
				String value = (String)entry.getValue();
				// translate any string substitution variables
				String translated = value;
				try {
					translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value, false);
				} catch (CoreException e) {
				}
				envMap.put(key, translated);
			}
			return envMap;
		}

		public void setBuildCommand(IPath location) throws CoreException {
			putString(IMakeCommonBuildInfo.BUILD_COMMAND, null);
			putString(BuildInfoFactory.BUILD_COMMAND, location.toString());
		}

		public IPath getBuildCommand() {
			if (isDefaultBuildCmd()) {
				String command = getBuildParameter("defaultCommand"); //$NON-NLS-1$
				if (command == null) {
					return new Path("make"); //$NON-NLS-1$
				}
				return new Path(command);
			}
			String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, getString(BuildInfoFactory.BUILD_COMMAND));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return new Path(result);
		}

		protected String getBuildParameter(String name) {
			IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES,
					ResourcesPlugin.PT_BUILDERS, getBuilderID());
			if (extension == null)
				return null;
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length == 0)
				return null;
			// The nature exists, or this builder doesn't specify a nature
			IConfigurationElement[] runElement = configs[0].getChildren("run"); //$NON-NLS-1$
			IConfigurationElement[] paramElement = runElement[0].getChildren("parameter"); //$NON-NLS-1$
			for (int i = 0; i < paramElement.length; i++) {
				if (paramElement[i].getAttribute("name").equals(name)) { //$NON-NLS-1$
					return paramElement[i].getAttribute("value"); //$NON-NLS-1$
				}
			}
			return null;
		}

		protected abstract String getBuilderID();

		public void setBuildLocation(IPath location) throws CoreException {
			putString(IMakeCommonBuildInfo.BUILD_LOCATION, null);
			putString(BuildInfoFactory.BUILD_LOCATION, location.toString());
		}

		public IPath getBuildLocation() {
			String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_LOCATION, getString(BuildInfoFactory.BUILD_LOCATION));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return new Path(result);
		}

		public String getBuildArguments() {
			String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, getString(BuildInfoFactory.BUILD_ARGUMENTS));
			if (result == null) {
				return ""; //$NON-NLS-1$
			}
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return result;
		}

		public void setBuildArguments(String args) throws CoreException {
			putString(IMakeCommonBuildInfo.BUILD_ARGUMENTS, null);
			putString(BuildInfoFactory.BUILD_ARGUMENTS, args);
		}
		
		public void setStopOnError(boolean enabled) throws CoreException {
			putString(STOP_ON_ERROR, new Boolean(enabled).toString());
		}

		public boolean isStopOnError() {
			return getBoolean(STOP_ON_ERROR);
		}

		public void setAutoBuildTarget(String target) throws CoreException {
			putString(IMakeBuilderInfo.BUILD_TARGET_AUTO, null);
			putString(BuildInfoFactory.BUILD_TARGET_AUTO, target);
		}

		public String getAutoBuildTarget() {
			String result = getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, getString(BuildInfoFactory.BUILD_TARGET_AUTO));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return result;
		}

		public void setIncrementalBuildTarget(String target) throws CoreException {
			putString(IMakeBuilderInfo.BUILD_TARGET_INCREAMENTAL, null);
			putString(BuildInfoFactory.BUILD_TARGET_INCREMENTAL, target);
		}

		public String getIncrementalBuildTarget() {
			String result = getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREAMENTAL,
					getString(BuildInfoFactory.BUILD_TARGET_INCREMENTAL));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return result;
		}

		public void setFullBuildTarget(String target) throws CoreException {
			putString(IMakeBuilderInfo.BUILD_TARGET_FULL, null);
			putString(BuildInfoFactory.BUILD_TARGET_FULL, target);
		}

		public String getFullBuildTarget() {
			String result = getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_FULL, getString(BuildInfoFactory.BUILD_TARGET_FULL));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return result;
		}

		public void setCleanBuildTarget(String target) throws CoreException {
			putString(IMakeBuilderInfo.BUILD_TARGET_CLEAN, null);
			putString(BuildInfoFactory.BUILD_TARGET_CLEAN, target);
		}

		public String getCleanBuildTarget() {
			String result = getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, getString(BuildInfoFactory.BUILD_TARGET_CLEAN));
			try {
				result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
			} catch (CoreException e) {
			}
			return result;
		}

		public void setAutoBuildEnable(boolean enabled) throws CoreException {
			putString(BUILD_AUTO_ENABLED, new Boolean(enabled).toString());
		}

		public boolean isAutoBuildEnable() {
			return getBoolean(BUILD_AUTO_ENABLED);
		}

		public void setIncrementalBuildEnable(boolean enabled) throws CoreException {
			putString(BUILD_INCREMENTAL_ENABLED, new Boolean(enabled).toString());
		}

		public boolean isIncrementalBuildEnabled() {
			return getBoolean(BUILD_INCREMENTAL_ENABLED);
		}

		public void setFullBuildEnable(boolean enabled) throws CoreException {
			putString(BUILD_FULL_ENABLED, new Boolean(enabled).toString());
		}

		public boolean isFullBuildEnabled() {
			return getBoolean(BUILD_FULL_ENABLED);
		}

		public void setCleanBuildEnable(boolean enabled) throws CoreException {
			putString(BUILD_CLEAN_ENABLED, new Boolean(enabled).toString());
		}

		public boolean isCleanBuildEnabled() {
			return getBoolean(BUILD_CLEAN_ENABLED);
		}

		public String[] getErrorParsers() {
			String parsers = getString(ErrorParserManager.PREF_ERROR_PARSER);
			if (parsers != null && parsers.length() > 0) {
				StringTokenizer tok = new StringTokenizer(parsers, ";"); //$NON-NLS-1$
				List list = new ArrayList(tok.countTokens());
				while (tok.hasMoreElements()) {
					list.add(tok.nextToken());
				}
				return (String[])list.toArray(new String[list.size()]);
			}
			return new String[0];
		}

		public void setErrorParsers(String[] parsers) throws CoreException {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < parsers.length; i++) {
				buf.append(parsers[i]).append(';');
			}
			putString(ErrorParserManager.PREF_ERROR_PARSER, buf.toString());
		}

		public Map getEnvironment() {
			return decodeMap(getString(ENVIRONMENT));
		}

		public void setEnvironment(Map env) throws CoreException {
			putString(ENVIRONMENT, encodeMap(env));
		}

		public boolean appendEnvironment() {
			if (getString(BUILD_APPEND_ENVIRONMENT) != null) {
				return getBoolean(BUILD_APPEND_ENVIRONMENT);
			}
			return true;
		}

		public void setAppendEnvironment(boolean append) throws CoreException {
			putString(BUILD_APPEND_ENVIRONMENT, new Boolean(append).toString());
		}

		public boolean getBoolean(String property) {
			return Boolean.valueOf(getString(property)).booleanValue();
		}

		protected Map decodeMap(String value) {
			Map map = new HashMap();
			if (value != null) {
				StringBuffer envStr = new StringBuffer(value);
				String escapeChars = "|\\"; //$NON-NLS-1$
				char escapeChar = '\\';
				try {
					while (envStr.length() > 0) {
						int ndx = 0;
						while (ndx < envStr.length()) {
							if (escapeChars.indexOf(envStr.charAt(ndx)) != -1) {
								if (envStr.charAt(ndx - 1) == escapeChar) { 
									// escaped '|' - remove '\' and continue on.
									envStr.deleteCharAt(ndx - 1);
									if (ndx == envStr.length()) {
										break;
									}
								}
								if (envStr.charAt(ndx) == '|')
									break;
							}
							ndx++;
						}
						StringBuffer line = new StringBuffer(envStr.substring(0, ndx));
						int lndx = 0;
						while (lndx < line.length()) {
							if (line.charAt(lndx) == '=') {
								if (line.charAt(lndx - 1) == escapeChar) {
									// escaped '=' - remove '\' and continue on.
									line.deleteCharAt(lndx - 1);
								} else {
									break;
								}
							}
							lndx++;
						}
						map.put(line.substring(0, lndx), line.substring(lndx + 1));
						envStr.delete(0, ndx + 1);
					}
				} catch (StringIndexOutOfBoundsException e) {
				}
			}
			return map;
		}

		protected String encodeMap(Map values) {
			StringBuffer str = new StringBuffer();
			Iterator entries = values.entrySet().iterator();
			while (entries.hasNext()) {
				Entry entry = (Entry)entries.next();
				str.append(escapeChars((String)entry.getKey(), "=|\\", '\\')); //$NON-NLS-1$
				str.append("="); //$NON-NLS-1$
				str.append(escapeChars((String)entry.getValue(), "|\\", '\\')); //$NON-NLS-1$
				str.append("|"); //$NON-NLS-1$
			}
			return str.toString();
		}

		protected String escapeChars(String string, String escapeChars, char escapeChar) {
			StringBuffer str = new StringBuffer(string);
			for (int i = 0; i < str.length(); i++) {
				if (escapeChars.indexOf(str.charAt(i)) != -1) {
					str.insert(i, escapeChar);
					i++;
				}
			}
			return str.toString();
		}

		protected abstract void putString(String name, String value) throws CoreException;
		protected abstract String getString(String property);
 	}

	private static class BuildInfoPreference extends AbstractBuildInfo {

		private Preferences prefs;
		private String builderID;
		private boolean useDefaults;

		BuildInfoPreference(Preferences prefs, String builderID, boolean useDefaults) {
			this.prefs = prefs;
			this.builderID = builderID;
			this.useDefaults = useDefaults;
		}

		protected void putString(String name, String value) {
			if (useDefaults) {
				if (value != null) {
					prefs.setDefault(name, value);
				}
			} else {
				if (value == null) {
					prefs.setValue(name, prefs.getDefaultString(name));
					return;
				}
				prefs.setValue(name, value);
			}
		}

		protected String getString(String property) {
			if (!prefs.contains(property)) {
				return null;
			}
			if (useDefaults) {
				return prefs.getDefaultString(property);
			}
			return prefs.getString(property);
		}

		protected String getBuilderID() {
			return builderID;
		}
	}

	private static class BuildInfoProject extends AbstractBuildInfo {

		private IProject project;
		private String builderID;
		private Map args;

		BuildInfoProject(IProject project, String builderID) throws CoreException {
			this.project = project;
			this.builderID = builderID;
			ICommand builder;
			builder = MakeProjectNature.getBuildSpec(project.getDescription(), builderID);
			if (builder == null) {
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("BuildInfoFactory.Missing_Builder") + builderID, null)); //$NON-NLS-1$
			}
			args = builder.getArguments();
		}

		protected void putString(String name, String value) throws CoreException {
			String curValue = (String)args.get(name);
			if (curValue != null && curValue.equals(value)) {
				return;
			}
			if (value == null) {
				args.remove(name);
			} else {
				args.put(name, value);
			}
			IProjectDescription description = project.getDescription();
			ICommand builder = MakeProjectNature.getBuildSpec(description, builderID);
			builder.setArguments(args);
			builder.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, isAutoBuildEnable());
			builder.setBuilding(IncrementalProjectBuilder.FULL_BUILD, isFullBuildEnabled());
			builder.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, isIncrementalBuildEnabled());
			builder.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, isCleanBuildEnabled());
			MakeProjectNature.setBuildSpec(description, builder);
			project.setDescription(description, null);
		}

		protected String getString(String name) {
			return (String)args.get(name);
		}

		protected String getBuilderID() {
			return builderID;
		}
	}

	private static class BuildInfoMap extends AbstractBuildInfo {

		private Map args;
		private String builderID;

		BuildInfoMap(Map args, String builderID) {
			this.args = args;
			this.builderID = builderID;
		}

		protected void putString(String name, String value) {
			if (value == null) {
				args.remove(name);
			} else {
				args.put(name, value);
			}
		}

		protected String getString(String name) {
			return (String)args.get(name);
		}

		protected String getBuilderID() {
			return builderID;
		}
	}

	public static IMakeBuilderInfo create(Preferences prefs, String builderID, boolean useDefaults) {
		return new BuildInfoFactory.BuildInfoPreference(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo create(IProject project, String builderID) throws CoreException {
		return new BuildInfoFactory.BuildInfoProject(project, builderID);
	}

	public static IMakeBuilderInfo create(Map args, String builderID) {
		return new BuildInfoFactory.BuildInfoMap(args, builderID);
	}
}
