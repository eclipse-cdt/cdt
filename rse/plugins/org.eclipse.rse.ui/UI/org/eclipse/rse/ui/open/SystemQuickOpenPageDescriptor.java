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

package org.eclipse.rse.ui.open;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;


public class SystemQuickOpenPageDescriptor implements Comparable {
	
	public final static String PAGE_TAG = "page";
	private final static String ID_ATTRIBUTE = "id";
	private final static String ICON_ATTRIBUTE = "icon";
	private final static String CLASS_ATTRIBUTE = "class";
	private final static String LABEL_ATTRIBUTE = "label";
	private final static String SIZE_ATTRIBUTE = "sizeHint";
	private final static String TAB_POSITION_ATTRIBUTE = "tabPosition";
	// private final static String SSF_ID = "ssfid";
	
	public final static Point UNKNOWN_SIZE = new Point(SWT.DEFAULT, SWT.DEFAULT);
	
	private IConfigurationElement element;

	/**
	 * Constructor for quick open page descriptor.
	 * @param a configuration element.
	 */
	public SystemQuickOpenPageDescriptor(IConfigurationElement element) {
		this.element = element;
	}
	
	/**
	 * Creates a new quick open page from the descriptor.
	 */
	public ISystemQuickOpenPage createObject() {
		ISystemQuickOpenPage result = null;
		
		try {
			result = (ISystemQuickOpenPage)(element.createExecutableExtension(CLASS_ATTRIBUTE));
		}
		catch (CoreException e) {
			SystemBasePlugin.logError("Error trying to create a quick open page from configuration element", e);
			return null;
		}
		catch (ClassCastException e) {
			SystemBasePlugin.logError("Error trying to create a quick open page from configuration element", e);
			return null;
		}
		
		if (result != null) {
			result.setTitle(getLabel());
		}
		
		return result;
	}
	
	
	// --------------------------------------------------------------
	// XML attributes
	// --------------------------------------------------------------

	/**
	 * Returns the id of the page.
	 * @return the id of the page.
	 */
	public String getId() {
		return element.getAttribute(ID_ATTRIBUTE);
	}
	
	/**
	 * Returns the label of the page.
	 */
	public String getLabel() {
		return element.getAttribute(LABEL_ATTRIBUTE);
	}
	 
	/**
	 * Returns the image for the page.
	 */
	public ImageDescriptor getImage() {
		
		String imageName = element.getAttribute(ICON_ATTRIBUTE);
		
		if (imageName == null) {
			return null;
		}
		
		URL url = null;
		
		try {
		    String nameSpace = element.getDeclaringExtension().getNamespace();
		    Bundle bundle = Platform.getBundle(nameSpace);
			url = new URL(bundle.getEntry("/"), imageName);
		}
		catch (java.net.MalformedURLException e) {
			SystemBasePlugin.logError("Error trying to get image", e);
			return null;
		}
		
		return ImageDescriptor.createFromURL(url);
	}
	
	/**
	 * Returns the page's preferred size
	 */
	public Point getPreferredSize() {
		return StringConverter.asPoint(element.getAttribute(SIZE_ATTRIBUTE), UNKNOWN_SIZE);
	}
	
	/**
	 * Returns the page's tab position relative to the other tabs.
	 * @return	the tab position or <code>Integer.MAX_VALUE</code> if not defined in
				the plugins.xml file
	 *
	 */
	public int getTabPosition() {
		
		int position = Integer.MAX_VALUE / 2;
		
		String str = element.getAttribute(TAB_POSITION_ATTRIBUTE);
		
		if (str != null) {
			
			try {
				position = Integer.parseInt(str);
			}
			catch (NumberFormatException e) {
				SystemBasePlugin.logError("Error trying to get tab position", e);			
			}
		}
		
		return position;
	}
	
	/**
	 * Returns whether the page is enabled.
	 * @return <code>true</code> if the page is enabled, <code>false</code> otherwise.
	 */
	public boolean isEnabled() {
		return true;
	}
	
	
	// -----------------------------------------------------------
	// compare
	// -----------------------------------------------------------

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		
		int myPos = getTabPosition();
		int objsPos = ((SystemQuickOpenPageDescriptor)o).getTabPosition();
		
		if (myPos == Integer.MAX_VALUE && objsPos == Integer.MAX_VALUE || myPos == objsPos) {
			return getLabel().compareTo(((SystemQuickOpenPageDescriptor)o).getLabel());
		}
		else {
			return myPos - objsPos;
		}
	}
}