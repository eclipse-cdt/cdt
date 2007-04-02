/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - 168870: created adapter for ui portions of SubSystemConfigurationProxy
 ********************************************************************************/
package org.eclipse.rse.internal.ui.subsystems;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class SubSystemConfigurationProxyAdapter {
	
	private static final ImageDescriptor defaultNormalDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID);
	private static final ImageDescriptor defaultLiveDescriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE_ID);
	private ISubSystemConfigurationProxy proxy = null;
	
	/**
	 * Create the adapter. This should be done only by the adapter factory.
	 * @param proxy The {@link ISubSystemConfigurationProxy} for which to create this adapter.
	 */
	public SubSystemConfigurationProxyAdapter(ISubSystemConfigurationProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
	 * Get an image descriptor given an image location
	 * @param url the location of the image
	 * @param defaultDescriptor the descriptor to use if a resource at this location is not found
	 * @return an image descriptor
	 */
	private ImageDescriptor getDescriptor(URL url, ImageDescriptor defaultDescriptor) {
		ImageDescriptor result = null;
		if (url != null) {
			result = ImageDescriptor.createFromURL(url);
		}
		if (result == null) {
			result = defaultDescriptor;
		}
		return result;
	}
	
	/**
	 * @return the image descriptor representing the non-connected state of this particular subsystem
	 */
	public ImageDescriptor getImageDescriptor() {
		URL url = proxy.getImageLocation();
		ImageDescriptor result = getDescriptor(url, defaultNormalDescriptor);
		return result;
	}

	/**
	 * @return the image descriptor representing the connected state of the particular subsystem
	 */
	public ImageDescriptor getLiveImageDescriptor() {
		URL url = proxy.getLiveImageLocation();
		ImageDescriptor result = getDescriptor(url, defaultLiveDescriptor);
		return result;
	}

}
