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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvVarOperationProcessor;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.core.resources.IResource;

/**
 * This substitutor resolves all macro references except for the environment macro references
 * If a user has chosen to keep those macros in the buildfile, the environment macro references
 * are converted to the buildfile variable references, otherwise those macros are also resolved
 * 
 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor
 * @since 3.0
 */
public class BuildfileMacroSubstitutor extends DefaultMacroSubstitutor {
	private static final String PATTERN_MACRO_NAME = "="; //$NON-NLS-1$
	private IConfiguration fConfiguration;
	private IBuilder fBuilder;
	private HashSet fCaseInsensitiveReferencedNames;
	
	private class DefaultReservedMacroNameSupplier implements IReservedMacroNameSupplier{
		String fReservedNames[];
		
		public DefaultReservedMacroNameSupplier(IConfiguration configuration){
			IBuilder builder = configuration.getToolChain().getBuilder();
			String reservedNames[] = builder.getReservedMacroNames();
			String buildVars[] = getConfigurationReservedNames(configuration);

			if(reservedNames == null || reservedNames.length == 0)
				fReservedNames = buildVars;
			else if(buildVars == null || buildVars.length == 0)
				fReservedNames = reservedNames;
			else {
				fReservedNames = new String[reservedNames.length + buildVars.length];
				System.arraycopy(reservedNames,0,fReservedNames,0,reservedNames.length);
				System.arraycopy(buildVars,0,fReservedNames,reservedNames.length,buildVars.length);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier#isReservedName(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration)
		 */
		public boolean isReservedName(String macroName, IConfiguration configuration) {
			if(fReservedNames != null && fReservedNames.length > 0){
				for(int i = 0; i < fReservedNames.length; i++){
					Pattern p = Pattern.compile(fReservedNames[i]);
					Matcher m = p.matcher(macroName);
					if(m.matches())
						return true;
				}
			}
			return false;
		}
		
		protected String[] getConfigurationReservedNames(IConfiguration configuration){
			ITool tools[] = configuration.getFilteredTools();
			if(tools != null){
				Set set = new HashSet();
				for(int i = 0; i < tools.length; i++){
					IOutputType ots[] = tools[i].getOutputTypes();
					if(ots != null){
						for(int j = 0; j < ots.length; j++){
							String varName = ots[j].getBuildVariable();
							if(varName != null){
								set.add(varName);
							}
						}
					}

					IInputType its[] = tools[i].getInputTypes();
					if(its != null){
						for(int j = 0; j < its.length; j++){
							String varName = its[j].getBuildVariable();
							if(varName != null){
								set.add(varName);
							}
						}
					}

				}
				
				return (String[])set.toArray(new String[set.size()]);
			}
			return null;
		}
	}
	
	public BuildfileMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
		super(contextType, contextData, inexistentMacroValue, listDelimiter);
		init();
	}

	public BuildfileMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
		super(contextInfo, inexistentMacroValue, listDelimiter);
		init();
	}
	
	private void init(){
		IMacroContextInfo contextInfo = getMacroContextInfo();
		if(contextInfo == null)
			return;
		
		int type = contextInfo.getContextType();
		switch(type){
		case IBuildMacroProvider.CONTEXT_FILE:
			contextInfo = contextInfo.getNext();
			if(contextInfo == null)
				break;
		case IBuildMacroProvider.CONTEXT_OPTION:
			contextInfo = contextInfo.getNext();
			if(contextInfo == null)
				break;
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:{
				Object contextData = contextInfo.getContextData();
				if(contextData instanceof IConfiguration)
					fConfiguration = (IConfiguration)contextData;
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:{
				Object contextData = contextInfo.getContextData();
				if(contextData instanceof IManagedProject){
					IResource rc = ((IManagedProject)contextData).getOwner();
					if(rc != null){
						IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(rc);
						fConfiguration = info.getDefaultConfiguration();
					}
				}
			}
			break;
		}
		if(fConfiguration != null){
			IToolChain toolChain = fConfiguration.getToolChain();
			if(toolChain != null)
				fBuilder = toolChain.getBuilder();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor#resolveMacro(org.eclipse.cdt.managedbuilder.macros.IBuildMacro)
	 */
	protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
		ResolvedMacro resolved = null;
			
		if(fConfiguration != null && fBuilder != null && 
				!UserDefinedMacroSupplier.getInstance().areMacrosExpanded(fConfiguration) && 
				macro instanceof EnvironmentMacroSupplier.EnvVarMacro &&
				!MacroResolver.isStringListMacro(macro.getMacroValueType())){
			String ref = getMacroReference(macro);
			if(ref != null)
				resolved = new ResolvedMacro(macro.getName(),ref);

		}
		if(resolved != null)
			return resolved;
		return super.resolveMacro(macro);
	}
	
	public IConfiguration getConfiguration(){
		return fConfiguration;
	}
	
	protected IReservedMacroNameSupplier getReservedMacroNameSupplier(){
		if(fBuilder == null)
			return null;
		IReservedMacroNameSupplier supplier = fBuilder.getReservedMacroNameSupplier();
		if(supplier == null)
			supplier = new DefaultReservedMacroNameSupplier(fConfiguration);
		
		return supplier;
	}
	
	protected String getMacroReference(IBuildMacro macro){
		String macroName = macro.getName();
		String ref = null;
		IReservedMacroNameSupplier supplier = getReservedMacroNameSupplier();
		//on win32 all environment variable names are converted to upper case
		macroName = EnvVarOperationProcessor.normalizeName(macroName);
		if(supplier == null || !supplier.isReservedName(macroName,fConfiguration)){
			String pattern = fBuilder.getBuilderVariablePattern();
			if(pattern != null && pattern.indexOf(PATTERN_MACRO_NAME) != -1){
				if(fBuilder.isVariableCaseSensitive() || getCaseInsensitiveReferencedNames().add(macroName.toUpperCase())){
					ref = pattern.replaceAll(PATTERN_MACRO_NAME,macroName);
				}
			}
		}
		return ref;
	}
	
	protected Set getCaseInsensitiveReferencedNames(){
		if(fCaseInsensitiveReferencedNames == null)
			fCaseInsensitiveReferencedNames = new HashSet();
		return fCaseInsensitiveReferencedNames;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor#setMacroContextInfo(org.eclipse.cdt.managedbuilder.internal.macros.IMacroContextInfo)
	 */
	public void setMacroContextInfo(IMacroContextInfo info)
				throws BuildMacroException{
		super.setMacroContextInfo(info);
		init();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#setMacroContextInfo(int, java.lang.Object)
	 */
	public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException{
		super.setMacroContextInfo(contextType, contextData);
		init();
	}

}
