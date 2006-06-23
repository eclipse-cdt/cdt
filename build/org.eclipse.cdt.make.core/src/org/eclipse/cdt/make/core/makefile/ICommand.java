/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

import java.io.File;
import java.io.IOException;


/**
 * ICommand
 * Commands are associated with a rule and executed by
 * the make program when building a target.
 */
public interface ICommand extends IDirective {
	
	final public static char HYPHEN = '-';

	final public static String HYPHEN_STRING = "-"; //$NON-NLS-1$

	final public static char AT = '@';

	final public static String AT_STRING = "@"; //$NON-NLS-1$

	final public static char PLUS = '+';

	final public static String PLUS_STRING = "+"; //$NON-NLS-1$

	final public static char TAB = '\t';

	/**
	 *   -    If the command prefix contains a hyphen, or the -i option is
	 * present, or the special target .IGNORE has either the current
	 * target as a prerequisite or has no prerequisites, any error
	 * found while executing the command will be ignored.
	 */
	boolean shouldIgnoreError();

	/**
	 * @    If the command prefix contains an at sign and the
	 * command-line -n option is not specified, or the -s option is
	 * present, or the special target .SILENT has either the current
	 * target as a prerequisite or has no prerequisites, the command
	 * will not be written to standard output before it is executed.
	 */
	boolean shouldBeSilent();

	/**
	 * +    If the command prefix contains a plus sign, this indicates a
	 * command line that will be executed even if -n, -q or -t is
	 * specified.
	 */
	boolean shouldExecute();


	/**
	 * Executes the command in a separate process with the
	 * specified environment and working directory.
	 *
	 */
	Process execute(String shell, String[] envp, File dir) throws IOException;
}
