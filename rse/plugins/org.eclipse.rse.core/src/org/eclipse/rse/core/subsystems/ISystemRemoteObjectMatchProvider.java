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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

/**
 * This interface is designed to allow remote property pages to be registered
 * against specific remote system objects of specific name, type or subtype.
 */
public interface ISystemRemoteObjectMatchProvider extends IRemoteObjectIdentifier {

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
	 * Return a value for the type property for this object.
	 * <p>
	 * The value must not be translated, so that property pages registered
	 * via xml can subset by it.
	 * </p>
	 * @return the type id of this remote object for filtering.
	 */
	public String getRemoteType(Object element);

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
	 * Return the label for this object.
	 * @see #getName(Object)
	 * @param element the element for which to return the internal name.
	 * @return a String representing the UI visible label of the given element.
	 */
	public String getText(Object element);

}
