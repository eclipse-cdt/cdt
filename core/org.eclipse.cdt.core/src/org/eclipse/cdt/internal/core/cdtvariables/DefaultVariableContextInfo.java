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
package org.eclipse.cdt.internal.core.cdtvariables;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * This is the default implementation of the IMacroContextInfo
 *
 * @since 3.0
 */
public class DefaultVariableContextInfo implements ICoreVariableContextInfo {

	private ICdtVariableSupplier fSuppliers[];
	private int fType;
	private Object fData;

	public DefaultVariableContextInfo(int type, Object data){
		fType = type;
		fData = data;
	}

	protected DefaultVariableContextInfo(int type, Object data, ICdtVariableSupplier suppliers[]){
		fType = type;
		fData = data;
		fSuppliers = suppliers;
	}

	protected ICdtVariableSupplier[] getSuppliers(int type, Object data){
		switch(type){
		case CONTEXT_CONFIGURATION:
			if(data instanceof ICConfigurationDescription){
				return new ICdtVariableSupplier[]{
						CdtVariableManager.fUserDefinedMacroSupplier,
						CdtVariableManager.fBuildSystemVariableSupplier,
						CdtVariableManager.fEnvironmentMacroSupplier,
						CdtVariableManager.fCdtMacroSupplier
				};
			}
			break;
		case CONTEXT_WORKSPACE:
			if(data == null || data instanceof IWorkspace){
				return new ICdtVariableSupplier[]{
						CdtVariableManager.fUserDefinedMacroSupplier,
						CdtVariableManager.fEnvironmentMacroSupplier,
						CdtVariableManager.fCdtMacroSupplier,
						CdtVariableManager.fEclipseVariablesMacroSupplier
				};
			}
			break;
		case CONTEXT_INSTALLATIONS:
			if(data == null){
				return new ICdtVariableSupplier[]{
						CdtVariableManager.fCdtMacroSupplier
				};
			}
			break;
		case CONTEXT_ECLIPSEENV:
			if(data == null){
				return new ICdtVariableSupplier[]{
						CdtVariableManager.fEnvironmentMacroSupplier
				};
			}
			break;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextType()
	 */
	@Override
	public int getContextType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextData()
	 */
	@Override
	public Object getContextData() {
		return fData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getSuppliers()
	 */
	@Override
	public ICdtVariableSupplier[] getSuppliers() {
		if(fSuppliers == null)
			fSuppliers = getSuppliers(fType, fData);
		return fSuppliers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
	 */
	@Override
	public IVariableContextInfo getNext() {
		switch(fType){
		case CONTEXT_CONFIGURATION:
			if(fData instanceof ICConfigurationDescription){
				IWorkspace wsp = ResourcesPlugin.getWorkspace();
				if(wsp != null)
					return new DefaultVariableContextInfo(
							CONTEXT_WORKSPACE,
							wsp);
			}
			break;
		case CONTEXT_WORKSPACE:
			if(fData instanceof IWorkspace){
				return new DefaultVariableContextInfo(
						CONTEXT_INSTALLATIONS,
						null);
			}
			break;
		case CONTEXT_INSTALLATIONS:
			if(fData == null){
				return new DefaultVariableContextInfo(
						CONTEXT_ECLIPSEENV,
						null);
			}
			break;
		case CONTEXT_ECLIPSEENV:
			if(fData == null){
				return null;
			}
			break;
		}
		return null;
	}
}
