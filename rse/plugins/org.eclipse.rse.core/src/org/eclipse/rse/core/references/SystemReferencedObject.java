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
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 ********************************************************************************/

package org.eclipse.rse.core.references;

import org.eclipse.rse.core.model.RSEModelObject;

/**
 * A class to encapsulate the operations required of an object which supports
 * references to it by other objects ({@link SystemReferencingObject}). This
 * type of class needs to support maintaining an in-memory list of all who
 * reference it so that list can be following on delete and rename operations.
 * 
 * @noextend This class is not intended to be subclassed by clients. The
 *           standard extensions are included in the framework.
 *
 * @since org.eclipse.rse.core 3.0
 */
public abstract class SystemReferencedObject extends RSEModelObject implements IRSEReferencedObject {

	protected SystemReferencedObjectHelper helper = null;

	/**
	 * Default constructor. Typically called by EMF factory method.
	 */
	protected SystemReferencedObject() {
		helper = new SystemReferencedObjectHelper();
	}

	/**
	 * Add a reference, increment reference count, return new count
	 * @param ref the referencing object
	 * @return new count of how many referencing objects reference this object.
	 */
	public int addReference(IRSEBaseReferencingObject ref) {
		return helper.addReference(ref);
	}

	/**
	 * Remove a reference, decrement reference count, return new count
	 * @param ref the referencing object
	 * @return new count of how many referencing objects reference this object.
	 */
	public int removeReference(IRSEBaseReferencingObject ref) {
		return helper.removeReference(ref);
	}

	/**
	 * @return a count of how many referencing objects reference this object.
	 */
	public int getReferenceCount() {
		return helper.getReferenceCount();
	}

	/**
	 * Clear the list of referenced objects.
	 */
	public void removeAllReferences() {
		helper.removeAllReferences();
	}

	/**
	 * @return a list of all referencing objects of this object
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects() {
		return helper.getReferencingObjects();
	}

}