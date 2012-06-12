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

package org.eclipse.cdt.managedbuilder.xlc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.ToolchainBuiltinSpecsDetector;

/**
 * Language settings provider to detect built-in compiler settings for IBM XLC compiler.
 */
public class XlcBuiltinSpecsDetector extends ToolchainBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	// must match the toolchain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String XLC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.xlc.exe.debug";  //$NON-NLS-1$

	private static final Pattern OPTIONS_PATTERN = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int OPTION_GROUP = 0;

	/*	Sample output:

		> xlC -E -V -P -w ~/tmp/spec.C
		export XL_CONFIG=/etc/vac.cfg:xlC
		/usr/vac/exe/xlCcpp /home/me/tmp/spec.C - -qc++=/usr/vacpp/include -D_AIX -D_AIX32 -D_AIX41 -D_AIX43 -D_AIX50 -D_AIX51 -D_AIX52 -D_IBMR2 -D_POWER -E -P -w -qlanglvl=ansi -qansialias
		rm /tmp/xlcW0lt4Jia
		rm /tmp/xlcW1lt4Jib
		rm /tmp/xlcW2lt4Jic
	 */
	@SuppressWarnings("nls")
	private static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("-I\\s*([\"'])(.*)\\1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new IncludePathOptionParser("-qc\\+\\+=\\s*([^\\s\"']*)", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("-D\\s*([\"'])([^=]*)(=(.*))?\\1", "$2", "$4", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(\\\\([\"']))(.*?)\\2", "$1", "$3$4$3", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([\"'])(.*?)\\2", "$1", "$3", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)(=([^\\s\"']*))?", "$1", "$3", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
	};

	@Override
	public String getToolchainId() {
		return XLC_TOOLCHAIN_ID;
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@Override
	protected List<String> parseOptions(String line) {
		List<String> options = new ArrayList<String>();
		Matcher optionMatcher = OPTIONS_PATTERN.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(OPTION_GROUP);
			if (option!=null) {
				options.add(option);
			}
		}
		return options;
	}

	@Override
	public XlcBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		return (XlcBuiltinSpecsDetector) super.cloneShallow();
	}

	@Override
	public XlcBuiltinSpecsDetector clone() throws CloneNotSupportedException {
		return (XlcBuiltinSpecsDetector) super.clone();
	}

}
