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
package org.eclipse.cdt.build.internal.core.scannerconfig2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgScannerConfigUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildConfigurationData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;

public class CfgScannerConfigInfoFactory2 {
	private static final QualifiedName CONTAINER_INFO_PROPERTY = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "ScannerConfigBuilderInfo2Container"); //$NON-NLS-1$

	private static class ContainerInfo{
		int fCode;
		IScannerConfigBuilderInfo2Set fContainer;
		
		ContainerInfo(ICProjectDescription des, IScannerConfigBuilderInfo2Set container){
			this.fCode = des.hashCode();
			this.fContainer = container;
		}
		
		public boolean matches(ICProjectDescription des){
			return des.hashCode() == fCode;
		}
	}
	private static class CfgInfo implements ICfgScannerConfigBuilderInfo2Set {
		private Configuration cfg;
		private IScannerConfigBuilderInfo2Set fContainer;
//		private HashMap map;
		
		CfgInfo(Configuration cfg){
			this.cfg = cfg;
//			init();
		}
		
		public CfgInfoContext[] getContexts() {
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> map = createMap();
			return map.keySet().toArray(new CfgInfoContext[map.size()]);
		}

		public IScannerConfigBuilderInfo2 getInfo(CfgInfoContext context) {
			return createMap().get(context);
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
				if(!cfg.isPreference()){
					ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
					if(cfgDes != null){
						ICProjectDescription projDes = cfgDes.getProjectDescription();
						if(projDes != null){
							ContainerInfo cInfo = (ContainerInfo)projDes.getSessionProperty(CONTAINER_INFO_PROPERTY);
							if(cInfo != null && cInfo.matches(projDes)){
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
				} else {
					Preferences prefs = MakeCorePlugin.getDefault().getPluginPreferences();
					fContainer = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(prefs, false);
				}
			}
			return fContainer;
		}
		
		private Map<CfgInfoContext, IScannerConfigBuilderInfo2> createMap(){
			HashMap<CfgInfoContext, IScannerConfigBuilderInfo2> map = new HashMap<CfgInfoContext, IScannerConfigBuilderInfo2>();
			try{
				IScannerConfigBuilderInfo2Set container = getContainer();

				boolean isPerRcType = cfg.isPerRcTypeDiscovery();
				Map<InfoContext, IScannerConfigBuilderInfo2> baseMap = container.getInfoMap();
				if(!isPerRcType){
					CfgInfoContext c = new CfgInfoContext(cfg);
					InfoContext baseContext = c.toInfoContext();
					IScannerConfigBuilderInfo2 info = container.getInfo(baseContext);
	
					if(info == null){
						String id = cfg.getDiscoveryProfileId();
						if(id == null)
							id = CfgScannerConfigUtil.getFirstProfileId(cfg.getFilteredTools());
						
						IScannerConfigBuilderInfo2 prefInfo = null;
						if(!cfg.isPreference()){
							IConfiguration prefCfg = ManagedBuildManager.getPreferenceConfiguration(false);
							ICfgScannerConfigBuilderInfo2Set prefContainer = create(prefCfg);
							prefInfo = prefContainer.getInfo(new CfgInfoContext(prefCfg));
						}
						if(prefInfo == null){
							if(id != null)
								info = container.createInfo(baseContext, id);
							else
								info = container.createInfo(baseContext);
						} else {
							if(id != null)
								info = container.createInfo(baseContext, prefInfo, id);
							else
								info = container.createInfo(baseContext, prefInfo, prefInfo.getSelectedProfileId());
						}
					}
					map.put(new CfgInfoContext(cfg), info);				
				} else {
					Map<CfgInfoContext, IScannerConfigBuilderInfo2> configMap = getConfigInfoMap(baseMap);
					
					IResourceInfo[] rcInfos = cfg.getResourceInfos();
					for (IResourceInfo rcInfo : rcInfos) {
						ITool tools[];
						if(rcInfo instanceof IFolderInfo) {
							tools = ((IFolderInfo)rcInfo).getFilteredTools();
						} else {
							tools = ((IFileInfo)rcInfo).getToolsToInvoke();
						}
						for (ITool tool : tools) {
							IInputType types[] = tool.getInputTypes();
							if(types.length != 0){
								for (IInputType inputType : types) {
									CfgInfoContext context = new CfgInfoContext(rcInfo, tool, inputType);
									context = CfgScannerConfigUtil.adjustPerRcTypeContext(context);
									if(context != null && context.getResourceInfo() != null){
										IScannerConfigBuilderInfo2 info = configMap.get(context);
										if(info == null && !inputType.isExtensionElement() && inputType.getSuperClass() != null){
											CfgInfoContext superContext = new CfgInfoContext(rcInfo, tool, inputType.getSuperClass());
											superContext = CfgScannerConfigUtil.adjustPerRcTypeContext(superContext);
											if(superContext != null && superContext.getResourceInfo() != null){
												info = configMap.get(superContext);
											}
											String id = CfgScannerConfigUtil.getDefaultProfileId(context, true);
											InfoContext baseContext = context.toInfoContext();
											if(info == null){
												if(id != null){
													info = container.createInfo(baseContext, id);
												} else {
													info = container.createInfo(baseContext);
												}
											} else {
												if(id != null){
													info = container.createInfo(baseContext, info, id);
												} else {
													info = container.createInfo(baseContext, info);
												}
											}
										}
										if(info != null){
											map.put(context, info);
										}
									}
								}
							} else {
								if(cfg.isPreference())
									continue;
								CfgInfoContext context = new CfgInfoContext(rcInfo, tool, null);
								context = CfgScannerConfigUtil.adjustPerRcTypeContext(context);
								if(context != null && context.getResourceInfo() != null){
									IScannerConfigBuilderInfo2 info = configMap.get(context);
									if(info == null){
										String id = CfgScannerConfigUtil.getDefaultProfileId(context, true);
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
					}
					
					if(!configMap.isEmpty()){
						for (Entry<CfgInfoContext, IScannerConfigBuilderInfo2> entry : configMap.entrySet()) {
							if(map.containsKey(entry.getKey()))
								continue;
							CfgInfoContext c = entry.getKey();
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
		
		private Map<CfgInfoContext, IScannerConfigBuilderInfo2> getConfigInfoMap(Map<InfoContext, IScannerConfigBuilderInfo2> baseMap){
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> map = new HashMap<CfgInfoContext, IScannerConfigBuilderInfo2>();
			
			for (Entry<InfoContext, IScannerConfigBuilderInfo2> entry : baseMap.entrySet()) {
				InfoContext baseContext = entry.getKey();
				CfgInfoContext c = CfgInfoContext.fromInfoContext(cfg, baseContext);
				if(c != null){
					IScannerConfigBuilderInfo2 info = entry.getValue();
					map.put(c, info);
				}
			}

			return map;
		}

		public Map<CfgInfoContext,IScannerConfigBuilderInfo2> getInfoMap() {
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

		public boolean isProfileSupported(CfgInfoContext context,
				String profileId) {
			if(!isPerRcTypeDiscovery())
				return true;

			return CfgScannerConfigProfileManager.isPerFileProfile(profileId);
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

	public static void save(BuildConfigurationData data, ICProjectDescription des, ICProjectDescription baseDescription, boolean force) throws CoreException{
		ContainerInfo info = (ContainerInfo)des.getSessionProperty(CONTAINER_INFO_PROPERTY);
		if(info != null){
			if(info.matches(baseDescription)){
				IScannerConfigBuilderInfo2Set baseContainer = info.fContainer;
				baseContainer.save();
			}
			des.setSessionProperty(CONTAINER_INFO_PROPERTY, null);
		} else if (force){
			Configuration cfg = (Configuration)data.getConfiguration();
			CfgInfo cfgInfo = new CfgInfo(cfg);
			cfg.setCfgScannerConfigInfo(cfgInfo);
			cfgInfo.getInfoMap();
			cfgInfo.fContainer.save();
			des.setSessionProperty(CONTAINER_INFO_PROPERTY, null);
		}
	}
	
	public static void savePreference(IConfiguration cfg) throws CoreException{
		ICfgScannerConfigBuilderInfo2Set container = ((Configuration)cfg).getCfgScannerConfigInfo();
		if(container != null){
			IScannerConfigBuilderInfo2Set baseContainer = ((CfgInfo)container).fContainer;
			if(baseContainer != null){
				baseContainer.save();
			}
		}
	}
}
