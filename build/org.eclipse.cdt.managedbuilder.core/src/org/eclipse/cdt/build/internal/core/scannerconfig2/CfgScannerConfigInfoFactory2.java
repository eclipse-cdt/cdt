/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.internal.core.scannerconfig2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;


/**
 * New ScannerConfigInfoFactory
 * 
 * @author vhirsl
 */
public class CfgScannerConfigInfoFactory2 {
	private static final QualifiedName CONTAINER_INFO_PROPERTY = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "ScannerConfigBuilderInfo2Container"); //$NON-NLS-1$

	private static class ContainerInfo{
		ICProjectDescription fDes;
		IScannerConfigBuilderInfo2Set fContainer;
		
		ContainerInfo(ICProjectDescription des, IScannerConfigBuilderInfo2Set container){
			this.fDes = des;
			this.fContainer = container;
		}
	}
	private static class CfgInfo implements ICfgScannerConfigBuilderInfo2Set {
		private Configuration cfg;
		private IScannerConfigBuilderInfo2Set fContainer;
//		private HashMap map;
		
		CfgInfo(Configuration cfg){
			this.cfg = (Configuration)cfg;
//			init();
		}
		
		public CfgInfoContext[] getContexts() {
			Map map = createMap();
			return (CfgInfoContext[])map.keySet().toArray(new CfgInfoContext[map.size()]);
		}

		public IScannerConfigBuilderInfo2 getInfo(CfgInfoContext context) {
			return (IScannerConfigBuilderInfo2)createMap().get(context);
//			IScannerConfigBuilderInfo2 info = null;
//			if(!isPerRcTypeDiscovery()){
//				info = cfg.getScannerConfigInfo();
//				if(info == null){
//					info = ScannerConfigInfoFactory2.create(cfg, ManagedBuilderCorePlugin.getDefault().getPluginPreferences());
//				}
//			} else {
//				Tool tool = (Tool)context.getTool();
//				if(tool != null)
//					info = tool.getScannerConfigInfo(context.getInputType()); 
////				else
////					info = getDefaultInfo();
//			}
//			return info;
		}

		public boolean isPerRcTypeDiscovery() {
			return cfg.isPerRcTypeDiscovery();
		}
		
		private IScannerConfigBuilderInfo2Set getContainer() throws CoreException{
			if(fContainer == null){
				ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
				if(cfgDes != null){
					ICProjectDescription projDes = cfgDes.getProjectDescription();
					if(projDes != null){
						ContainerInfo cInfo = (ContainerInfo)projDes.getSessionProperty(CONTAINER_INFO_PROPERTY);
						if(cInfo != null && cInfo.fDes == projDes){
							fContainer = cInfo.fContainer;
						} else {
							fContainer = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(cfg.getOwner().getProject());
							cInfo = new ContainerInfo(projDes, fContainer);
							projDes.setSessionProperty(CONTAINER_INFO_PROPERTY, cInfo);
						}
					}
				}
				
				if(fContainer == null){
					fContainer = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(cfg.getOwner().getProject());
				}
			}
			return fContainer;
		}
		
		private Map createMap(){
			HashMap map = new HashMap();
			try{
				IScannerConfigBuilderInfo2Set container = getContainer();

				boolean isPerRcType = cfg.isPerRcTypeDiscovery();
				Map baseMap = container.getInfoMap();
				if(!isPerRcType){
					CfgInfoContext c = new CfgInfoContext(cfg);
					InfoContext baseContext = c.toInfoContext();
					IScannerConfigBuilderInfo2 info = container.getInfo(baseContext);
	
					if(info == null){
						String id = cfg.getDiscoveryProfileId();
						if(id != null)
							info = container.createInfo(baseContext, id);
						else
							info = container.createInfo(baseContext);
					}
					map.put(new CfgInfoContext(cfg), info);				
				} else {
					Map configMap = getConfigInfoMap(baseMap);
					
					IResourceInfo[] rcInfos = cfg.getResourceInfos();
					for(int i = 0; i < rcInfos.length; i++){
						ITool tools[];
						IResourceInfo rcInfo = rcInfos[i];
						if(rcInfo instanceof IFolderInfo) {
							tools = ((IFolderInfo)rcInfo).getFilteredTools();
						} else {
							tools = ((IFileInfo)rcInfo).getToolsToInvoke();
						}
						for(int k = 0; k < tools.length; k++){
							Tool tool = (Tool)tools[k];
							IInputType types[] = tool.getInputTypes();
							if(types.length != 0){
								for(int t = 0; t < types.length; t++){
									InputType type = (InputType)types[t];
									CfgInfoContext context = new CfgInfoContext(rcInfo, tool, type);
									IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2)configMap.remove(context);
									if(info == null &&  type.hasScannerConfigSettings()){
										InfoContext baseContext = context.toInfoContext();
										if(!type.isExtensionElement() && type.getSuperClass() != null){
											CfgInfoContext tmpCfgC = new CfgInfoContext(rcInfo, tool, type.getSuperClass());
											info = (IScannerConfigBuilderInfo2)configMap.get(tmpCfgC);
											if(info != null){
												info =  container.createInfo(baseContext, info);
											}
										}
										
										if(info == null){
											String id = type.getDiscoveryProfileId(tool);
											if(id != null){
												info = container.createInfo(baseContext, id);
											} else {
												info = container.createInfo(baseContext);
											}
										}
									}
									
									if(info != null){
										map.put(context, info);
									}
								}
							} else {
								CfgInfoContext context = new CfgInfoContext(rcInfo, tool, null);
								IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2)configMap.get(context);
								if(info == null && tool.hasScannerConfigSettings(null)){
									String id = tool.getDiscoveryProfileId();
									InfoContext baseContext = context.toInfoContext();
									if(id != null){
										info = container.createInfo(baseContext, id);
									} else {
										info = container.createInfo(baseContext);
									}
								}
								if(info != null){
									map.put(context, info);
								}
							}
						}
					}
					
					if(!configMap.isEmpty()){
						for(Iterator iter = configMap.entrySet().iterator(); iter.hasNext();){
							Map.Entry entry = (Map.Entry)iter.next();
							CfgInfoContext c = (CfgInfoContext)entry.getKey();
							if(c.getResourceInfo() != null || c.getTool() != null || c.getInputType() != null){
								InfoContext baseC = c.toInfoContext();
								if(!baseC.isDefaultContext())
									container.removeInfo(baseC);
							}
						}
					}
				}
			} catch (CoreException e){
				ManagedBuilderCorePlugin.log(e);
			}

			return map;
		}
		
		private Map getConfigInfoMap(Map baseMap){
			Map map = new HashMap();
			
			for(Iterator iter = baseMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				InfoContext baseContext = (InfoContext)entry.getKey();
				CfgInfoContext c = CfgInfoContext.fromInfoContext(cfg, baseContext);
				if(c != null){
					IScannerConfigBuilderInfo2 info = (IScannerConfigBuilderInfo2)entry.getValue();
					map.put(c, info);
				}
			}

			return map;
		}

		public Map getInfoMap() {
			return createMap();
		}

		public void setPerRcTypeDiscovery(boolean on) {
			cfg.setPerRcTypeDiscovery(on);
		}

		public IScannerConfigBuilderInfo2 applyInfo(CfgInfoContext context,
				IScannerConfigBuilderInfo2 base) {
			try {
				IScannerConfigBuilderInfo2 newInfo;
				IScannerConfigBuilderInfo2Set container = getContainer();
				InfoContext baseContext = context.toInfoContext();
				if(base != null){
					newInfo = container.createInfo(baseContext, base);
				} else {
					if(!baseContext.isDefaultContext())
						container.removeInfo(baseContext);
					newInfo = getInfo(context);
				}
				
				return newInfo;
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
			return null;
		}

		public IConfiguration getConfiguration() {
			return cfg;
		}
	}

	public static ICfgScannerConfigBuilderInfo2Set create(IConfiguration cfg){
		Configuration c = (Configuration)cfg;
		ICfgScannerConfigBuilderInfo2Set container = c.getCfgScannerConfigInfo();
		if(container == null){
			container = new CfgInfo(c);
			c.setCfgScannerConfigInfo(container);
		}
		return container;
	}
	
	public static void save(ICProjectDescription des, ICProjectDescription baseDescription) throws CoreException{
		ContainerInfo info = (ContainerInfo)des.getSessionProperty(CONTAINER_INFO_PROPERTY);
		if(info != null){
			if(info.fDes == baseDescription){
				IScannerConfigBuilderInfo2Set baseContainer = info.fContainer;
				baseContainer.save();
			}
			des.setSessionProperty(CONTAINER_INFO_PROPERTY, null);
		}
	}
}
