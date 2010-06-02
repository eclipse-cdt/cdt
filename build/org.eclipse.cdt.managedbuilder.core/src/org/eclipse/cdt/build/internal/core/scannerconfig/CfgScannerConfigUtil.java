/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.internal.core.scannerconfig;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.runtime.Assert;

import com.ibm.icu.text.MessageFormat;

public class CfgScannerConfigUtil {
	public static CfgInfoContext adjustPerRcTypeContext(CfgInfoContext context){
		if(((Configuration)context.getConfiguration()).isPreference())
			return context;
        Tool tool = (Tool)context.getTool();
        IResourceInfo rcInfo = context.getResourceInfo();
        IInputType inType = context.getInputType();
        boolean adjust = false;
        CfgInfoContext newContext = context;
        
   		if(tool != null){
   			if(inType != null){
        		if(!tool.hasScannerConfigSettings(inType)){
//	        			tool = null;
        			inType = null;
        			adjust = true;
        		}
   			}
   			if(inType == null){
        		if(!tool.hasScannerConfigSettings(null)){
        			tool = null;
        			adjust = true;
        		}
   			}
   		}
   		if(tool == null){
   			if(inType != null){
   				inType = null;
   				adjust = true;
   			}
    			
   			if(rcInfo != null){
    			ToolChain tc = getToolChain(rcInfo);
    					
    			if(tc != null){
    				if(!tc.hasScannerConfigSettings()){
    					adjust = true;
    					rcInfo = null;
    				}
    			}
   			}
   		}
//        } else {
//        	if(tool != null){
//        		tool = null;
//        		adjust = true;
//        	}
//        	if(rcInfo != null){
//        		rcInfo = null;
//        		adjust = true;
//        	}
//        	if(inType != null){
//        		inType = null;
//        		adjust = true;
//        	}
//        }
        
        if(adjust){
        	if(rcInfo == null)
        		newContext = new CfgInfoContext(context.getConfiguration());
        	else
        		newContext = new CfgInfoContext(rcInfo, tool, inType);
        }
        
        return newContext;
	}
	
	private static ToolChain getToolChain(IResourceInfo rcInfo){
		return rcInfo instanceof FolderInfo ? 
				(ToolChain)((FolderInfo)rcInfo).getToolChain()
				: (ToolChain)((ResourceConfiguration)rcInfo).getBaseToolChain();
	}
	
	public static String getDefaultProfileId(CfgInfoContext context, boolean searchFirstIfNone){
		String id = null;
		if(context.getInputType() != null)
			id = context.getInputType().getDiscoveryProfileId(context.getTool());
		if(id == null && context.getTool() != null)
			id = ((Tool)context.getTool()).getDiscoveryProfileId();
		if(id == null && context.getResourceInfo() != null){
			ToolChain tCh = getToolChain(context.getResourceInfo());
			if(tCh != null)
				id = tCh.getScannerConfigDiscoveryProfileId();
		}
		if(id == null){
			id = ((Configuration)context.getConfiguration()).getDiscoveryProfileId();
		}
		
		if(id == null && searchFirstIfNone){
			id = getFirstProfileId(context.getConfiguration().getFilteredTools());
		}
		return id;
	}
	
	public static String getFirstProfileId(ITool[] tools){
		String id = null;
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			IInputType[] types = tool.getInputTypes();
			
			if(types.length != 0){
				for(int k = 0; k < types.length; k++){
					id = types[k].getDiscoveryProfileId(tool);
					if(id != null)
						break;
				}
			} else {
				id = ((Tool)tool).getDiscoveryProfileId();
			}

			if(id != null)
				break;
		}
		
		return id;
	}

	/**
	 * Search for toolchain's discovery profiles. Discovery profiles could be
	 * specified on toolchain level, input types level or in their super-classes.
	 * 
	 * @param toolchain - toolchain to search for scanner discovery profiles.
	 * @return all available discovery profiles in given toolchain
	 */
	public static Set<String> getAllScannerDiscoveryProfileIds(IToolChain toolchain) {
		Assert.isNotNull(toolchain);
		
		Set<String> profiles = new TreeSet<String>();
		
		if (toolchain!=null) {
			String toolchainProfileId = toolchain.getScannerConfigDiscoveryProfileId();
			if (toolchainProfileId!=null && toolchainProfileId.length()>0) {
				profiles.add(toolchainProfileId);
			}
			ITool[] tools = toolchain.getTools();
			for (ITool tool : tools) {
				profiles.addAll(getAllScannerDiscoveryProfileIds(tool));
			}
			IToolChain superClass = toolchain.getSuperClass();
			if (superClass!=null) {
				profiles.addAll(getAllScannerDiscoveryProfileIds(superClass));
			}
		}
		
		return profiles;
	}
	
	/**
	 * Search for tool's discovery profiles. Discovery profiles could be retrieved
	 * from tool/input type super-class. Input type could hold list of profiles
	 * separated by pipe character '|'.
	 * 
	 * @param tool - tool to search for scanner discovery profiles
	 * @return all available discovery profiles in given configuration
	 */
	public static Set<String> getAllScannerDiscoveryProfileIds(ITool tool) {
		Assert.isNotNull(tool);

		if ( ! (tool instanceof Tool) ) {
			String msg = MessageFormat.format(ManagedMakeMessages.getString("CfgScannerConfigUtil_ErrorNotSupported"), //$NON-NLS-1$
					new String[] { Tool.class.getName() });
			throw new UnsupportedOperationException(msg);
		}
		
		Set<String> profiles = new TreeSet<String>();
		
		for (IInputType inputType : ((Tool) tool).getAllInputTypes()) {
			for (String profileId : getAllScannerDiscoveryProfileIds(inputType)) {
				profiles.add(profileId);
			}
		}
		
		ITool superClass = tool.getSuperClass();
		if (superClass!=null) {
			profiles.addAll(getAllScannerDiscoveryProfileIds(superClass));
		}
		return profiles;
	}
	
	/**
	 * Search for input type's discovery profiles. Discovery profiles could be specified
	 * on input type super-class. Input type could hold list of profiles
	 * separated by pipe character '|'.
	 * 
	 * @param inputType - input type to search for scanner discovery profiles
	 * @return all available discovery profiles in given configuration
	 */
	private static Set<String> getAllScannerDiscoveryProfileIds(IInputType inputType) {
		Assert.isNotNull(inputType);
		
		if ( ! (inputType instanceof InputType) ) {
			String msg = MessageFormat.format(ManagedMakeMessages.getString("CfgScannerConfigUtil_ErrorNotSupported"), //$NON-NLS-1$
					new String[] { InputType.class.getName() });
			throw new UnsupportedOperationException(msg);
		}
		
		Set<String> profiles = new TreeSet<String>();

		String attribute = ((InputType) inputType).getDiscoveryProfileIdAttribute();
		if (attribute!=null) {
			// FIXME: temporary; we should add new method to IInputType instead of that
			for (String profileId : attribute.split("\\|")) { //$NON-NLS-1$
				profiles.add(profileId);
			}
		}
		IInputType superClass = inputType.getSuperClass();
		if (superClass!=null) {
			profiles.addAll(getAllScannerDiscoveryProfileIds(superClass));
		}

		return profiles;
	}
}
