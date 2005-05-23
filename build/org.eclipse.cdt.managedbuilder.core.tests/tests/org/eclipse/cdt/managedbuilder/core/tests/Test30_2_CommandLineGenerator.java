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
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;

/**
 *  Test command line generator
 */
public class Test30_2_CommandLineGenerator implements
		IManagedCommandLineGenerator {

	public final String AT = "@";	//$NON-NLS-1$
	public final String COLON = ":";	//$NON-NLS-1$
	public final String DOT = ".";	//$NON-NLS-1$
	public final String ECHO = "echo";	//$NON-NLS-1$
	public final String IN_MACRO = "$<";	//$NON-NLS-1$
	public final String LINEBREAK = "\\\n";	//$NON-NLS-1$
	public final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	public final String OUT_MACRO = "$@";	//$NON-NLS-1$
	public final String SEPARATOR = "/";	//$NON-NLS-1$
	public final String SINGLE_QUOTE = "'";	//$NON-NLS-1$
	public final String TAB = "\t";	//$NON-NLS-1$
	public final String WHITESPACE = " ";	//$NON-NLS-1$
	public final String WILDCARD = "%";	//$NON-NLS-1$
	public final String UNDERLINE = "_"; //$NON-NLS-1$
	
	public final String VAR_FIRST_CHAR = "$"; //$NON-NLS-1$
	public final char VAR_SECOND_CHAR = '{'; //$NON-NLS-1$
	public final String VAR_FINAL_CHAR = "}"; //$NON-NLS-1$
	public final String CLASS_PROPERTY_PREFIX = "get"; //$NON-NLS-1$
	
	public final String CMD_LINE_PRM_NAME = "COMMAND"; //$NON-NLS-1$
	public final String FLAGS_PRM_NAME = "FLAGS"; //$NON-NLS-1$
	public final String OUTPUT_FLAG_PRM_NAME = "OUTPUT_FLAG"; //$NON-NLS-1$
	public final String OUTPUT_PREFIX_PRM_NAME = "OUTPUT_PREFIX"; //$NON-NLS-1$
	public final String OUTPUT_PRM_NAME = "OUTPUT"; //$NON-NLS-1$
	public final String INPUTS_PRM_NAME = "INPUTS"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator#generateCommandLineInfo(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
	 */
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool,
			String commandName, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		ManagedBuildCommandLineInfo info = new ManagedBuildCommandLineInfo();
		//  We are only going to change the "flags" to remove the "test30_2.tar-list.filename" option
		info.commandName = new String(commandName);
		info.commandOutputFlag = new String(outputFlag);
		info.commandOutputPrefix = new String(outputPrefix);
		info.commandOutput = new String(outputName);
		info.commandLinePattern = new String(commandLinePattern);
		info.commandInputs = new String();
		for (int i = 0; i < inputResources.length; i++) {
			if (i > 0) info.commandInputs += " ";
			info.commandInputs += inputResources[i];
		}
		info.commandFlags = new String();
		IOption opt = tool.getOptionBySuperClassId("test30_2.tar-list.filename");
		String optVal = "";
		try {
			optVal = opt.getStringValue();
		} catch (Exception e) {}
		for (int i = 0; i < flags.length; i++) {
			if (!(flags[i].equals(optVal))) {
				if (i > 0) info.commandFlags += " ";
				info.commandFlags += flags[i];
			}
		}
		//  Generate the command line
		int start =  0;
		int stop = 0;
		StringBuffer sb = new StringBuffer();
		while( (start = commandLinePattern.indexOf( VAR_FIRST_CHAR, start )) >= 0 ) {
			if( commandLinePattern.charAt( start + 1 ) != VAR_SECOND_CHAR  ) {
				sb.append(VAR_FIRST_CHAR);
				start++;
				continue;
			}
			if( start > stop ) {
				sb.append( commandLinePattern.substring(stop, start) );
			}
			stop = commandLinePattern.indexOf( VAR_FINAL_CHAR, start + 1 );
			if( stop > 0 && stop <= commandLinePattern.length() ) try {
				String varName = commandLinePattern.substring( start+2, stop ).trim();
				if( varName.compareToIgnoreCase( CMD_LINE_PRM_NAME ) == 0 ) sb.append( info.commandName.trim() );
				else if( varName.compareToIgnoreCase( FLAGS_PRM_NAME ) == 0 ) sb.append( info.commandFlags );
				else if( varName.compareToIgnoreCase( OUTPUT_FLAG_PRM_NAME ) == 0 ) sb.append( info.commandOutputFlag.trim() );
				else if( varName.compareToIgnoreCase( OUTPUT_PREFIX_PRM_NAME ) == 0 ) sb.append( info.commandOutputPrefix.trim() );
				else if( varName.compareToIgnoreCase( OUTPUT_PRM_NAME ) == 0 ) sb.append( info.commandOutput.trim() );
				else if( varName.compareToIgnoreCase( INPUTS_PRM_NAME ) == 0 ) sb.append( info.commandInputs );
				else sb.append( VAR_FIRST_CHAR + VAR_SECOND_CHAR + varName + VAR_FINAL_CHAR );
			} catch( Exception ex ) {
				// 	do nothing for a while
			}
			start = ++stop;
		}
		info.commandLine = sb.toString();
		return info;
	}

}
