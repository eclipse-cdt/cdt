package org.eclipse.rse.internal.useractions.files.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.internal.useractions.ui.compile.SystemDefaultCompileCommand;

/**
 * Specialization of the the SystemDefaultCompileCommand for an IBM-supplied 
 *  compile command for universal files.
 */
public class UniversalIBMCompileCommand extends SystemDefaultCompileCommand {
	/**
	 * Constructor for UniversalCompileIBMCommand.
	 * @param commandLabel label of the command		
	 * @param commandName name of the command
	 */
	public UniversalIBMCompileCommand(String commandLabel, String commandName) {
		super(commandLabel, commandName);
	}

	/**
	 * Constructor for UniversalCompileIBMCommand.
	 * @param commandName name of the command
	 */
	public UniversalIBMCompileCommand(String commandName) {
		super(commandName);
	}

	/**
	 * Constructor that takes a command name and label and the parameters.
	 * This avoids you having to call setAdditionalCommandParameters.
	 */
	public UniversalIBMCompileCommand(String commandLabel, String commandName, String parameters) {
		super(commandLabel, commandName, parameters);
	}
}
