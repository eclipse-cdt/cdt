/********************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * This interface provides a means of extending RSE model objects and property sets
 * with labels that can be used for display purposes.
 * The persistence characteristics of labels are left to the implementing 
 * objects.
 */
public interface ILabeledObject {

	/**
	 * @return the display label for the object. If this has not 
	 * previously been set, this can return the name of object or
	 * some other generated or constant label. It may return null
	 * if no label can be determined.
	 */
	public String getLabel();
	
	/**
	 * @param label A display label for this object.
	 */
	public void setLabel(String label);

}
