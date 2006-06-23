/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * This is the default implementation of the IMacroContextInfo
 * 
 * @since 3.0
 */
public class DefaultMacroContextInfo implements IMacroContextInfo {
	private IBuildMacroSupplier fSuppliers[];
	private int fType;
	private Object fData;
	
	public DefaultMacroContextInfo(int type, Object data){
		fType = type;
		fData = data;
	}
	
	protected DefaultMacroContextInfo(int type, Object data, IBuildMacroSupplier suppliers[]){
		fType = type;
		fData = data;
		fSuppliers = suppliers;
	}

	protected IBuildMacroSupplier[] getSuppliers(int type, Object data){
		switch(type){
		case IBuildMacroProvider.CONTEXT_FILE:
			if(data instanceof IFileContextData){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fMbsMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			if(data instanceof IOptionContextData){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fMbsMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			if(data instanceof ITool){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fMbsMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(data instanceof IConfiguration){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fUserDefinedMacroSupplier,
						BuildMacroProvider.fExternalExtensionMacroSupplier,
						BuildMacroProvider.fEnvironmentMacroSupplier,
						BuildMacroProvider.fMbsMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(data instanceof IManagedProject){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fUserDefinedMacroSupplier,
						BuildMacroProvider.fExternalExtensionMacroSupplier,
						BuildMacroProvider.fEnvironmentMacroSupplier,
						BuildMacroProvider.fMbsMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(data instanceof IWorkspace){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fUserDefinedMacroSupplier,
						BuildMacroProvider.fEnvironmentMacroSupplier,
						BuildMacroProvider.fMbsMacroSupplier,
						BuildMacroProvider.fCdtPathEntryMacroSupplier,
						BuildMacroProvider.fEclipseVariablesMacroSupplier
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			if(data == null){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fMbsMacroSupplier 
				};
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			if(data == null){
				return new IBuildMacroSupplier[]{
						BuildMacroProvider.fEnvironmentMacroSupplier
				};
			}
			break;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextType()
	 */
	public int getContextType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getContextData()
	 */
	public Object getContextData() {
		return fData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getSuppliers()
	 */
	public IBuildMacroSupplier[] getSuppliers() {
		if(fSuppliers == null)
			fSuppliers = getSuppliers(fType, fData);
		return fSuppliers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo#getNext()
	 */
	public IMacroContextInfo getNext() {
		switch(fType){
		case IBuildMacroProvider.CONTEXT_FILE:
			if(fData instanceof IFileContextData){
				IFileContextData fileContext = (IFileContextData)fData;
				IOptionContextData optionContext = fileContext.getOptionContextData();
				if(optionContext != null)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_OPTION,
							optionContext);
			}
			break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			if(fData instanceof IOptionContextData){
				IOptionContextData optionContext = (IOptionContextData)fData;
				IHoldsOptions ho = OptionContextData.getHolder(optionContext);
				if(ho instanceof ITool)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_TOOL,
							ho);
				else if(ho instanceof IToolChain)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							((IToolChain)ho).getParent());
				else {
					IBuildObject buildObj = optionContext.getParent();
                    IConfiguration cfg = null; 
                    if(buildObj instanceof ITool) 
                       buildObj = ((ITool)buildObj).getParent(); 
                    if(buildObj instanceof IToolChain) 
                       cfg = ((IToolChain)buildObj).getParent(); 
                    else if(buildObj instanceof IResourceConfiguration) 
                       cfg = ((IResourceConfiguration)buildObj).getParent(); 
                    else if(buildObj instanceof IConfiguration) 
                       cfg = (IConfiguration)buildObj; 

                    if(cfg != null){ 
                        return new DefaultMacroContextInfo( 
                        		IBuildMacroProvider.CONTEXT_CONFIGURATION, 
                        		cfg); 
                    } 
				}
			}
			break;
		case IBuildMacroProvider.CONTEXT_TOOL:
			if(fData instanceof ITool){
				IBuildObject parent = ((ITool)fData).getParent();
				IConfiguration cfg = null;
				if(parent instanceof IToolChain)
					cfg = ((IToolChain)parent).getParent();
				else if(parent instanceof IResourceConfiguration)
					cfg = ((IResourceConfiguration)parent).getParent();
					
				if(cfg != null)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							cfg);
			}
			break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(fData instanceof IConfiguration){
				IConfiguration configuration = (IConfiguration)fData;
				IManagedProject managedProject = configuration.getManagedProject();
				if(managedProject != null)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_PROJECT,
							managedProject);
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(fData instanceof IManagedProject){
				IWorkspace wsp = ResourcesPlugin.getWorkspace();
				if(wsp != null)
					return new DefaultMacroContextInfo(
							IBuildMacroProvider.CONTEXT_WORKSPACE,
							wsp);
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(fData instanceof IWorkspace){
				return new DefaultMacroContextInfo(
						IBuildMacroProvider.CONTEXT_INSTALLATIONS,
						null);
			}
			break;
		case IBuildMacroProvider.CONTEXT_INSTALLATIONS:
			if(fData == null){
				return new DefaultMacroContextInfo(
						IBuildMacroProvider.CONTEXT_ECLIPSEENV,
						null);
			}
			break;
		case IBuildMacroProvider.CONTEXT_ECLIPSEENV:
			if(fData == null){
				return null;
			}
			break;
		}
		return null;
	}
}
