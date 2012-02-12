/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;


// ---------------------------------------------------------------------------
// Extension
// ---------------------------------------------------------------------------

/** Facade/utility class for dealing with Eclipse extensions. */
public class Extension extends ExtensionElement
{	
	// --- static methods ---
	
    /** Gets extension point declaration with specified full ID (including plugin ID prefix). */
    public static IExtensionPoint getExtensionPoint(String extensionPointID)
    {
		IExtensionPoint extensionPoint =
			Platform.getExtensionRegistry().getExtensionPoint(
				extensionPointID);
		return extensionPoint;
    }

    /** Gets extension point declaration with specified name defined by specified plugin. */
    public static IExtensionPoint getExtensionPoint(String pluginID, String extensionPointName)
    {
		IExtensionPoint extensionPoint =
			Platform.getExtensionRegistry().getExtensionPoint(
				pluginID, extensionPointName);
		return extensionPoint;
    }

	/** Gets list of extensions for specified extension point ID (including plugin ID prefix). */
	public static List<Extension> getExtensions(String extensionPointID)
	{
		IExtensionPoint extensionPoint = getExtensionPoint(extensionPointID);
		return getExtensions(extensionPoint);
	}

	/** Gets list of extensions for specified plugin and extension point name. */
	public static List<Extension> getExtensions(String pluginID, String extensionPointName)
	{
		IExtensionPoint extensionPoint = getExtensionPoint(pluginID, extensionPointName);
		return getExtensions(extensionPoint);
	}
	
	/** Gets list of extensions for specified plugin and extension point name. */
	public static List<Extension> getExtensions(IExtensionPoint extensionPoint)
	{
		return (extensionPoint == null) ? null :
			Extension.wrapExtensions(extensionPoint.getConfigurationElements());
	}

	/** Wraps list of raw extension declarations. */
	public static List<Extension> wrapExtensions(IConfigurationElement[] elements)
	{
		int count = (elements == null) ? 0 : elements.length;
		List<Extension> result = new ArrayList<Extension>(count);
		for (int i=0; i<count; ++i) {
			result.add(new Extension(elements[i]));
		}
		return result;
	}
	
   	// --- members ---
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public Extension(IConfigurationElement element) {
		super(element);
	}
	
	// --- methods ---
	
}
