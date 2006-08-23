/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;

public class FileMacroExplicitSubstitutor extends DefaultMacroSubstitutor {
		
	public FileMacroExplicitSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
		super(contextType, contextData, inexistentMacroValue, listDelimiter);
	}

	public FileMacroExplicitSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
		super(contextInfo, inexistentMacroValue, listDelimiter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor#resolveMacro(org.eclipse.cdt.managedbuilder.macros.IBuildMacro)
	 */
	protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
		if(macro instanceof MbsMacroSupplier.FileContextMacro){
			MbsMacroSupplier.FileContextMacro fileMacro = (MbsMacroSupplier.FileContextMacro)macro;
			String val = fileMacro.getExplicitMacroValue();
			return new ResolvedMacro(macro.getName(), val);
		}
		return super.resolveMacro(macro);
	}
	
}
