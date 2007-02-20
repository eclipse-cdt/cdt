/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.variables.IStringVariable;

/**
 * 
 * @since 3.0
 */
public interface ICdtVariableManager{
/*	public final static int CONTEXT_FILE = 1;
	public final static int CONTEXT_OPTION = 2;
	public final static int CONTEXT_CONFIGURATION = 3;
	public final static int CONTEXT_PROJECT = 4;
	public final static int CONTEXT_WORKSPACE = 5;
	public final static int CONTEXT_INSTALLATIONS = 6;
	public final static int CONTEXT_ECLIPSEENV = 7;
	public final static int CONTEXT_TOOL = 8;
*/
	/**
	 * 
	 * Returns reference to the IBuildMacro interface representing Macro of the
	 * specified name or null if there is there is no such macro
	 * @param macroName macro name
	 * @param contextType represents the context type. Should be set to one of the the
	 * IBuildMacroProvider. CONTEXT_xxx constants 
	 * @param contextData represents the additional data needed by the Build Macro Provider
	 * and Macro Suppliers in order to obtain the macro value. The type of the context data
	 * differs depending on the context type and can be one of the following: 
	 * 1. IFileContextData interface � used to represent currently selected file context
	 *      the IFileContextData interface is defined as follows:
	 * 	    pulic interface IFileContextData{
	 *     		IFile getFile();
	 *	    	IOption getOption();
	 *  	    }
	 *     NOTE: the IFileContextData is passed that represents the current file and the option 
	 *     for that file because Macro Value Provider needs to know what option should be used 
	 *     as a context in case macro is not found for �current file� context
	 * 2.  IOptionContextData interface used to represent the currently selected option context
	 * 3.  IConfiguration � used to represent the currently selected configuration context
	 * 4.  IProject � used to represent current project context
	 * 5.  IWorkspace � used to represent current workspace context
	 * 6.  null � to represent the CDT and Eclipse installation context
	 * 7.  null � to represent process environment context
	 * @param includeParentContext specifies whether lower-precedence context macros should 
	 *     be included
	 */
	public ICdtVariable getVariable(String name, ICConfigurationDescription cfg);

	/**
	 * 
	 * @return the array of the IBuildMacro representing all available macros 
	 */
	public ICdtVariable[] getVariables(ICConfigurationDescription cfg);


	/**
	 * This method is defined to be used primarily by the UI classes and should not be used by the
	 * tool-integrator
	 * @return the array of the provider-internal suppliers for the given context
	 */
/*	public IBuildMacroSupplier[] getSuppliers(int contextType, 
					Object contextData);
*/

	/**
	 * 
	 * converts StringList value into String of the following format:
	 * "<value_1>< listDelimiter ><value_2>< listDelimiter > ... <value_n>"
	 */
	public String convertStringListToString (String value[], String listDelimiter);

	/**
	 * 
	 * resolves all macros in the string. 
	 * @param value the value to be resolved
	 * @param nonexistentMacrosValue specifies the value that inexistent macro references will be 
	 * expanded to. If null the BuildMacroException is thrown in case the string to be resolved 
	 * references inexistent macros
	 * @param listDelimiter if not null, StringList macros are expanded as
	 * �<value_1>< listDelimiter ><value_2>< listDelimiter > ... <value_n>�
	 * otherwise the BuildMacroException is thrown in case the string to be resolved references 
	 * string-list macros 
	 * @param contextType context from which the macro search should be started
	 * @param contextData context data
	 */
	public String resolveValue(String value, 
					String nonexistentMacrosValue,
					String listDelimiter, 
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * if the string contains a value that can be treated as a StringList resolves it to arrays of strings
	 * otherwise throws the BuildMacroException exception
	 * @see isStringListValue
	 */
	public String[] resolveStringListValue(String value, 
					String nonexistentMacrosValue,
					String listDelimiter,
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * resolves macros in the array of string-list values
	 * 
	 * @see isStringListValue
	 */
	public String[] resolveStringListValues(String value[], 
					String nonexistentMacrosValue,
					String listDelimiter,
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * @return true if the specified expression can be treated as StringList
	 * 1. The string value is �${<some_StringList_Macro_name>}�
	 */
	public boolean isStringListValue(String value, ICConfigurationDescription cfg)
							throws CdtVariableException;

	/**
	 * 
	 * checks the integrity of the Macros 
	 * If there are inconsistencies, such as when a macro value refers to a  nonexistent macro
	 * or when two macros refer to each other, this method will throw the BuildMacroException exception
	 * The BuildMacroException will contain the human-readable string describing  
	 * the inconsistency and the array of the IBuildMacro interfaces that will represent the macros that
	 * caused the inconsistency. This information will be used in the UI to notify the user about 
	 * the macro inconsistencies (see also the �User interface for viewing and editing Build Macros�
	 * section of this design)
	 */
	public void checkVariableIntegrity(ICConfigurationDescription cfg) throws CdtVariableException;
	
	public boolean isEnvironmentVariable(ICdtVariable variable, ICConfigurationDescription cfg);
	
	public boolean isUserVariable(ICdtVariable variable, ICConfigurationDescription cfg);

	public IStringVariable toEclipseVariable(ICdtVariable variable, ICConfigurationDescription cfg);
}

