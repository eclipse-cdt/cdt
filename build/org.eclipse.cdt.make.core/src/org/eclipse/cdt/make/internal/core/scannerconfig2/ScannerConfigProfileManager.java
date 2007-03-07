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
public class ScannerConfigProfileManager {
	public static final String SI_PROFILE_SIMPLE_ID = "ScannerConfigurationDiscoveryProfile";	//$NON-NLS-1$
	public static final String PER_PROJECT_PROFILE_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCStandardMakePerProjectProfile"; //$NON-NLS-1$
	public static final String DEFAULT_SI_PROFILE_ID = PER_PROJECT_PROFILE_ID;
    public static final String NULL_PROFILE_ID = "";//$NON-NLS-1$
	
	private Map projectToProfileInstanceMap;
	private List profileIds;
	private List contextAwareProfileIds;
	
	/**
	 * Singleton pattern
	 */
	private ScannerConfigProfileManager() {
		projectToProfileInstanceMap = new HashMap();
	}
	private static ScannerConfigProfileManager instance = null;

	public static ScannerConfigProfileManager getInstance() {
		if (instance == null) {
			instance = new ScannerConfigProfileManager();
		}
		return instance;
	}

	private String getProfileId(IProject project, InfoContext context) {
		String profileId;
		try {
			IScannerConfigBuilderInfo2Set container = createScannerConfigBuildInfo2Set(project);
			IScannerConfigBuilderInfo2 buildInfo = container.getInfo(context);
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
	 * @param project
	 * @param profile
	 */
	public void addProfile(IProject project, ScannerConfigProfile profile) {
		addProfile(project, new InfoContext(project), profile);
	}

	public void addProfile(IProject project, InfoContext context, ScannerConfigProfile profile) {
		getProfileMap(project, true).put(context, profile);
	}
	
	private Map getProfileMap(IProject project, boolean create){
		Map map = (Map)projectToProfileInstanceMap.get(project);
		if(map == null && create){
			map = new HashMap();
			projectToProfileInstanceMap.put(project, map);
		}
		return map;
	}
	
	/**
	 * @param project
	 * @param profileId - if null, get the one associated with the project
	 * @return Returns the scannerConfigProfile instance for a project.
	 */
	public SCProfileInstance getSCProfileInstance(IProject project, String profileId) {
		return getSCProfileInstance(project, new InfoContext(project), profileId);
	}

	public SCProfileInstance getSCProfileInstance(IProject project, InfoContext context, String profileId) {

		// if not specified read from .project file
        if (profileId == NULL_PROFILE_ID) {
            profileId = getProfileId(project, context);
        }
        // is the project's profile already loaded?
        Map map = getProfileMap(project, true);
        SCProfileInstance profileInstance = (SCProfileInstance) map.get(context);
        if (profileInstance == null || !profileInstance.getProfile().getId().equals(profileId)) {
            profileInstance = new SCProfileInstance(project, context, getSCProfileConfiguration(profileId));
            map.put(context, profileInstance);
        }
        return profileInstance;
	}

    /**
     * @param profileId
     * @return
     */
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
	 * @return
	 */
	public List getProfileIds() {
		if (profileIds == null) {
			profileIds = new ArrayList();
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
		return profileIds;	
	}
	
	/**
	 * returns the list od profile IDs supported for this context
	 * @param context
	 * @return
	 */
	public List getProfileIds(InfoContext context){
		if(context.isDefaultContext() || context.getProject() == null)
			return getProfileIds();
		
		if(contextAwareProfileIds == null){
			contextAwareProfileIds = new ArrayList();
			List all = getProfileIds();
			
			for(int i = 0; i < all.size(); i++){
				String id = (String)all.get(i);
				ScannerConfigProfile profile = getSCProfileConfiguration(id);
				if(profile.supportsContext())
					contextAwareProfileIds.add(id);
			}
		}
		
		return contextAwareProfileIds;
	}

	/**
	 * @return default profile id
	 */
	public static String getDefaultSIProfileId() {
		return DEFAULT_SI_PROFILE_ID;
	}
	
	/**
     * Set selectedProfile to profileId 
     * @param project
     * @param profileId
     * @return
     * @throws CoreException
	 */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(IProject project, String profileId) throws CoreException {
		return ScannerConfigInfoFactory2.create(project, profileId);
	}

    /**
     * Use stored selectedProfile
     * @param project
     * @return
     * @throws CoreException
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
     * @param prefs
     * @param profileId
     * @param useDefaults
     * @return
	 */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(Preferences prefs, String profileId, boolean useDefaults) {
		return ScannerConfigInfoFactory2.create(prefs, profileId, useDefaults);
	}

    /**
     * Use stored selectedProfile
     * @param prefs
     * @param useDefaults
     * @return
     */
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(Preferences prefs, boolean useDefaults) {
        return ScannerConfigInfoFactory2.create(prefs, ScannerConfigProfileManager.NULL_PROFILE_ID, useDefaults);
    }

}
