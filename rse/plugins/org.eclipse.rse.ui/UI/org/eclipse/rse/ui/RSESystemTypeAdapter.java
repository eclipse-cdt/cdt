/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 *                     - created and used RSEPreferencesManager
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [181939] avoid subsystem plugin activation just for enablement checking
 * Uwe Stieber (Wind River)      - [199032] [api] Remove method acceptContextMenuActionContribution(...) from RSESystemTypeAdapter
 * Martin Oberhuber (Wind River) - [218655][api] Provide SystemType enablement info in non-UI
 ********************************************************************************/
package org.eclipse.rse.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSESystemTypeConstants;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardDescriptor;
import org.eclipse.ui.IViewPart;
import org.osgi.framework.Bundle;

/**
 * Adapter for RSE system types.
 */
public class RSESystemTypeAdapter extends RSEAdapter {

	public RSESystemTypeAdapter() {
		super();
	}

	/**
     * Returns the image descriptor for the icon of this system type.
     * Returns the default live connection image descriptor if no icon has been configured.
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		ImageDescriptor img = getImage(object, IRSESystemTypeConstants.ICON);
		if (img==null) img = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID);
		return img;
	}

	/**
     * Returns the "live" image descriptor for this system type.
     *
     * If no "live" icon is found, but a non-live icon was specified,
     * the non-live icon is returned instead. If a non-live icon also
     * was not specified, the default live connection image descriptor
     * is returned.
     *
     * @param object The object to get an image descriptor for.
     * @return ImageDescriptor
	 */
	public ImageDescriptor getLiveImageDescriptor(Object object) {
		ImageDescriptor img = getImage(object, IRSESystemTypeConstants.ICON_LIVE);
		if (img==null) img = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE_ID);
		return img;
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
     * @param value the absolute or relative path
     * @param definingBundle bundle to be used for relative paths (may be null)
     * @return ImageDescriptor
     */
    public static ImageDescriptor getImage(String value, Bundle definingBundle) {
        URL url = getUrl(value, definingBundle);
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }

    /**
	 * Create a URL from the argument absolute or relative path. The bundle parameter is
	 * used as the base for relative paths and may be null.
	 *
	 * @param value the absolute or relative path
	 * @param definingBundle bundle to be used for relative paths (may be null)
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
			return ((IRSESystemType)object).getLabel();
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
		if (object instanceof IRSESystemType) {
			String property = ((IRSESystemType)object).getProperty(IRSESystemTypeConstants.ENABLE_OFFLINE);
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
	 * @deprecated Clients should use {@link IRSESystemType#isEnabled()}, which is
	 *     available without UI dependencies, in order to guarantee consistent
	 *     availability of system types.
	 *     Providers of custom RSESystemTypeAdapter implementations, which override
	 *     this method, should move their code to their IRSESystemType implementations.
	 *     See https://bugs.eclipse.org/bugs/show_bug.cgi?id=218655#c1 for details.
	 */
	public final boolean isEnabled(Object object) {
		IRSESystemType systemType = getSystemType(object);
		if (systemType != null) {
			return systemType.isEnabled();
		}
		return false;
	}

	/**
	 * Sets the enabled state of a particular system type.
	 * <p>
	 * Note that system types which are provided by extenders via the dynamic
	 * sytemTypeProviders extension point may have their own logic to determine
	 * whether they are enabled or not, so changing the enabled setting may not
	 * work for them.
	 *
	 * @param object The system type being adapted.
	 * @param isEnabled true if the system type is enabled. false if it is not.
	 * @deprecated Use
	 *             {@link RSEPreferencesManager#setIsSystemTypeEnabled(IRSESystemType, boolean)}
	 */
	public void setIsEnabled(Object object, boolean isEnabled) {
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			RSEPreferencesManager.setIsSystemTypeEnabled(systemType, isEnabled);
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
			result = RSEPreferencesManager.getDefaultUserId(systemType);
		}
		return result;
	}

	/**
	 * Set the default user id for this system type. Stored in the RSE core
	 * preferences.
	 * 
	 * @param object the system type that we are adapting
	 * @param defaultUserId the id to set for this system type
	 * @deprecated Use
	 *             {@link RSEPreferencesManager#setDefaultUserId(IRSESystemType, String)}
	 */
	public void setDefaultUserId(Object object, String defaultUserId) {
		IRSESystemType systemType = getSystemType(object);
		if ( systemType != null) {
			RSEPreferencesManager.setDefaultUserId(systemType, defaultUserId);
		}
	}

	private static IRSESystemType getSystemType(Object systemTypeCandidate) {
		IRSESystemType result = null;
		if (systemTypeCandidate instanceof IRSESystemType) {
			result = (IRSESystemType) systemTypeCandidate;
		}
		return result;
	}

	/**
	 * Called from <code>SystemViewPart#fillLocalToolBar(boolean)</code> to allow dynamic system
	 * type providers to customize the RSE standard toolbar structure regarding their needs.
	 * <p>
	 * <b>Note:</b> This method is called for each system type. If a single system type adapter
	 *              is associated with multiple system types, this method is called multiple times.
	 *              Overriders must check if the required groups are added to the toolbar already
	 *              and must avoid to add them multiple times!
	 *
	 * @param view The view to customize. Must be not <code>null</code>.
	 */
	public void addCustomToolbarGroups(IViewPart view) {
		// The static standard RSE system types have no custom toolbar groups.
	}

	/**
	 * Called from {@link org.eclipse.rse.internal.ui.view.SystemView#createStandardGroups(IMenuManager)} to allow dynamic system
	 * type providers to customize the RSE standard menu structure regarding their needs.
	 * <p>
	 * <b>Note:</b> This method is called for each system type. If a single system type adapter
	 *              is associated with multiple system types, this method is called multiple times.
	 *              Overriders must check if the required groups are added to the menu already
	 *              and must avoid to add them multiple times!
	 *
	 * @param menu The menu manager. Must be not <code>null</code>.
	 */
	public void addCustomMenuGroups(IMenuManager menu) {
		// The static standard RSE system types have no custom menu groups.
	}

	/**
	 * Checks if the given wizard descriptor is accepted for the system types the
	 * adapter is covering.
	 *
	 * @param wizardConfigurationElementName The wizard configuration element name. Must be not <code>null</code>.
	 * @param descriptor The wizard descriptor. Must be not <code>null</code>.
	 *
	 * @return <code>True</code> is accepted, <code>false</code> otherwise.
	 */
	public boolean acceptWizardDescriptor(String wizardConfigurationElementName, IRSEWizardDescriptor descriptor) {
		assert wizardConfigurationElementName != null && descriptor != null;

		// We always accept the default RSE new connection wizard
		if ("org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard".equals(descriptor.getId())) { //$NON-NLS-1$
			return true;
		}

		return false;
	}

}