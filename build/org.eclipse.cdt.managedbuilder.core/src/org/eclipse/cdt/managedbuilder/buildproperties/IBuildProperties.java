/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildproperties;

import org.eclipse.core.runtime.CoreException;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildProperties extends Cloneable {
	IBuildProperty[] getProperties();
	
	IBuildProperty getProperty(String id);
	
//	IBuildProperty addProperty(IBuildProperty property);
	
	IBuildProperty setProperty(String propertyId, String propertyValue) throws CoreException;

//	IBuildProperty addProperty(IBuildPropertyType type, String propertyValue) throws CoreException;

	IBuildProperty removeProperty(String id);
	
	boolean containsValue(String propertyId, String valueId);
	
//	IBuildProperty removeProperty(IBuildPropertyType propertyType);

	void clear();
	
	Object clone();
}
