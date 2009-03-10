/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - adding new persistence support
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 ********************************************************************************/

package org.eclipse.rse.core.model;

/**
 * This is the interface for all objects that contain persistent data.
 * The objects themselves may not have a persistent form, but can lie in the
 * persistence hierarchy and be reconstructed when the persistent form is restored.
 * An example of this is the SytemFilterPoolManager, which is itself not persisted, but
 * has this interface since it can be reconstructed from its ordering and exists
 * in the parent chain from SystemFilterPool to SystemProfile.
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework. 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEPersistableContainer {
	
	/**
	 * A constant indicating the presence of no children for a given container.
	 * This can be used when implementing {@link #getPersistableChildren()}.
	 * The value is an empty array.
	 */
	public static final IRSEPersistableContainer[] NO_CHILDREN = new IRSEPersistableContainer[0];

	/**
	 * Request a persistence manager to persist this object.
	 * @return true if the object was persisted.
	 */
	public boolean commit();

	/**
	 * An object was restored if it originated from a persistent form.
	 * @return true if the object was created from its persistent form,
	 * false if the object has never been persisted.
	 */
	public boolean wasRestored();

	/**
	 * Sets the restored state of the object. Only persistence managers 
	 * should do this. Persistable objects should be initially created with 
	 * this as false and the persistence manager set this to true.
	 * This attribute is "sticky" in the face of most property changes.
	 * It should be set false if the object is renamed or re-parented.
	 * @param flag true if the object was restored.
	 */
	public void setWasRestored(boolean flag);
		
	/**
	 * An object is dirty if a change has been made to it that requires
	 * it to be persisted.
	 * @return true if the object is dirty
	 */
	public boolean isDirty();

	/**
	 * <p>An object is dirty if a change has been made to it that requires
	 * it to be persisted. Objects should usually mark themselves dirty
	 * when a persistable change is made. However, there may be a need
	 * to mark related objects dirty as well.
	 * <p>Persistable changes are:
	 * <ul>
	 * <li>Any modification of a persistable attribute</li>
	 * <li>A rename</li>
	 * <li>A deletion of a child object</li>
	 * <li>A reordering of the list of child objects</li>
	 * <li>The addition of a child object</li>
	 * </ul>
	 * <p>A rename may also cause a parent to be marked dirty if the 
	 * parent refers to the child by name.
	 * <p>Setting this value to false should be done only in the persistence
	 * manager after the object has been written to the DOM.
	 * <p>Marking an object as dirty must cause it and all of its ancestors
	 * in the persistence hierarchy to be marked as tainted.
	 * @param flag true if the object must be persisted.
	 */
	public void setDirty(boolean flag);

	/**
	 * An object is tainted if it contains an object that is dirty 
	 * somewhere in its containment hierarchy.
	 * @return true if the object is tainted.
	 */
	public boolean isTainted();
	
	/**
	 * Sets the tainted attribute for this object. This should set to 
	 * true only by child objects when they have been marked dirty or tainted.
	 * Setting this to true will cause all parent objects in the containment
	 * hierarchy to be marked tainted.
	 * It should be set to false only by a persistence manager when the
	 * object has been committed.
	 * @param flag the tainted state of the object.
	 */
	public void setTainted(boolean flag);
	
	/**
	 * Retrieve the parent of this object in the persistence containment hierarchy.
	 * This is related to, but not necessarily the same as, the model hierarchy.
	 * @return the parent persistent object. This is null if there is no parent.
	 */
	public IRSEPersistableContainer getPersistableParent();
	
	/**
	 * Retrieves the children of this object in the persistence containment hierarchy.
	 * This is related to, but not necessarily the same as, the model hierarchy.
	 * @return the array of persistent children in the order they are to be stored in the 
	 * persistent form. This is an empty array if there are no children.
	 * See {@link #NO_CHILDREN}.
	 */
	public IRSEPersistableContainer[] getPersistableChildren();

}