package org.eclipse.rse.internal.useractions.files.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.internal.useractions.ui.compile.SystemDefaultCompileCommands;

/**
 * Specialization of the SystemDefaultCompileCommands for the IBM-supplied compile commands
 *  for universal files
 */
public class UniversalIBMCompileCommands extends SystemDefaultCompileCommands {
	private static UniversalIBMCompileCommands ibmCompileCommands;
	// Source types supported
	public static final String TYPE_C = "c"; //$NON-NLS-1$
	public static final String TYPE_CC = "cc"; //$NON-NLS-1$
	public static final String TYPE_CXX = "cxx"; //$NON-NLS-1$
	public static final String TYPE_CPP = "cpp"; //$NON-NLS-1$
	public static final String TYPE_CPP_C = "C"; //$NON-NLS-1$
	public static final String TYPE_CPP_CC = "CC"; //$NON-NLS-1$
	public static final String TYPE_CPP_CXX = "CXX"; //$NON-NLS-1$
	public static final String TYPE_JAVA = "java"; //$NON-NLS-1$
	public static final String[] ALL_IBM_SRC_TYPES = { TYPE_C, TYPE_CPP, TYPE_CC, TYPE_CPP_CC, TYPE_CXX, TYPE_CPP_CXX, TYPE_JAVA };

	/**
	 * Constructor 
	 */
	public UniversalIBMCompileCommands() {
		super();
	}

	/**
	 * Get all IBM supplied compilable source types.
	 */
	public String[] getAllDefaultSuppliedSourceTypes() {
		return ALL_IBM_SRC_TYPES;
	}

	/**
	 * Return the singleton instance of the list of commands IBM recognizes by default
	 */
	public static UniversalIBMCompileCommands getIBMCompileCommands() {
		if (ibmCompileCommands == null) {
			ibmCompileCommands = new UniversalIBMCompileCommands();
			UniversalIBMCompileCommand cmd = null;
			cmd = new UniversalIBMCompileCommand("JAVAC", "javac", "-deprecation -classpath . ${resource_name}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cmd.setSourceTypes(new String[] { TYPE_JAVA });
			ibmCompileCommands.addCommand(cmd);
			cmd = new UniversalIBMCompileCommand("GCC", "gcc", "-c ${resource_name} -o ${resource_name_root}.o"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cmd.setSourceTypes(new String[] { TYPE_C, TYPE_CC, TYPE_CPP, TYPE_CPP_CC, TYPE_CPP_CXX, TYPE_CXX });
			ibmCompileCommands.addCommand(cmd);
			cmd = new UniversalIBMCompileCommand("CC", "cc", "-c ${resource_name} -o ${resource_name_root}.o"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cmd.setSourceTypes(new String[] { TYPE_C, TYPE_CC, TYPE_CPP, TYPE_CPP_CC, TYPE_CPP_CXX, TYPE_CXX });
			ibmCompileCommands.addCommand(cmd);
		}
		return ibmCompileCommands;
	}
}
