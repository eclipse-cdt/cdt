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
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.references;

/**
 * Referencing objects are shadows of real objects. Typically, shadows are created
 * to enable a GUI which does not allow the same real object to appear multiple times.
 * In these cases, a unique shadow object is created for each unique instance of the
 * real object.
 * <p>
 * This interface captures the simple set of methods such a shadow must implement.
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEBaseReferencingObject {
	/**
	 * Set the object to which we reference
	 * @param obj the object to reference
	 */
	public void setReferencedObject(IRSEBaseReferencedObject obj);

	/**
	 * @return the object which we reference
	 */
	public IRSEBaseReferencedObject getReferencedObject();

	/**
	 * Fastpath to getReferencedObject().removeReference(this).
	 * @return new reference count of master object
	 */
	public int removeReference();

	/**
	 * @param broken true if this reference is currently broken/unresolved
	 */
	public void setReferenceBroken(boolean broken);

	/**
	 * @return true if this reference is currently broken/unresolved
	 */
	public boolean isReferenceBroken();

}
