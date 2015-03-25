/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui;

import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;

/**
 * Implemented type that contains BuildEnvironmentalVariables.
 * 
 */
public class LlvmBuildEnvironmentVariable implements IBuildEnvironmentVariable {

	private final String name;
	private final String value;
	private final int operation;

	/**
	 * Constructor.
	 * 
	 * @param name Name for the environment variable
	 * @param value Value for the environment variable
	 * @param operation Operation of the environment variable
	 */
	public LlvmBuildEnvironmentVariable(String name, String value, int operation) {
		super();
		this.name = name;
		this.value = value;
		this.operation = operation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getDelimiter()
	 */
	/**
	 * Get a delimiter.
	 * 
	 * @return String delimiter
	 */
	@Override
	public String getDelimiter() {
		return ";"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getName()
	 */
	/**
	 * Get name of the llvm environment variable.
	 * 
	 * @return name The name of the llvm environment variable
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getOperation()
	 */
	/**
	 * Get operation of the llvm environment variable.
	 * 
	 * @return operation The operation of the llvm environment variable
	 */
	@Override
	public int getOperation() {
		return this.operation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getValue()
	 */
	/**
	 * Get value of the llvm environment variable.
	 * 
	 * @return value The value of the llvm environment variable.
	 */
	@Override
	public String getValue() {
		return this.value;
	}
	
}