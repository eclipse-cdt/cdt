/*******************************************************************************
 * Copyright (c) 2007, 2020 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Alexander Fedorov (ArSysOp) - Bug 561992
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.Calendar;

import org.eclipse.cdt.core.parser.IToken;

public final class TimeMacro extends DynamicMacro {
	public TimeMacro(char[] name) {
		super(name);
	}

	@Override
	public Token execute(MacroExpander expander) {
		return new TokenWithImage(IToken.tSTRING, null, 0, 0, createDate());
	}

	private char[] createDate() {
		StringBuilder buffer = new StringBuilder("\""); //$NON-NLS-1$
		Calendar cal = Calendar.getInstance();
		append(buffer, cal.get(Calendar.HOUR_OF_DAY));
		buffer.append(":"); //$NON-NLS-1$
		append(buffer, cal.get(Calendar.MINUTE));
		buffer.append(":"); //$NON-NLS-1$
		append(buffer, cal.get(Calendar.SECOND));
		buffer.append("\""); //$NON-NLS-1$
		return buffer.toString().toCharArray();
	}

	@Override
	public char[] getExpansionImage() {
		return createDate();
	}
}
