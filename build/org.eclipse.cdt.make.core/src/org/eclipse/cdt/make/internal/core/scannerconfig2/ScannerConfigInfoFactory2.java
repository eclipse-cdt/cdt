/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *  James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.ScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

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
	private static final String SCANNER_CONFIG_AUTODISCOVERY_ENABLED_SUFFIX = "enabled";//$NON-NLS-1$
	private static final String SCANNER_CONFIG_SELECTED_PROFILE_ID_SUFFIX = "selectedProfileId";//$NON-NLS-1$
    private static final String SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED_SUFFIX = "problemReportingEnabled"; //$NON-NLS-1$
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
	private static final String INFO_INSTANCE_IDS = SCD + "instanceIds";//$NON-NLS-1$
	private static final String DELIMITER = ";";//$NON-NLS-1$
	
	
	private static final String ELEMENT_CS_INFO = "scannerConfigBuildInfo";//$NON-NLS-1$
	private static final String ATTRIBUTE_CS_INFO_INSTANCE_ID = "instanceId";//$NON-NLS-1$
	
	private static class ScannerConfigInfoSet extends StoreSet {
		private IProject fProject;
		
		ScannerConfigInfoSet(IProject project, String profileId){
			this.fProject = project;
			load(profileId);
		}
		
		private void load(String profileId) {
			ICDescriptor descriptor;
			try {
				descriptor = CCorePlugin.getDefault().getCProjectDescription(fProject, false);
				ICStorageElement rootEl = descriptor != null ? descriptor.getProjectStorageElement(SCANNER_CONFIG) : null;
				InfoContext defaultContext = new InfoContext(fProject);
				if(rootEl == null || !rootEl.hasChildren()){
					BuildProperty prop = new BuildProperty(this, fProject, defaultContext, (Store)create(MakeCorePlugin.getDefault().getPluginPreferences(), profileId, false), profileId);
					fMap.put(defaultContext, prop);
					prop.isDirty = true;
				} else {
					BuildProperty prop = new BuildProperty(this, fProject, defaultContext, profileId, rootEl);
					fMap.put(defaultContext, prop);

					for (ICStorageElement sc : rootEl.getChildren()) {
						if (sc.getName().equals(ELEMENT_CS_INFO)) {
							String instanceId = sc.getAttribute(ATTRIBUTE_CS_INFO_INSTANCE_ID);
							if(instanceId != null && instanceId.length() > 0){
								InfoContext c = new InfoContext(fProject, instanceId);
								BuildProperty p = new BuildProperty(this, fProject, c, profileId, sc);
								fMap.put(c, p);
							}
						}
					}
				}
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		}

		public void save() throws CoreException {
			save(false);
		}

		public void save(boolean serializeDescriptor) throws CoreException {
			if (isDirty()) {
				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
				ICStorageElement sc = descriptor.getProjectStorageElement(SCANNER_CONFIG);
				
				// Clear all child settings
				sc.clear();
                
				BuildProperty prop = (BuildProperty)fMap.get(new InfoContext(fProject));
				prop.store(sc);
				
				for (Entry<InfoContext, IScannerConfigBuilderInfo2> entry : fMap.entrySet()) {
					
					InfoContext context = entry.getKey();
					if(context.isDefaultContext())
						continue;
					
					String instanceId = context.getInstanceId();
					if(instanceId.length() == 0)
						continue;

					BuildProperty p = (BuildProperty)entry.getValue();
					if(p == prop)
						continue;
					
					ICStorageElement el = sc.createChild(ELEMENT_CS_INFO);
					el.setAttribute(ATTRIBUTE_CS_INFO_INSTANCE_ID, instanceId);
					p.store(el);
				}
				
				if(serializeDescriptor)
					descriptor.saveProjectData();
				
				fIsDirty = false;
			}
		}
		
		public IProject getProject() {
			return fProject;
		}

		@Override
		protected Store doCreateStore(InfoContext context, Store base, String profileId) {
			return new BuildProperty(this, fProject, context, base, profileId);
		}
	}
	
	private static class PreferenceSet extends StoreSet {
		private Preferences prefs; 
		private boolean useDefaults;
		PreferenceSet(Preferences prefs, String profileId, boolean loadDefaults){
			this.prefs = prefs;
			this.useDefaults = loadDefaults;
			load(profileId);
		}
		
	    private void load(String profileId) {
			InfoContext defaultContext = new InfoContext(null);
			String instancesStr = getString(INFO_INSTANCE_IDS);
			String[] instanceIds = CDataUtil.stringToArray(instancesStr, DELIMITER);
			Preference pref = new Preference(this, prefs, defaultContext, profileId, useDefaults);
			fMap.put(defaultContext, pref);
			
			if(instanceIds != null && instanceIds.length != 0){
				for(int i = 0; i < instanceIds.length; i++) {
					String id = instanceIds[i];
					if(id.length() == 0)
						continue;
					
					InfoContext c = new InfoContext(null, id);
					
					Preference p = new Preference(this, prefs, c, profileId, useDefaults);
					fMap.put(c, p);
				}
			}

		}
		
		public void save() throws CoreException {
			if (isDirty()) {
                
				Set<String> idSet = new HashSet<String>(fMap.size() - 1);
				
				Preference pref = (Preference)fMap.get(new InfoContext(null));
				pref.store();
				
				for (Entry<InfoContext, IScannerConfigBuilderInfo2> entry : fMap.entrySet()) {
					InfoContext context = entry.getKey();
					if(context.isDefaultContext())
						continue;
					
					String instanceId = context.getInstanceId();
					if(instanceId.length() == 0)
						continue;

					Preference p = (Preference)entry.getValue();
					if(p == pref)
						continue;

					p.store();
					
					idSet.add(instanceId);
				}
				
				if(idSet.size() != 0){
					String[] ids = idSet.toArray(new String[idSet.size()]);
					String idsString = CDataUtil.arrayToString(ids, DELIMITER);
					set(INFO_INSTANCE_IDS, idsString);
				}
				
				fIsDirty = false;
			}
		}
		
		public IProject getProject() {
			return null;
		}

		@Override
		protected Store doCreateStore(InfoContext context, Store base, String profileId) {
			return new Preference(this, prefs, context, base, profileId, useDefaults);
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
	}

	
	private static abstract class StoreSet implements IScannerConfigBuilderInfo2Set {
		protected HashMap<InfoContext, IScannerConfigBuilderInfo2> fMap = new HashMap<InfoContext, IScannerConfigBuilderInfo2>();
		protected boolean fIsDirty;
		
		StoreSet(){
		}
		
		public IScannerConfigBuilderInfo2 createInfo(InfoContext context,
				IScannerConfigBuilderInfo2 base, String profileId){
			fIsDirty = true;
			Store store = doCreateStore(context, (Store)base, profileId);
			fMap.put(context, store);
			return store;
		}
		
		protected abstract Store doCreateStore(InfoContext context, Store base, String profileId);

		public IScannerConfigBuilderInfo2 createInfo(InfoContext context,
				IScannerConfigBuilderInfo2 base){
			fIsDirty = true;
			return createInfo(context, base, ScannerConfigProfileManager.NULL_PROFILE_ID);
		}

		public InfoContext[] getContexts() {
			return fMap.keySet().toArray(new InfoContext[fMap.size()]);
		}

		public IScannerConfigBuilderInfo2 getInfo(InfoContext context) {
			return fMap.get(context);
		}

		public Map<InfoContext, IScannerConfigBuilderInfo2> getInfoMap() {
			return Collections.unmodifiableMap(fMap);
		}

		public IScannerConfigBuilderInfo2 removeInfo(InfoContext context) throws CoreException {
			checkRemoveInfo(context);
			fIsDirty = true;
			return fMap.remove(context);
		}
		
		private void checkRemoveInfo(InfoContext context) throws CoreException{
			if(context.isDefaultContext())
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, MakeConfigurationDataProviderMessages.getString("ScannerConfigInfoFactory2.0"))); //$NON-NLS-1$
		}
		
		public boolean isDirty(){
			if(fIsDirty)
				return true;
			for (IScannerConfigBuilderInfo2 prop : fMap.values()) {
				if(((Store)prop).isDirty)
					return true;
			}
			
			return false;
		}

		public IScannerConfigBuilderInfo2 createInfo(InfoContext context) {
			fIsDirty = true;
			return createInfo(context, ScannerConfigProfileManager.NULL_PROFILE_ID);
		}

		public IScannerConfigBuilderInfo2 createInfo(InfoContext context,
				String profileId) {
			fIsDirty = true;
			IScannerConfigBuilderInfo2 base = getInfo(new InfoContext(getProject()));
			return createInfo(context, base, profileId);
		}
	}

	private static abstract class Store implements IScannerConfigBuilderInfo2 {
		protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
        protected boolean isDirty;	// derived

		protected boolean autoDiscoveryEnabled;
		protected boolean problemReportingEnabled;
		protected String selectedProfile = EMPTY_STRING;
		/** Map from profile ID -> ProfileOptions */
        protected Map<String, ProfileOptions> profileOptionsMap; 
        static class ProfileOptions {
    		protected boolean buildOutputFileActionEnabled;
    		protected String buildOutputFilePath = EMPTY_STRING;
    		protected boolean buildOutputParserEnabled;
    		/** Map from provider ID -> providerOptions */
    		protected Map<String, ProviderOptions> providerOptionsMap; 
    		static class ProviderOptions {
    			protected String providerKind;	// derived
    			protected boolean providerOutputParserEnabled;
    			protected boolean providerRunUseDefault;
    			protected String providerRunCommand = EMPTY_STRING;
    			protected String providerRunArguments = EMPTY_STRING;
    			protected String providerOpenFilePath = EMPTY_STRING;
    			
    			ProviderOptions(){
    			}

    			ProviderOptions(ProviderOptions base){
    				this.providerKind = base.providerKind;
    				this.providerOutputParserEnabled = base.providerOutputParserEnabled;
    				this.providerRunUseDefault = base.providerRunUseDefault;
    				this.providerRunCommand = base.providerRunCommand;
    				this.providerRunArguments = base.providerRunArguments;
    				this.providerOpenFilePath = base.providerOpenFilePath;
    			}

    		}
    		
        	ProfileOptions(){
        	}
        	
        	ProfileOptions(ProfileOptions base){
        		this.buildOutputFileActionEnabled = base.buildOutputFileActionEnabled;
        		this.buildOutputFilePath = base.buildOutputFilePath;
        		this.buildOutputParserEnabled = base.buildOutputParserEnabled;
        		this.providerOptionsMap = new LinkedHashMap<String, ProviderOptions>(base.providerOptionsMap);
        		for (Map.Entry<String, ProviderOptions> entry : providerOptionsMap.entrySet()) {
        			ProviderOptions basePo = entry.getValue();
        			entry.setValue(new ProviderOptions(basePo));
        		}
        	}

        }
        
		protected Store() {
			isDirty = false;
		}
		
		protected Store(Store base, String profileId){
			this.autoDiscoveryEnabled = base.autoDiscoveryEnabled;
			this.problemReportingEnabled = base.problemReportingEnabled;
			this.selectedProfile = ScannerConfigProfileManager.NULL_PROFILE_ID.equals(profileId) ? base.selectedProfile : profileId;
			this.profileOptionsMap = new LinkedHashMap<String, ProfileOptions>(base.profileOptionsMap);
			for (Map.Entry<String, ProfileOptions> entry : profileOptionsMap.entrySet()) {
    			ProfileOptions basePo = entry.getValue();
    			entry.setValue(new ProfileOptions(basePo));
    		}
			
			isDirty = true;
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
        public List<String> getProfileIdList() {
            return new ArrayList<String>(profileOptionsMap.keySet());
        }

        /* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isBuildOutputFileActionEnabled()
		 */
		public boolean isBuildOutputFileActionEnabled() {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            return (po != null) ? po.buildOutputFileActionEnabled : false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputFileActionEnabled(boolean)
		 */
		public void setBuildOutputFileActionEnabled(boolean enable) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputFileActionEnabled = setDirty(po.buildOutputFileActionEnabled, enable);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getBuildOutputFilePath()
		 */
		public String getBuildOutputFilePath() {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
			return (po != null) ? po.buildOutputFilePath : EMPTY_STRING;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputFilePath(java.lang.String)
		 */
		public void setBuildOutputFilePath(String path) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputFilePath = setDirty(po.buildOutputFilePath, path);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isBuildOutputParserEnabled()
		 */
		public boolean isBuildOutputParserEnabled() {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
			return (po != null) ? po.buildOutputParserEnabled : true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setBuildOutputParserEnabled(boolean)
		 */
		public void setBuildOutputParserEnabled(boolean enable) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                po.buildOutputParserEnabled = setDirty(po.buildOutputParserEnabled, enable);
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getSIProviderIdList()
		 */
		public List<String> getProviderIdList() {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            return (po != null) ? new ArrayList<String>(po.providerOptionsMap.keySet()) : new ArrayList<String>(0);
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
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
    			ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
    			return (ppo == null) ? false : ppo.providerOutputParserEnabled;
            }
            return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setSIProviderOutputParserEnabled(java.lang.String, boolean)
		 */
		public void setProviderOutputParserEnabled(String providerId, boolean enable) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
    			ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerOutputParserEnabled = setDirty(ppo.providerOutputParserEnabled, enable);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#isUseDefaultProviderCommand(java.lang.String)
		 */
		public boolean isUseDefaultProviderCommand(String providerId) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
                return (ppo == null) ? false : ppo.providerRunUseDefault;
            }
            return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setUseDefaultProviderCommand(java.lang.String, boolean)
		 */
		public void setUseDefaultProviderCommand(String providerId, boolean enable) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunUseDefault = setDirty(ppo.providerRunUseDefault, enable);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderRunCommand(java.lang.String)
		 */
		public String getProviderRunCommand(String providerId) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerRunCommand;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderRunCommand(java.lang.String, java.lang.String)
		 */
		public void setProviderRunCommand(String providerId, String command) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunCommand = setDirty(ppo.providerRunCommand, command);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderRunArguments(java.lang.String)
		 */
		public String getProviderRunArguments(String providerId) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerRunArguments;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderRunArguments(java.lang.String, java.lang.String)
		 */
		public void setProviderRunArguments(String providerId, String arguments) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
    			if (ppo != null) {
                    ppo.providerRunArguments = setDirty(ppo.providerRunArguments, arguments);
    			}
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#getProviderOpenFilePath(java.lang.String)
		 */
		public String getProviderOpenFilePath(String providerId) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
                return (ppo == null) ? null : ppo.providerOpenFilePath;
            }
            return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2#setProviderOpenFilePath(java.lang.String, java.lang.String)
		 */
		public void setProviderOpenFilePath(String providerId, String filePath) {
            ProfileOptions po = profileOptionsMap.get(selectedProfile);
            if (po != null) {
                ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
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
        
//		protected abstract void load();
//		public abstract void store();

        /**
         * Populate buildInfo based on profile configuration
         */
        protected void loadFromProfileConfiguration(ProfileOptions po, String profileId) {
            ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
                    getSCProfileConfiguration(profileId);
            List<String> providerIds = configuredProfile.getSIProviderIds();
            
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
            po.providerOptionsMap = new LinkedHashMap<String, ProfileOptions.ProviderOptions>(providerIds.size());
            for (int i = 0; i < providerIds.size(); ++i) {
                ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
                String providerId = providerIds.get(i);
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
	private static class BuildProperty extends Store {
		private IProject project;
		private InfoContext context;
        private String profileId;
        private ScannerConfigInfoSet container;

		BuildProperty(ScannerConfigInfoSet container, IProject project, InfoContext context, String profileId, ICStorageElement element) {
			super();
			this.project = project;
			this.context = context;
            this.profileId = profileId;
            this.container = container;
			load(element);
		}
		
		BuildProperty(ScannerConfigInfoSet container, IProject project, InfoContext context, Store base, String profileId) {
			super(base, profileId);
			this.project = project;
			this.context = context;
			this.container = container;
			if(!profileId.equals(ScannerConfigProfileManager.NULL_PROFILE_ID)){
				this.profileId = profileId;
			} else if(base instanceof BuildProperty){
				BuildProperty prop = (BuildProperty)base;
				this.profileId = prop.profileId;
			} else {
				Preference pref = (Preference)base;
				this.profileId = pref.profileId;
			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigInfoFactory2.Store#load()
         */
        protected void load(ICStorageElement element) {
//			ICDescriptor descriptor;
            List<String> profileIds = ScannerConfigProfileManager.getInstance().getProfileIds(context);
            List<String> loadedProfiles = new ArrayList<String>();
//			try {
//				descriptor = CCorePlugin.getDefault().getCProjectDescription(project, false);
            	for (ICStorageElement sc : element.getChildren()) {
                    if (sc.getName().equals(SC_AUTODISCOVERY)) {
                        autoDiscoveryEnabled = Boolean.valueOf(
                                sc.getAttribute(ENABLED)).booleanValue();
                        selectedProfile = (profileId == ScannerConfigProfileManager.NULL_PROFILE_ID) 
                                ? sc.getAttribute(SELECTED_PROFILE_ID)
                                : profileId;
                        problemReportingEnabled = Boolean.valueOf(
                        		sc.getAttribute(PROBLEM_REPORTING_ENABLED)).booleanValue();
                    }
                    else if (sc.getName().equals(PROFILE)) {
						if (profileIds.contains(sc.getAttribute(ID))) {
							loadProfile(sc);
                            loadedProfiles.add(sc.getAttribute(ID));
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
                	for (String profileId : profileIds) {
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
//				MakeCorePlugin.log(e);
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

            po.providerOptionsMap = new LinkedHashMap<String, ProfileOptions.ProviderOptions>();
            
            List<String> providerIds = configuredProfile.getSIProviderIds();
            for (String providerId : providerIds) {
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
                profileOptionsMap = new LinkedHashMap<String, ProfileOptions>();
            }
            profileOptionsMap.put(profileId, po);
        }

        /**
		 * @param profileId
		 */
		private boolean migrateScannerConfigBuildInfo(String profileId) {
			boolean rc = true;
			try {
				IScannerConfigBuilderInfo oldInfo = MakeCorePlugin.
						createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
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
				String providerId = configuredProfile.getSIProviderIds().get(0);
                po.providerOptionsMap = new LinkedHashMap<String, ProfileOptions.ProviderOptions>(1);
				po.providerOptionsMap.put(providerId, ppo);
                
                profileOptionsMap = new LinkedHashMap<String, ProfileOptions>(1);
                profileOptionsMap.put(profileId, po);
                
                // store migrated data
                isDirty = true;
                save();
			} 
			catch (CoreException e) {
				MakeCorePlugin.log(e);
				rc = false;
			}
			return rc;
		}

		/**
		 * @param profile
		 */
		private void loadProfile(ICStorageElement profile) {
            if (profileOptionsMap == null) {
                profileOptionsMap = new LinkedHashMap<String, ProfileOptions>(1);
            }
            ProfileOptions po = new ProfileOptions();
            String profileId = profile.getAttribute(ID);
            profileOptionsMap.put(profileId, po);
            // get the list of providers from the profile configuration
            ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
                    getSCProfileConfiguration(profileId);
            List<String> providerIds = configuredProfile.getSIProviderIds();
            int providerCounter = 0;
            po.providerOptionsMap = new LinkedHashMap<String, ProfileOptions.ProviderOptions>(providerIds.size());

            for (ICStorageElement child : profile.getChildren()) {
    			// buildOutputProvider element
    			if (BUILD_OUTPUT_PROVIDER.equals(child.getName())) {
    				for (ICStorageElement grandchild : child.getChildren()) {
        				if (OPEN_ACTION.equals(grandchild.getName())) {
        					po.buildOutputFileActionEnabled = Boolean.valueOf(
                                    grandchild.getAttribute(ENABLED)).booleanValue();
        					po.buildOutputFilePath = grandchild.getAttribute(FILE_PATH);
        				}
                        else if (PARSER.equals(grandchild.getName())) {
        					po.buildOutputParserEnabled = Boolean.valueOf(
                                    grandchild.getAttribute(ENABLED)).booleanValue();
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
						
                        for (ICStorageElement grandchild : child.getChildren()) {
                            // action
    						if (RUN_ACTION.equals(grandchild.getName())) {
    							ppo.providerRunUseDefault = Boolean.valueOf(
                                        grandchild.getAttribute(USE_DEFAULT)).booleanValue(); 
    							ppo.providerRunCommand = grandchild.getAttribute(COMMAND);
    							ppo.providerRunArguments = grandchild.getAttribute(ARGUMENTS);
    						}
    						else if (OPEN_ACTION.equals(grandchild.getName())) {
    							ppo.providerOpenFilePath = grandchild.getAttribute(FILE_PATH);
    						}
                            // parser
                            else if (PARSER.equals(grandchild.getName())) {
                                ppo.providerOutputParserEnabled = Boolean.valueOf(
                                        grandchild.getAttribute(ENABLED)).booleanValue();
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

		private void store(ICStorageElement sc)/* throws CoreException */{
//			if (isDirty || force) {
//				ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
//				Element sc = descriptor.getProjectData(SCANNER_CONFIG);

                // Clear children
                for (ICStorageElement child : sc.getChildren())
                	sc.removeChild(child);

                ICStorageElement autod = sc.createChild(SC_AUTODISCOVERY);
				autod.setAttribute(ENABLED, Boolean.toString(autoDiscoveryEnabled));
				autod.setAttribute(SELECTED_PROFILE_ID, selectedProfile);
				autod.setAttribute(PROBLEM_REPORTING_ENABLED, Boolean.toString(problemReportingEnabled));

				for (String profileId : profileOptionsMap.keySet()) {
                    ICStorageElement profile = sc.createChild(PROFILE);
					profile.setAttribute(ID, profileId);
					store(profile, profileOptionsMap.get(profileId));
                }
				
				isDirty = false;
//                return true;
//			}
//            return false;
		}

		/**
		 * @param profile element
		 * @param profile options 
		 */
		private void store(ICStorageElement profile, ProfileOptions po) {
			ICStorageElement child, grandchild;
			// buildOutputProvider element
			child = profile.createChild(BUILD_OUTPUT_PROVIDER);
			grandchild = child.createChild(OPEN_ACTION);
			grandchild.setAttribute(ENABLED, Boolean.toString(po.buildOutputFileActionEnabled));
			if(po.buildOutputFilePath != null)
				grandchild.setAttribute(FILE_PATH, po.buildOutputFilePath);
			grandchild = child.createChild(PARSER);
			grandchild.setAttribute(ENABLED, Boolean.toString(po.buildOutputParserEnabled));
			// scannerInfoProvider elements
			// get the list of providers from the profile configuration
//			ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
//					getSCProfileConfiguration(selectedProfile);
//			List providerIds = configuredProfile.getSIProviderIds();
            List<String> providerIds = new ArrayList<String>(po.providerOptionsMap.keySet());
			for (int i = 0; i < providerIds.size(); ++i) {
				String providerId = providerIds.get(i);
				ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
				
				if (ppo != null) {
					child = profile.createChild(SCANNER_INFO_PROVIDER);
					child.setAttribute(ID, providerId);
					
					// action
//                    String providerKind = configuredProfile.getScannerInfoProviderElement(
//							providerId).getProviderKind();
                    String providerKind = ppo.providerKind;
                    
					if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
						grandchild = child.createChild(RUN_ACTION);
						grandchild.setAttribute(USE_DEFAULT, Boolean.toString(ppo.providerRunUseDefault)); 
						if(ppo.providerRunCommand != null)
							grandchild.setAttribute(COMMAND, ppo.providerRunCommand);
						if(ppo.providerRunArguments != null)
							grandchild.setAttribute(ARGUMENTS, ppo.providerRunArguments);
					}
					else if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
						grandchild = child.createChild(OPEN_ACTION);
						if(ppo.providerOpenFilePath != null)
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
        public synchronized void save() throws CoreException {
        	if(isDirty){
        		container.save(true);
        		isDirty = false;
        	}
        }
        
        public InfoContext getContext(){
        	return context;
        }

	}

	/**
	 * Preferences
	 * 
	 * @author vhirsl
	 */
	private static class Preference extends Store {
		private Preferences prefs;
		private String profileId;
		private boolean useDefaults;
		private PreferenceSet prefsContainer;
		private InfoContext context;

		/**
		 * @param prefs
		 * @param profileId
		 * @param useDefaults
		 */
		public Preference(PreferenceSet container, Preferences prefs, InfoContext context, String profileId, boolean useDefaults) {
			super();
			this.prefs = prefs;
			this.profileId = profileId;
			this.useDefaults = useDefaults;
			this.prefsContainer = container;
			this.context = context;
			load();
		}
		
		Preference(PreferenceSet container, Preferences prefs, InfoContext context, Store base, String profileId, boolean useDefaults) {
			super(base, profileId);
			this.prefs = prefs;
			this.prefsContainer = container;
			this.useDefaults = useDefaults;
			this.context = context;

			if(!profileId.equals(ScannerConfigProfileManager.NULL_PROFILE_ID)){
				this.profileId = profileId;
			} else if(base instanceof BuildProperty){
				BuildProperty prop = (BuildProperty)base;
				this.profileId = prop.profileId;
			} else {
				Preference pref = (Preference)base;
				this.profileId = pref.profileId;
			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigInfoFactory2.Store#load()
         */
        protected void load() {
        	String instanceId = context.getInstanceId();
        	String prefix = instanceId.length() == 0 ? "" : ATTRIBUTE_CS_INFO_INSTANCE_ID + DOT + instanceId + DOT; //$NON-NLS-1$
        	
			autoDiscoveryEnabled = getBoolean(prefix + SCANNER_CONFIG_AUTODISCOVERY_ENABLED_SUFFIX);
			selectedProfile = (ScannerConfigProfileManager.NULL_PROFILE_ID.equals(profileId)) ? 
							  getString(prefix + SCANNER_CONFIG_SELECTED_PROFILE_ID_SUFFIX) : 
							  profileId;
			problemReportingEnabled = getBoolean(prefix + SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED_SUFFIX);
            if (ScannerConfigProfileManager.NULL_PROFILE_ID.equals(selectedProfile) && !useDefaults) {
                // get the default value
                selectedProfile = prefs.getDefaultString(prefix + SCANNER_CONFIG_SELECTED_PROFILE_ID_SUFFIX);
            }
            List<String> profileIds = ScannerConfigProfileManager.getInstance().getProfileIds(context);
            profileOptionsMap = new LinkedHashMap<String, ProfileOptions>(profileIds.size());
            for (String profileId : profileIds) {
	            ProfileOptions po = new ProfileOptions();
	            profileOptionsMap.put(profileId, po);
	            
	            boolean profileStored = getBoolean(SCD + prefix + profileId + DOT + ENABLED);
                if (!profileStored && !useDefaults) {
                    loadFromProfileConfiguration(po, profileId);
                    continue;
                }

                po.buildOutputFileActionEnabled = getBoolean(SCD + prefix + profileId + BUILD_OUTPUT_OPEN_ACTION_ENABLED);
				po.buildOutputFilePath = getString(SCD + prefix + profileId + BUILD_OUTPUT_OPEN_ACTION_FILE_PATH);
				po.buildOutputParserEnabled = getBoolean(SCD + prefix + profileId + BUILD_OUTPUT_PARSER_ENABLED);
				
				ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
						getSCProfileConfiguration(profileId);
				List<String> providerIds = configuredProfile.getSIProviderIds();
				po.providerOptionsMap = new LinkedHashMap<String, ProfileOptions.ProviderOptions>(providerIds.size());
				for (String providerId : providerIds) {
					ProfileOptions.ProviderOptions ppo = new ProfileOptions.ProviderOptions();
					po.providerOptionsMap.put(providerId, ppo);
                    ppo.providerKind = configuredProfile.getScannerInfoProviderElement(
                            providerId).getProviderKind();
					
					ppo.providerOutputParserEnabled = getBoolean(SCD + prefix + profileId + DOT + 
							providerId + SI_PROVIDER_PARSER_ENABLED);
					if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
						ppo.providerRunUseDefault = getBoolean(SCD + prefix + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_USE_DEFAULT); 
						ppo.providerRunCommand = getString(SCD + prefix + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_COMMAND);
						ppo.providerRunArguments = getString(SCD + prefix + profileId + DOT + providerId + 
								SI_PROVIDER_RUN_ACTION_ARGUMENTS);
					}
					else if (ppo.providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
						ppo.providerOpenFilePath = getString(SCD + prefix + profileId + DOT + providerId + 
								SI_PROVIDER_OPEN_ACTION_FILE_PATH);
					}
				}
            }
		}
        
		private void store() {
        	String instanceId = context.getInstanceId();
        	String prefix = instanceId.length() == 0 ? "" : ATTRIBUTE_CS_INFO_INSTANCE_ID + DOT + instanceId + DOT; //$NON-NLS-1$
			if (isDirty) {
				set(prefix + SCANNER_CONFIG_AUTODISCOVERY_ENABLED_SUFFIX, autoDiscoveryEnabled);
				set(prefix + SCANNER_CONFIG_SELECTED_PROFILE_ID_SUFFIX, selectedProfile);
				set(prefix + SCANNER_CONFIG_PROBLEM_REPORTING_ENABLED_SUFFIX, problemReportingEnabled);
				
				for (String profileId : profileOptionsMap.keySet()) {
					ProfileOptions po = profileOptionsMap.get(profileId);
					
                    set(SCD + prefix + profileId + DOT + ENABLED, !useDefaults);
					set(SCD + prefix + profileId + BUILD_OUTPUT_OPEN_ACTION_ENABLED, po.buildOutputFileActionEnabled);
					set(SCD + prefix + profileId + BUILD_OUTPUT_OPEN_ACTION_FILE_PATH, po.buildOutputFilePath);
					set(SCD + prefix + profileId + BUILD_OUTPUT_PARSER_ENABLED, po.buildOutputParserEnabled);
	
					ScannerConfigProfile configuredProfile = ScannerConfigProfileManager.getInstance().
							getSCProfileConfiguration(profileId);
					List<String> providerIds = configuredProfile.getSIProviderIds();
					for (String providerId : providerIds) {
						ProfileOptions.ProviderOptions ppo = po.providerOptionsMap.get(providerId);
						
						set(SCD + prefix + profileId + DOT + providerId + SI_PROVIDER_PARSER_ENABLED,
								ppo.providerOutputParserEnabled);
//						String providerKind = configuredProfile.getScannerInfoProviderElement(
//								providerId).getProviderKind();
						String providerKind = ppo.providerKind;
						
						if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.RUN)) {
							set(SCD + prefix + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_USE_DEFAULT,
									ppo.providerRunUseDefault);
							set(SCD + prefix + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_COMMAND, 
									ppo.providerRunCommand);
							set(SCD + prefix + profileId + DOT + providerId + SI_PROVIDER_RUN_ACTION_ARGUMENTS,
									ppo.providerRunArguments);
						}
						else if (providerKind.equals(ScannerConfigProfile.ScannerInfoProvider.OPEN)) {
							set(SCD + prefix + profileId + DOT + providerId + SI_PROVIDER_OPEN_ACTION_FILE_PATH,
									ppo.providerOpenFilePath);
						}
					}
				}
				
				isDirty = false;
			}
		}
		
		private boolean getBoolean(String name) {
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
        	if(isDirty)
        		prefsContainer.save();
        }

        public InfoContext getContext(){
        	return context;
        }
	}

	public static IScannerConfigBuilderInfo2 create(IProject project, String profileId) throws CoreException {
		IScannerConfigBuilderInfo2Set container = createInfoSet(project, profileId);
		return container.getInfo(new InfoContext(project));
	}

	public static IScannerConfigBuilderInfo2 create(Preferences prefs, String profileId, boolean useDefaults) {
		IScannerConfigBuilderInfo2Set container = createInfoSet(prefs, profileId, useDefaults);
		return container.getInfo(new InfoContext(null));
	}

	public static IScannerConfigBuilderInfo2Set createInfoSet(Preferences prefs, String profileId, boolean useDefaults){
		return new ScannerConfigInfoFactory2.PreferenceSet(prefs, profileId, useDefaults);
	}

	public static IScannerConfigBuilderInfo2Set createInfoSet(IProject project, String profileId){
		return new ScannerConfigInfoFactory2.ScannerConfigInfoSet(project, profileId);
	}
}
