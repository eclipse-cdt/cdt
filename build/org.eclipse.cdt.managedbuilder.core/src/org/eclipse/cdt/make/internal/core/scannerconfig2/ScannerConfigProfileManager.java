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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IConfigurationScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
	private static final String OLD_MAKE_CORE_PLUGIN_ID = "org.eclipse.cdt.make.core";	//$NON-NLS-1$
	public static final String SI_PROFILE_SIMPLE_ID = "ScannerConfigurationDiscoveryProfile";	//$NON-NLS-1$
	public static final String PER_PROJECT_PROFILE_ID = OLD_MAKE_CORE_PLUGIN_ID + ".GCCStandardMakePerProjectProfile"; //$NON-NLS-1$
	public static final String DEFAULT_SI_PROFILE_ID = PER_PROJECT_PROFILE_ID;
    public static final String NULL_PROFILE_ID = "";//$NON-NLS-1$
    public static final String OLD_SI_PROFILE_FULL_ID = OLD_MAKE_CORE_PLUGIN_ID + "." + SI_PROFILE_SIMPLE_ID;	//$NON-NLS-1$
	
    private class CachedInstance {
    	InfoContext context;
    	SCProfileInstance instance;
    	
    	CachedInstance(InfoContext c, SCProfileInstance instance){
    		this.context = c;
    		this.instance = instance;
    	
    	}
    }
	private Map projectProfileInstance;
	private List profileIds;
	
	/**
	 * Singleton pattern
	 */
	private ScannerConfigProfileManager() {
		projectProfileInstance = new HashMap();
	}
	private static ScannerConfigProfileManager instance = null;

	public static ScannerConfigProfileManager getInstance() {
		if (instance == null) {
			instance = new ScannerConfigProfileManager();
		}
		return instance;
	}

	/*uncomment
	private String getProfileId(IProject project) {
		String profileId;
		IScannerConfigBuilderInfo2 buildInfo = null;
		try {
			buildInfo = createScannerConfigBuildInfo2(project);
			profileId = buildInfo.getSelectedProfileId();
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			profileId = DEFAULT_SI_PROFILE_ID;
		}
		return profileId;
	}
	*/

	private String getProfileId(InfoContext context) {
		String profileId;
		IScannerConfigBuilderInfo2 buildInfo = null;
		try {
			buildInfo = createScannerConfigBuildInfo2(context);
			profileId = buildInfo.getSelectedProfileId();
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
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
//	public void addProfile(IProject project, ScannerConfigProfile profile) {
//		projectProfileInstance.put(project, profile);
//	}
	
	/**
	 * @param project
	 * @param profileId - if null, get the one associated with the project
	 * @return Returns the scannerConfigProfile instance for a project.
	 */
	/*uncomment
	public SCProfileInstance getSCProfileInstance(IProject project, String profileId) {
        // if not specified read from .project file
        if (profileId == NULL_PROFILE_ID) {
            profileId = getProfileId(project);
        }
        // is the project's profile already loaded?
        SCProfileInstance profileInstance = (SCProfileInstance) projectProfileInstance.get(project);
        if (profileInstance == null || !profileInstance.getProfile().getId().equals(profileId)) {
            profileInstance = new SCProfileInstance(project, getSCProfileConfiguration(profileId));
            projectProfileInstance.put(project, profileInstance);
        }
        return profileInstance;
	}
	*/

	public SCProfileInstance getSCProfileInstance(IProject project, InfoContext context, String profileId) {
		if(context == null)
			context = ScannerConfigUtil.createContextForProject(project);
		return getSCProfileInstance(context, profileId);
	}

	public SCProfileInstance getSCProfileInstance(InfoContext context, String profileId) {
        // if not specified read from .project file
        if (profileId == NULL_PROFILE_ID) {
            profileId = getProfileId(context);
        }
        IResource owner = context.getConfiguration().getOwner();
        IProject project = null;
        if(owner != null)
        	owner.getProject();

        // is the project's profile already loaded?
        CachedInstance ci = (CachedInstance)projectProfileInstance.get(project);
        SCProfileInstance profileInstance = null; 
        if (ci == null || !ci.context.equals(context) || !ci.instance.getProfile().getId().equals(profileId)) {
            profileInstance = new SCProfileInstance(context, getSCProfileConfiguration(profileId));
            projectProfileInstance.put(project, new CachedInstance(context, profileInstance));
        } else {
        	profileInstance = ci.instance;
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
            profileInstance = new SCProfileInstance((IProject)null, getSCProfileConfiguration(profileId));
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

	private IExtension[] getExtensions(){
		IExtensionPoint extension = Platform.getExtensionRegistry().
		getExtensionPoint(ManagedBuilderCorePlugin.getUniqueIdentifier(), ScannerConfigProfileManager.SI_PROFILE_SIMPLE_ID);
		List list = new ArrayList();
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			list.addAll(Arrays.asList(extensions));
		}
		
		extension = Platform.getExtensionRegistry().
		getExtensionPoint(ScannerConfigProfileManager.OLD_SI_PROFILE_FULL_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			list.addAll(Arrays.asList(extensions));
		}
		
		return (IExtension[])list.toArray(new IExtension[list.size()]);

	}

	/**
	 * @return
	 */
	public List getProfileIds() {
		if (profileIds == null) {
			profileIds = new ArrayList();
//			IExtensionPoint extension = Platform.getExtensionRegistry().
//					getExtensionPoint(ManagedBuilderCorePlugin.getUniqueIdentifier(), ScannerConfigProfileManager.SI_PROFILE_SIMPLE_ID);
//			if (extension != null) {
				IExtension[] extensions = getExtensions(); //extension.getExtensions();
				for (int i = 0; i < extensions.length; ++i) {
					String rProfileId = extensions[i].getUniqueIdentifier();
					profileIds.add(rProfileId);
				}
//			}
		}
		return profileIds;	
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
	/*uncomment
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(IProject project, String profileId) throws CoreException {
		return ScannerConfigInfoFactory2.create(project, profileId);
	}
	*/

    /**
     * Use stored selectedProfile
     * @param project
     * @return
     * @throws CoreException
     */
    /*uncomment
    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(IProject project) throws CoreException {
        return ScannerConfigInfoFactory2.create(project, ScannerConfigProfileManager.NULL_PROFILE_ID);
    }
    */

    public static IScannerConfigBuilderInfo2 createScannerConfigBuildInfo2(InfoContext context) throws CoreException {
    	return ScannerConfigInfoFactory2.create(context, ScannerConfigProfileManager.NULL_PROFILE_ID);
    }
    
    public static IConfigurationScannerConfigBuilderInfo getCfgScannerConfigBuildInfo(IConfiguration cfg){
    	return ScannerConfigInfoFactory2.create(cfg, false);
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
