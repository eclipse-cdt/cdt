/*******************************************************************************
 * Copyright (c) 2013 AdaCore and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Philippe Gil (AdaCore) - Initial API and implementation
 *     Marc-Andre Laperle (Ericsson) - Fix parsing for old versions of GDB
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * '-gdb-show language' returns the current source language.
 *
 * sample output:
 *
 * -gdb-show language
 * ^done,value="auto"
 *
 * GDB 6.2-6.8:
 * -gdb-show language
 * ^done,value="auto; currently c"
 *
 * the different returned values are:
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
public class MIGDBShowLanguageInfo extends MIInfo {

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

	private String fLanguage = AUTO;

	public MIGDBShowLanguageInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord outr = out.getMIResultRecord();
			if (outr != null) {
				MIResult[] results = outr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							fLanguage = ((MIConst) value).getString();

							// Some versions of GDB (6.2-6.8) output "auto; currently c"
							// so we need to remove the semicolon part
							int semiColonIdx = fLanguage.indexOf(';');
							if (semiColonIdx != -1) {
								fLanguage = fLanguage.substring(0, semiColonIdx);
							}
						}
					}
				}
			}
		}
	}

	public String getLanguage() {
		return fLanguage;
	}
}
