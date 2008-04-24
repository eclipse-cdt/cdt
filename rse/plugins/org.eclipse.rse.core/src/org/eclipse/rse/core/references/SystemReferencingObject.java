/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup for noextend / noimplement where needed
 ********************************************************************************/

package org.eclipse.rse.core.references;

import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.internal.core.RSECoreMessages;

/**
 * A class to encapsulate the operations required of an object which is merely a
 * reference to another object, something we call a shadow. Such shadows are
 * needed to support a UI which displays the same object in multiple places. To
 * enable that, it is necessary not to use the same physical object in each UI
 * representation as the UI will only know how to update/refresh the first one
 * it finds.
 * 
 * @noextend This class is not intended to be subclassed by clients. The
 *           standard extensions are included in the framework.
 * @since org.eclipse.rse.core 3.0
 */
public abstract class SystemReferencingObject extends RSEModelObject implements IRSEReferencingObject {
	private SystemReferencingObjectHelper helper = null;
	protected boolean referenceBroken = false;

	/**
	 * Default constructor.
	 */
	protected SystemReferencingObject() {
		super();
		helper = new SystemReferencingObjectHelper(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#setReferencedObject(org.eclipse.rse.core.references.IRSEBaseReferencedObject)
	 */
	public void setReferencedObject(IRSEBaseReferencedObject obj) {
		helper.setReferencedObject(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#getReferencedObject()
	 */
	public IRSEBaseReferencedObject getReferencedObject() {
		return helper.getReferencedObject();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#removeReference()
	 */
	public int removeReference() {
		return helper.removeReference();
	}

	/**
	 * Set to true if this reference is currently broken/unresolved
	 */
	public void setReferenceBroken(boolean broken) {
		referenceBroken = broken;
	}

	/**
	 * Return true if this reference is currently broken/unresolved
	 */
	public boolean isReferenceBroken() {
		return referenceBroken;
	}

	public String getDescription() {
		return RSECoreMessages.RESID_MODELOBJECTS_REFERENCINGOBJECT_DESCRIPTION;
	}

	protected final SystemReferencingObjectHelper getHelper() {
		return helper;
	}
}