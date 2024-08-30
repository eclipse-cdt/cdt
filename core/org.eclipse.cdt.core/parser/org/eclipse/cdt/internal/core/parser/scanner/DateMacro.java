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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.cdt.core.parser.IToken;

public final class DateMacro extends DynamicMacro {
	public DateMacro(char[] name) {
		super(name);
	}

	@Override
	public Token execute(MacroExpander expander) {
		return new TokenWithImage(IToken.tSTRING, null, 0, 0, createDate());
	}

	private char[] createDate() {
		char[] charArray;
		StringBuilder buffer = new StringBuilder("\""); //$NON-NLS-1$
		Calendar cal = Calendar.getInstance();
		DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
		buffer.append(dfs.getShortMonths()[cal.get(Calendar.MONTH)]);
		buffer.append(" "); //$NON-NLS-1$
		int dom = cal.get(Calendar.DAY_OF_MONTH);
		if (dom < 10) {
			buffer.append(" "); //$NON-NLS-1$
		}
		buffer.append(dom);
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(cal.get(Calendar.YEAR));
		buffer.append("\""); //$NON-NLS-1$
		charArray = buffer.toString().toCharArray();
		return charArray;
	}

	@Override
	public char[] getExpansionImage() {
		return createDate();
	}
}
