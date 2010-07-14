/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;

/**
 * This class is used by the MacroResolver to collect and present
 * the explicit file macros referenced in the given expression
 * 
 * @since 3.0
 */
public class ExplicitFileMacroCollector extends SupplierBasedCdtVariableSubstitutor {
	private static final String EMPTY_STRING = "";	//$NON-NLS-1$
	
	private List fMacrosList = new ArrayList();

/*	public ExplicitFileMacroCollector(int contextType, Object contextData){
		super(contextType, contextData, EMPTY_STRING, EMPTY_STRING);
	}
*/
	public ExplicitFileMacroCollector(IMacroContextInfo contextInfo){
		super(contextInfo, EMPTY_STRING, EMPTY_STRING);
	}
/*
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
*/
	/* (non-Javadoc)
	 */
	@Override
	protected ResolvedMacro resolveMacro(ICdtVariable macro) throws CdtVariableException{
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
