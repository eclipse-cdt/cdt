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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.SystemPlugin;


/**
 * A utility class for quick open. It is a singleton.
 */
public class SystemQuickOpenUtil {
	
	public static final String QUICK_OPEN_PAGE_EXTENSION_POINT= "quickOpenPages";
	
	// singleton instance
	private static SystemQuickOpenUtil instance;
	
	// a list of page descriptors
	private List pageDescriptors;

	/**
	 * Constructor for the utility.
	 */
	private SystemQuickOpenUtil() {
		super();
	}
	
	/**
	 * Returns the singleton instance.
	 * @return the singleton instance. 
	 */
	public static SystemQuickOpenUtil getInstance() {
		
		if (instance == null) {
			instance = new SystemQuickOpenUtil();
		}
		
		return instance;
	}
	
	/**
	 * Returns all quick open pages contributed to the workbench.
	 * @param pageId a page id for which a descriptor must be returned.
	 * @return a list of quick open page descriptors.
	 */
	public List getQuickOpenPageDescriptors(String pageId) {
		Iterator iter = getQuickOpenPageDescriptors().iterator();
		List enabledDescriptors = new ArrayList();
		
		while (iter.hasNext()) {
			SystemQuickOpenPageDescriptor desc = (SystemQuickOpenPageDescriptor)(iter.next());
			
			if (desc.isEnabled() || desc.getId().equals(pageId)) {
				enabledDescriptors.add(desc);
			}
		}
		
		return enabledDescriptors;
	}
	
	/**
	 * Returns all quick open pages contributed to the workbench.
	 * @return a list of quick open pages.
	 */
	public List getQuickOpenPageDescriptors() {
		
		if (pageDescriptors == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(SystemPlugin.PLUGIN_ID, QUICK_OPEN_PAGE_EXTENSION_POINT);
			pageDescriptors = createQuickOpenPageDescriptors(elements);
		}
			
		return pageDescriptors;
	} 
	
	/**
	 * Creates quick open page descriptors.
	 * @param an array of elements.
	 * @return a list of descriptors that correspond to the given elements.
	 */
	private List createQuickOpenPageDescriptors(IConfigurationElement[] elements) {
		List result = new ArrayList();
		
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			
			if (SystemQuickOpenPageDescriptor.PAGE_TAG.equals(element.getName())) {
				SystemQuickOpenPageDescriptor desc = new SystemQuickOpenPageDescriptor(element);
				result.add(desc);
			}
		}
		
		// sort the list of descriptors
		Collections.sort(result);
		
		return result;
	}
}