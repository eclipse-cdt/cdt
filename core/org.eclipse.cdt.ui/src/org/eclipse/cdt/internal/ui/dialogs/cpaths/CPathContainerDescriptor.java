/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


public class CPathContainerDescriptor {
	private IConfigurationElement fConfigElement;

	private static final String ATT_EXTENSION = "CPathContainerPage"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_PAGE_CLASS = "class"; //$NON-NLS-1$	

	public CPathContainerDescriptor(IConfigurationElement configElement) throws CoreException {
		super();
		fConfigElement = configElement;

		String id = fConfigElement.getAttribute(ATT_ID);
		String name = configElement.getAttribute(ATT_NAME);
		String pageClassName = configElement.getAttribute(ATT_PAGE_CLASS);

		if (name == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Invalid extension (missing name): " + id, null)); //$NON-NLS-1$
		}
		if (pageClassName == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Invalid extension (missing page class name): " + id, null)); //$NON-NLS-1$
		}
	}

	public ICPathContainerPage createPage() throws CoreException {
		Object elem= CoreUtility.createExtension(fConfigElement, ATT_PAGE_CLASS);
		if (elem instanceof ICPathContainerPage) {
			return (ICPathContainerPage) elem;
		} else {
			String id= fConfigElement.getAttribute(ATT_ID);
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Invalid extension (page not of type IClasspathContainerPage): " + id, null)); //$NON-NLS-1$
		}
	}

	public String getName() {
		return fConfigElement.getAttribute(ATT_NAME);
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

	public static CPathContainerDescriptor[] getDescriptors() {
		ArrayList containers= new ArrayList();
		
		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, ATT_EXTENSION);
		if (extensionPoint != null) {
			CPathContainerDescriptor defaultPage= null;
			String defaultPageName= CPathContainerDefaultPage.class.getName();
			
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				try {
					CPathContainerDescriptor curr= new CPathContainerDescriptor(elements[i]);					
					if (defaultPageName.equals(curr.getPageClass())) {
						defaultPage= curr;
					} else {
						containers.add(curr);
					}
				} catch (CoreException e) {
					CUIPlugin.getDefault().log(e);
				}
			}
			if (defaultPageName != null && containers.isEmpty()) {
				// default page only added of no other extensions found
				containers.add(defaultPage);
			}
		}
		return (CPathContainerDescriptor[]) containers.toArray(new CPathContainerDescriptor[containers.size()]);
	}

}
