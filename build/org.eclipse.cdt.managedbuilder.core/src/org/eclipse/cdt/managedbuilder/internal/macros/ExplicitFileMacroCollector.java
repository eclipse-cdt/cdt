/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;

/**
 * This class is used by the MacroResolver to collect and present
 * the explicit file macros referenced in the given expression
 * 
 * @since 3.0
 */
public class ExplicitFileMacroCollector extends DefaultMacroSubstitutor {
	private static final String EMPTY_STRING = "";	//$NON-NLS-1$
	
	private List fMacrosList = new ArrayList();

	public ExplicitFileMacroCollector(int contextType, Object contextData){
		super(contextType, contextData, EMPTY_STRING, EMPTY_STRING);
	}

	public ExplicitFileMacroCollector(IMacroContextInfo contextInfo){
		super(contextInfo, EMPTY_STRING, EMPTY_STRING);
	}

	public ExplicitFileMacroCollector(ITool tool){
		super(null, EMPTY_STRING, EMPTY_STRING);
		IBuildObject bo = tool.getParent();
		IConfiguration cfg = null;
		if(bo instanceof IResourceConfiguration)
			cfg = ((IResourceConfiguration)bo).getParent();
		else if (bo instanceof IToolChain)
			cfg = ((IToolChain)bo).getParent();
		try{
			setMacroContextInfo(IBuildMacroProvider.CONTEXT_CONFIGURATION,cfg);
		}catch (BuildMacroException e){
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor#resolveMacro(org.eclipse.cdt.managedbuilder.macros.IBuildMacro)
	 */
	protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
		if(macro instanceof MbsMacroSupplier.FileContextMacro){
			MbsMacroSupplier.FileContextMacro fileMacro = (MbsMacroSupplier.FileContextMacro)macro;
			if(fileMacro.isExplicit())
				fMacrosList.add(macro);
			return null;
		}
		return super.resolveMacro(macro);
	}
	
	public IBuildMacro[] getExplicisFileMacros(){
		return (IBuildMacro[])fMacrosList.toArray(new IBuildMacro[fMacrosList.size()]);
	}

}
