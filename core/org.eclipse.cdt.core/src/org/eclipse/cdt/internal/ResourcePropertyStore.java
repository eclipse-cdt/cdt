package org.eclipse.cdt.internal;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.CoreException;

public class ResourcePropertyStore implements IPropertyStore {
	private IResource resource;
	private String pluginID;
	
	public ResourcePropertyStore(IResource resource, String pluginID) {
		this.resource = resource;
		this.pluginID = pluginID;
	}
	
	public String getString(String name) {
		QualifiedName qName = new QualifiedName(pluginID, name);
		try {
            return resource.getPersistentProperty(qName);
		} catch (CoreException e) {
        }
        return null;
	}
	
	public void setDefault(String name, String def) {
	}
	
	public void putValue(String name, String value) {
		QualifiedName qName = new QualifiedName(pluginID, name);
		try {
            resource.setPersistentProperty(qName, value);
		} catch (CoreException e) {
        }
	}

}

