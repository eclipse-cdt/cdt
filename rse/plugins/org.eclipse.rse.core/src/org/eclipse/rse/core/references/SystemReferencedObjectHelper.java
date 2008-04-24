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

import java.util.Vector;

/**
 * The class should be used by subclasses of {@link SystemReferencedObject} by
 * instantiating it and delegating to it.
 * 
 * @noextend This class is not intended to be subclassed by clients. The
 *           standard extensions are included in the framework.
 * @since org.eclipse.rse.core 3.0
 */
public class SystemReferencedObjectHelper {

	private Vector referencingObjects = new Vector();

	/**
	 * @see IRSEBaseReferencedObject#addReference(IRSEBaseReferencingObject)
	 */
	public int addReference(IRSEBaseReferencingObject ref) {
		referencingObjects.addElement(ref);
		return referencingObjects.size();
	}

	/**
	 * @see IRSEBaseReferencedObject#removeReference(IRSEBaseReferencingObject)
	 */
	public int removeReference(IRSEBaseReferencingObject ref) {
		referencingObjects.removeElement(ref);
		return referencingObjects.size();
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferenceCount()
	 */
	public int getReferenceCount() {
		return referencingObjects.size();
	}

	/**
	 * Clear the list of referenced objects.
	 */
	public void removeAllReferences() {
		IRSEBaseReferencingObject[] references = getReferencingObjects();
		for (int i = 0; i < references.length; i++) {
			IRSEBaseReferencingObject reference = references[i];
			removeReference(reference);
		}
	}

	/**
	 * @see IRSEBaseReferencedObject#getReferencingObjects()
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects() {
		IRSEBaseReferencingObject[] references = new IRSEBaseReferencingObject[referencingObjects.size()];
		referencingObjects.toArray(references);
		return references;
	}

}
