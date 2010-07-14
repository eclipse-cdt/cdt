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
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.core.cdtvariables.CdtVariable;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;

/**
 * This is the trivial implementation of the IBuildMacro used internaly by the MBS
 * 
 * @since 3.0
 */
public class BuildMacro extends CdtVariable implements IBuildMacro {

	public BuildMacro() {
		super();
	}

	public BuildMacro(ICdtVariable var) {
		super(var);
	}

	public BuildMacro(String name, int type, String value) {
		super(name, type, value);
	}

	public BuildMacro(String name, int type, String[] value) {
		super(name, type, value);
	}

	public int getMacroValueType() {
		return getValueType();
	}

	@Override
	public String[] getStringListValue() throws BuildMacroException {
		// TODO Auto-generated method stub
		try {
			return super.getStringListValue();
		} catch (CdtVariableException e) {
			throw new BuildMacroException(e);
		}
	}

	@Override
	public String getStringValue() throws BuildMacroException {
		try {
			return super.getStringValue();
		} catch (CdtVariableException e) {
			throw new BuildMacroException(e);
		}
	}
	
}
