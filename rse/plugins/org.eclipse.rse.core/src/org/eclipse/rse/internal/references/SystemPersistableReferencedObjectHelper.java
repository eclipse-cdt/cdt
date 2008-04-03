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

import org.eclipse.rse.core.references.IRSEBasePersistableReferencedObject;
import org.eclipse.rse.core.references.SystemReferencedObjectHelper;

/**
 * This class extends the support for a class that supports being managing by a transient 
 * in-memory reference to one which also supports the persistence of such references.
 * To do this, such a referencable class must be able to return a name that is 
 * so unique that it can be used after restoration from disk to resolve a pointer to this
 * specific object, in memory.
 */
public class SystemPersistableReferencedObjectHelper extends SystemReferencedObjectHelper implements IRSEBasePersistableReferencedObject {

	private String referenceName;

	/**
	 * Constructor for SystemPersistableReferencedObjectHelper
	 * @param referenceName The unique name that can be stored to identify this object.
	 */
	protected SystemPersistableReferencedObjectHelper(String referenceName) {
		setReferenceName(referenceName);
	}

	/**
	 * @return the unique reference name of this object, as set in the constructor
	 */
	public String getReferenceName() {
		return referenceName;
	}

	/**
	 * Set the unique reference name of this object. Overrides what was set in
	 * the constructor. Typically called on rename operation.
	 * @param name the name of this particular reference.
	 */
	public void setReferenceName(String name) {
		this.referenceName = name;
	}

}