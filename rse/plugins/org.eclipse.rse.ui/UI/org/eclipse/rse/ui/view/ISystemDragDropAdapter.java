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

package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemRemoteResourceSet;


public interface ISystemDragDropAdapter extends IRemoteObjectIdentifier 
{
	
	// ------------------------------------------
	// METHODS TO SUPPORT COMMON DRAG AND DROP FUNCTION...
	// ------------------------------------------	
	/**
	 *  Return true if this object can be copied to another location via drag and drop, or clipboard copy.
	 */
	public boolean canDrag(Object element);
	
	/**
	 *  Return true if these objects can be copied to another location via drag and drop, or clipboard copy.
	 */
	public boolean canDrag(SystemRemoteResourceSet elements);
	
	/**
	 * Perform the drag on the given object.
	 * @param element the object to copy
	 * @param sameSystemType indication of whether the source and target reside on the same type of system
	 * @param monitor the progress monitor
	 * @return a temporary local copy of the object that was copied
	 */
	public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor);
	
	
	/**
	 * Perform the drag on the given objects.
	 * @param set the set of objects to copy
	 * @param monitor the progress monitor
	 * @return a set of temporary files of the object that was copied
	 */
	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor);
	
	/**
	 * Return true if another object can be copied into this object
	 * @param element the target of a drop operation
	 * @return whether this object may be dropped on
	 */
	public boolean canDrop(Object element);
	
	/**
	 *  Perform drop from the "from" object to the "to" object
	 * @param from the source object for the drop
	 * @param to the target object for the drop
	 * @param sameSystemType indication of whether the source and target reside of the same type of system
	 * @param sameSystem indication of whether the source and target are on the same system
	 * @param srcType the type of object to be dropped.
	 * @param monitor the progress monitor
	 * @return the new copy of the object that was dropped
	 */ 
	public Object doDrop(Object from, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor);
	  
	/**
	 *  Perform drop from the "fromSet" of objects to the "to" object
	 * @param from the source objects for the drop
	 * @param to the target object for the drop
	 * @param sameSystemType indication of whether the source and target reside of the same type of system
	 * @param sameSystem indication of whether the source and target are on the same system
	 * @param srcType the type of objects to be dropped
	 * @param monitor the progress monitor
	 * 
	 * @return the set of new objects created from the drop
	 * 
	 */ 
	public ISystemResourceSet doDrop(ISystemResourceSet fromSet, Object to, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor);

  
    /**
      * Return true if it is valid for the src object to be dropped in the target
      * @param src the object to drop
      * @param target the object which src is dropped in
      * @param sameSystem whether this is the same system
      * @return whether this is a valid operation
      */ 
    public boolean validateDrop(Object src, Object target, boolean sameSystem);
  
    /**
     * Return true if it is valid for the src objects to be dropped in the target
     * @param srcSet set of resources to drop on the target
     * @param target the object which src is dropped in
     * @param sameSystem whether this is the same system
     * @return whether this is a valid operation
     */ 
   public boolean validateDrop(ISystemResourceSet srcSet, Object target, boolean sameSystem);
   
   	/**
     * Get the subsystem that corresponds to this object if one exists.
     * 	
     */
	public ISubSystem getSubSystem(Object element);	  
}