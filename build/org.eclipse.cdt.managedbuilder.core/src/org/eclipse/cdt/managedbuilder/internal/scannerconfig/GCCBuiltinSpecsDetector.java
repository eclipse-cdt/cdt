/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.core.runtime.CoreException;

/**
 * Class to detect built-in compiler settings. Note that currently this class is hardwired
 * to GCC toolchain {@code cdt.managedbuild.toolchain.gnu.base}.
 *
 */
public class GCCBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	// must match the toolchain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String GCC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.gnu.base";  //$NON-NLS-1$
	
	private enum State {NONE, EXPECTING_LOCAL_INCLUDE, EXPECTING_SYSTEM_INCLUDE, EXPECTING_FRAMEWORKS}
	State state = State.NONE;
	
	@SuppressWarnings("nls")
	static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("#include \"(\\S.*)\"", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL),
			new IncludePathOptionParser("#include <(\\S.*)>", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new IncludePathOptionParser("#framework <(\\S.*)>", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.FRAMEWORKS_MAC),
			new MacroOptionParser("#define (\\S*\\(.*?\\)) *(.*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("#define (\\S*) *(.*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
	};

	@Override
	protected String getToolchainId() {
		return GCC_TOOLCHAIN_ID;
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	private List<String> makeList(final String line) {
		return new ArrayList<String>() {{ add(line); }};
	}
	
	@Override
	protected List<String> parseForOptions(String line) {
		line = line.trim();

		// contribution of -dD option
		if (line.startsWith("#define")) {
			return makeList(line);
		}

		/**

Framework search starts here:
 /System/Library/Frameworks
 /Library/Frameworks
End of framework search list.

		 */

		// contribution of includes
		if (line.equals("#include \"...\" search starts here:")) {
			state = State.EXPECTING_LOCAL_INCLUDE;
		} else if (line.equals("#include <...> search starts here:")) {
			state = State.EXPECTING_SYSTEM_INCLUDE;
		} else if (line.startsWith("End of search list.")) {
			state = State.NONE;
		} else if (line.equals("Framework search starts here:")) {
			state = State.EXPECTING_FRAMEWORKS;
		} else if (line.startsWith("End of framework search list.")) {
			state = State.NONE;
		} else if (state==State.EXPECTING_LOCAL_INCLUDE) {
			// making that up for the parser to figure out
			line = "#include \""+line+"\"";
			return makeList(line);
		} else {
			String frameworkIndicator = "(framework directory)";
			if (state==State.EXPECTING_SYSTEM_INCLUDE) {
				// making that up for the parser to figure out
				if (line.contains(frameworkIndicator)) {
					line = "#framework <"+line.replace(frameworkIndicator, "").trim()+">";
				} else {
					line = "#include <"+line+">";
				}
				return makeList(line);
			} else if (state==State.EXPECTING_FRAMEWORKS) {
				// making that up for the parser to figure out
				line = "#framework <"+line.replace(frameworkIndicator, "").trim()+">";
				return makeList(line);
			}
		}

		return null;
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		super.startup(cfgDescription);
		
		state = State.NONE;
	}
	
	@Override
	public void shutdown() {
		state = State.NONE;

		super.shutdown();
	}

	@Override
	public GCCBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetector) super.cloneShallow();
	}

	@Override
	public GCCBuiltinSpecsDetector clone() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetector) super.clone();
	}

	
}
