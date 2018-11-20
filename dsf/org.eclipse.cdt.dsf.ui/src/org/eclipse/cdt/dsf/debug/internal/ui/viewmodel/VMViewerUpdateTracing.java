/*******************************************************************************
 *  Copyright (c) 2009, 2016 Freescale Semiconductors and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Freescale Semiconductor. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Constants and utility functions used to trace VMViewerUpdate results. As
 * VMViewerUpdate is an external class, we avoid polluting that API by housing
 * these trace facilities in an internal package.
 */
public final class VMViewerUpdateTracing {

	/**
	 * The value of the trace option "debug/vmUpdates/regex", which is a regular
	 * expression used to filter VMViewerUpdate traces.
	 */
	public final static String DEBUG_VMUPDATE_REGEX = Platform
			.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/updates/regex"); //$NON-NLS-1$

	/**
	 * Has the "debug/vmUpdates/properties" tracing option been turned on? Requires
	 * "debug/vmUpdates" to also be turned on.
	 */
	public static final boolean DEBUG_VMUPDATES = DsfUIPlugin.DEBUG
			&& Boolean.parseBoolean(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/updates")); //$NON-NLS-1$

	/**
	 * Looks at the optional filter (regular expression) set in the tracing
	 * options for VMViewerUpdates and determines if this class passes the
	 * filter (should be traced). If a filter is not set, then we trace all
	 * classes. Note that for optimization reasons, we expect the caller to
	 * first check that DEBUG_VMUPDATES is true before invoking us; we do not
	 * check it here (other than to assert it).
	 *
	 * @return true if this class's activity should be traced
	 */
	public static boolean matchesFilterRegex(Class<?> clazz) {
		assert DEBUG_VMUPDATES;
		if (DEBUG_VMUPDATE_REGEX == null || DEBUG_VMUPDATE_REGEX.length() == 0) {
			return true;
		}
		try {
			Pattern regex = Pattern.compile(DEBUG_VMUPDATE_REGEX);
			Matcher matcher = regex.matcher(clazz.toString());
			return matcher.find();
		} catch (PatternSyntaxException exc) {
			return false;
		}
	}
}
