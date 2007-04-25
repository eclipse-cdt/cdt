/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
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
	 * Return the name of this object, which may be different than
	 * the display text ({@link #getText(Object)}).
	 * <p>
	 * The unqualified name is used for checking uniqueness during rename
	 * operations.
	 * </p>
	 * @param element the element for which to return the internal name.
	 * @return a String representing the internal name of the given element.
	 */
	public String getName(Object element);
	
	/**
	 * Return the label for this object.
	 * @see #getName(Object)
	 * @param element the element for which to return the internal name.
	 * @return a String representing the UI visible label of the given element.
	 */
	public String getText(Object element);

    /**
     * Return fully qualified name that uniquely identifies this remote object's
     * remote parent within its subsystem.
     * <p>
     * This is used when deleting a remote resource for example, all occurrences of
     * its parent are found and refreshed in the RSE views.
     * </p><p>
     * Note that when using filters, there is no unique parent since the same object
     * may be found in multiple contexts, below multiple filters. Still, returning
     * the parent absolute name here may help finding potentially affected parents
     * more easily.
     * </p>
     * @see #getAbsoluteName(Object)
     * 
     * @return a String uniquely identifying the parent of this remote object,
     *     if it is known and exists. May also return <code>null</code> if the
     *     parent can not be determined or is not unique. 
     */
    public String getAbsoluteParentName(Object element);
    
	/**
	 * Return the subsystem that is responsible for getting this remote object.
	 * <p>
	 * When used together with getAbsoluteName, allows for unique
	 * identification of this object.
	 * </p>
	 * @return the subsystem owning this remote object.
	 *     Must not return <code>null</code>.
	 */
	public ISubSystem getSubSystem(Object element);

	/**
	 * Return the subsystem factory id that owns this remote object.
	 * <p>
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the ID of the subsystem configuration that created
	 *     the subsystem which owns this remote object.
	 */
	public String getSubSystemConfigurationId(Object element);	

	/**
	 * Return a value for the type category property for this object.
	 * <p>
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the category id of this remote object for filtering.
	 */
	public String getRemoteTypeCategory(Object element);	

	/**
	 * Return a value for the type property for this object.
	 * <p>
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the type id of this remote object for filtering.
	 */
	public String getRemoteType(Object element);	

	/**
	 * Return a value for the subtype property for this object.
	 * <p>
	 * Not all object types support a subtype, so returning null is ok.
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the subtype id of this remote object for filtering.
	 *     May return <code>null</code>.
	 */
	public String getRemoteSubType(Object element);	

	/**
	 * Return a value for the sub-subtype property for this object.
	 * <p>
	 * Not all object types support a sub-subtype, so returning null is ok.
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the subsubtype id of this remote object for filtering.
	 *     May return <code>null</code>.
	 */
	public String getRemoteSubSubType(Object element);	

	/**
	 * Return the source type of the selected object.
	 * <p>
	 * Typically, this only makes sense for compilable source members.
	 * For non-compilable remote objects, this typically just returns null.
	 * </p>
	 * @return the sourcetype id of this remote object,
	 *     or <code>null</code> if not applicable.
	 */
	public String getRemoteSourceType(Object element);

	/**
	 * Update a visible remote object with fresh data from a new object.
	 * <p>
	 * Short answer: treat this like clone(), and just copy any important
	 * instance variables. This allows keeping TreeItem references intact
	 * but refreshing the data shown, such that selections and expand state
	 * in the tree remain intact. 
	 * </p><p>
	 * Imagine the same remote resource is shown multiple times in the same tree view.... say
	 * because multiple filters resolve to it, or there are two connections to the same host. 
	 * Typically it is a different object in memory within the tree, but it refers to the same
	 * remote resource. <br>
	 * Now imagine one of the references is selected by the user and renamed via the rename action. This
	 * might only update the selected reference. What about the other objects which refer to the same
	 * remote resource... they need to update their in-memory "name" variable too. <br>
	 * That is what this method. Every reference to the same remote resource is found (they have the 
	 * same absolute name and come from a system with the same hostname) and this method is called
	 * on those other references. This is your opportunity to copy the attributes from the new element
	 * to the old element. 
	 * </p><p>
	 * Some view has updated the name or properties of this remote object. As a result, the 
	 * remote object's contents need to be refreshed. You are given the old remote object that has
	 * old data, and you are given the new remote object that has the new data. For example, on a
	 * rename the old object still has the old name attribute while the new object has the new 
	 * new attribute. You can copy the new name into the old object. Similar for any properties
	 * you allow the user to edit via the property sheet.
	 * </p><p>
	 * This is called by viewers like SystemView in response to rename and property change events.
	 * </p><p>
	 * @param oldElement the element that was found in the tree
	 * @param newElement the updated element that was passed in the REFRESH_REMOTE event
	 * @return true if you want the viewer that called this to refresh the children of this object,
	 *   such as is needed on a rename of a folder, say, if the child object cache the parent folder name 
	 *   or an absolute file name.
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement);
	
	/**
	 * Return the remote edit wrapper for this object.
	 * 
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
	 * Given a remote object, returns it remote parent object.
	 * <p>
	 * For instance, given a file, return the folder it is contained in.
	 * Not all subsystems support a unique parent-child relationship.
	 * Therefore, it is acceptable to return <code>null</code>.
	 * </p>
	 * @see #getAbsoluteParentName(Object)
	 * 
	 * @param shell FIXME why is this needed? Should be removed
	 * @param element The element for which to get the parent
	 * @return the parent element, or <code>null</code> if not applicable.
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception;

	/**
	 * Given a remote object, return the unqualified names of the objects
	 * <i>contained</i> in that parent.
	 * <p>
	 * This is used for testing for uniqueness on a rename operation, for example.
	 * Sometimes, it is not enough to just enumerate all the objects in the parent
	 * for this purpose, because duplicate names are allowed if the types are
	 * different, such as on iSeries. In this case return only the names which
	 * should be used to do name-uniqueness validation on a rename operation.
	 * </p>
	 * @param shell FIXME why is this needed? Should be removed
	 * @param element The element for which to get names in use
	 * @return a list of unqualified names contained in this folder to check
	 *    for uniqueness. FIXME may this return null? 
	 */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception;
	
	/**
	 * Returns whether user defined actions should be shown for the object.
	 * 
	 * @param object the object.
	 * @return <code>true</code> if the object supports user defined actions,
	 *     <code>false</code> otherwise.
	 */
	public boolean supportsUserDefinedActions(Object object);
}