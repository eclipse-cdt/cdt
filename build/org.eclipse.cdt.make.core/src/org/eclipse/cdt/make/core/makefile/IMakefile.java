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
package org.eclipse.cdt.make.core.makefile;

import java.io.IOException;
import java.io.Reader;


/**
 * IMakefile:
 *
 * Makefile : ( directive ) *
 * directive :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule | special_rule
 * inference_rule : target ':' [ ';' command ] <nl>
 *		 [ ( command ) * ]
 * target_rule : [ ( target ) + ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
 *               [ ( command ) *  ]
 * macro_definition : string '=' ( string )* 
 * comments : ('#' ( string ) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */
public interface IMakefile extends IParent {

	/**
	 * ITargetRule | IInferenceRule | ISpecialRule
	 * @return
	 */
	IRule[] getRules();

	/**
	 * Returns IInferenceRule
	 * @return
	 */
	IInferenceRule[] getInferenceRules();

	/**
	 * Returns ITargetRule
	 * @return
	 */
	ITargetRule[] getTargetRules();

	/**
	 * Return IMacroDefintion
	 * @return
	 */
	IMacroDefinition[] getMacroDefinitions();

	/**
	 * Return all the builtin directives.
	 * @return
	 */
	IDirective[] getBuiltins();

	/**
	 * Return all the buil-in MacroDefintions
	 * @return
	 */
	IMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * Returning after expanding any macros.
	 * @return String - expanded line
	 */
	String expandString(String line);

	/**
	 * Returning after expanding any macros.
	 * @param String - line to expand
	 * @param boolean -  if true recursively expand.
	 * @return String - expanded line
	 */
	String expandString(String line, boolean recursive);

	/**
	 * Clear the all statements and (re)parse the Makefile
	 * @param makefile
	 * @throws IOException
	 */
	void parse(Reader makefile) throws IOException;
}
