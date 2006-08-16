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

package org.eclipse.rse.core.references;


/**
 * A simple class that implements IRSEPersistableReferencingObject.
 * This is an object that is a shadow (reference) of a real master object
 * (IRSEPersistableReferencedObject). 
 * <p>
 * Objects of this class contain a pointer (in memory) to the master object, 
 * and a copy of this object's unique name or key (for storing on disk).
 * <p>
 * Only the name is saved to disk, and after restoring from disk, that name
 * is used to set the actual object reference.
 * <p>
 * The intention is that in your Rose model, your class extends this class.
 * Do this for any shadow/reference class which you want to persist. Use
 * a subclass of SystemPersistableReferenceManager to manage a list of these,
 * and manage the saving/restoring of that list.
 * <p>
 * <b>YOU MUST OVERRIDE resolveReferencesAfterRestore IN YOUR REFERENCE MANAGER SUBCLASS</b>
 */
/**
 * @lastgen interface SystemPersistableReferencingObject extends SystemReferencingObject {}
 */

public interface IRSEPersistableReferencingObject extends IRSEReferencingObject, IRSEBasePersistableReferencingObject{
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the ReferencedObjectName attribute
	 */
	String getReferencedObjectName();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the ReferencedObjectName attribute
	 */
	void setReferencedObjectName(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The ParentReferenceManager reference
	 */
	IRSEBasePersistableReferenceManager getParentReferenceManager();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param l The new value of the ParentReferenceManager reference
	 */
	void setParentReferenceManager(IRSEBasePersistableReferenceManager value);

}