/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.posix;

import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;

public class PosixBuiltinMacroDefinitions {
	MacroDefinition[] macros =
		new MacroDefinition[] {
			new MacroDefinition("MAKE=make"),
			new MacroDefinition("AR=ar"),
			new MacroDefinition("ARFLAGS=-rv"),
			new MacroDefinition("YACC=yacc"),
			new MacroDefinition("YFLAGS="),
			new MacroDefinition("LEX=lex"),
			new MacroDefinition("LFLAGS="),
			new MacroDefinition("LDFLAGS="),
			new MacroDefinition("CC=c89"),
			new MacroDefinition("CFLAGS=-O"),
			new MacroDefinition("FC=fort77"),
			new MacroDefinition("FFLAGS=-O 1"),
			new MacroDefinition("GET=get"),
			new MacroDefinition("GFLAGS="),
			new MacroDefinition("SCCSFLAGS="),
			new MacroDefinition("SCCSGETFLAGS=-s")};

	public MacroDefinition getMacroDefinition(String name) {
		for (int i = 0; i < macros.length; i++) {
			if (name.equals(macros[i].getName())) {
				return macros[i];
			}
		}
		return null;
	}

	public MacroDefinition[] getMacroDefinitions() {
		return macros;
	}
}
