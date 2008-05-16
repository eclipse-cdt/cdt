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
 * David Dykstal (IBM) - [213353] fix move of filter pool references within its container
 *******************************************************************************/

package org.eclipse.rse.internal.references;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rse.core.references.IRSEBasePersistableReferenceManager;
import org.eclipse.rse.core.references.IRSEBasePersistableReferencedObject;
import org.eclipse.rse.core.references.IRSEBasePersistableReferencingObject;

/**
 * <b>YOU MUST OVERRIDE resolveReferencesAfterRestore() IN THIS CLASS!</b>
 * <p>
 * <b>YOU MUST OVERRIDE getReferenceName() IN SYSTEMPERSISTABLEREFERENCEDOBJECT!</b>
 * <p>
 * @see org.eclipse.rse.core.references.IRSEBasePersistableReferenceManager
 * 
 * @lastgen class SystemPersistableReferenceManagerImpl Impl implements SystemPersistableReferenceManager, EObject {}
 */
public class SystemPersistableReferenceManager implements IRSEBasePersistableReferenceManager {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * @see #getName()
	 */
	protected static final String NAME_EDEFAULT = null;

	private IRSEBasePersistableReferencingObject[] listAsArray = null;
	public static boolean debug = true;
	public static HashMap EMPTY_MAP = new HashMap();

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected List referencingObjectList = null;

	/**
	 * Constructor. Typically called by EMF framework via factory create method.
	 */
	protected SystemPersistableReferenceManager() {
		super();
	}

	/**
	 * Internal method to get the mof List that is the current list.
	 */
	protected List internalGetList() {
		return getReferencingObjectList();
	}

	/**
	 * Internal method to invalidate any cached info.
	 * Must be called religiously by any method affecting list.
	 */
	protected void invalidateCache() {
		listAsArray = null;
	}

	/**
	 * Return an array of the referencing objects currently being managed.
	 * @return array of the referencing objects currently in this list.
	 */
	public IRSEBasePersistableReferencingObject[] getReferencingObjects() {
		if ((listAsArray == null) || (listAsArray.length != internalGetList().size())) {
			List list = internalGetList();
			listAsArray = new IRSEBasePersistableReferencingObject[list.size()];
			Iterator i = list.iterator();
			int idx = 0;
			while (i.hasNext()) {
				listAsArray[idx++] = (IRSEBasePersistableReferencingObject) i.next();
			}
		}
		return listAsArray;
	}

	/**
	 * Set in one shot the list of referencing objects. Replaces current list.
	 * @param objects An array of referencing objects which is to become the new list.
	 * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setReferencingObjects(IRSEBasePersistableReferencingObject[] objects, boolean deReference) {
		listAsArray = objects;
		if (deReference)
			removeAndDeReferenceAllReferencingObjects();
		else
			removeAllReferencingObjects();
		List list = internalGetList();
		for (int idx = 0; idx < objects.length; idx++)
			list.add(objects[idx]);
	}

	/*
	 * DWD this should probably operate on IRSEPersistableReferencingObject
	 * instead and call setParentManager. This involves recasting this class to 
	 * implement a new type or changing IRSEBasePersistableReferenceManager to
	 * deal with parent references - probably changing its name in the process.
	 * We could collapse IRSEBasePersistableReferencingObject and its subinterface
	 * into one interface.
	 */
	/**
	 * Add a referencing object to the managed list.
	 * @return new count of referenced objects being managed.
	 */
	public int addReferencingObject(IRSEBasePersistableReferencingObject object) {
		List list = internalGetList();
		list.add(object);
		invalidateCache();
		return getReferencingObjectCount();
	}

	/**
	 * Remove a referencing object from the managed list.
	 * <p>Does NOT call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeReferencingObject(IRSEBasePersistableReferencingObject object) {
		List list = internalGetList();
		list.remove(object);
		invalidateCache();
		return getReferencingObjectCount();
	}

	/**
	 * Remove and dereferences a referencing object from the managed list.
	 * <p>DOES call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeAndDeReferenceReferencingObject(IRSEBasePersistableReferencingObject object) {
		object.removeReference();
		return removeReferencingObject(object);
	}

	/**
	 * Remove all objects from the list.
	 * <p>Does NOT call removeReference on the master referenced objects.
	 */
	public void removeAllReferencingObjects() {
		internalGetList().clear();
	}

	/**
	 * Remove and dereference all objects from the list.
	 * <p>DOES call removeReference on the master referenced objects.
	 */
	public void removeAndDeReferenceAllReferencingObjects() {
		IRSEBasePersistableReferencingObject[] objs = getReferencingObjects();
		for (int idx = 0; idx < objs.length; idx++) {
			objs[idx].removeReference();
		}
		removeAllReferencingObjects();
	}

	/**
	 * Return how many referencing objects are currently in the list.
	 * @return current count of referenced objects being managed.
	 */
	public int getReferencingObjectCount() {
		return internalGetList().size();
	}

	/**
	 * Return the zero-based position of the given referencing object within the list.
	 * Does a memory address comparison (==) to find the object.
	 * @param object The referencing object to find position of.
	 * @return zero-based position within the list. If not found, returns -1
	 */
	public int getReferencingObjectPosition(IRSEBasePersistableReferencingObject object) {
		List list = internalGetList();
		int position = -1;
		boolean match = false;

		Iterator i = list.iterator();
		int idx = 0;

		while (!match && i.hasNext()) {
			IRSEBasePersistableReferencingObject curr = (IRSEBasePersistableReferencingObject) i.next();
			if (curr == object) {
				match = true;
				position = idx;
			} else
				idx++;
		}
		return position;
	}

	/**
	 * Move the given referencing object to a new zero-based position in the list.
	 * This does not call back or send any events nor does it mark anything dirty.
	 * @param newPosition New zero-based position
	 * @param object The referencing object to move
	 */
	public void moveReferencingObjectPosition(int newPosition, IRSEBasePersistableReferencingObject object) {
		int oldPosition = referencingObjectList.indexOf(object);
		if (oldPosition >= 0) {
			if (oldPosition != newPosition) {
				referencingObjectList.remove(oldPosition);
				referencingObjectList.add(newPosition, object);
				invalidateCache();
			}
		}
	}

	/**
	 * Return true if the given referencable object is indeed referenced by a referencing object
	 * in the current list. This is done by comparing the reference names of each, not the
	 * in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return true if found in list, false otherwise.
	 */
	public boolean isReferenced(IRSEBasePersistableReferencedObject object) {
		return (getReferencedObject(object) != null);
	}

	/**
	 * Search list of referencing objects to see if one of them references the given referencable object.
	 * This is done by comparing the reference names of each, not the in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return the referencing object within this list which references the given referencable object, or
	 * null if no reference found.
	 */
	public IRSEBasePersistableReferencingObject getReferencedObject(IRSEBasePersistableReferencedObject object) {
		List list = internalGetList();
		IRSEBasePersistableReferencingObject match = null;
		Iterator i = list.iterator();
		int idx = 0;

		while ((match == null) && i.hasNext()) {
			IRSEBasePersistableReferencingObject curr = (IRSEBasePersistableReferencingObject) i.next();
			if (curr.getReferencedObjectName().equals(object.getReferenceName())) {
				match = curr;
			} else
				idx++;
		}
		return match;
	}

	/**
	 * Return string identifying this filter
	 */
	public String toString() {
		return getName();
	}

	// ---------------------------------------------------------------------------
	// Methods for saving and restoring if not doing your own in your own subclass
	// ---------------------------------------------------------------------------

	/**
	 * Ensure given path ends with path separator.
	 */
	public static String addPathTerminator(String path) {
		if (!path.endsWith(File.separator)) path = path + File.separatorChar;
		//else
		//  path = path;
		return path;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getName() {
		return name;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public List getReferencingObjectList() {
		if (referencingObjectList == null) {
			referencingObjectList = new ArrayList();
			//FIXME new EObjectContainmentWithInversejava.util.List(SystemPersistableReferencingObject.class, this, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCE_MANAGER__REFERENCING_OBJECT_LIST, ReferencesPackage.SYSTEM_PERSISTABLE_REFERENCING_OBJECT__PARENT_REFERENCE_MANAGER);
		}
		return referencingObjectList;
	}

	//FIXME obsolete?
	public String toStringGen() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}

}
