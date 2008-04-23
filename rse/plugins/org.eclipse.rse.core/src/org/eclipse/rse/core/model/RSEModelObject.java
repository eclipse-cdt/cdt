/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.internal.core.RSECoreMessages;

/**
 * Provides common support for local RSE model objects.
 * Extenders inherit property set support.
 */
public abstract class RSEModelObject extends PropertySetContainer implements IRSEModelObject {

	public String getDescription() {
		return RSECoreMessages.RESID_MODELOBJECTS_MODELOBJECT_DESCRIPTION;
	}
	
}