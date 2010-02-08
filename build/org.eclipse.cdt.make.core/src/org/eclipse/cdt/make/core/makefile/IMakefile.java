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
	 * Get the makefile Reader provider used to create this makefile.
	 * @return IMakefileReaderProvider or <code>null</code>
	 */
	IMakefileReaderProvider getMakefileReaderProvider();
	
	/**
	 * Clear all statements and (re)parse the Makefile
	 * 
	 * @param filePath
	 * @param makefile
	 * @throws IOException
	 */
	void parse(String filePath, Reader makefile) throws IOException;
	
	/**
	 * Clear all statements and (re)parse the Makefile
	 * 
	 * @param fileURI
	 * @param makefile
	 * @throws IOException
	 */
	void parse(URI fileURI, Reader makefile) throws IOException;
	
	/**
	 * Clear the all statements and (re)parse the Makefile
	 * using the given makefile Reader provider
	 * 
	 * @param fileURI
	 * @param makefileReaderProvider provider, or <code>null</code> to use a FileReader
	 * @throws IOException
	 */
	void parse(URI fileURI, IMakefileReaderProvider makefileReaderProvider) throws IOException;
	

	/**
	 * @return the <code>URI</code> of this makefile
	 */
	URI getFileURI();
}
