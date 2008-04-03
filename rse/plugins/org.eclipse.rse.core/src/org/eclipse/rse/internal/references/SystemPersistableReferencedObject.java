/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 ********************************************************************************/
package org.eclipse.rse.internal.references;

import org.eclipse.rse.core.references.IRSEPersistableReferencedObject;
import org.eclipse.rse.core.references.SystemReferencedObject;

/**
 * @see org.eclipse.rse.core.references.IRSEBasePersistableReferenceManager
 */
public abstract class SystemPersistableReferencedObject extends SystemReferencedObject implements IRSEPersistableReferencedObject {
	/**
	 * Constructor.
	 */
	public SystemPersistableReferencedObject() {
		super();
	}

	/**
	 * Return the unique reference name of this object.
	 * <p>
	 * As required by the {@link org.eclipse.rse.core.references.IRSEPersistableReferencedObject} 
	 * interface.
	 * <p>
	 * YOUR SUBCLASS MUST OVERRIDE THIS!
	 */
	public String getReferenceName() {
		return null;
	}
}