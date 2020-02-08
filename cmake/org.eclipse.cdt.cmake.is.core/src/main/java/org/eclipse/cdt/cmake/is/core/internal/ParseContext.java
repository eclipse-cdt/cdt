/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.IArglet;
import org.eclipse.cdt.cmake.is.core.IToolCommandlineParser;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.Platform;

/**
 * Default implementation of IParseContext.
 *
 * @author Martin Weber
 */
public class ParseContext implements IArglet.IParseContext, IToolCommandlineParser.IResult {
	@SuppressWarnings("nls")
	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/CECC/entries"));
	private final List<ICLanguageSettingEntry> entries = new ArrayList<>();
	private final List<String> args = new ArrayList<>();

	@Override
	public void addSettingEntry(ICLanguageSettingEntry entry) {
		if (DEBUG)
			System.out.printf("    Added entry: %s%n", entry); //$NON-NLS-1$
		entries.add(entry);
	}

	@Override
	public void addBuiltinDetectionArgument(String argument) {
		args.add(argument);
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries() {
		return entries;
	}

	@Override
	public List<String> getBuiltinDetectionArgs() {
		return args;
	}

}
