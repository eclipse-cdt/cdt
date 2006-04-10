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

package org.eclipse.rse.references;
import org.eclipse.core.resources.IFolder;


/**
 * A class for managing a list of shadow objects that reference master objects.
 * <p>
 * Sometimes we have a master list of objects, and we let the user select
 * a subset of that list and we wish to persist that users selections. To
 * accomplish this, in your Rose model, follow these steps:
 * <ol>
 *   <li>Include the references package from the SystemReferences .cat file
 *   <li>Ensure the class for the master objects subclass SystemPersistableReferencedObject,
 *        or implement ISystemPersistableReferencedObject.
 *        <b>YOU MUST OVERRIDE getReferenceName() IN SYSTEMPERSISTABLEREFERENCEDOBJECT!</b>
 *   <li>Create a class subclassing SystemPersistableReferencingObject to hold a reference
 *        to the master object. This will hold a transient pointer, and a persistable
 *        name, of the master object. The name must be sufficient to be able to re-create
 *        the pointer upon restoration from disk. When you set the pointer via the
 *        setReferencedObject method, it will automatically extract the name of that
 *        object (by calling its getReferenceName method) and store it in the mof-modelled
 *        attribute of the SystemPersistableReferencingObject class.
 *   <li>Create a class subclassing this class (SystemPersistableReferenceManager)
 *        to manage the list of referencing objects. Each time you instantiate a reference
 *        object, add it to the referencingObjects list managed by this class.
 *        <b>YOU MUST OVERRIDE resolveReferencesAfterRestore() IN SYSTEMPERSISTABLEREFERENCEMANAGERIMPL!</b>
 * </ol>
 * <p>
 * Once you have an instantiated and populated instance of this class, you can either 
 * choose to save it to disk in its own file (save/restore methods are supplied for this)
 * or you can simply choose to store it as part of your own class via your own save
 * and restore methods. If using MOF, and the containment of the manager class is modelled in
 * your own containing class, this will happen automatically when you use mof to save
 * your containing class instance.
 */
/**
 * @lastgen interface SystemPersistableReferenceManager  {}
 */

public interface ISystemBasePersistableReferenceManager 
{

	/**
	 * Return an array of the referencing objects currently being managed.
	 * @param array of the referencing objects currently in this list.
	 */
	public ISystemBasePersistableReferencingObject[] getReferencingObjects();
	/**
	 * Set in one shot the list of referencing objects. Replaces current list.
	 * @param objects An array of referencing objects which is to become the new list.
     * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setReferencingObjects(ISystemBasePersistableReferencingObject[] objects, boolean deReference);
	/**
	 * Add a referencing object to the managed list.
	 * @return new count of referenced objects being managed.
	 */
	public int addReferencingObject(ISystemBasePersistableReferencingObject object);
	/**
	 * Remove a referencing object from the managed list.
	 * <p>Does NOT call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeReferencingObject(ISystemBasePersistableReferencingObject object);	
	/**
	 * Remove and dereferences a referencing object from the managed list.
	 * <p>DOES call removeReference on the master referenced object.
	 * @return new count of referenced objects being managed.
	 */
	public int removeAndDeReferenceReferencingObject(ISystemBasePersistableReferencingObject object);	
	/**
	 * Remove all objects from the list.
	 * <p>Does NOT call removeReference on the master referenced objects.
	 */
	public void removeAllReferencingObjects();	
	/**
	 * Remove and dereference all objects from the list.
	 * <p>DOES call removeReference on the master referenced objects.
	 */
	public void removeAndDeReferenceAllReferencingObjects();	
	/**
	 * Return how many referencing objects are currently in the list.
	 * @return current count of referenced objects being managed.
	 */
	public int getReferencingObjectCount();	
	/**
	 * Return the zero-based position of the given referencing object within the list.
     * Does a memory address comparison (==) to find the object.
	 * @param object The referencing object to find position of.
	 * @return zero-based position within the list. If not found, returns -1
	 */
	public int getReferencingObjectPosition(ISystemBasePersistableReferencingObject object);	
	/**
	 * Move the given referencing object to a new zero-based position in the list.
	 * @param newPosition New zero-based position
	 * @param object The referencing object to move
	 */
	public void moveReferencingObjectPosition(int newPosition, ISystemBasePersistableReferencingObject object);	
	/**
	 * Return true if the given referencable object is indeed referenced by a referencing object
	 * in the current list. This is done by comparing the reference names of each, not the
	 * in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return true if found in list, false otherwise.
	 */
	public boolean isReferenced(ISystemBasePersistableReferencedObject object);	
	/**
	 * Search list of referencing objects to see if one of them references the given referencable object.
	 * This is done by comparing the reference names of each, not the in-memory pointers.
	 * @param object The referencable object to which to search for a referencing object within this list
	 * @return the referencing object within this list which references the given referencable object, or
	 * null if no reference found.
	 */
	public ISystemBasePersistableReferencingObject getReferencedObject(ISystemBasePersistableReferencedObject object);	
	
    /**
     * Attempt to save contents of manager to disk. Only call if not doing your own save from
     *  your own model that uses a subclass of this.
     * @param folder The folder in which to save the manager.
     * @param fileName The unqualified file name to save to. Should include extension, such as .xmi
     */
    public void save(IFolder folder, String fileName)
       throws Exception;
	
    /**
     * After restoring this from disk, there is only the referenced object name,
     * not the referenced object pointer, for each referencing object.
     * <p>
     * This method is called after restore and for each restored object in the list must:
     * <ol>
     *   <li>Do what is necessary to find the referenced object, and set the internal reference pointer.
     *   <li>Call addReference(this) on that object so it can maintain it's in-memory list
     *          of all referencing objects.
     * </ol>
     * @return true if resolved successfully. False if some references were not found and
     *  hence those referencing objects removed from the restored list.
     */
    public boolean resolveReferencesAfterRestore();
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Name attribute
	 */
	String getName();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Name attribute
	 */
	void setName(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of ReferencingObjectList references
	 */
	java.util.List getReferencingObjectList();

}