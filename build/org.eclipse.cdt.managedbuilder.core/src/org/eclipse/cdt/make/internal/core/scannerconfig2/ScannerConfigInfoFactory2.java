/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.make.core.scannerconfig.IConfigurationScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.ScannerInfoProvider;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;

/**
 * New ScannerConfigInfoFactory
 * 
 * @author vhirsl
 */
public class ScannerConfigInfoFactory2 {
	// build properties	
	private static final String SCANNER_CONFIG = "scannerConfiguration"; //$NON-NLS-1$
	private static final String SC_AUTODISCOVERY = "autodiscovery"; //$NON-NLS-1$
	private static final String ENABLED = "enabled"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE_ID = "selectedProfileId"; //$NON-NLS-1$
	private static final String PROBLEM_REPORTING_ENABLED = "problemReportingEnabled"; //$NON-NLS-1$
	private static final String PROFILE = "profile"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String BUILD_OUTPUT_PROVIDER = "buildOutputProvider"; //$NON-NLS-1$
	private static final String OPEN_ACTION = "openAction"; //$NON-NLS-1$
	private static final String FILE_PATH = "filePath"; //$NON-NLS-1$
	private static final String PARSER = "parser"; //$NON-NLS-1$
	private static final String SCANNER_INFO_PROVIDER = "scannerInfoProvider"; //$NON-NLS-1$
	private static final String RUN_ACTION = "runAction"; //$NON-NLS-1$
	private static final String USE_DEFAULT = "useDefault"; //$NON-NLS-1$
	private static final String COMMAND = "command"; //$NON-NLS-1$
	private static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
	// preferences
	private static final String DOT = ".";//$NON-NLS-1$
    private static final String SCD = "SCD.";//$NON-NLS-1$
	private static final String SCANNER_CONFIG_AUTODISCOVERY_ENABLED = "SCD.enabled";//$NON-NLS-1$
	private static final String SCANNER_CONFIG_SELECTED_PROFILE_ID = "SCD.selectedProfileId";//$NON-NLS-1$
    private static final String SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED = "SCD.problemReportingEnabled"; //$NON-NLS-1$
//	 following require prefix: profileId
	private static final String BUILD_OUTPUT_OPEN_ACTION_ENABLED = ".BOP.open.enabled";//$NON-NLS-1$
	private static final String BUILD_OUTPUT_OPEN_ACTION_FILE_PATH = ".BOP.open.path";//$NON-NLS-1$
	private static final String BUILD_OUTPUT_PARSER_ENABLED = ".BOP.parser.enabled";//$NON-NLS-1$
	// following require prefix: profileId + "." + SCANNER_INFO_PROVIDER + "." + providerId
	private static final String SI_PROVIDER_RUN_ACTION_USE_DEFAULT = ".run.useDefault";//$NON-NLS-1$
	private static final String SI_PROVIDER_RUN_ACTION_COMMAND = ".run.command";//$NON-NLS-1$
	private static final String SI_PROVIDER_RUN_ACTION_ARGUMENTS = ".run.arguments";//$NON-NLS-1$
	private static final String SI_PROVIDER_OPEN_ACTION_FILE_PATH = ".open.path";//$NON-NLS-1$
	private static final String SI_PROVIDER_PARSER_ENABLED = ".parser.enabled";//$NON-NLS-1$
	
	private static class CfgInfo implements IConfigurationScannerConfigBuilderInfo {
		private Configuration cfg;
//		private HashMap map;
		
		CfgInfo(Configuration cfg){
			this.cfg = (Configuration)cfg;
//			init();
		}
		
		public InfoContext[] getContexts() {
			Map map = createMap();
			return (InfoContext[])map.keySet().toArray(new InfoContext[map.size()]);
		}

		public IScannerConfigBuilderInfo2 getInfo(InfoContext context) {
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
		
		private Map createMap(){
			HashMap map = new HashMap();
			boolean isPerRcType = cfg.isPerRcTypeDiscovery();
			if(!isPerRcType){
				IScannerConfigBuilderInfo2 info = cfg.getScannerConfigInfo();
				if(info == null){
					String id = cfg.getDiscoveryProfileId();
					if(id == null)
						id = ScannerConfigProfileManager.NULL_PROFILE_ID;
					info = create(cfg, ManagedBuilderCorePlugin.getDefault().getPluginPreferences(), id);
					cfg.setScannerConfigInfo(info);
				}
				map.put(new InfoContext(cfg), info);
			} else {
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
								IInputType type = types[t];
								IScannerConfigBuilderInfo2 tInfo = tool.getScannerConfigInfo(type);
								if(tInfo != null){
									map.put(new InfoContext(rcInfo, tool, type), tInfo);
								}
							}
						} else {
							IScannerConfigBuilderInfo2 tInfo = tool.getScannerConfigInfo(null);
							if(tInfo != null){
								map.put(new InfoContext(rcInfo, tool, null), tInfo);
							}
						}
					}
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

		public IScannerConfigBuilderInfo2 getDefaultInfo() {
			IScannerConfigBuilderInfo2 info = cfg.getScannerConfigInfo();
			if(info == null){
				Map map = createMap();
				if(map.size() != 0){
					info = (IScannerConfigBuilderInfo2)map.values().iterator().next();
				} else {
					info = ScannerConfigInfoFactory2.create(cfg, ManagedBuilderCorePlugin.getDefault().getPluginPreferences(), cfg.getDiscoveryProfileId());
				}
			}
			return info;
		}

		public IScannerConfigBuilderInfo2 applyInfo(InfoContext context,
				IScannerConfigBuilderInfo2 base) {
			IScannerConfigBuilderInfo2 newInfo;
			if(base != null){
				newInfo = create(context, base, ScannerConfigProfileManager.NULL_PROFILE_ID);
				setInfo(context, newInfo);
			} else {
				setInfo(context, null);
				newInfo = getInfo(context);
			}
			
			return newInfo;
		}
		
		private void setInfo(InfoContext context, IScannerConfigBuilderInfo2 info) {
			if(!isPerRcTypeDiscovery()){
				cfg.setScannerConfigInfo(info);
			} else {
				Tool tool = (Tool)context.getTool();
				if(tool != null)
					info = tool.setScannerConfigInfo(context.getInputType(), info); 
//				else
//					info = getDefaultInfo();
			}
		}

	}

	private static abstract class Store implements IScannerConfigBuilderInfo2, Cloneable {
		protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
        protected boolean isDirty;	// derived

		protected boolean autoDiscoveryEnabled;
		protected boolean problemReportingEnabled;
		protected String selectedProfile = EMPTY_STRING;
        protected LinkedHashMap profileOptionsMap; // (profileId, options)
        static class ProfileOptions implements Cloneable{
    		protected boolean buildOutputFileActionEnabled;
    		protected String buildOutputFilePath = EMPTY_STRING;
    		protected boolean buildOutputParserEnabled;
    		protected LinkedHashMap providerOptionsMap; // {providerId, options}
    		static class ProviderOptions implements Cloneable{
    			protected String providerKind;	// derived
    			protected boolean providerOutputParserEnabled;
    			protected boolean providerRunUseDefault;
    			protected String providerRunCommand = EMPTY_STRING;
    			protected String providerRunArguments = EMPTY_STRING;
    			protected String providerOpenFilePath = EMPTY_STRING;
				
    			public Object clone(){
					try {
						return super.clone();
					} catch (CloneNotSupportedException e) {
						return null;
					}
				}
    			
    			
    		}

    		protected Object clone(){
				try {
					ProfileOptions clone = (ProfileOptions) super.clone();
					clone.providerOptionsMap = (LinkedHashMap)providerOptionsMap.clone();
					for(Iterator iter = clone.providerOptionsMap.entrySet().iterator(); iter.hasNext();){
						Map.Entry entry = (Map.Entry)iter.next();
						ProviderOptions o = (ProviderOptions)entry.getValue();
						o = (ProviderOptions)o.clone();
						entry.setValue(o);
					}
					return clone;
				} catch (CloneNotSupportedException e) {
					return null;
				}
			}
        }
        
        protected void copySettingsTo(Store clone){
            clone.isDirty = isDirty;

    		clone.autoDiscoveryEnabled = autoDiscoveryEnabled;
    		clone.problemReportingEnabled  = problemReportingEnabled;
    		clone.selectedProfile = selectedProfile;
			clone.profileOptionsMap = (LinkedHashMap)profileOptionsMap.clone();
			for(Iterator iter = clone.profileOptionsMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				ProfileOptions o = (ProfileOptions)entry.getValue();
				o = (ProfileOptions)o.clone();
				entry.setValue(o);
			}
        }
        
		protected Object clone() {
			try {
				Store clone =  (Store)super.clone();
				copySettingsTo(clone);
				return clone;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		protected Store() {
			isDirty = false;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isAutoDiscoveryEnabled()
		 */
		public boolean isAutoDiscoveryEnabled() {
			return autoDiscoveryEnabled;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setAutoDiscoveryEnabled(boolean)
		 */
		public void setAutoDiscoveryEnabled(boolean enable) {
            autoDiscoveryEnabled = setDirty(autoDiscoveryEnabled, enable);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isSIProblemGenerationEnabled()
		 */
		public boolean isProblemReportingEnabled() {
			return problemReportingEnabled;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setSIProblemGenerationEnabled(boolean)
		 */
		public void setProblemReportingEnabled(boolean enable) {
            problemReportingEnabled = setDirty(problemReportingEnabled, enable);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getSelectedProfileId()
		 */
		public String getSelectedProfileId() {
			return selectedProfile;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setSelectedProfileId(java.lang.String)
		 */
		public void setSelectedProfileId(String profileId) {
            selectedProfile = setDirty(selectedProfile, profileId);
//			if (isDirty) {
//				try {
//					load();
//					isDirty = false;
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProfileIdList()
         */
        public List getProfileIdList() {
            return new ArrayList(profileOptionsMap.keySet());
        }

        /* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isBuildOutputFileActionEnabled()
		 */
		public boolean isBuildOutputFileActionEnabled() {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            return (po != null) ? po.buildOutputFileActionEnabled : false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputFileActionEnabled(boolean)
		 */
		public void setBuildOutputFileActionEnabled(boolean enable) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputFileActionEnabled = setDirty(po.buildOutputFileActionEnabled, enable);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getBuildOutputFilePath()
		 */
		public String getBuildOutputFilePath() {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
			return (po != null) ? po.buildOutputFilePath : EMPTY_STRING;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputFilePath(java.lang.String)
		 */
		public void setBuildOutputFilePath(String path) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputFilePath = setDirty(po.buildOutputFilePath, path);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isBuildOutputParserEnabled()
		 */
		public boolean isBuildOutputParserEnabled() {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
			return (po != null) ? po.buildOutputParserEnabled : true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputParserEnabled(boolean)
		 */
		public void setBuildOutputParserEnabled(boolean enable) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputParserEnabled = setDirty(po.buildOutputParserEnabled, enable);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getSIProviderIdList()
		 */
		public List getProviderIdList() {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            return (po != null) ? new ArrayList(po.providerOptionsMap.keySet()) : new ArrayList(0);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#addSIProvider(java.lang.String)
		 */
//		public void addSIProvider(String providerId) {
//			providerOptionsMap.put(providerId, new ProviderOptions());
//		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#removeSIProvider(java.lang.String)
		 */
//		public void removeSIProvider(String providerId) {
//			providerOptionsMap.put(providerId, null);
//		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isSIProviderOutputParserEnabled(java.lang.String)
		 */
		public boolean isProviderOutputParserEnabled(String providerId) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
    			ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			return (ppo == null) ? false : ppo.providerOutputParserEnabled;
            }
            return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setSIProviderOutputParserEnabled(java.lang.String, boolean)
		 */
		public void setProviderOutputParserEnabled(String providerId, boolean enable) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
    			ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerOutputParserEnabled = setDirty(ppo.providerOutputParserEnabled, enable);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isUseDefaultProviderCommand(java.lang.String)
		 */
		public boolean isUseDefaultProviderCommand(String providerId) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
                return (ppo == null) ? false : ppo.providerRunUseDefault;
            }
            return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setUseDefaultProviderCommand(java.lang.String, boolean)
		 */
		public void setUseDefaultProviderCommand(String providerId, boolean enable) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunUseDefault = setDirty(ppo.providerRunUseDefault, enable);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderRunCommand(java.lang.String)
		 */
		public String getProviderRunCommand(String providerId) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerRunCommand;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderRunCommand(java.lang.String, java.lang.String)
		 */
		public void setProviderRunCommand(String providerId, String command) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunCommand = setDirty(ppo.providerRunCommand, command);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderRunArguments(java.lang.String)
		 */
		public String getProviderRunArguments(String providerId) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerRunArguments;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderRunArguments(java.lang.String, java.lang.String)
		 */
		public void setProviderRunArguments(String providerId, String arguments) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunArguments = setDirty(ppo.providerRunArguments, arguments);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderOpenFilePath(java.lang.String)
		 */
		public String getProviderOpenFilePath(String providerId) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerOpenFilePath;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderOpenFilePath(java.lang.String, java.lang.String)
		 */
		public void setProviderOpenFilePath(String providerId, String filePath) {
            ProfileOptions po = (ProfileOptions) profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerOpenFilePath = setDirty(ppo.providerOpenFilePath, filePath);
    			}
            }
		}
		
		private boolean setDirty(boolean l, boolean r) {
			isDirty = isDirty || (l != r);
			return r;
		}
		private String setDirty(String l, String r) {
			isDirty = isDirty || !l.equals(r);
			return r;
		}
        
		protected abstract void load(ICStorageElement el, String profileId);
//		public abstract void store();

        /**
         * Populate buildInfo based on profile configuration
         */
        protected void loadFromProfileConfiguration(ProfileOptions po, String profileId) {
            ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
                    getSCProfileConfiguration(profileId);
            List providerIds = configuredProfile.getSIProviderIds();
            
            po.buildOutputParserEnabled  = false;
            po.buildOutputFileActionEnabled = false;
            po.buildOutputFilePath = EMPTY_STRING;
            if (configuredProfile.getBuildOutputProviderElement() != null) {
                po.buildOutputParserEnabled  = true;
                if (configuredProfile.getBuildOutputProviderElement().getAction() != null) {
                    po.buildOutputFileActionEnabled = true;
                    String buildOutputFilePath = configuredProfile.getBuildOutputProviderElement().
                            getAction().getAttribute("file");//$NON-NLS-1$
                    po.buildOutputFilePath = (buildOutputFilePath != null) ? buildOutputFilePath : EMPTY_STRING;  
                }
            }
            po.providerOptionsMap = new LinkedHashMap(providerIds.size());
            for (int i = 0; i < providerIds.size(); ++i) {
                ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
                String providerId = (String) providerIds.get(i);
                po.providerOptionsMap.put(providerId, ppo);
                
                ppo.providerOutputParserEnabled = (configuredProfile.getScannerInfoProviderElement(providerId) == null) ? false : true;
                ppo.providerKind = configuredProfile.getScannerInfoProviderElement(providerId).getProviderKind();
                String attrValue;
                if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
                    attrValue = configuredProfile.getScannerInfoProviderElement(providerId).
                            getAction().getAttribute(COMMAND);
                    ppo.providerRunCommand = (attrValue != null) ? attrValue : EMPTY_STRING; 
                    attrValue = configuredProfile.getScannerInfoProviderElement(providerId).
                            getAction().getAttribute(ARGUMENTS);
                    ppo.providerRunArguments = (attrValue != null) ? attrValue : EMPTY_STRING;
                    
                    ppo.providerRunUseDefault = true;
                }
                else if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
                    attrValue = configuredProfile.getScannerInfoProviderElement(providerId).
                            getAction().getAttribute("file");//$NON-NLS-1$
                    ppo.providerOpenFilePath = (attrValue != null) ? attrValue : EMPTY_STRING;
                }
            }
        }

	}

	/**
	 * Build properties stored in .cdtproject file
	 * 
	 * @author vhirsl
	 */
	public static class BuildProperty extends Store implements Cloneable{
//		private IProject project;
		private InfoContext context;
//		private IResourceInfo rcInfo;
//		private ITool tool;
//		private InputType inType;
        private String profileId;

//		private BuildProperty(IProject project, String profileId) {
//			super();
//			this.project = project;
//            this.profileId = profileId;
//			load();
//		}
		
		private BuildProperty(InfoContext context, ICStorageElement el, String profileId) {
			super();

			this.context = context;
			
			load(el, profileId);
			// TODO Auto-generated constructor stub
		}
		
		private BuildProperty(InfoContext context, Store store){
			super();

			this.context = context;

			store.copySettingsTo(this);
		}
		
		public boolean isDirty(){
			return isDirty;
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigInfoFactory2.Store#load()
         */
        protected void load(ICStorageElement el, String profileId) {
			
            List profileIds = ScannerConfigProfileManager.getInstance().getProfileIds();
            List loadedProfiles = new ArrayList();
//			try {
//				descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
				ICStorageElement children[] = el.getChildren();
				for (int i = 0; i < children.length; i++) {
					ICStorageElement child = children[i];
                    if (child.getName().equals(SC_AUTODISCOVERY)) {
                        autoDiscoveryEnabled = Boolean.valueOf(
                                child.getAttribute(ENABLED)).booleanValue();
                        selectedProfile = (profileId.equals(ScannerConfigProfileManager.NULL_PROFILE_ID)) 
                                ? child.getAttribute(SELECTED_PROFILE_ID)
                                : profileId;
                        problemReportingEnabled = Boolean.valueOf(
                        		child.getAttribute(PROBLEM_REPORTING_ENABLED)).booleanValue();
                    }
                    else if (child.getName().equals(PROFILE)) {
						if (profileIds.contains(child.getAttribute(ID))) {
							loadProfile(child);
                            loadedProfiles.add(child.getAttribute(ID));
						}
					}
				}
				if (loadedProfiles.size() < 1) {
                    // No ScannerConfigDiscovery entry, try old project location - .project
					if (migrateScannerConfigBuildInfo(ScannerConfigProfileManager.PER_PROJECT_PROFILE_ID)) {
                        loadedProfiles.add(ScannerConfigProfileManager.PER_PROJECT_PROFILE_ID);
					}
                    else {
                        // disable autodiscovery
                        autoDiscoveryEnabled = false;
                    }
				}
                if (loadedProfiles.size() < profileIds.size()) {
                    // initialize remaining profiles with default values
                    for (Iterator i = profileIds.iterator(); i.hasNext(); ) {
                        /*String */profileId = (String) i.next();
                        if (!loadedProfiles.contains(profileId)) {
                            loadDefaults(profileId);
                            loadedProfiles.add(profileId);
                        }
                    }
//                    // store migrated data
//                    isDirty = true;
//                    store();
//                    save();
                }
//			} catch (CoreException e) {
//				ManagedBuilderCorePlugin.log(e);
//			}
		}

		/**
         * Load profile defaults
         * @param profileId
         */
        private void loadDefaults(String profileId) {
            ProfileOptions po = new ProfileOptions();
            po.buildOutputFileActionEnabled = false;
            po.buildOutputParserEnabled = true;
            
            ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
                    getSCProfileConfiguration(profileId);

            po.providerOptionsMap = new LinkedHashMap();
            for (Iterator i = configuredProfile.getSIProviderIds().iterator(); i.hasNext(); ) {
                String providerId = (String) i.next();
                ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
                ScannerInfoProvider configuredProvider = configuredProfile.
                        getScannerInfoProviderElement(providerId);
                ppo.providerKind = configuredProvider.getProviderKind();
                ppo.providerOutputParserEnabled = false;
                if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
                    ppo.providerRunUseDefault = true;
                    ppo.providerRunCommand = configuredProvider.getAction().getAttribute(COMMAND);
                    ppo.providerRunArguments = configuredProvider.getAction().getAttribute(ARGUMENTS);
                }
                else if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
                    ppo.providerOpenFilePath = configuredProvider.getAction().getAttribute("file");//$NON-NLS-1$ 
                }
                
                po.providerOptionsMap.put(providerId, ppo);
            }
            
            if (profileOptionsMap == null) {
                profileOptionsMap = new LinkedHashMap();
            }
            profileOptionsMap.put(profileId, po);
        }

        /**
		 * @param profileId
		 */
		private boolean migrateScannerConfigBuildInfo(String profileId) {
			boolean rc = true;
			try {
				IScannerConfigBuilderInfo oldInfo = ManagedBuilderCorePlugin.
						createScannerConfigBuildInfo(context.getConfiguration().getOwner().getProject(), ScannerConfigBuilder.BUILDER_ID);
				autoDiscoveryEnabled = oldInfo.isAutoDiscoveryEnabled();
				problemReportingEnabled = oldInfo.isSIProblemGenerationEnabled();
				// effectively a PerProject profile
				selectedProfile = profileId;
                
                ProfileOptions po = new ProfileOptions();
				po.buildOutputFileActionEnabled = false;
				po.buildOutputParserEnabled = oldInfo.isMakeBuilderConsoleParserEnabled();
				
				ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
                ppo.providerKind = ScannerConfigProfile.ScannerInfoProvider.RUN;
				ppo.providerOutputParserEnabled = oldInfo.isESIProviderCommandEnabled();
				ppo.providerRunUseDefault = oldInfo.isDefaultESIProviderCmd();
				ppo.providerRunCommand = oldInfo.getESIProviderCommand().toString();
				ppo.providerRunArguments = oldInfo.getESIProviderArguments();

				ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
						getSCProfileConfiguration(selectedProfile);
				// get the one and only provider id
				String providerId = (String) configuredProfile.getSIProviderIds().get(0);
                po.providerOptionsMap = new LinkedHashMap(1);
				po.providerOptionsMap.put(providerId, ppo);
                
                profileOptionsMap = new LinkedHashMap(1);
                profileOptionsMap.put(profileId, po);
                
                // store migrated data
                isDirty = true;
                save();
			} 
			catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
				rc = false;
			}
			return rc;
		}

		/**
		 * @param profile
		 */
		private void loadProfile(ICStorageElement profile) {
            if (profileOptionsMap == null) {
                profileOptionsMap = new LinkedHashMap(1);
            }
            ProfileOptions po = new ProfileOptions();
            String profileId = profile.getAttribute(ID);
            profileOptionsMap.put(profileId, po);
            // get the list of providers from the profile configuration
            ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
                    getSCProfileConfiguration(profileId);
            List providerIds = configuredProfile.getSIProviderIds();
            int providerCounter = 0;
            po.providerOptionsMap = new LinkedHashMap(providerIds.size());

            ICStorageElement children[] = profile.getChildren();
			for (int i = 0; i < children.length; i++) {
				ICStorageElement child = children[i];
    			// buildOutputProvider element
    			if (BUILD_OUTPUT_PROVIDER.equals(child.getName())) {
    				ICStorageElement grandChildren[] = child.getChildren();
    				for (int k = 0; k < grandChildren.length; k++) {
                        ICStorageElement grandChild = grandChildren[k];
        				if (OPEN_ACTION.equals(grandChild.getName())) {
        					po.buildOutputFileActionEnabled = Boolean.valueOf(
                                    grandChild.getAttribute(ENABLED)).booleanValue();
        					po.buildOutputFilePath = grandChild.getAttribute(FILE_PATH);
        				}
                        else if (PARSER.equals(grandChild.getName())) {
        					po.buildOutputParserEnabled = Boolean.valueOf(
                                    grandChild.getAttribute(ENABLED)).booleanValue();
        				}
                    }
    			}
                else if (SCANNER_INFO_PROVIDER.equals(child.getName())) {
					String providerId = child.getAttribute(ID);
					if (providerIds.get(providerCounter).equals(providerId)) {
						// new provider
						ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
						po.providerOptionsMap.put(providerId, ppo);
                        ppo.providerKind = configuredProfile.getScannerInfoProviderElement(
                              providerId).getProviderKind();
						
                        ICStorageElement granChildren[] = child.getChildren();
                        for (int k = 0; k < granChildren.length; k++) {
                        	ICStorageElement grandChild = granChildren[k];
                            // action
    						if (RUN_ACTION.equals(grandChild.getName())) {
    							ppo.providerRunUseDefault = Boolean.valueOf(
                                        grandChild.getAttribute(USE_DEFAULT)).booleanValue(); 
    							ppo.providerRunCommand = grandChild.getAttribute(COMMAND);
    							ppo.providerRunArguments = grandChild.getAttribute(ARGUMENTS);
    						}
    						else if (OPEN_ACTION.equals(grandChild.getName())) {
    							ppo.providerOpenFilePath = grandChild.getAttribute(FILE_PATH);
    						}
                            // parser
                            else if (PARSER.equals(grandChild.getName())) {
                                ppo.providerOutputParserEnabled = Boolean.valueOf(
                                        grandChild.getAttribute(ENABLED)).booleanValue();
                            }
                        }
                        ++providerCounter;
					}
					else {
    						// mismatch - error
    						// TODO Vmir define error
    				}
                }
            }
		}

		private boolean store(ICStorageElement el) {
//			if (isDirty) {
//				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
//				Element sc = descriptor.getProjectData(SCANNER_CONFIG);
//                Document doc = sc.getOwnerDocument();

                // Clear out all current children
//                Node child = sc.getFirstChild();
//                while (child != null) {
//                    sc.removeChild(child);
//                    child = sc.getFirstChild();
//                }

                ICStorageElement autod = el.createChild(SC_AUTODISCOVERY);
				autod.setAttribute(ENABLED, Boolean.toString(autoDiscoveryEnabled));
				autod.setAttribute(SELECTED_PROFILE_ID, selectedProfile);
				autod.setAttribute(PROBLEM_REPORTING_ENABLED, Boolean.toString(problemReportingEnabled));

				for (Iterator i = profileOptionsMap.keySet().iterator(); i.hasNext();) {
                    String profileId = (String) i.next();
                    ICStorageElement profile = el.createChild(PROFILE);
					profile.setAttribute(ID, profileId);
					store(profile, (ProfileOptions) profileOptionsMap.get(profileId));
                }
				
				isDirty = false;
                return true;
//			}
//            return false;
		}

		/**
		 * @param profile element
		 * @param profile options 
		 */
		private void store(ICStorageElement el, ProfileOptions po) {
			ICStorageElement child, grandchild;
			child = el.createChild(BUILD_OUTPUT_PROVIDER);
			grandchild = child.createChild(OPEN_ACTION);
			grandchild.setAttribute(ENABLED, Boolean.toString(po.buildOutputFileActionEnabled));
			grandchild.setAttribute(FILE_PATH, po.buildOutputFilePath);
			grandchild = child.createChild(PARSER);
			grandchild.setAttribute(ENABLED, Boolean.toString(po.buildOutputParserEnabled));
			// scannerInfoProvider elements
			// get the list of providers from the profile configuration
//			ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
//					getSCProfileConfiguration(selectedProfile);
//			List providerIds = configuredProfile.getSIProviderIds();
            List providerIds = new ArrayList(po.providerOptionsMap.keySet());
			for (int i = 0; i < providerIds.size(); ++i) {
				String providerId = (String) providerIds.get(i);
				ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) po.providerOptionsMap.get(providerId);
				
				if (ppo != null) {
					child = el.createChild(SCANNER_INFO_PROVIDER);
					child.setAttribute(ID, providerId);
					
					// action
//                    String providerKind = configuredProfile.getScannerInfoProviderElement(
//							providerId).getProviderKind();
                    String providerKind = ppo.providerKind;
                    
					if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
						grandchild = child.createChild(RUN_ACTION); 
						grandchild.setAttribute(USE_DEFAULT, Boolean.toString(ppo.providerRunUseDefault)); 
						grandchild.setAttribute(COMMAND, ppo.providerRunCommand);
						grandchild.setAttribute(ARGUMENTS, ppo.providerRunArguments);
					}
					else if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
						grandchild = child.createChild(OPEN_ACTION); 
						grandchild.setAttribute(FILE_PATH, ppo.providerOpenFilePath);
					}
					// parser
                    grandchild = child.createChild(PARSER);
                    grandchild.setAttribute(ENABLED, Boolean.toString(ppo.providerOutputParserEnabled));
				}
				else {
					// missing provider options - error
					// TODO Vmir define error
				}
			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#save()
         */
        public void save() throws CoreException {
        	Tool tool = (Tool)context.getTool();
        	if(tool != null){
        		tool.setScannerConfigInfo(context.getInputType(), this);
        	} else {
        		Configuration cfg = (Configuration)context.getConfiguration();
        		cfg.setScannerConfigInfo(this);
        	}
//            if (store()) {
//                ICDescriptorOperation op = new ICDescriptorOperation() {
//                    
//                     public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
//                         descriptor.saveProjectData();
//                     }
//                      
//                 };
//                 CCorePlugin.getDefault().getCDescriptorManager().
//                         runDescriptorOperation(project, op, null);
//            }
        }
	}

	/**
	 * Preferences
	 * 
	 * @author vhirsl
	 */
	private static class Preference extends Store implements Cloneable {
		private Preferences prefs;
		private String profileId;
		private boolean useDefaults;

		/**
		 * @param prefs
		 * @param profileId
		 * @param useDefaults
		 */
		public Preference(Preferences prefs, String profileId, boolean useDefaults) {
			super();
			this.prefs = prefs;
			this.profileId = profileId;
			this.useDefaults = useDefaults;
			load(null, profileId);
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigInfoFactory2.Store#load()
         */
        protected void load(ICStorageElement el, String profileId) {
			autoDiscoveryEnabled = getBoolean(SCANNER_CONFIG_AUTODISCOVERY_ENABLED, Boolean.TRUE);
			selectedProfile = (ScannerConfigProfileManager.NULL_PROFILE_ID.equals(profileId)) ? 
							  getString(SCANNER_CONFIG_SELECTED_PROFILE_ID) : 
							  profileId;
			problemReportingEnabled = getBoolean(SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED);
            if (ScannerConfigProfileManager.NULL_PROFILE_ID.equals(selectedProfile) && !useDefaults) {
                // get the default value
                selectedProfile = prefs.getDefaultString(SCANNER_CONFIG_SELECTED_PROFILE_ID);
            }
            List profileIds = ScannerConfigProfileManager.getInstance().getProfileIds();
            profileOptionsMap = new LinkedHashMap(profileIds.size());
            for (Iterator I = profileIds.iterator(); I.hasNext(); ) {
            	/*String */profileId = (String) I.next();
	            ProfileOptions po = new ProfileOptions();
	            profileOptionsMap.put(profileId, po);
	            
	            boolean profileStored = getBoolean(SCD + profileId + DOT + ENABLED);
                if (!profileStored && !useDefaults) {
                    loadFromProfileConfiguration(po, profileId);
                    continue;
                }

                po.buildOutputFileActionEnabled = getBoolean(SCD + profileId + BUILD_OUTPUT_OPEN_ACTION_ENABLED);
				po.buildOutputFilePath = getString(SCD + profileId + BUILD_OUTPUT_OPEN_ACTION_FILE_PATH);
				po.buildOutputParserEnabled = getBoolean(SCD + profileId + BUILD_OUTPUT_PARSER_ENABLED);
				
				ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
						getSCProfileConfiguration(profileId);
				List providerIds = configuredProfile.getSIProviderIds();
				po.providerOptionsMap = new LinkedHashMap(providerIds.size());
				for (int i = 0; i < providerIds.size(); ++i) {
					String providerId = (String) providerIds.get(i);
					ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
					po.providerOptionsMap.put(providerId, ppo);
                    ppo.providerKind = configuredProfile.getScannerInfoProviderElement(
                            providerId).getProviderKind();
					
					ppo.providerOutputParserEnabled = getBoolean(SCD + profileId + DOT + 
							providerId + SI_PROVIDER_PARSER_ENABLED);
					if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
						ppo.providerRunUseDefault = getBoolean(SCD + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_USE_DEFAULT); 
						ppo.providerRunCommand = getString(SCD + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_COMMAND);
						ppo.providerRunArguments = getString(SCD + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_ARGUMENTS);
					}
					else if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
						ppo.providerOpenFilePath = getString(SCD + profileId + DOT + providerId + 
								SI_PROVIDER_OPEN_ACTION_FILE_PATH);
					}
				}
            }
		}

		private void store() {
			if (isDirty) {
				set(SCANNER_CONFIG_AUTODISCOVERY_ENABLED, autoDiscoveryEnabled);
				set(SCANNER_CONFIG_SELECTED_PROFILE_ID, selectedProfile);
				set(SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED, problemReportingEnabled);
				
				List profileIds = new ArrayList(profileOptionsMap.keySet());
				for (Iterator I = profileIds.iterator(); I.hasNext(); ) {
					String profileId = (String) I.next();
					ProfileOptions po = (ProfileOptions) profileOptionsMap.get(profileId);
					
                    set(SCD + profileId + DOT + ENABLED, !useDefaults);
					set(SCD + profileId + BUILD_OUTPUT_OPEN_ACTION_ENABLED, po.buildOutputFileActionEnabled);
					set(SCD + profileId + BUILD_OUTPUT_OPEN_ACTION_FILE_PATH, po.buildOutputFilePath);
					set(SCD + profileId + BUILD_OUTPUT_PARSER_ENABLED, po.buildOutputParserEnabled);
	
					ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
							getSCProfileConfiguration(profileId);
					List providerIds = configuredProfile.getSIProviderIds();
					for (int i = 0; i < providerIds.size(); ++i) {
						String providerId = (String) providerIds.get(i);
						ProfileOptions.ProviderOptions ppo = (ProfileOptions.ProviderOptions) 
								po.providerOptionsMap.get(providerId);
						
						set(SCD + profileId + DOT + providerId + SI_PROVIDER_PARSER_ENABLED,
								ppo.providerOutputParserEnabled);
//						String providerKind = configuredProfile.getScannerInfoProviderElement(
//								providerId).getProviderKind();
						String providerKind = ppo.providerKind;
						
						if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
							set(SCD + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_USE_DEFAULT,
									ppo.providerRunUseDefault);
							set(SCD + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_COMMAND, 
									ppo.providerRunCommand);
							set(SCD + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_ARGUMENTS,
									ppo.providerRunArguments);
						}
						else if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
							set(SCD + profileId + DOT + providerId + SI_PROVIDER_OPEN_ACTION_FILE_PATH,
									ppo.providerOpenFilePath);
						}
					}
				}
				
				isDirty = false;
			}
		}

		private boolean getBoolean(String name) {
			return getBoolean(name,  null);
		}

		private boolean getBoolean(String name, Boolean defaultValue) {
			if(defaultValue != null && !prefs.contains(name)){
				return defaultValue.booleanValue();
			}
			if (useDefaults) {
				return prefs.getDefaultBoolean(name);
			}
			return prefs.getBoolean(name);
		}
		private void set(String name, boolean value) {
			if (useDefaults) {
				prefs.setDefault(name, value);
			}
			else {
				prefs.setValue(name, value);
			}
		}

		private String getString(String name) {
			if (useDefaults) {
				return prefs.getDefaultString(name);
			}
			return prefs.getString(name);
		}
		private void set(String name, String value) {
			if (useDefaults) {
				prefs.setDefault(name, value);
			}
			else {
				prefs.setValue(name, value);
			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#save()
         */
        public void save() throws CoreException {
            store();
        }

	}

//	public static IScannerConfigBuilderInfo2 create(IProject project, String profileId) throws CoreException {
//		return new ScannerConfigInfoFactory3.BuildProperty(project, profileId);
//	}
	public static IScannerConfigBuilderInfo2 create(IConfiguration cfg, ICStorageElement el) {
		return new ScannerConfigInfoFactory2.BuildProperty(new InfoContext(cfg), el, ScannerConfigProfileManager.NULL_PROFILE_ID);
	}

	public static IScannerConfigBuilderInfo2 create(IConfiguration cfg, Preferences prefs, String id) {
		return create(new InfoContext(cfg), prefs, id, false);
	}
	
	public static IScannerConfigBuilderInfo2 create(InfoContext context, Preferences prefs, String profileId, boolean useDefaults) {
		ScannerConfigInfoFactory2.Preference pref = new ScannerConfigInfoFactory2.Preference(prefs, profileId, useDefaults);
		return new ScannerConfigInfoFactory2.BuildProperty(context, pref);
	}

	public static IScannerConfigBuilderInfo2 create(Preferences prefs, String profileId, boolean useDefaults) {
		return new ScannerConfigInfoFactory2.Preference(prefs, profileId, useDefaults);
	}

	public static IScannerConfigBuilderInfo2 create(InfoContext context, ICStorageElement el, String profileId) {
		return new ScannerConfigInfoFactory2.BuildProperty(context, el, profileId);
	}
	
	public static IScannerConfigBuilderInfo2 create(InfoContext context, String profileId){
    	Configuration cfg = (Configuration)context.getConfiguration();
    	
    	IConfigurationScannerConfigBuilderInfo cfgInfo = create(cfg, false);
    	return cfgInfo.getInfo(context);
//    	if(cfgInfo.isPerRcTypeDiscovery()){
//    		Tool tool = (Tool)context.getTool();
//    		if(tool != null){
//    			info = tool.getScannerConfigInfo(context.getInputType());
//    		} else {
//    		}
//    	} else {
//    		info = cfg.getScannerConfigInfo();
//    	}
//    	
//    	return info;
	}
	
	public static IScannerConfigBuilderInfo2 create(InfoContext context,
			IScannerConfigBuilderInfo2 base, String profileId){
		ScannerConfigInfoFactory2.BuildProperty prop = (ScannerConfigInfoFactory2.BuildProperty)((ScannerConfigInfoFactory2.BuildProperty)base).clone();
		prop.context = context;
		prop.profileId = profileId;
		
		return prop;
	}
	
	public static void serialize(IScannerConfigBuilderInfo2 info, ICStorageElement el){
		((ScannerConfigInfoFactory2.BuildProperty)info).store(el);
	}
	
	public static IConfigurationScannerConfigBuilderInfo create(IConfiguration cfg, boolean newInstance){
		Configuration c = (Configuration)cfg;
		if(newInstance)
			return new CfgInfo(c);
		return c.getCfgScannerConfigInfo();
	}
}
