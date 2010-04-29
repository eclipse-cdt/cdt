/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

/**
 * Manages profiles per project
 * 
 * @author vhirsl
 */
public final class ScannerConfigProfileManager {
	public static final String SI_PROFILE_SIMPLE_ID = "ScannerConfigurationDiscoveryProfile";	//$NON-NLS-1$
	public static final String PER_PROJECT_PROFILE_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCStandardMakePerProjectProfile"; //$NON-NLS-1$
	public static final String NULL_PROFILE_ID = "";//$NON-NLS-1$
	public static final String DEFAULT_SI_PROFILE_ID = NULL_PROFILE_ID;
	
	private final Map<IProject, Map<InfoContext, Object>> projectToProfileInstanceMap;
	private List<String> profileIds;
	private List<String> contextAwareProfileIds;
	private final Object fLock = new Object();

	/**
	 * Singleton pattern
	 */
	private ScannerConfigProfileManager() {
		projectToProfileInstanceMap = new HashMap<IProject, Map<InfoContext, Object>>();
	}
	private static final ScannerConfigProfileManager instance = new ScannerConfigProfileManager();

	public static ScannerConfigProfileManager getInstance() {
		return instance;
	}

	private String getProfileId(IProject project, InfoContext context) {
		String profileId;
		try {
			IScannerConfigBuilderInfo2Set container = createScannerConfigBuildInfo2Set(project);
			IScannerConfigBuilderInfo2 buildInfo = container.getInfo(context);
			if(buildInfo == null)
				buildInfo = container.getInfo(new InfoContext(project));
			profileId = buildInfo.getSelectedProfileId();
		} catch (CoreException e) {
			MakeCorePlugin.log(e);
			profileId = DEFAULT_SI_PROFILE_ID;
		}
		return profileId;
	}

	/**
	 * For projects that do not have profile id specified in .project file.
	 * For example managed projects.
	 */
	public void addProfile(IProject project, ScannerConfigProfile profile) {
		addProfile(project, new InfoContext(project), profile);
	}

	public void addProfile(IProject project, InfoContext context, ScannerConfigProfile profile) {
		getProfileMap(project, true).put(context, profile);
	}
	
	private Map<InfoContext, Object> getProfileMap(IProject project, boolean create){
		synchronized (fLock) {
			Map<InfoContext, Object> map = projectToProfileInstanceMap.get(project);
			if(map == null && create){
				map = new HashMap<InfoContext, Object>();
				projectToProfileInstanceMap.put(project, map);
			}
			return Collections.synchronizedMap(map);
		}
	}
	
	public void handleProjectRemoved(IProject project){
		synchronized (fLock) {
			projectToProfileInstanceMap.remove(project);
		}
	}
	
	/**
	 * @param profileId - if null, get the one associated with the project
	 * @return the scannerConfigProfile instance for a project.
	 */
	public SCProfileInstance getSCProfileInstance(IProject project, String profileId) {
		return getSCProfileInstance(project, new InfoContext(project), profileId);
	}

	public SCProfileInstance getSCProfileInstance(IProject project, InfoContext context, String profileId) {

		// if not specified read from .project file
        if (profileId == NULL_PROFILE_ID) {
            profileId = getProfileId(project, context);
        }
		synchronized (fLock) {
	        // is the project's profile already loaded?
	        Map<InfoContext, Object> map = getProfileMap(project, true);
	        SCProfileInstance profileInstance = (SCProfileInstance) map.get(context);
	        if (profileInstance == null || !profileInstance.getProfile().getId().equals(profileId)) {
	            profileInstance = new SCProfileInstance(project, context, getSCProfileConfiguration(profileId));
	            map.put(context, profileInstance);
	        }
	        return profileInstance;
		}
	}

    public SCProfileInstance getSCProfileInstance(String profileId) {
        SCProfileInstance profileInstance = null;
        if (profileId != NULL_PROFILE_ID) {
            profileInstance = new SCProfileInstance(null, getSCProfileConfiguration(profileId));
        }
        return profileInstance;
    }

	/**
	 * @param profileId - if null, get the default one
	 * @return Returns the scannerConfigProfile for a project.
	 */
	public ScannerConfigProfile getSCProfileConfiguration(String profileId) {
		profileId = (profileId == NULL_PROFILE_ID) ? getDefaultSIProfileId() : profileId;
		return new ScannerConfigProfile(profileId);
	}

	/**
	 * @return a list of available scanner config profile id's.
	 */
	public List<String> getProfileIds() {
		synchronized (fLock) {
			if (profileIds == null) {
				profileIds = new ArrayList<String>();
				IExtensionPoint extension = Platform.getExtensionRegistry().
						getExtensionPoint(MakeCorePlugin.PLUGIN_ID, ScannerConfigProfileManager.SI_PROFILE_SIMPLE_ID);
				if (extension != null) {
					IExtension[] extensions = extension.getExtensions();
					for (int i = 0; i < extensions.length; ++i) {
						String rProfileId = extensions[i].getUniqueIdentifier();
						profileIds.add(rProfileId);
					}
				}
			}
		}
		return Collections.unmodifiableList(profileIds);	
	}
	
	/**
	 * @return the list of profile IDs supported for this context
	 */
	public List<String> getProfileIds(InfoContext context){
		if(context.isDefaultContext() || context.getProject() == null)
			return getProfileIds();
		
		synchronized (fLock) {
			if(contextAwareProfileIds == null){
				contextAwareProfileIds = new ArrayList<String>();
				List<String> all = getProfileIds();

				for(int i = 0; i < all.size(); i++){
					String id = all.get(i);
					ScannerConfigProfile profile = getSCProfileConfiguration(id);
					if(profile.supportsContext())
						contextAwareProfileIds.add(id);
				}
			}
		}
		return Collections.unmodifiableList(contextAwareProfileIds);
	}

	/**
	 * @return default profile id
	 */
	public static String getDefaultSIProfileId() {
		return DEFAULT_SI_PROFILE_ID;
	}
	
	/**
     * Set selectedProfile to profileId 
	 */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(IProject project, String profileId) throws CoreException {
		return ScannerConfigInfoFactory2.create(project, profileId);
	}

    /**
     * Use stored selectedProfile
     */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(IProject project) throws CoreException {
        return ScannerConfigInfoFactory2.create(project, ScannerConfigProfileManager.NULL_PROFILE_ID);
    }
    
    public static IScannerConfigBuilderInfo2Set createScannerConfigBuildInfo2Set(IProject project) throws CoreException {
        return ScannerConfigInfoFactory2.createInfoSet(project, ScannerConfigProfileManager.NULL_PROFILE_ID);
    }

    public static IScannerConfigBuilderInfo2Set createScannerConfigBuildInfo2Set(IProject project, String profileId) throws CoreException {
        return ScannerConfigInfoFactory2.createInfoSet(project, profileId);
    }

    public static IScannerConfigBuilderInfo2Set createScannerConfigBuildInfo2Set(Preferences prefs, boolean useDefaults) throws CoreException {
    	return ScannerConfigInfoFactory2.createInfoSet(prefs, ScannerConfigProfileManager.NULL_PROFILE_ID, useDefaults);
    }

    public static IScannerConfigBuilderInfo2Set createScannerConfigBuildInfo2Set(Preferences prefs, String profileId, boolean useDefaults) throws CoreException {
        return ScannerConfigInfoFactory2.createInfoSet(prefs, profileId, useDefaults);
    }

	/**
     * Set selectedProfile to profileId 
	 */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(Preferences prefs, String profileId, boolean useDefaults) {
		return ScannerConfigInfoFactory2.create(prefs, profileId, useDefaults);
	}

    /**
     * Use stored selectedProfile
     */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(Preferences prefs, boolean useDefaults) {
        return ScannerConfigInfoFactory2.create(prefs, ScannerConfigProfileManager.NULL_PROFILE_ID, useDefaults);
    }

}
