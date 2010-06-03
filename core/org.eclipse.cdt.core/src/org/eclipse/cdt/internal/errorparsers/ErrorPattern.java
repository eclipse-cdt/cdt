/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

/**
 * @deprecated use org.eclipse.cdt.core.errorparsers.ErrorPattern
 * this class is moved to public package
 */
@Deprecated
public class ErrorPattern extends org.eclipse.cdt.core.errorparsers.ErrorPattern {
	public ErrorPattern(String pattern, int groupFileName, int groupLineNum, int groupDesc, int groupVarName,
			int severity) {
		super(pattern, groupFileName, groupLineNum, groupDesc, groupVarName, severity);
	}

	public ErrorPattern(String pattern, int groupDesc, int severity) {
		super(pattern, groupDesc, severity);
	}

	public ErrorPattern(String pattern) {
		super(pattern);
	}
}
