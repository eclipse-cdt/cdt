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

import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.swt.widgets.Shell;




/**
 * This is an interface that only remote system objects supply adapters for.
 * <p>
 * This interface is designed to allow remote property pages to be registered
 * against specific remote system objects of specific name, type or subtype.
 */
public interface ISystemRemoteElementAdapter extends IRemoteObjectIdentifier
{
	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 */
	public String getName(Object element);
	/**
	 * Return the fully qualified name of this remote object. 
	 * Unlike getName, this should include the full path to the name.
	 * This should be enough information to uniquely identify this object within its subsystem.
	 */
	public String getAbsoluteName(Object element);
    /**
     * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem.
     * This is used when deleting a remote resource for example, all occurrences of its parent are found and refreshed in the RSE views.
     */
    public String getAbsoluteParentName(Object element);
	/**
	 * Return the subsystem that is responsible for getting this remote object.
	 * When used together with getAbsoluteName, allows for unique identification of this object.
	 */
	public ISubSystem getSubSystem(Object element);
	/**
	 * Return the subsystem factory id that owns this remote object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getSubSystemConfigurationId(Object element);	
	/**
	 * Return a value for the type category property for this object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getRemoteTypeCategory(Object element);	
	/**
	 * Return a value for the type property for this object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getRemoteType(Object element);	
	/**
	 * Return a value for the subtype property for this object.
	 * Not all object types support a subtype, so returning null is ok.
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getRemoteSubType(Object element);	
	/**
	 * Return a value for the sub-subtype property for this object.
	 * Not all object types support a sub-subtype, so returning null is ok.
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getRemoteSubSubType(Object element);	
	/**
	 * Return the source type of the selected object. Typically, this only makes sense for compilable
	 *  source members. For non-compilable remote objects, this typically just returns null.
	 */
	public String getRemoteSourceType(Object element);
	/**
	 * <p>Short answer: treat this like clone(), and just copy any important instance variables</p>
	 * <p>
	 * Imagine the same remote resource is shown multiple times in the same tree view.... say
	 *  because multiple filters resolve to it, or there are two connections to the same host. 
	 *  Typically it is a different object in memory within the tree, but it refers to the same
	 *  remote resource. <br>
	 * Now imagine one of the references is selected by the user and renamed via the rename action. This
	 *  might only update the selected reference. What about the other objects which refer to the same
	 *  remote resource... they need to update their in-memory "name" variable too. <br>
	 * That is what this method. Every reference to the same remote resource is found (they have the 
	 *  same absolute name and come from a system with the same hostname) and this method is called
	 *  on those other references. This is your opportunity to copy the attributes from the new element
	 *  to the old element. 
	 * <p>
	 * Some view has updated the name or properties of this remote object. As a result, the 
	 *  remote object's contents need to be refreshed. You are given the old remote object that has
	 *  old data, and you are given the new remote object that has the new data. For example, on a
	 *  rename the old object still has the old name attribute while the new object has the new 
	 *  new attribute. You can copy the new name into the old object. Similar for any properties
	 *  you allow the user to edit via the property sheet.
	 * <p>
	 * This is called by viewers like SystemView in response to rename and property change events.
	 * <p>
	 * @param oldElement the element that was found in the tree
	 * @param newElement the updated element that was passed in the REFRESH_REMOTE event
	 * @return true if you want the viewer that called this to refresh the children of this object,
	 *   such as is needed on a rename of a folder, say, if the child object cache the parent folder name 
	 *   or an absolute file name.
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement);
	
	
	/**
	 * Return the remote edit wrapper for this object.
	 * @param object the object to edit
	 * @return the editor wrapper for this object
	 */
	public ISystemEditableRemoteObject getEditableRemoteObject(Object object);
	
	/**
	 * Indicates whether the specified object can be edited or not.
	 * @param object the object to edit
	 * @return true if the object can be edited.
	 */
	public boolean canEdit(Object object);
	
	/**
	 * Return a filter string that corresponds to this object.
	 * @param object the object to obtain a filter string for
	 * @return the corresponding filter string if applicable
	 */
	public String getFilterStringFor(Object object);
	
	/**
	 * Given a remote object, returns it remote parent object. Eg, given a file, return the folder
	 *  it is contained in.
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception;
	/**
	 * Given a remote object, return the unqualified names of the objects <i>contained</i> in that parent. This is
	 *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not 
	 *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
	 *  names are allowed if the types are different, such as on iSeries. In this case return only 
	 *  the names which should be used to do name-uniqueness validation on a rename operation.
	 */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception;
	
	/**
	 * Returns whether user defined actions should be shown for the object.
	 * @param object the object.
	 * @return <code>true</code> if the object supports user defined actions, <code>false</code> otherwise.
	 */
	public boolean supportsUserDefinedActions(Object object);
}