/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;

public class CPathContainerDescriptor implements IContainerDescriptor {

	private IConfigurationElement fConfigElement;

	private static final String ATT_EXTENSION = "PathContainerPage"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_PAGE_CLASS = "class"; //$NON-NLS-1$	

	private Image pageImage;

	public CPathContainerDescriptor(IConfigurationElement configElement) throws CoreException {
		super();
		fConfigElement = configElement;

		String id = fConfigElement.getAttribute(ATT_ID);
		String name = configElement.getAttribute(ATT_NAME);
		String pageClassName = configElement.getAttribute(ATT_PAGE_CLASS);

		if (name == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Invalid extension (missing name): " + id, //$NON-NLS-1$
					null));
		}
		if (pageClassName == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0,
					"Invalid extension (missing page class name): " + id, null)); //$NON-NLS-1$
		}
	}

	public ICPathContainerPage createPage() throws CoreException {
		Object elem = CoreUtility.createExtension(fConfigElement, ATT_PAGE_CLASS);
		if (elem instanceof ICPathContainerPage) {
			return (ICPathContainerPage) elem;
		} else {
			String id = fConfigElement.getAttribute(ATT_ID);
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0,
					"Invalid extension (page not of type IClasspathContainerPage): " + id, null)); //$NON-NLS-1$
		}
	}

	public String getName() {
		return fConfigElement.getAttribute(ATT_NAME);
	}

	public Image getImage() {
		if (pageImage == null) {
			String imageName = fConfigElement.getAttribute(ATT_ICON);
			if (imageName != null) {
				IExtension extension = fConfigElement.getDeclaringExtension();
				IPluginDescriptor pd = extension.getDeclaringPluginDescriptor();
				Image image = getImageFromPlugin(pd, imageName);
				pageImage = image;
			}
		}
		return pageImage;
	}

	public Image getImageFromPlugin(IPluginDescriptor pluginDescriptor, String subdirectoryAndFilename) {
		URL installURL = pluginDescriptor.getInstallURL();
		return getImageFromURL(installURL, subdirectoryAndFilename);
	}

	public Image getImageFromURL(URL installURL, String subdirectoryAndFilename) {
		Image image = null;
		try {
			URL newURL = new URL(installURL, subdirectoryAndFilename);
			ImageDescriptor desc = ImageDescriptor.createFromURL(newURL);
			image = desc.createImage();
		} catch (MalformedURLException e) {
		} catch (SWTException e) {
		}
		return image;
	}

	public String getPageClass() {
		return fConfigElement.getAttribute(ATT_PAGE_CLASS);
	}

	public boolean canEdit(IPathEntry entry) {
		String id = fConfigElement.getAttribute(ATT_ID);
		if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
			String type = entry.getPath().segment(0);
			return id.equals(type);
		}
		return false;
	}

	public static IContainerDescriptor[] getDescriptors() {
		ArrayList containers = new ArrayList();

		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, ATT_EXTENSION);
		if (extensionPoint != null) {
			IContainerDescriptor defaultPage = null;
			String defaultPageName = CPathContainerDefaultPage.class.getName();

			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				try {
					CPathContainerDescriptor curr = new CPathContainerDescriptor(elements[i]);
					if (defaultPageName.equals(curr.getPageClass())) {
						defaultPage = curr;
					} else {
						containers.add(curr);
					}
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
			if (defaultPageName != null && containers.isEmpty()) {
				// default page only added if no other extensions found
				containers.add(defaultPage);
			}
		}
		return (CPathContainerDescriptor[]) containers.toArray(new CPathContainerDescriptor[containers.size()]);
	}

}