/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Modified from PosixMakeFile to support Automake files
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.IOException;

public class Automakefile extends GNUAutomakefile {

	public Automakefile() {
		super();
	}

	protected IDirective getDirectiveContainingLine(int line) {
		int startLine, endLine;
		IDirective[] directives = this.getDirectives();
		for (int i = 0; i < directives.length; i++) {
			IDirective directive = directives[i];
			startLine = directive.getStartLine();
			endLine = directive.getEndLine();
			if (startLine <= line && endLine >= line)
				return directive;
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			String filename = "Makefile.am"; //$NON-NLS-1$
			if (args.length == 1) {
				filename = args[0];
			}
			Automakefile makefile = new Automakefile();
			makefile.parse(filename);
			IDirective[] directives = makefile.getDirectives();
			//IDirective[] directives = makefile.getBuiltins();
			for (int i = 0; i < directives.length; i++) {
				//System.out.println("Rule[" + i +"]");
				System.out.print(directives[i]);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
}
