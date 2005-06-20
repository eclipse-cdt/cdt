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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IPathEntryVariableManager;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.runtime.IPath;

/**
 * This supplier suplies the macros that represent the CDT Path entry variables
 * 
 * @since 3.0
 */
public class CdtPathEntryMacroSupplier implements IBuildMacroSupplier {
	private static CdtPathEntryMacroSupplier fInstance;
	
	private CdtPathEntryMacroSupplier(){
		
	}

	public static CdtPathEntryMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new CdtPathEntryMacroSupplier();
		return fInstance;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		if(contextType != IBuildMacroProvider.CONTEXT_WORKSPACE)
			return null;
		if(macroName == null || "".equals(macroName))	//$NON-NLS-1$
			return null;

		IPathEntryVariableManager mngr = CCorePlugin.getDefault().getPathEntryVariableManager();
		if(mngr == null)
			return null;

		IPath path = mngr.getValue(macroName);
		if(path != null)
			return new BuildMacro(macroName,BuildMacro.VALUE_PATH_ANY,path.toOSString());
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		if(contextType != IBuildMacroProvider.CONTEXT_WORKSPACE)
			return null;
		IPathEntryVariableManager mngr = CCorePlugin.getDefault().getPathEntryVariableManager();
		if(mngr == null)
			return null;
		
		String names[] = mngr.getVariableNames();
		BuildMacro macros[] = new BuildMacro[names.length];
		for(int i = 0; i < names.length; i++)
			macros[i] = new BuildMacro(names[i],BuildMacro.VALUE_PATH_ANY,mngr.getValue(names[i]).toOSString());
		return macros;
	}

}
