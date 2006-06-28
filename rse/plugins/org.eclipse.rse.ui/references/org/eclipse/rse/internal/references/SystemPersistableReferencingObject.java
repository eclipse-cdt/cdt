/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.references;

import org.eclipse.rse.references.ISystemBasePersistableReferenceManager;
import org.eclipse.rse.references.ISystemBasePersistableReferencedObject;
import org.eclipse.rse.references.ISystemBaseReferencedObject;
import org.eclipse.rse.references.ISystemPersistableReferencingObject;

/**
 * This class represents an object that references another object in the model.
 * The reference is persistable.
 * <p>
 * @see org.eclipse.rse.references.ISystemPersistableReferencingObject
 */
// DWD Change this name to SystemPersistableReference? Ditto for the interface.
public abstract class SystemPersistableReferencingObject extends SystemReferencingObject implements ISystemPersistableReferencingObject {

	protected String referencedObjectName = null;
	protected ISystemBasePersistableReferenceManager _referenceManager;

	/**
	 * Create a new referencing object.
	 */
	protected SystemPersistableReferencingObject() {
		super();
	}

	/**
	 * Set the persistable referenced object name
	 */
	public void setReferencedObjectName(String newReferencedObjectName) {
		referencedObjectName = newReferencedObjectName;
	}

	/**
	 * Set the in-memory reference to the master object.
	 * This implementation also extracts that master object's name and calls
	 * setReferencedObjectName as part of this method call.
	 * @see org.eclipse.rse.references.ISystemBasePersistableReferencingObject#setReferencedObject(ISystemBasePersistableReferencedObject)
	 */
	public void setReferencedObject(ISystemBasePersistableReferencedObject obj) {
		getHelper().setReferencedObject((ISystemBaseReferencedObject) obj);
		setReferencedObjectName(obj.getReferenceName());
	}

	/**
	 * Get the persistable referenced object name.
	 */
	public String getReferencedObjectName() {
		return referencedObjectName;
	}

	/**
	 * @return The reference manager for this reference. 
	 */
	public ISystemBasePersistableReferenceManager getParentReferenceManager() {
		return _referenceManager;
	}

	/**
	 * Sets the reference manager for this reference. Must be done when this reference is created.
	 */
	public void setParentReferenceManager(ISystemBasePersistableReferenceManager newParentReferenceManager) {
		_referenceManager = newParentReferenceManager;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (referencedObjectName: ");
		result.append(referencedObjectName);
		result.append(')');
		return result.toString();
	}

}