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
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.ui.dialogs.PropertyPage;

public abstract class AbstractBuildPropertyPage extends PropertyPage {

	private Map clonedConfigMap;
	
	private Map getClonedConfigMap(){
		if(clonedConfigMap == null)
			clonedConfigMap = new HashMap();
		return clonedConfigMap;
	}
	
	public IConfiguration getClonedConfig(IConfiguration config){
		IConfiguration clonedCfg = (IConfiguration)getClonedConfigMap().get(config.getId());
		if(clonedCfg == null){
			clonedCfg = new Configuration((ManagedProject)config.getManagedProject(), 
					(Configuration)config, 
					ManagedBuildManager.calculateChildId(config.getId(), null),
					true, 
					true);
			getClonedConfigMap().put(config.getId(),clonedCfg);
		}
		return clonedCfg;
	}
	
	public IConfiguration getRealConfig(IConfiguration config){
		Set set = getClonedConfigMap().entrySet();
		Iterator iter = set.iterator();
		while(iter.hasNext()){
			Map.Entry entry = (Map.Entry)iter.next();
			if(entry.getValue().equals(config))
				return config.getManagedProject().getConfiguration((String)entry.getKey());
		}
		return null;
	}
	
	public IToolChain getClonedToolChain(IToolChain toolChain){
		return getClonedConfig(toolChain.getParent()).getToolChain();
	}
	
	public IToolChain getRealToolChain(IToolChain toolChain){
		IConfiguration cfg = getRealConfig(toolChain.getParent());
		if(cfg != null)
			return cfg.getToolChain();
		return null;
	}
	
	public IHoldsOptions getClonedHoldsOptions(IHoldsOptions ho){
		if(ho instanceof IToolChain)
			return getClonedToolChain((IToolChain)ho);
		else if(ho instanceof ITool)
			return getClonedTool((ITool)ho);
		return null;
	}
	
	public IHoldsOptions getRealHoldsOptions(IHoldsOptions ho){
		if(ho instanceof IToolChain)
			return getRealToolChain((IToolChain)ho);
		else if(ho instanceof ITool)
			return getRealTool((ITool)ho);
		return null;
	}
	
	public IResourceConfiguration getClonedRcConfig(IResourceConfiguration rcCfg){
		return getClonedConfig(rcCfg.getParent()).getResourceConfiguration(rcCfg.getResourcePath());
	}
	
	public IResourceConfiguration getRealRcConfig(IResourceConfiguration rcCfg){
		IConfiguration cfg = getRealConfig(rcCfg.getParent());
		if(cfg != null)
			return cfg.getResourceConfiguration(rcCfg.getResourcePath());
		return null;
	}

	public ITool getClonedTool(ITool tool){
		IConfiguration cfg = getConfigurationFromTool(tool);
		if(cfg != null)
			return getToolForConfig(getClonedConfig(cfg),tool);
		return null;
	}
	
	public ITool getRealTool(ITool tool){
		IConfiguration cfg = getConfigurationFromTool(tool);
		if(cfg != null){
			cfg = getRealConfig(cfg);
			if(cfg != null)
				return getToolForConfig(cfg,tool);
		}
		return null;
	}
	
	protected ITool getToolForConfig(IConfiguration cfg, ITool tool){
			if(tool.getParent() instanceof IToolChain){
				ITool tools[] = cfg.getTools();
				for(int i = 0; i < tools.length; i++){
					if(tool.getSuperClass() != null){
						if(tools[i].getSuperClass() != null && tools[i].getSuperClass().getId().equals(
								tool.getSuperClass().getId()))
							return tools[i];
					}
					//TODO: shoud we handle this?
				}
			} else if (tool.getParent() instanceof IResourceConfiguration){
				IResourceConfiguration rcCfg = (IResourceConfiguration)tool.getParent();
				IResourceConfiguration otherRcCfg = cfg.getResourceConfiguration(rcCfg.getResourcePath());
				ITool tools[] = otherRcCfg.getTools();
				ITool superTool = tool.getSuperClass();
				if(superTool != null && (superTool = superTool.getSuperClass()) != null){
					for(int i = 0; i < tools.length; i++){
						ITool otherSuperTool = tools[i].getSuperClass();
						if(otherSuperTool != null 
								&& (otherSuperTool = otherSuperTool.getSuperClass()) != null
								&& otherSuperTool.getId().equals(superTool.getId()))
							return tools[i];
					}
				}
			}
		return null;
	}

	public IConfiguration getConfigurationFromTool(ITool tool){
		IBuildObject bo = tool.getParent();
		if(bo instanceof IToolChain)
			return ((IToolChain)bo).getParent();
		else if(bo instanceof IResourceConfiguration)
			return ((IResourceConfiguration)bo).getParent();
		return null;
	}
	
	public IConfiguration getConfigurationFromHoldsOptions(IHoldsOptions ho){
		if(ho instanceof IToolChain)
			return ((IToolChain)ho).getParent();
		else if(ho instanceof ITool)
			return getConfigurationFromTool((ITool)ho);
		return null;
	}
	
	public IOption getClonedOption(IOption option, IHoldsOptions ho){
		IHoldsOptions clonedHo = getClonedHoldsOptions(ho);
		if(clonedHo != null)
			return getOptionForHoldsOptions(clonedHo,option);
		return null;
	}
	
	public IOption getRealOption(IOption option, IHoldsOptions ho){
		IHoldsOptions realHo = getRealHoldsOptions(ho);
		if(realHo != null)
			return getOptionForHoldsOptions(realHo,option);
		return null;
	}
	
	protected IOption getOptionForHoldsOptions(IHoldsOptions ho, IOption otherOption){
		IOption opt = null;
		if(otherOption.isExtensionElement())
			opt = ho.getOptionBySuperClassId(otherOption.getId());
		else if(otherOption.getSuperClass() != null)
			opt = ho.getOptionBySuperClassId(otherOption.getSuperClass().getId());
		if(opt != null)
			return opt;
		return otherOption;
	}
	
	public IOptionCategory getClonedOptionCategory(IOptionCategory optCategory){
		if(optCategory instanceof Tool)
			return (Tool)getClonedTool((Tool)optCategory);
		return optCategory;
	}
	
	public IOptionCategory getRealOptionCategory(IOptionCategory optCategory){
		if(optCategory instanceof Tool)
			return (Tool)getRealTool((Tool)optCategory);
		return optCategory;
	}
	
/*	protected String calculateId(String id){
		String version = ManagedBuildManager.getVersionFromIdAndVersion(id);
		int n = ManagedBuildManager.getRandomNumber();
		if ( version != null)		// If the 'id' contains version information
			return ManagedBuildManager.getIdFromIdAndVersion(id) + "." + n + "_" + version;		//$NON-NLS-1$ //$NON-NLS-2$
		return id + "." + n;		//$NON-NLS-1$
	}
*/
}
