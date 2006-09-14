/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.persistance;

public interface IRSEPersistableContainer
{
	/**
	 * An object is dirty if a change has been made to it that requires
	 * it to be persisted.
	 * @return true if the object is dirty
	 */
	public boolean isDirty();

	/**
	 * An object is dirty if a change has been made to it that requires
	 * it to be persisted. Objects should usually mark themselves dirty
	 * when a persistable change is made. However, there may be a need
	 * to mark related objects dirty as well.
	 * Setting this value to false should be done only in the persistence
	 * manager after the object has been written to the DOM.
	 * @param flag true if the object must be persisted.
	 */
	public void setDirty(boolean flag);
	
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
	 * The the "restored" state of the object. Only persistence managers 
	 * should do this.
	 * @param flag true if the object was restored.
	 */
	public void setWasRestored(boolean flag);
	
}