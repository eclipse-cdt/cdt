/*******************************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

public interface IManagedCommandLineGenerator {
	/**
	 * 
	 * @param tool
	 * @param commandNamee
	 * @param flags
	 * @param outputFlag
	 * @param outputPrefix
	 * @param outputName
	 * @param inputResources
	 * @param commandLinePattern
	 * @return
	 */
	public IManagedCommandLineInfo generateCommandLineInfo( ITool tool, String commandName, String[] flags, 
			String outputFlag, String outputPrefix, String outputName, String[] inputResources, String commandLinePattern );
}
