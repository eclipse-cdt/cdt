/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;

/**
 * This is the trivial implementation of the IBuildMacro used internaly by the MBS
 * 
 * @since 3.0
 */
public class BuildMacro implements IBuildMacro {
	protected String fName;
	protected int fType;
	protected String fStringValue;
	protected String fStringListValue[];

	protected BuildMacro(){
		
	}

	public BuildMacro(String name, int type, String value){
		fName = name;
		fType = type;
		fStringValue = value;
	}

	public BuildMacro(String name, int type, String value[]){
		fName = name;
		fType = type;
		fStringListValue = value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getMacroValueType()
	 */
	public int getMacroValueType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
	 */
	public String getStringValue() throws BuildMacroException {
		if(MacroResolver.isStringListMacro(fType))
			throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_NOT_STRING,fName,null,fName,0,null);
		
		return fStringValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildMacroException {
		if(!MacroResolver.isStringListMacro(fType))
			throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_NOT_STRINGLIST,fName,null,fName,0,null);

		return fStringListValue;
	}

}
