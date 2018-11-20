/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

/**
 * Creates a ScannerConfigBuilderInfo variant
 * @author vhirsl
 */
public class ScannerConfigInfoFactory {
	private static final String PREFIX = MakeCorePlugin.getUniqueIdentifier();

	static final String BUILD_SCANNER_CONFIG_ENABLED = PREFIX + ".ScannerConfigDiscoveryEnabled"; //$NON-NLS-1$
	static final String MAKE_BUILDER_PARSER_ENABLED = PREFIX + ".makeBuilderParserEnabled"; //$NON-NLS-1$
	static final String MAKE_BUILDER_PARSER_ID = PREFIX + ".makeBuilderParserId"; //$NON-NLS-1$
	static final String ESI_PROVIDER_COMMAND_ENABLED = PREFIX + ".esiProviderCommandEnabled"; //$NON-NLS-1$
	static final String USE_DEFAULT_ESI_PROVIDER_CMD = PREFIX + ".useDefaultESIProviderCmd"; //$NON-NLS-1$
	static final String ESI_PROVIDER_COMMAND = PREFIX + ".esiProviderCommand"; //$NON-NLS-1$
	static final String ESI_PROVIDER_ARGUMENTS = PREFIX + ".esiProviderArguments"; //$NON-NLS-1$
	static final String ESI_PROVIDER_PARSER_ID = PREFIX + ".esiProviderParserId"; //$NON-NLS-1$
	static final String SI_PROBLEM_GENERATION_ENABLED = PREFIX + ".siProblemGenerationEnabled"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	static final String SI_PROFILE_ID = PREFIX + ".siProfileId"; //$NON-NLS-1$

	/**
	 *
	 * @author vhirsl
	 */
	private abstract static class Store implements IScannerConfigBuilderInfo {
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#isAutoDiscoveryEnabled()
		 */
		@Override
		public boolean isAutoDiscoveryEnabled() {
			return getBoolean(BUILD_SCANNER_CONFIG_ENABLED);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setAutoDiscoveryEnabled(boolean)
		 */
		@Override
		public void setAutoDiscoveryEnabled(boolean enabled) throws CoreException {
			putString(BUILD_SCANNER_CONFIG_ENABLED, Boolean.toString(enabled));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#isMakeBuilderConsoleParserEnabled()
		 */
		@Override
		public boolean isMakeBuilderConsoleParserEnabled() {
			if (getString(MAKE_BUILDER_PARSER_ENABLED) == null
					|| getString(MAKE_BUILDER_PARSER_ENABLED).length() == 0) { // if no property then default to true
				return true;
			}
			return getBoolean(MAKE_BUILDER_PARSER_ENABLED);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setMakeBuilderConsoleParserEnabled(boolean)
		 */
		@Override
		public void setMakeBuilderConsoleParserEnabled(boolean enabled) throws CoreException {
			putString(MAKE_BUILDER_PARSER_ENABLED, Boolean.toString(enabled));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#getMakeBuilderConsoleParserId()
		 */
		@Override
		public String getMakeBuilderConsoleParserId() {
			String parserId = getString(MAKE_BUILDER_PARSER_ID);
			if (parserId == null || parserId.length() == 0) {
				String[] parserIds = MakeCorePlugin.getDefault().getScannerInfoConsoleParserIds("makeBuilder"); //$NON-NLS-1$
				// the default is the first one in the registry
				parserId = parserIds[0];
			}
			return parserId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setMakeBuilderConsoleParserId(java.lang.String)
		 */
		@Override
		public void setMakeBuilderConsoleParserId(String parserId) throws CoreException {
			putString(MAKE_BUILDER_PARSER_ID, parserId);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#isESIProviderCommandEnabled()
		 */
		@Override
		public boolean isESIProviderCommandEnabled() {
			if (getString(ESI_PROVIDER_COMMAND_ENABLED) == null
					|| getString(ESI_PROVIDER_COMMAND_ENABLED).length() == 0) { // if no property then default to true
				return true;
			}
			return getBoolean(ESI_PROVIDER_COMMAND_ENABLED);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setESIProviderCommandEnabled(boolean)
		 */
		@Override
		public void setESIProviderCommandEnabled(boolean enabled) throws CoreException {
			putString(ESI_PROVIDER_COMMAND_ENABLED, Boolean.toString(enabled));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#isDefaultESIProviderCmd()
		 */
		@Override
		public boolean isDefaultESIProviderCmd() {
			if (getString(USE_DEFAULT_ESI_PROVIDER_CMD) == null
					|| getString(USE_DEFAULT_ESI_PROVIDER_CMD).length() == 0) { // if no property then default to true
				return true;
			}
			return getBoolean(USE_DEFAULT_ESI_PROVIDER_CMD);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setUseDefaultESIProviderCmd(boolean)
		 */
		@Override
		public void setUseDefaultESIProviderCmd(boolean on) throws CoreException {
			putString(USE_DEFAULT_ESI_PROVIDER_CMD, Boolean.toString(on));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#getESIProviderCommand()
		 */
		@Override
		public IPath getESIProviderCommand() {
			if (isDefaultESIProviderCmd()) {
				String command = getESIProviderParameter("defaultCommand"); //$NON-NLS-1$
				if (command == null) {
					return new Path("gcc"); //$NON-NLS-1$
				}
				return new Path(command);
			}
			return new Path(getString(ESI_PROVIDER_COMMAND));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setESIProviderCommand(org.eclipse.core.runtime.IPath)
		 */
		@Override
		public void setESIProviderCommand(IPath command) throws CoreException {
			putString(ESI_PROVIDER_COMMAND, command.toString());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#getESIProviderArguments()
		 */
		@Override
		public String getESIProviderArguments() {
			if (isDefaultESIProviderCmd()) {
				String attributes = getESIProviderParameter("defaultAttributes"); //$NON-NLS-1$
				if (attributes == null) {
					attributes = "-E -P -v -dD ${plugin_state_location}/${specs_file}"; //$NON-NLS-1$
				}
				return attributes;
			}
			return getString(ESI_PROVIDER_ARGUMENTS);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setESIProviderArguments(java.lang.String)
		 */
		@Override
		public void setESIProviderArguments(String args) throws CoreException {
			putString(ESI_PROVIDER_ARGUMENTS, args);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#getESIProviderConsoleParserId()
		 */
		@Override
		public String getESIProviderConsoleParserId() {
			String parserId = getString(ESI_PROVIDER_PARSER_ID);
			if (parserId == null || parserId.length() == 0) {
				String[] parserIds = MakeCorePlugin.getDefault()
						.getScannerInfoConsoleParserIds("externalScannerInfoProvider"); //$NON-NLS-1$
				// the default is the first one in the registry
				parserId = parserIds[0];
			}
			return parserId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setESIProviderConsoleParserId(java.lang.String)
		 */
		@Override
		public void setESIProviderConsoleParserId(String parserId) throws CoreException {
			putString(ESI_PROVIDER_PARSER_ID, parserId);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#isSIProblemGenerationEnabled()
		 */
		@Override
		public boolean isSIProblemGenerationEnabled() {
			if (getString(SI_PROBLEM_GENERATION_ENABLED) == null
					|| getString(SI_PROBLEM_GENERATION_ENABLED).length() == 0) { // if no property then default to true
				return true;
			}
			return getBoolean(SI_PROBLEM_GENERATION_ENABLED);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo#setSIProblemGenerationEnabled(boolean)
		 */
		@Override
		public void setSIProblemGenerationEnabled(boolean enabled) throws CoreException {
			putString(SI_PROBLEM_GENERATION_ENABLED, Boolean.toString(enabled));
		}

		protected boolean getBoolean(String property) {
			return Boolean.valueOf(getString(property)).booleanValue();
		}

		protected abstract String getBuilderID();

		protected abstract String getString(String property);

		protected abstract void putString(String name, String value) throws CoreException;

		protected String getESIProviderParameter(String name) {
			IExtension extension = Platform.getExtensionRegistry().getExtension(MakeCorePlugin.getUniqueIdentifier(),
					MakeCorePlugin.EXTERNAL_SI_PROVIDER_SIMPLE_ID,
					// TODO VMIR make this configurable
					MakeCorePlugin.DEFAULT_EXTERNAL_SI_PROVIDER_ID);
			if (extension == null)
				return null;
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length == 0)
				return null;
			IConfigurationElement[] runElement = configs[0].getChildren("run"); //$NON-NLS-1$
			IConfigurationElement[] paramElement = runElement[0].getChildren("parameter"); //$NON-NLS-1$
			for (int i = 0; i < paramElement.length; i++) {
				if (paramElement[i].getAttribute("name").equals(name)) { //$NON-NLS-1$
					return paramElement[i].getAttribute("value"); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	private static class Preference extends Store {
		private Preferences prefs;
		private String builderID;
		private boolean useDefaults;

		Preference(Preferences prefs, String builderID, boolean useDefaults) {
			this.prefs = prefs;
			this.builderID = builderID;
			this.useDefaults = useDefaults;
		}

		@Override
		protected void putString(String name, String value) {
			if (useDefaults) {
				prefs.setDefault(name, value);
			} else {
				prefs.setValue(name, value);
			}
		}

		@Override
		protected String getString(String property) {
			if (useDefaults) {
				return prefs.getDefaultString(property);
			}
			return prefs.getString(property);
		}

		@Override
		protected String getBuilderID() {
			return builderID;
		}
	}

	private static class BuildProperty extends Store {
		private IProject project;
		private String builderID;
		private Map<String, String> args;

		BuildProperty(IProject project, String builderID) throws CoreException {
			this.project = project;
			this.builderID = builderID;
			ICommand builder = ScannerConfigNature.getBuildSpec(project.getDescription(), builderID);
			if (builder == null) {
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("ScannerConfigInfoFactory.Missing_Builder")//$NON-NLS-1$
								+ builderID,
						null));
			}
			Map<String, String> bArgs = builder.getArguments();
			args = bArgs;
		}

		@Override
		protected void putString(String name, String value) throws CoreException {
			String curValue = args.get(name);
			if (curValue != null && curValue.equals(value)) {
				return;
			}
			IProjectDescription description = project.getDescription();
			ICommand builder = ScannerConfigNature.getBuildSpec(description, builderID);
			args.put(name, value);
			builder.setArguments(args);
			MakeProjectNature.setBuildSpec(description, builder);
			project.setDescription(description, null);
		}

		@Override
		protected String getString(String name) {
			String value = args.get(name);
			return value == null ? "" : value; //$NON-NLS-1$
		}

		@Override
		protected String getBuilderID() {
			return builderID;
		}
	}

	private static class BuildArguments extends Store {
		private Map<String, String> args;
		private String builderID;

		BuildArguments(Map<String, String> args, String builderID) {
			this.args = args;
			this.builderID = builderID;
		}

		@Override
		protected void putString(String name, String value) {
			args.put(name, value);
		}

		@Override
		protected String getString(String name) {
			return args.get(name);
		}

		@Override
		protected String getBuilderID() {
			return builderID;
		}
	}

	public static IScannerConfigBuilderInfo create(Preferences prefs, String builderID, boolean useDefaults) {
		return new ScannerConfigInfoFactory.Preference(prefs, builderID, useDefaults);
	}

	public static IScannerConfigBuilderInfo create(IProject project, String builderID) throws CoreException {
		return new ScannerConfigInfoFactory.BuildProperty(project, builderID);
	}

	public static IScannerConfigBuilderInfo create(Map<String, String> args, String builderID) {
		return new ScannerConfigInfoFactory.BuildArguments(args, builderID);
	}
}
