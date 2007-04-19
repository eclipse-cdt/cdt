/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.internal.core.model.RSEModelResources;

/**
 * Provides common support for local RSE model objects
 * Extenders inherit property set support
 * @author dmcknigh
 *
 */
public abstract class RSEModelObject extends PropertySetContainer implements IRSEModelObject {

	public String getDescription() {
		return RSEModelResources.RESID_MODELOBJECTS_MODELOBJECT_DESCRIPTION;
	}
	
}