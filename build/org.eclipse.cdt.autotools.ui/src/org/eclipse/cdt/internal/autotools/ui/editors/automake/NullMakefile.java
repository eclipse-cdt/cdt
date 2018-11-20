/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.Reader;
import java.net.URI;

import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.core.makefile.IBuiltinFunction;
import org.eclipse.cdt.make.core.makefile.IDirective;

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
		return "";
	}

	@Override
	public void parse(String name, Reader makefile) {
	}

	@Override
	public void parse(URI fileURI, Reader reader) {
	}

	protected void parse(URI fileURI, MakefileReader reader) {
	}

	@Override
	public IBuiltinFunction[] getBuiltinFunctions() {
		return new IBuiltinFunction[0];
	}

	@Override
	public IAutomaticVariable[] getAutomaticVariables() {
		return new IAutomaticVariable[0];
	}
}
