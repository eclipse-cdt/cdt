/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	 * @param name macro name
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
	 * "<value_1>< listDelimiter ><value_2>< listDelimiter > ... <value_n>"
	 * otherwise the BuildMacroException is thrown in case the string to be resolved references 
	 * string-list macros 
	 */
	public String resolveValue(String value, 
					String nonexistentMacrosValue,
					String listDelimiter, 
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * if the string contains a value that can be treated as a StringList resolves it to arrays of strings
	 * otherwise throws the BuildMacroException exception
	 */
	public String[] resolveStringListValue(String value, 
					String nonexistentMacrosValue,
					String listDelimiter,
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * resolves macros in the array of string-list values
	 * 
	 * @see #isStringListValue
	 */
	public String[] resolveStringListValues(String value[], 
					String nonexistentMacrosValue,
					String listDelimiter,
					ICConfigurationDescription cfg) throws CdtVariableException;

	/**
	 * 
	 * @return true if the specified expression can be treated as StringList
	 * 1. The string value is "${<some_StringList_Macro_name>}"
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
	 * the macro inconsistencies (see also the "User interface for viewing and editing Build Macros"
	 * section of this design)
	 */
	public void checkVariableIntegrity(ICConfigurationDescription cfg) throws CdtVariableException;
	
	public boolean isEnvironmentVariable(ICdtVariable variable, ICConfigurationDescription cfg);
	
	public boolean isUserVariable(ICdtVariable variable, ICConfigurationDescription cfg);

	public IStringVariable toEclipseVariable(ICdtVariable variable, ICConfigurationDescription cfg);
}

