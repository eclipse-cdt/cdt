/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

/**
 * An interface representing setting entries.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * Any class implementing this interface should be immutable so the setting
 * entries could be safely pooled by {@link CDataUtil#getPooledEntry(ICSettingEntry)}.
 */
public interface ICSettingEntry {
	/**
	 * A constant known to be zero (0), used to indicate that no flags are set.
	 * @since 5.6
	 */
	int NONE = 0;

	/**
	 * Flag {@code BUILTIN} indicates settings built in a tool (compiler) itself.
	 * That kind of settings are not passed as options to a compiler but indexer
	 * or other clients might need them.
	 */
	int BUILTIN = 1;

	/**
	 * Flag {@code READONLY} means that the entry is not intended to be overwritten by user.
	 */
	int READONLY = 1 << 1;

	/**
	 * Flag {@code LOCAL} is used during creation of {@link IIncludeEntry}
	 * to indicate that an include path is not a system path.
	 * "System" path is denoted by angle brackets as in #include <x.h>
	 * "Local" path is denoted by quotes as in #include "x.h"
	 */
	int LOCAL = 1 << 2;

	/**
	 * Flag {@code VALUE_WORKSPACE_PATH} is used to indicate that the entry
	 * is a resource managed by eclipse in the workspace. It does not always mean
	 * that the path is rooted in the workspace root. In some cases it may be
	 * a project path.
	 */
	int VALUE_WORKSPACE_PATH = 1 << 3;

	/**
	 * Flag {@code RESOLVED} means that any build or other variables (for example ${ProjDirPath})
	 * have been expanded to their values.
	 */
	int RESOLVED = 1 << 4;

	/**
	 * Flag {@code UNDEFINED} indicates that the entry should not be defined.
	 * It's main purpose to provide the means to negate entries defined elsewhere.
	 *
	 * @since 5.4
	 */
	int UNDEFINED = 1 << 5;

	/**
	 * Flag {@code FRAMEWORKS_MAC} applies for path entries. Such a path entry will be treated
	 * in a special way to imitate resolving paths by Apple's version of gcc, see bug 69529.
	 *
	 * @since 5.4
	 */
	int FRAMEWORKS_MAC = 1 << 6;

	/**
	 * Flag {@code UNDEFINED} indicates that the entry is "Exported" to referencing projects.
	 * It will be passed to the projects configurations referencing the configuration the entry
	 * belongs to.
	 *
	 * @since 5.6
	 */
	int EXPORTED = 1 << 7;

	int INCLUDE_PATH = 1;
	int INCLUDE_FILE = 1 << 1;
	int MACRO = 1 << 2;
	int MACRO_FILE = 1 << 3;
	int LIBRARY_PATH = 1 << 4;
	int LIBRARY_FILE = 1 << 5;
	int OUTPUT_PATH = 1 << 6;
	int SOURCE_PATH = 1 << 7;
	int ALL = ~0;

	boolean isReadOnly();

	int getKind();

	String getName();

	String getValue();

	boolean isBuiltIn();

	boolean isResolved();

	boolean equalsByName(ICSettingEntry entry);

	boolean equalsByContents(ICSettingEntry entry);

	int getFlags();
}
