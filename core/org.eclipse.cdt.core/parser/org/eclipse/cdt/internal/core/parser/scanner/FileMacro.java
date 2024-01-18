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

import org.eclipse.cdt.core.parser.IToken;

public final class FileMacro extends DynamicMacro {
	public FileMacro(char[] name) {
		super(name);
	}

	@Override
	public Token execute(MacroExpander expander) {
		StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
		buffer.append(expander.getCurrentFilename().replace("\\", "\\\\")); //$NON-NLS-1$//$NON-NLS-2$
		buffer.append('\"');
		return new TokenWithImage(IToken.tSTRING, null, 0, 0, buffer.toString().toCharArray());
	}

	@Override
	public char[] getExpansionImage() {
		return "\"file\"".toCharArray(); //$NON-NLS-1$
	}
}
