/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.osgi.framework.Bundle;

/**
 * Adapter for RSE system types.
 */
public class RSESystemTypeAdapter extends RSEAdapter implements IRSESystemTypeConstants {

	public RSESystemTypeAdapter() {
		super();
	}

	/**
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return getImage(object, ICON);
	}

	/**
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getLiveImageDescriptor(Object object) {
		return getImage(object, ICON_LIVE);
	}

	private ImageDescriptor getImage(Object object, String propertyKey) {

		if ((object != null) && (object instanceof IRSESystemType)) {
			IRSESystemType sysType = (IRSESystemType)object;

			String property = sysType.getProperty(propertyKey);

			if (property != null) {
				return getImage(property, sysType.getDefiningBundle());
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

    /**
     * Create a descriptor from the argument absolute or relative path to an
     * image file. bundle parameter is used as the base for relative paths and
     * is allowed to be null.
     *
     * @param value
     *            the absolute or relative path
     * @param definingBundle
     *            bundle to be used for relative paths (may be null)
     * @return
     */
    public static ImageDescriptor getImage(String value, Bundle definingBundle) {
        URL url = getUrl(value, definingBundle);
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }

    /**
	 * Create a URL from the argument absolute or relative path. The bundle parameter is 
	 * used as the base for relative paths and may be null.
	 * 
	 * @param value
	 *            the absolute or relative path
	 * @param definingBundle
	 *            bundle to be used for relative paths (may be null)
	 * @return the URL to the resource
	 */
	public static URL getUrl(String value, Bundle definingBundle) {
		URL result = null;
		try {
			if (value != null) {
				result = new URL(value);
			}
		} catch (MalformedURLException e) {
			if (definingBundle != null) {
				IPath path = new Path(value);
				result = FileLocator.find(definingBundle, path, null);
			}
		}
		return result;
	}

	/**
	 * Returns the name of the system type if the object passed in is of type <code>IRSESystemType</code>. Otherwise, returns the value of the parent implementation.
	 * 
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object object) {

		if ((object != null) && (object instanceof IRSESystemType)) {
			return ((IRSESystemType)object).getName();
		}
		else {
			return super.getLabel(object);
		}
	}

	/**
	 * Returns the description of the system type if the object passed in is of type <code>IRSESystemType</code>.
	 * Otherwise, returns the value of the parent implementation.
	 * @see org.eclipse.rse.ui.RSEAdapter#getDescription(java.lang.Object)
	 */
	public String getDescription(Object object) {

		if ((object != null) && (object instanceof IRSESystemType)) {
			return ((IRSESystemType)object).getDescription();
		}
		else {
			return super.getDescription(object);
		}
	}

	public boolean isEnableOffline(Object object) {
		if ((object != null) && (object instanceof IRSESystemType)) {
			String property = ((IRSESystemType)object).getProperty(ENABLE_OFFLINE);
			if (property != null) {
				return Boolean.valueOf(property).booleanValue();
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Returns the enabled state of a particular system type.
	 * @param object the object being adapted, usually a system type.
	 * @return true if that system type is enabled. false if the object is
	 * not a system type or if it is not enabled.
	 */
	public boolean isEnabled(Object object) {
		boolean result = false;
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			Preferences prefs = RSECorePlugin.getDefault().getPluginPreferences();
			String key = getPreferencesKey(systemType, IRSEPreferenceNames.ST_ENABLED);
			if (!prefs.contains(key)) {
				prefs.setDefault(key, true);
			}
			result = prefs.getBoolean(key);
		}
		return result;
	}

	/**
	 * Sets the enabled state of a particular system type.
	 * @param object The system type being adapted.
	 * @param isEnabled true if the system type is enabled. false if it is not.
	 */
	public void setIsEnabled(Object object, boolean isEnabled) {
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			Plugin core = RSECorePlugin.getDefault();
			Preferences prefs = core.getPluginPreferences();
			String key = getPreferencesKey(systemType, IRSEPreferenceNames.ST_ENABLED);
			prefs.setValue(key, isEnabled);
			core.savePluginPreferences();
		}
	}

	/**
	 * Return the default user id for a particular system type. If none
	 * is defined then the "user.name" system property is used.
	 * @param object The system type being adapted.
	 * @return The default user id. Will be null if the object is not a system type
	 */
	public String getDefaultUserId(Object object) {
		String result = null;
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			Preferences prefs = RSECorePlugin.getDefault().getPluginPreferences();
			String key = getPreferencesKey(systemType, IRSEPreferenceNames.ST_DEFAULT_USERID);
			if (!prefs.contains(key)) {
				prefs.setDefault(key, System.getProperty("user.name"));
			}
			result = prefs.getString(key);
		}
		return result;
	}

	/**
	 * Set the default user id for this system type. Stored in the RSE core preferences.
	 * @param object the system type that we are adapting
	 * @param defaultUserId the id to set for this system type
	 */
	public void setDefaultUserId(Object object, String defaultUserId) {
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			Plugin core = RSECorePlugin.getDefault();
			Preferences prefs = core.getPluginPreferences();
			String key = getPreferencesKey(systemType, IRSEPreferenceNames.ST_DEFAULT_USERID);
			prefs.setValue(key, defaultUserId);
			core.savePluginPreferences();
		}
	}
	
	private String getPreferencesKey(IRSESystemType systemType, String preference) {
		String key = systemType.getName() + "." + preference;
		return key;
	}
	
	private IRSESystemType getSystemType(Object systemTypeCandidate) {
		IRSESystemType result = null;
		if (systemTypeCandidate instanceof IRSESystemType) {
			result = (IRSESystemType) systemTypeCandidate;
		}
		return result;
	}
	
}