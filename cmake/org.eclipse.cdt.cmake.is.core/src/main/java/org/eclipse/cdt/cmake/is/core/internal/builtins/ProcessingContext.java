/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsOutputProcessor;
import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.Platform;

/**
 * Default implementation of IProcessingContext.
 *
 * @author Martin Weber
 */
class ProcessingContext implements IBuiltinsOutputProcessor.IProcessingContext, IBuiltinsOutputProcessor.IResult {
	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/CECC/builtins/entries"));

	private final List<ICLanguageSettingEntry> entries = Collections
			.synchronizedList(new ArrayList<ICLanguageSettingEntry>());

	public ProcessingContext() {
	}

	@Override
	public boolean addSettingEntry(ICLanguageSettingEntry entry) {
		if (entry != null) {
			if (DEBUG)
				System.out.printf("Added builtin entry: %s%n", entry);
			entries.add(entry);
			return true;
		}
		return false;
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries() {
		return entries;
	}

}
