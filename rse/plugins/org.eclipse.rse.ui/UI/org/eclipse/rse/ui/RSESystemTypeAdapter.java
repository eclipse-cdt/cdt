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

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
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
     * Create an url from the argument absolute or relative path. The bundle
     * parameter is used as the base for relative paths and is allowed to be
     * null.
     *
     * @param value
     *            the absolute or relative path
     * @param definingBundle
     *            bundle to be used for relative paths (may be null)
     * @return
     */
    public static URL getUrl(String value, Bundle definingBundle) {
        try {
            if (value != null)
                return new URL(value);
        } catch (MalformedURLException e) {
            if (definingBundle != null)
                return Platform.find(definingBundle, new Path(value));
        }

        return null;
    }

	/**
	 * Returns the name of the system type if the object passed in is of type <code>IRSESystemType</code>.
	 * Otherwise, returns the value of the parent implementation.
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

	public boolean isEnabled(Object object) {
		//TODO
		return true;
	}

	public void setIsEnabled(Object object, boolean isEnabled) {
		//TODO
	}

	public String getDefaultUserId(Object object) {
		//TODO
		return "";
	}

	public void setDefaultUserId(Object object, String defaultUserId) {
		//TODO
	}
}