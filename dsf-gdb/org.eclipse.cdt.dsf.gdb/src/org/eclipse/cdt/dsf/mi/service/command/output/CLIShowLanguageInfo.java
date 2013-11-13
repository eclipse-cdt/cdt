/*******************************************************************************
 * Copyright (c) 2013 AdaCore and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     AdaCore - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'show language' returns the current source language.
 * 
 * sample output: 
 *
 * (gdb) set language c
 * (gdb) show language
 * The current source language is "c".
 * Warning: the current language does not match this frame.
 * 
 * (gdb) set language auto
 * (gdb) show language
 * The current source language is "auto; currently ada".
 *
 * (gdb) set language local
 * (gdb) show language
 * The current source language is "auto; currently ada".
 *
 * (gdb) help set language
 * Set the current source language.
 * The currently understood settings are:
 *
 * local or auto    Automatic setting based on source file
 * ada              Use the Ada language
 * c                Use the C language
 * c++              Use the C++ language
 * asm              Use the Asm language
 * minimal          Use the Minimal language
 * d                Use the D language
 * fortran          Use the Fortran language
 * objective-c      Use the Objective-c language
 * go               Use the Go language
 * java             Use the Java language
 * modula-2         Use the Modula-2 language
 * opencl           Use the Opencl language
 * pascal           Use the Pascal language
 * 
 * @since 4.3
 */
public class CLIShowLanguageInfo extends MIInfo {

	final private static Pattern outputPattern = Pattern.compile("The current source language is \"(.*)\"\\."); //$NON-NLS-1$
	final private static Pattern autoPattern = Pattern.compile("auto; currently (.*)"); //$NON-NLS-1$

	public static final String LOCAL = "local"; //$NON-NLS-1$
	public static final String AUTO = "auto"; //$NON-NLS-1$

	public static final String ADA = "ada"; //$NON-NLS-1$
	public static final String C = "c"; //$NON-NLS-1$
	public static final String C_PLUS_PLUS = "c++"; //$NON-NLS-1$
	public static final String ASM = "asm"; //$NON-NLS-1$
	public static final String MINIMAL = "minimal"; //$NON-NLS-1$
	public static final String D = "d"; //$NON-NLS-1$
	public static final String FORTRAN = "fortran"; //$NON-NLS-1$
	public static final String OBJECTIVE_C = "objective-c"; //$NON-NLS-1$
	public static final String GO = "go"; //$NON-NLS-1$
	public static final String JAVA = "java"; //$NON-NLS-1$
	public static final String MODULA_2 = "modula-2"; //$NON-NLS-1$
	public static final String OPENCL = "opencl"; //$NON-NLS-1$
	public static final String PASCAL = "pascal"; //$NON-NLS-1$

	private String language = null;
	private String current= null;
	
	public CLIShowLanguageInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					Matcher matcher = outputPattern.matcher(line);
					if (matcher.matches()) {
						language = matcher.group(1);
						matcher = autoPattern.matcher(language);
						if (matcher.matches()) {
							language = AUTO;
							current = matcher.group(1);
						}
						else {
							current = language;							
						}
						break;
					}
				}
			}
		}
	}

	public String getLanguage() {
		return language;
	}

	public String getCurrent() {
		return current;
	}
}
