/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;

/**
 * This is the utility class used to resolve macro references and that provides
 * other functionality related to the macro resolving 
 * 
 * @since 3.0
 */
public class MacroResolver {
	private static final String EMPTY_STRING = "";	//$NON-NLS-1$
	
	public static final String MACRO_PREFIX = "${";	//$NON-NLS-1$
	public static final char MACRO_SUFFIX = '}';	//$NON-NLS-1$
	public static final char MACRO_ESCAPE_CHAR = '\\';	//$NON-NLS-1$
	private static final int MACRO_PREFIX_LENGTH = MACRO_PREFIX.length();
	private static final String PATTERN_MACRO_NAME = "="; //$NON-NLS-1$

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
	 * @throws BuildMacroException
	 */
	static public String resolveToString(String string, IMacroSubstitutor substitutor)
			throws BuildMacroException{
		return (String)resolve(string,substitutor,false,false);
	}

	/**
	 * finds the macro references in the given string and calls the macro substitutor for each macro found
	 * this could be used for obtaining the list of macros referenced in the given string, etc.
	 * 
	 * @param string
	 * @param substitutor
	 * @throws BuildMacroException
	 */
	static public void checkMacros(String string, IMacroSubstitutor substitutor)
			throws BuildMacroException{
		resolve(string, substitutor, false, true);
	}

	static private Object resolve(String string, IMacroSubstitutor substitutor, boolean asList, boolean checkOnly)
			throws BuildMacroException{
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
			macroStart = string.indexOf(MACRO_PREFIX, macroEnd+1);
			if(macroStart == -1){
				if(buffer != null)
					buffer.append(string.substring(processed,length));
				break;
			}

			//macro prefix found, find macro suffix
			macroEnd = string.indexOf(MACRO_SUFFIX, macroStart);
			if(macroEnd == -1){
				if(buffer != null)
					buffer.append(string.substring(processed,length));
				break;
			}

			if(asList && macroStart == 0 && macroEnd == length - 1)
				listMode = true;
			
			//check whether macro is prepended with the back-clash
			if(macroStart > 0 && MACRO_ESCAPE_CHAR == string.charAt(macroStart - 1)){
				int num;
				for(num = macroStart-2; num >= 0 && MACRO_ESCAPE_CHAR == string.charAt(num); num--){}
				
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
				
			String name = string.substring(macroStart + MACRO_PREFIX_LENGTH, macroEnd);
			if(!EMPTY_STRING.equals(name)){
			
				if(listMode){
					listValue = substitutor.resolveToStringList(name);
					if(listValue == null)
						throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_UNDEFINED,(String)null,string,name,0,null);
				}
				else{
					String resolved = substitutor.resolveToString(name);
					if(resolved == null)
						throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_UNDEFINED,(String)null,string,name,0,null);
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
	 * @throws BuildMacroException
	 */
	static public String[] resolveStringListValues(String values[], IMacroSubstitutor substitutor, boolean ignoreErrors) 
						throws BuildMacroException {
		String result[] = null;
		if(values == null || values.length == 0)
			result = values;
		else if(values.length == 1)
			try {
				result = MacroResolver.resolveToStringList(values[0], substitutor);
			} catch (BuildMacroException e) {
				if(!ignoreErrors)
					throw e;
			}
		else {	
			List list = new ArrayList();
			for(int i = 0; i < values.length; i++){
				String resolved[];
				try {
					resolved = MacroResolver.resolveToStringList(values[i], substitutor);
					if(resolved != null && resolved.length > 0)
						list.addAll(Arrays.asList(resolved));
				} catch (BuildMacroException e) {
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
	 * @throws BuildMacroException
	 */
	static public String[] resolveToStringList(String string, IMacroSubstitutor substitutor)
			throws BuildMacroException{
		return (String[])resolve(string,substitutor,true,false);
	}

	/**
	 * returns true if the given macro is a String-list macro.
	 * 
	 * @param macroType
	 * @return
	 */
	public static boolean isStringListMacro(int macroType){
		switch(macroType){
		case IBuildMacro.VALUE_TEXT_LIST:
		case IBuildMacro.VALUE_PATH_FILE_LIST:
		case IBuildMacro.VALUE_PATH_DIR_LIST:
		case IBuildMacro.VALUE_PATH_ANY_LIST:
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
	 * @throws BuildMacroException
	 */
	public static void checkIntegrity(
			IMacroContextInfo info,
			IMacroSubstitutor substitutor) throws BuildMacroException{
		
		if(info != null){
			IBuildMacro macros[] = BuildMacroProvider.getMacros(info,true);
			if(macros != null){
				for(int i = 0; i < macros.length; i++){
					IBuildMacro macro = macros[i];
					if(isStringListMacro(macro.getMacroValueType()))
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
	public static String createMacroReference(String name){
		return MACRO_PREFIX + name + MACRO_SUFFIX;
	}
	
	/**
	 * answers whether the builder used for the given configuration is capable
	 * of handling macros in the buildfile
	 * 
	 * @param cfg
	 * @return
	 */
	public static boolean canKeepMacrosInBuildfile(IConfiguration cfg){
		if(cfg != null){
			IToolChain toolChain = cfg.getToolChain();
			if(toolChain != null)
				return canKeepMacrosInBuildfile(toolChain.getBuilder());
		}
		return false;
	}

	/**
	 * answers whether the given builder is capable
	 * of handling macros in the buildfile
	 * 
	 * @param builder
	 * @return
	 */
	public static boolean canKeepMacrosInBuildfile(IBuilder builder){
		if(builder != null){
			String pattern = builder.getBuilderVariablePattern();
			if(pattern != null && pattern.indexOf(PATTERN_MACRO_NAME) != -1)
				return true;
		}
		return false;
	}
	
	/**
	 * creates a macro reference in the buildfile format for the given builder.
	 * If the builder can not treat macros, returns null
	 * @param name
	 * @param builder
	 * @return String
	 */
	public static String createBuildfileMacroReference(String name, IBuilder builder){
		String ref = null;
		if(builder != null){
			String pattern = builder.getBuilderVariablePattern();
			if(pattern != null && pattern.indexOf(PATTERN_MACRO_NAME) != -1)
					ref = pattern.replaceAll(PATTERN_MACRO_NAME,name);
		}
		return ref;
	}

	/**
	 * creates a macro reference in the buildfile format for the builder used for
	 * the given configuration.
	 * If the builder can not treat macros, returns null
	 * @param name
	 * @param cfg
	 * @return String
	 */
	public static String createBuildfileMacroReference(String name, IConfiguration cfg){
		String ref = null;
		if(cfg != null){
			IToolChain toolChain = cfg.getToolChain();
			if(toolChain != null)
				ref = createBuildfileMacroReference(name,toolChain.getBuilder());
		}
		return ref;
	}
	
	/**
	 * Returns the array of the explicit file macros, referenced in the tool's options
	 * (Explicit file macros are the file-specific macros, whose values are not provided
	 * by the tool-integrator. As a result these macros contain explicit values, but not the values
	 * specified in the format of the builder automatic variables and text functions)
	 * 
	 * @param tool
	 * @return
	 */
	public static IBuildMacro[] getReferencedExplitFileMacros(ITool tool){
		if(tool instanceof Tool){
			Tool t = (Tool)tool;
			ExplicitFileMacroCollector collector = new ExplicitFileMacroCollector(tool);
			try {
				t.getToolCommandFlags(null,null,collector);
			} catch (BuildException e){
			}
			return collector.getExplicisFileMacros();
		}
		return new IBuildMacro[0];
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
	public static IBuildMacro[] getReferencedExplitFileMacros(String expression, int contextType, Object contextData){
		ExplicitFileMacroCollector collector = new ExplicitFileMacroCollector(contextType,contextData);
		try {
			resolveToString(expression,collector);
		} catch (BuildMacroException e){
		}
		return collector.getExplicisFileMacros();
	}

}
