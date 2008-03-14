package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;

/**
 * @author coulthar
 *
 * Classes that implement this know how to create the approprate
 * subclass of SystemXMLElementWrapper
 */
public interface ISystemXMLElementWrapperFactory {
	/**
	 * Given an xml element node, create an instance of the appropriate
	 * subclass of SystemXMLElementWrapper to represent it.
	 */
	public SystemXMLElementWrapper createElementWrapper(IPropertySet xmlElementToWrap, ISystemProfile profile, int domain);

	/**
	 * Return the tag name for these elements. Will be "Action" or "Type"
	 */
	public String getTagName();
}
