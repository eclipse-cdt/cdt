/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is a data storage class to be used by the GDBErrorHandler
 */
public class GDBError {
	private Pattern fKeyword;
	private int fSeverity;
	private boolean IsAll;

	public GDBError(String keyword, int severity) {
		this.fKeyword = Pattern.compile(keyword);
		this.fSeverity = severity;
		IsAll = keyword.equals(Messages.GDBErrorHandler_All_Regex);
	}

	public int getSeverity() {
		return fSeverity;
	}

	public boolean matches(String message) {
		Matcher matcher = fKeyword.matcher(message);
		return matcher.find();
	}

	public boolean isAll() {
		return IsAll;
	}
}
