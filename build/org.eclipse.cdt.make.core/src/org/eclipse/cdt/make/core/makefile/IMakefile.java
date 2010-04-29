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
package org.eclipse.cdt.make.core.makefile;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;


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
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMakefile extends IParent {

	/**
	 * ITargetRule | IInferenceRule | ISpecialRule
	 */
	IRule[] getRules();

	/**
	 * @return the IRule for target.
	 */
	IRule[] getRules(String target);

	/**
	 * @return IInferenceRule
	 * 
	 */
	IInferenceRule[] getInferenceRules();

	/**
	 * @return the IInferenceRules for target.
	 */
	IInferenceRule[] getInferenceRules(String target);

	/**
	 * @return ITargetRule
	 */
	ITargetRule[] getTargetRules();

	/**
	 * @return the ITargetRules for name.
	 */
	ITargetRule[] getTargetRules(String target);

	/**
	 * @return the IMacroDefinitions.
	 */
	IMacroDefinition[] getMacroDefinitions();

	/**
	 * @return the IMacroDefinitions for name.
	 */
	IMacroDefinition[] getMacroDefinitions(String name);

	/**
	 * @return all the built-in directives.
	 */
	IDirective[] getBuiltins();

	/**
	 * @return all the built-in MacroDefintions
	 */
	IMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * @return the built-in macro definition for name.
	 */
	IMacroDefinition[] getBuiltinMacroDefinitions(String name);

	/**
	 * @return line after expanding any macros.
	 */
	String expandString(String line);

	/**
	 * @return line after expanding any macros.
	 * 
	 * @param line - line to expand
	 * @param recursive -  if true recursively expand.
	 */
	String expandString(String line, boolean recursive);

	/**
	 * @return  the makefile Reader provider used to create this makefile or <code>null</code>
	 */
	IMakefileReaderProvider getMakefileReaderProvider();
	
	/**
	 * Clear all statements and (re)parse the Makefile
	 */
	void parse(String filePath, Reader makefile) throws IOException;
	
	/**
	 * Clear all statements and (re)parse the Makefile
	 */
	void parse(URI fileURI, Reader makefile) throws IOException;
	
	/**
	 * Clear the all statements and (re)parse the Makefile
	 * using the given makefile Reader provider
	 * 
	 * @param makefileReaderProvider provider, or <code>null</code> to use a FileReader
	 */
	void parse(URI fileURI, IMakefileReaderProvider makefileReaderProvider) throws IOException;
	

	/**
	 * @return the <code>URI</code> of this makefile
	 */
	URI getFileURI();
}
