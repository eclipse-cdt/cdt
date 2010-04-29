/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public class NullMakefile extends AbstractMakefile {

	public final static IDirective[] EMPTY_DIRECTIVES = new IDirective[0];

	public NullMakefile() {
		super(null);
	}

	@Override
	public IDirective[] getDirectives() {
		return EMPTY_DIRECTIVES;
	}

	@Override
	public IDirective[] getBuiltins() {
		return EMPTY_DIRECTIVES;
	}

	public void addDirective(IDirective directive) {
	}

	@Override
	public String toString() {
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMakefile#parse(java.io.Reader)
	 */
	public void parse(String name, Reader makefile) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMakefile#getMakefileReaderProvider()
	 */
	public IMakefileReaderProvider getMakefileReaderProvider() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMakefile#parse(java.lang.String, org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider)
	 */
	public void parse(String name,
			IMakefileReaderProvider makefileReaderProvider) throws IOException {
	}
	public void parse(URI fileURI, Reader makefile) throws IOException {
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMakefile#parse(java.net.URI, org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider)
	 */
	public void parse(URI fileURI,
			IMakefileReaderProvider makefileReaderProvider) throws IOException {
	}
}
