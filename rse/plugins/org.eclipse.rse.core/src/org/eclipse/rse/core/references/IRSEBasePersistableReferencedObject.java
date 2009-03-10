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
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 ********************************************************************************/

package org.eclipse.rse.core.references;

/**
 * Referenced objects are objects that have shadow objects (referencing objects)
 * of them. Typically, references are created to enable a UI which does not
 * allow the same real object to appear multiple times. In these cases, a unique
 * reference object is created for each unique instance of the real object.
 * <p>
 * The parent interface IRSEReferencedObject captures the simple set of methods
 * an object that supports such a real object implement.
 * <p>
 * This interface specializes that for the case of real objects that support
 * references that must be persisted. Typically, we build the references in
 * memory at runtime to satisfy the UI. However, occasionally we build the list
 * of references for a more permanent reason, such as when we let a user choose
 * a subset from a master list.
 * <p>
 * When we persist such a reference, we can't persist the memory reference to
 * the master object. Instead, we persist the unique name of that object, and
 * upon restoring from disk we then resolve that into a runtime reference to a
 * real memory object.
 * <p>
 * This interface supplies the method to allow a referencing object to query
 * that unique name or key from this real object.
 *
 * @noimplement This interface is not intended to be implemented by clients. The
 *              standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEBasePersistableReferencedObject extends IRSEBaseReferencedObject {

	/**
	 * @return the unique name or key of this master object to record in the referencing object.
	 */
	public String getReferenceName();
}