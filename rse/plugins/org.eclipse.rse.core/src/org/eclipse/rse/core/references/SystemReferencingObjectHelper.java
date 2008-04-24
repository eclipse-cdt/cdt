/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 *******************************************************************************/

package org.eclipse.rse.core.references;


/**
 * The class should be used by subclasses of {@link SystemReferencingObject} by
 * instantiating it and delegating to it.
 * 
 * @noextend This class is not intended to be subclassed by clients. The
 *           standard extensions are included in the framework.
 * @noinstantiate This class is not intended to be instantiated by clients. The
 *                standard instances are created by the framework.
 * @since org.eclipse.rse.core 3.0
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
