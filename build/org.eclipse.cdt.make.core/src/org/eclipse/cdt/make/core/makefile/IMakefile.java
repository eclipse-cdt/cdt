/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
	 * Returns the IRule for target.
	 * 
	 * @param target
	 * @return
	 */
	IRule[] getRules(String target);

	/**
	 * Returns IInferenceRule
	 * @return
	 */
	IInferenceRule[] getInferenceRules();

	/**
	 * Returns the IInferenceRules for target.
	 * @param target
	 * @return
	 */
	IInferenceRule[] getInferenceRules(String target);

	/**
	 * Returns ITargetRule
	 * @return
	 */
	ITargetRule[] getTargetRules();

	/**
	 * Returns the ITargetRules for name.
	 * 
	 * @param target
	 * @return
	 */
	ITargetRule[] getTargetRules(String target);

	/**
	 * Return IMacroDefinition
	 * @return
	 */
	IMacroDefinition[] getMacroDefinitions();

	/**
	 * Returns the IMacroDefinitions for name.
	 * 
	 * @param name
	 * @return
	 */
	IMacroDefinition[] getMacroDefinitions(String name);

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
	 * Returns the Builtin macro definition for name.
	 *  
	 * @param name
	 * @return
	 */
	IMacroDefinition[] getBuiltinMacroDefinitions(String name);

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
	 * 
	 * @param name
	 * @param makefile
	 * @throws IOException
	 */
	void parse(String name, Reader makefile) throws IOException;

}
