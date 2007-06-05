/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.references;

import org.eclipse.rse.core.references.IRSEBaseReferencedObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;

/**
 * This is a class that implements all the methods in the IRSEReferencingObject.
 * It makes implementing this interface trivial.
 * The easiest use of this class is to subclass it, but since that is not
 * always possible, it is not abstract and hence can be leveraged via containment.
 */
public class SystemReferencingObjectHelper {

	private IRSEBaseReferencedObject masterObject = null;
	private IRSEBaseReferencingObject caller = null;

	/**
	 * Default constructor.
	 * @param caller the reference that this object is helping. 
	 */
	public SystemReferencingObjectHelper(IRSEBaseReferencingObject caller) {
		super();
		this.caller = caller;
	}

	/**
	 * Constructor that saves effort of calling setReferencedObject.
	 * @param caller the reference that this object is helping.
	 * @param obj the object to which this reference will point.
	 */
	public SystemReferencingObjectHelper(IRSEBaseReferencingObject caller, IRSEBaseReferencedObject obj) {
		this(caller);
		setReferencedObject(obj);
	}

	/**
	 * Set the object to which this reference will point.
	 * Stores the reference in memory, replacing whatever was there.
	 * Also calls obj.addReference(caller);
	 * @param obj the object to which this reference will point.
	 */
	public void setReferencedObject(IRSEBaseReferencedObject obj) {
		this.masterObject = obj;
		if (obj != null) obj.addReference(caller);
	}

	/**
	 * Get the object which is referenced. May be null if the reference is not set or has not been resolved.
	 * @return the referenced object.
	 */
	public IRSEBaseReferencedObject getReferencedObject() {
		return masterObject;
	}

	/**
	 * Removes this reference from the referenced object and clears this reference.
	 * Also, nulls out our memory reference.
	 * @return new reference count of master object
	 */
	public int removeReference() {
		int newCount = 0;
		IRSEBaseReferencedObject masterObject = getReferencedObject();
		if (masterObject != null) newCount = masterObject.removeReference(caller);
		masterObject = null;
		return newCount;
	}

}
