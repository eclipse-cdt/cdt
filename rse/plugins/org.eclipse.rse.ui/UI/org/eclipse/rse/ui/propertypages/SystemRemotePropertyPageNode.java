/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.propertypages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemPropertyPageExtension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPropertyPage;


/**
 * Our version of PropertyPageNode that does not require a RegistryPageContributor input.
 */
public class SystemRemotePropertyPageNode extends PreferenceNode 
{
	
	private SystemPropertyPageExtension contributor;
	private IWorkbenchPropertyPage page;
	private Image icon;
	private IAdaptable element;
	/**
	 * Constructor.
	 */
	public SystemRemotePropertyPageNode(SystemPropertyPageExtension contributor, IAdaptable element) 
	{
		super(contributor.getId());
		this.contributor = contributor;
		this.element = element;
	}
	/**
	 * Creates the preference page this node stands for. If the page is null,
	 * it will be created by loading the class. If loading fails,
	 * empty filler page will be created instead.
	 */
	public void createPage() 
	{
		page = contributor.createPage(element);
		setPage(page);
	}
	/** (non-Javadoc)
	 * Method declared on IPreferenceNode.
	 */
	public void disposeResources() 
	{
		page = null;
		if (icon != null) 
		{
			icon.dispose();
			icon = null;
		}
	}
	/**
	 * Returns page icon, if defined.
	 */
	public Image getLabelImage() 
	{
		if (icon==null) 
		{
			ImageDescriptor desc = contributor.getImage();
			if (desc != null) 
			{
				icon = desc.createImage();
			}
		}
		return icon;
	}
	/**
	 * Returns page label as defined in the registry.
	 */
	public String getLabelText() 
	{
		return contributor.getName();	
	}
		
}