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
package org.eclipse.cdt.utils.cdtvariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;

/**
 * This is the utility class used to resolve macro references and that provides
 * other functionality related to the macro resolving 
 * 
 * @since 3.0
 */
public class CdtVariableResolver {
	private static final String EMPTY_STRING = "";	//$NON-NLS-1$
	
	public static final String VARIABLE_PREFIX = "${";	//$NON-NLS-1$
	public static final char VARIABLE_SUFFIX = '}';	//$NON-NLS-1$
	public static final char VARIABLE_ESCAPE_CHAR = '\\';	//$NON-NLS-1$
	private static final int VARIABLE_PREFIX_LENGTH = VARIABLE_PREFIX.length();

	static public String convertStringListToString(String value[], String listDelimiter) {
		
		if(value == null || value.length == 0)
			return EMPTY_STRING;
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < value.length; i++){
			buffer.append(value[i]);
			if(listDelimiter != null && !EMPTY_STRING.equals(listDelimiter) && i < value.length -1 )
				buffer.append(listDelimiter);
		}
		return buffer.toString();
	}

	/**
	 * resolves macros in the given string by calling the macro subsitutor for each macro reference found
	 * 
	 * @param string
	 * @param substitutor
	 * @return resolved string
	 * 
	 * @throws CdtVariableException
	 */
	static public String resolveToString(String string, IVariableSubstitutor substitutor)
			throws CdtVariableException{
		return (String)resolve(string,substitutor,false,false);
	}

	/**
	 * finds the macro references in the given string and calls the macro substitutor for each macro found
	 * this could be used for obtaining the list of macros referenced in the given string, etc.
	 * 
	 * @param string
	 * @param substitutor
	 * @throws CdtVariableException
	 */
	static public void checkVariables(String string, IVariableSubstitutor substitutor)
			throws CdtVariableException{
		resolve(string, substitutor, false, true);
	}

	static private Object resolve(String string, IVariableSubstitutor substitutor, boolean asList, boolean checkOnly)
			throws CdtVariableException{
		if(string == null)
			return EMPTY_STRING;

		int macroStart = -1;
		int macroEnd = -1;
		int processed = 0;
		StringBuffer buffer = checkOnly ? null : new StringBuffer();
		boolean listMode = false;
		String listValue[] = null;
		final int length = string.length();

		do{
			//find macro prefix
			macroStart = string.indexOf(VARIABLE_PREFIX, macroEnd+1);
			if(macroStart == -1){
				if(buffer != null)
					buffer.append(string.substring(processed,length));
				break;
			}

			//macro prefix found, find macro suffix
			macroEnd = string.indexOf(VARIABLE_SUFFIX, macroStart);
			if(macroEnd == -1){
				if(buffer != null)
					buffer.append(string.substring(processed,length));
				break;
			}

			if(asList && macroStart == 0 && macroEnd == length - 1)
				listMode = true;
			
			//check whether macro is prepended with the back-clash
			if(macroStart > 0 && VARIABLE_ESCAPE_CHAR == string.charAt(macroStart - 1)){
				int num;
				for(num = macroStart-2; num >= 0 && VARIABLE_ESCAPE_CHAR == string.charAt(num); num--){}
				
				//number of back-slashes
				num = macroStart - num - 1;
				if(buffer != null)
					buffer.append(string.substring(processed,macroStart - ((num + 1) >> 1)));

				if((num & 1) == 0)
					processed = macroStart;
				else {
					if(buffer != null)
						buffer.append(string.substring(macroStart,macroEnd+1));
					processed = macroEnd+1;
					continue;
				}
			}

			if(macroStart > processed && buffer != null)
				buffer.append(string.substring(processed,macroStart));
				
			String name = string.substring(macroStart + VARIABLE_PREFIX_LENGTH, macroEnd);
			if(!EMPTY_STRING.equals(name)){
			
				if(listMode){
					listValue = substitutor.resolveToStringList(name);
					if(listValue == null)
						throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED,(String)null,string,name);
				}
				else{
					String resolved = substitutor.resolveToString(name);
					if(resolved == null)
						throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED,(String)null,string,name);
					if(buffer != null)
						buffer.append(resolved);
				}
			}
			processed = macroEnd+1;

		}while(true);

		if(asList){
			String result[] = null;
			if(listMode){
				if(listValue != null)
					result = listValue;
				else
					result = new String[0];
			}
			else if(buffer != null)
				result = new String[]{buffer.toString()};
			return result;
		}
		else if(buffer != null)
			return buffer.toString();
		return null;
	}

	/**
	 * resolves macros in the array of string-list values
	 * @param values
	 * @param substitutor
	 * @param ignoreErrors 
	 * @return
	 * @throws CdtVariableException
	 */
	static public String[] resolveStringListValues(String values[], IVariableSubstitutor substitutor, boolean ignoreErrors) 
						throws CdtVariableException {
		String result[] = null;
		if(values == null || values.length == 0)
			result = values;
		else if(values.length == 1)
			try {
				result = CdtVariableResolver.resolveToStringList(values[0], substitutor);
			} catch (CdtVariableException e) {
				if(!ignoreErrors)
					throw e;
			}
		else {	
			List list = new ArrayList();
			for(int i = 0; i < values.length; i++){
				String resolved[];
				try {
					resolved = CdtVariableResolver.resolveToStringList(values[i], substitutor);
					if(resolved != null && resolved.length > 0)
						list.addAll(Arrays.asList(resolved));
				} catch (CdtVariableException e) {
					if(!ignoreErrors)
						throw e;
				}
			}

			result =  (String[])list.toArray(new String[list.size()]);
		}
		return result;
	}

	/**
	 * Resolves macros in the given String to the String-list
	 * 
	 * @param string
	 * @param substitutor
	 * @return
	 * @throws CdtVariableException
	 */
	static public String[] resolveToStringList(String string, IVariableSubstitutor substitutor)
			throws CdtVariableException{
		return (String[])resolve(string,substitutor,true,false);
	}

	/**
	 * returns true if the given macro is a String-list macro.
	 * 
	 * @param macroType
	 * @return
	 */
	public static boolean isStringListVariable(int macroType){
		switch(macroType){
		case ICdtVariable.VALUE_TEXT_LIST:
		case ICdtVariable.VALUE_PATH_FILE_LIST:
		case ICdtVariable.VALUE_PATH_DIR_LIST:
		case ICdtVariable.VALUE_PATH_ANY_LIST:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * checks the macros integrity for the given context
	 * 
	 * @param provider
	 * @param contextType
	 * @param contextData
	 * @throws CdtVariableException
	 */
	public static void checkIntegrity(
			IVariableContextInfo info,
			IVariableSubstitutor substitutor) throws CdtVariableException{
		
		if(info != null){
			ICdtVariable macros[] = SupplierBasedCdtVariableManager.getVariables(info,true);
			if(macros != null){
				for(int i = 0; i < macros.length; i++){
					ICdtVariable macro = macros[i];
					if(isStringListVariable(macro.getValueType()))
						substitutor.resolveToStringList(macro.getName());
					else
						substitutor.resolveToString(macro.getName());
				}
			}
		}
	}
	
	/**
	 * creates a macro reference given the macro name
	 * e.g. if the "macro1" name is passed, returns "${macro1}"
	 * 
	 * @param name
	 * @return String
	 */
	public static String createVariableReference(String name){
		return VARIABLE_PREFIX + name + VARIABLE_SUFFIX;
	}
	
	/**
	 * Returns the array of the explicit file macros, referenced in the given string
	 * (Explicit file macros are the file-specific macros, whose values are not provided
	 * by the tool-integrator. As a result these macros contain explicit values, but not the values
	 * specified in the format of the builder automatic variables and text functions)
	 * 
	 * @param expression
	 * @param contextType
	 * @param contextData
	 * @return
	 */
/*	public static IBuildMacro[] getReferencedExplitFileMacros(String expression, int contextType, Object contextData){
		ExplicitFileMacroCollector collector = new ExplicitFileMacroCollector(contextType,contextData);
		try {
			resolveToString(expression,collector);
		} catch (BuildMacroException e){
		}
		return collector.getExplicisFileMacros();
	}
*/
/*	static public ICdtVariable[] filterMacros(ICdtVariable macros[], String remove[]){
		
		if(macros == null || macros.length == 0)
			return macros;
		
		ICdtVariable filtered[] = new ICdtVariable[macros.length];
		int filteredNum = 0;
		for(int i = 0; i < macros.length; i++){
			ICdtVariable var = macros[i];
			String name = null;
			if(var != null && (name = EnvVarOperationProcessor.normalizeName(var.getName())) != null){
				boolean skip = false;
				if(remove != null && remove.length > 0){
					for(int j = 0; j < remove.length; j++){
						if(remove[j] != null && remove[j].equals(name)){
							skip = true;
							break;
						}
					}
				}
				if(!skip)
					filtered[filteredNum++] = var;
			}
		}

		if(filteredNum != filtered.length){
			IBuildMacro m[] = new IBuildMacro[filteredNum];
			for(int i = 0; i < filteredNum; i++)
				m[i] = filtered[i];
			filtered = m;
		}
		return filtered;
	}
*/
}
