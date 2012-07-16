/*******************************************************************************
 *  Copyright (c) 2009 Freescale Semiconductors and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Freescale Semiconductor. - initial API and implementation
 *     Jason Litton (Sage Electronic Engineering, LLC) - Added Dynamic Debug Tracing (Bug 385076)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.dsf.internal.ui.DsfUiDebugOptions;

/**
 * Constants and utility functions used to trace VMViewerUpdate results. As
 * VMViewerUpdate is an external class, we avoid polluting that API by housing
 * these trace facilities in an internal package.
 * 
 * @deprecated use org.eclipse.cdt.dsf.internal.ui.DsfUiDebugOptions instead
 * to take advantage of dynamic tracing
 */
@Deprecated
public final class VMViewerUpdateTracing {

	/**
	 * The value of the trace option "debug/vmUpdates/regex", which is a regular
	 * expression used to filter VMViewerUpdate traces.
	 * 
	 * @deprecated
	 */
    @Deprecated
	public final static String DEBUG_VMUPDATE_REGEX = DsfUiDebugOptions.DEBUG_VM_UPDATES_REGEX;

	/**
	 * Has the "debug/vmUpdates/properties" tracing option been turned on? Requires
	 * "debug/vmUpdates" to also be turned on.
	 * 
	 * @deprecated
	 */
    @Deprecated
	public static final boolean DEBUG_VMUPDATES = DsfUiDebugOptions.DEBUG  && DsfUiDebugOptions.DEBUG_VM_UPDATES;

	/**
	 * Looks at the optional filter (regular expression) set in the tracing
	 * options for VMViewerUpdates and determines if this class passes the
	 * filter (should be traced). If a filter is not set, then we trace all
	 * classes. Note that for optimization reasons, we expect the caller to
	 * first check that DEBUG_VMUPDATES is true before invoking us; we do not
	 * check it here (other than to assert it).
	 * 
	 * @return true if this class's activity should be traced
	 * 
	 * @deprecated
	 */
    @Deprecated
    public static boolean matchesFilterRegex(Class<?> clazz) {
    	assert DsfUiDebugOptions.DEBUG && DsfUiDebugOptions.DEBUG_VM_UPDATES;
    	if (DsfUiDebugOptions.DEBUG_VM_UPDATES_REGEX == null || DsfUiDebugOptions.DEBUG_VM_UPDATES_REGEX.length() == 0) {
    		return true;
    	}
    	try {
	    	Pattern regex = Pattern.compile(DsfUiDebugOptions.DEBUG_VM_UPDATES_REGEX);
	    	Matcher matcher = regex.matcher(clazz.toString());
	    	return matcher.find();
    	}
    	catch (PatternSyntaxException exc) {
    		return false;
    	}
    }
}

