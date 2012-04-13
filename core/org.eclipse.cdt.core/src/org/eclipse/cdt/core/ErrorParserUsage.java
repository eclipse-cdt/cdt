/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicates how <code>{@link org.eclipse.cdt.core.IErrorParser}</code>s are used.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 5.4
 */
public class ErrorParserUsage {
	public static final ErrorParserUsage BUILD = new ErrorParserUsage("BUILD"); //$NON-NLS-1$
	public static final ErrorParserUsage CODAN = new ErrorParserUsage("CODAN"); //$NON-NLS-1$

	private static final Map<String, ErrorParserUsage[]> USAGE_MAPPINGS = new HashMap<String, ErrorParserUsage[]>();
	private static final ErrorParserUsage[] NO_USAGES = new ErrorParserUsage[0];

	private final String name;
	
	static {
		addUsageMapping("", BUILD); //$NON-NLS-1$
		addUsageMapping("build", BUILD); //$NON-NLS-1$
		addUsageMapping("build,codan", BUILD, CODAN); //$NON-NLS-1$
		addUsageMapping("codan", CODAN); //$NON-NLS-1$
	}
	
	private static void addUsageMapping(String description, ErrorParserUsage...usages) {
		USAGE_MAPPINGS.put(description, usages);
	}
	
	public static ErrorParserUsage[] findMatching(String usage) {
		String usageValue = (usage == null) ? "" : usage; //$NON-NLS-1$
		ErrorParserUsage[] errorParserUsages = USAGE_MAPPINGS.get(usageValue);
		return (errorParserUsages == null) ? NO_USAGES : errorParserUsages;
	}

	private ErrorParserUsage(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
