/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools;

import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.builder.ACTool;

/**
 * Represents a generic GNU tool.
 */
public class CGnuTool extends ACTool {

	private final static String TOOL_TYPE_PREFIX = "org.eclipse.cdt.core.builder.";
	private final static String TOOL_ID_PREFIX = "org.eclipse.cdt.gnu.tools.";

	CGnuTool(String id, String exeName) {
		super(TOOL_TYPE_PREFIX + id, TOOL_ID_PREFIX + id, exeName);
	};
	
	/**
	 * @see org.eclipse.cdt.core.builder.model.ICTool#getErrorParser()
	 */
	public IErrorParser getErrorParser() {
		// TODO: implementation
		return null;
	}

}
