/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;

public class FileMacroExplicitSubstitutor extends SupplierBasedCdtVariableSubstitutor {
	private IConfiguration fCfg;	
	private IBuilder fBuilder;	
//	public FileMacroExplicitSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
//		super(contextType, contextData, inexistentMacroValue, listDelimiter);
//	}

	public FileMacroExplicitSubstitutor(IMacroContextInfo contextInfo, 
			IConfiguration cfg,
			IBuilder builder,
			String inexistentMacroValue, String listDelimiter){
		super(contextInfo, inexistentMacroValue, listDelimiter);
		fCfg = cfg;
		fBuilder = builder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor#resolveMacro(org.eclipse.cdt.managedbuilder.macros.IBuildMacro)
	 */
	@Override
	protected ResolvedMacro resolveMacro(ICdtVariable macro) throws CdtVariableException{
		if(macro instanceof MbsMacroSupplier.FileContextMacro){
			MbsMacroSupplier.FileContextMacro fileMacro = (MbsMacroSupplier.FileContextMacro)macro;
			String val = fileMacro.getExplicitMacroValue(fCfg, fBuilder);
			return new ResolvedMacro(macro.getName(), val);
		}
		return super.resolveMacro(macro);
	}
	
}
