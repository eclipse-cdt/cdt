/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemUDAConstants
 * Xuan Chen        (IBM)    - [222263] Need to provide a PropertySet Adapter for System Team View (cleanup some use action stuff)
 * Kevin Doyle		(IBM)	 - [240725] Add Null Pointer checking when there are no default user actions
 * Kevin Doyle 		(IBM)	 - [222829] MoveUp/Down Broken in Work with User Actions Dialog
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertySetContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.swt.graphics.Image;


/**
 * This is a base class for classes that wrapper UDA elements.
 * Eg, there are child classes to represent action UDA elements, and
 *  type UDA elements.
 */
public abstract class SystemXMLElementWrapper implements IAdaptable {
	//parameters
	protected IPropertySet elm;
	private boolean isDomainElement;
	private SystemUDBaseManager database; // For setChanged()
	private ISystemProfile profile;
	private int domainType;
	// constants
	/**
	 * What we store in UDA document for TRUE
	 */
	private static final String UDA_TRUE = "True"; //$NON-NLS-1$
	/**
	 * What we store in UDA document for FALSE
	 */
	private static final String UDA_FALSE = "False"; //$NON-NLS-1$
	/**
	 * The XML attribute name for the "IBM-Supplied" attribute
	 */
	private static final String UDA_ATTR_VENDOR = "Vendor"; //$NON-NLS-1$
	/**
	 * The XML attribute name for the "User-Changed" attribute
	 */
	private static final String UDA_ATTR_CHANGED = "UserChanged"; //$NON-NLS-1$
	/**
	 * The value we place in the Vendor attribute for IBM-supplied actions/types
	 */
	private static final String VENDOR_IBM = "IBM"; //$NON-NLS-1$

	/**
	 * Constructor
	 * @param elm - The actual UDA element for this action/type
	 * @param mgr - The parent manager of these actions/types
	 * @param profile - The system profile which owns this action
	 * @param domainType - The integer representation of the domain this is in (or this is, for a domain element)
	 */
	public SystemXMLElementWrapper(IPropertySet elm, SystemUDBaseManager mgr, ISystemProfile profile, int domainType) {
		super();
		this.elm = elm;
		this.isDomainElement = elm.getPropertyValue(ISystemUDAConstants.TYPE_ATTR).equals(ISystemUDAConstants.XE_DOMAIN);
		this.domainType = domainType;
		database = mgr;
		this.profile = profile;
	}

	// ----------------------------------
	// METHODS THAT MUST BE OVERRIDDEN...
	// ----------------------------------
	protected abstract String getTagName();

	public abstract Image getImage();

	// -------------------------------------
	//
	// -------------------------------------
	/**
	 * Convert to a string
	 * Same as calling getName
	 */
	public String toString() {
		return getName();
	}

	/**
	 * As required by the IAdaptable interface.
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Get the profile this is associated with
	 */
	public ISystemProfile getProfile() {
		return profile;
	}

	/**
	 * Get the manager that manages the document this element is part of.
	 */
	public SystemUDBaseManager getManager() {
		return database;
	}

	/**
	 * Get the UDA element this node wraps
	 */
	public IPropertySet getElement() {
		return elm;
	}

	/**
	 * Get the document this element is a part of
	 */
	public IPropertySet getDocument() {
		// this method added by phil.
		// this allows getChildren in xxxmanager classes to avoid deducing the document
		return (IPropertySet)elm.getContainer();
	}

	/**
	 * Get the parent UDA element of this element.
	 * If domains aren't supported, this will return null
	 */
	public IPropertySet getParentDomainElement() {
		IPropertySet parent = getParentElement();
		if ((parent != null) && parent.getPropertyValue(ISystemUDAConstants.NAME_ATTR).equals(ISystemUDAConstants.XE_DOMAIN))
			return parent;
		else
			return null;
	}

	/**
	 * Get the parent UDA element of this element.
	 * Only returns null if this is the root, which should never happen.
	 */
	public IPropertySet getParentElement() {
		Object parent = elm.getContainer();
		if (parent instanceof IPropertySet)
			return (IPropertySet) parent;
		else
			return null;
	}

	/** 
	 * Is this a "Domain" tag?
	 */
	public boolean isDomain() {
		return isDomainElement;
	}

	/**
	 * Return the domain this element is in, or represents if it is a domain element itself.
	 * This is the integer representation used internally.
	 * Will be -1 if domains not supported for this subsystem.
	 */
	public int getDomain() {
		return domainType;
	}

	/**
	 * Return the value of this node's "name" attribute
	 */
	public String getName() {
		return elm.getPropertyValue(ISystemUDAConstants.NAME_ATTR);
	}

	/**
	 * Set the value of this tag's "Name" attribute.
	 * If this is an IBM-supplied user action, then it will cause an addition attribute to
	 *  be created named "OriginalName", containing the IBM-supplied name.
	 */
	public void setName(String s) {
		if (isIBM()) {
			String orgName = elm.getPropertyValue(ISystemUDAConstants.ORIGINAL_NAME_ATTR);
			if ((orgName != null) && (orgName.length() > 0)) {
				// no need to do anything, as its already set.
			} 
			else
			{
				IProperty property = elm.getProperty(ISystemUDAConstants.ORIGINAL_NAME_ATTR);
				if (null == property)
				{
					elm.addProperty(ISystemUDAConstants.ORIGINAL_NAME_ATTR, getName());
				}
				else
				{
					property.setValue(getName());
				}
			}
		}
		setAttribute(ISystemUDAConstants.NAME_ATTR, s);
		setUserChanged(true);
	}
	
	/**
	 * Return the value of this node's "order" attribute
	 */
	public int getOrder() {
		IProperty orderProperty = elm.getProperty(ISystemUDAConstants.ORDER_ATTR);
		int order = -1;
		
		if (orderProperty != null)
		{
			order = Integer.valueOf(orderProperty.getValue()).intValue();
		}
		return order;
	}
	
	public void setOrder(int order) {
		elm.addProperty(ISystemUDAConstants.ORDER_ATTR, Integer.toString(order));
	}

	/**
	 * For IBM-supplied elements that have been edited, returns the original IBM-supplied name
	 */
	public String getOriginalName() {
		String s = elm.getPropertyValue(ISystemUDAConstants.ORIGINAL_NAME_ATTR);
		if ((s == null) || (s.length() == 0))
			return getName();
		else
			return s;
	}

	/**
	 * Return the value of this node's "IBM" attribute.
	 * That is, is this an IBM-supplied tag?
	 */
	public boolean isIBM() {
		String vendor = elm.getPropertyValue(UDA_ATTR_VENDOR);
		if (vendor == null)
			return false;
		else
			return vendor.equals(VENDOR_IBM);
	}

	/**
	 * Set the name of the vendor who supplied this user action or type
	 */
	public void setVendor(String vendor) {
		setAttribute(UDA_ATTR_VENDOR, vendor);
	}

	/**
	 * Get the name of the vendor who supplied this user action or type.
	 * May be null, if created by a user
	 */
	public String getVendor() {
		return elm.getPropertyValue(UDA_ATTR_VENDOR);
	}

	/**
	 * Set the value of this tag's "Vendor" attribute to "IBM",
	 * or clear the IBM attribute (after a duplication action for example).
	 */
	public void setIBM(boolean isFromIBM) {
		if (isFromIBM)
			setAttribute(UDA_ATTR_VENDOR, VENDOR_IBM);
		else
			setAttribute(UDA_ATTR_VENDOR, null);
	}

	/**
	 * Return the value of this node's "user-changed" attribute.
	 * That is, if this an IBM-supplied tag, has the user changed it?
	 */
	public boolean isUserChanged() {
		boolean changed = false;
		if (!isIBM())
			changed = true;
		else if (isDomainElement)
			changed = false;
		else
			changed = getBooleanAttribute(UDA_ATTR_CHANGED, false);
		//System.out.println("Inside isUserChanged, returning "+changed+": isIBM()="+isIBM()+", isDomainElement="+isDomainElement);
		return changed;
	}

	/**
	 * Set the value of this tag's "user-changed" attribute
	 */
	public void setUserChanged(boolean isUserChanged) {
		if (isIBM() && !isDomainElement) setBooleanAttribute(UDA_ATTR_CHANGED, isUserChanged);
	}

	/**
	 * Delete this element from the document
	 */
	public void deleteElement() {
		// Not intended for root.  Only for Actions
		//elm.getParentNode().removeChild(elm);
		int elmOrder = getOrder();
		elm.getContainer().removePropertySet(elm.getName());
		IPropertySetContainer parentElement = elm.getContainer();
		IPropertySet[] allChildren = parentElement.getPropertySets();
		for (int i = 0; i < allChildren.length; i++) {
			IProperty orderProperty = allChildren[i].getProperty(ISystemUDAConstants.ORDER_ATTR);
			int order = -1;
			if (orderProperty != null)
			{
				order = Integer.valueOf(orderProperty.getValue()).intValue();
			}
			
			// Decrease the order of all elements greater then elmOrder
			if (order > elmOrder) {
				allChildren[i].addProperty(ISystemUDAConstants.ORDER_ATTR, Integer.toString(order - 1));
			}
		}
		
		
	}
	
	/**
	 * Set the value of a boolean attribute
	 */
	public void setBooleanAttribute(String attr, boolean b) {
		IProperty property = elm.getProperty(attr);
		if (null == property)
		{
			elm.addProperty(attr, (b) ? UDA_TRUE : UDA_FALSE);
		}
		else
		{
			property.setValue((b) ? UDA_TRUE : UDA_FALSE);
		}
		database.setChanged(profile);
	}

	/**
	 * Return the boolean value of a given attribute. It must exist!
	 * @param attr - name of the attribute to query
	 */
	public boolean getBooleanAttribute(String attr) {
		String val = elm.getPropertyValue(attr);
		if (UDA_TRUE.equals(val)) return true;
		return false;
	}

	/**
	 * Return the boolean value of a given attribute. 
	 * @param attr - name of the attribute to query
	 * @param defaultValue - value to return if the attribute is not found
	 */
	public boolean getBooleanAttribute(String attr, boolean defaultValue) {
		String val =  elm.getPropertyValue(attr);
		if (val == null) return defaultValue;
		if (UDA_TRUE.equals(val)) return true;
		return false;
	}

	/**
	 * Set the text value of the given attribute.
	 * Specify a default value to return if the attribute is not found
	 */
	public String getAttribute(String attr, String defaultValue) {
		String value =  elm.getPropertyValue(attr);
		if (value == null) value = defaultValue;
		return value;
	}

	/**
	 * Set the text value of the given attribute to a given value
	 */
	public void setAttribute(String attr, String value) {
		if (value != null)
		{
			IProperty property = elm.getProperty(attr);
			if (property == null)
			{
				elm.addProperty(attr, value);
			}
			else
			{
				property.setValue(value);
			}
		}
		else
		{
			elm.removeProperty(attr);
		}
		database.setChanged(profile);
	}

	/**
	 * For unique-name checking.
	 * If this is a domain element, returns all child action names.
	 * If this is an action/tag element, returns all sibling action names, minus this one.
	 * Always returns a non-null vector, although it may be empty
	 */
	public Vector getExistingNames() {
		IPropertySet parentElement = null;
		String currName = null;
		if (isDomain())
			parentElement = this.getElement();
		else {
			parentElement = getParentElement();
			currName = getName();
		}
		Vector nameList = getExistingNames(parentElement, getDocument());
		if (currName != null) nameList.remove(currName);
		return nameList;
	}

	/**
	 * For unique-name checking.
	 * Given a parent element XML node, returns all child action names.
	 * Always returns a non-null vector, although it may be empty
	 */
	public Vector getExistingNames(IPropertySet parentElement, IPropertySet xdoc) {
		return getExistingNames(parentElement, xdoc, getTagName());
	}

	/**
	 * For unique-name checking.
	 * Given a parent UDA node, returns all child action names.
	 * Always returns a non-null vector of Strings, although it may be empty
	 */
	public static Vector getExistingNames(IPropertySet parentElement, IPropertySet xdoc, String tagName) {
		Vector nameList = new Vector();
		IPropertySet[] subList = null;
		if (parentElement != null)
			subList = parentElement.getPropertySets();
		else if (xdoc != null)
			subList = xdoc.getPropertySets();
		if (subList != null) {
			for (int idx = 0; idx < subList.length; idx++) {
				IPropertySet sn = subList[idx];
				if (sn.getPropertyValue(ISystemUDAConstants.TYPE_ATTR).equals(tagName))
				{
					nameList.add(sn.getPropertyValue(ISystemUDAConstants.NAME_ATTR));
				}
			} // end for all subnodes
		} // end if sublist != null		
		return nameList;
	}

	/**
	 * Given a parent element XML node, returns wrappers of all child tags of which we are interested
	 * Always returns a non-null vector, although it may be empty
	 */
	public Vector getChildren(Vector children, IPropertySet parentElement, IPropertySet xdoc, ISystemProfile profile) {
		return getChildren(children, parentElement, xdoc, profile, database, getDomain());
	}

	/**
	 * Given a parent element XML node, returns wrappers all child tag elements with the given tag name
	 * Always returns a non-null vector, although it may be empty.
	 * If the parentElement is null, uses the roots of the given document. Should only be true if domains not supported!
	 * @return Vector of SystemXMLElementWrapper objects
	 */
	public static Vector getChildren(Vector children, IPropertySet parentElement, IPropertySet xdoc, ISystemProfile profile, ISystemXMLElementWrapperFactory factory, int domain) {
		if (children == null) children = new Vector();
		Vector ordered = new Vector();
		String tagName = factory.getTagName();
		
		IPropertySet[] subList = null;
		if (parentElement != null)
			subList = parentElement.getPropertySets();
		else if (xdoc != null)
			subList = xdoc.getPropertySets();
		if (subList != null) {
			Vector unordered = new Vector();
			for (int idx = 0; idx < subList.length; idx++) {
				IPropertySet sn = subList[idx];
				if (sn.getPropertyValue(ISystemUDAConstants.TYPE_ATTR).equals(tagName))
				{
					unordered.add(sn);
				}
			} // end for all subnodes
			ordered.setSize(unordered.size());
			for (int i = 0; i < unordered.size(); i++) {
				int order = i;
				// get the ordering
				IPropertySet sn = ((IPropertySet) unordered.get(i));
				IProperty orderProperty = sn.getProperty(ISystemUDAConstants.ORDER_ATTR);
				if (orderProperty != null) {
					order = Integer.valueOf(orderProperty.getValue()).intValue();
				}
				
				SystemXMLElementWrapper thisWrapper = factory.createElementWrapper(sn, profile, domain);
				try { 
					ordered.remove(order);
					ordered.add(order, thisWrapper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} // end if sublist != null		
		// Set the order position of all attributes to handle 3.0 where we didn't have the order attribute
		for (int i = 0; i < ordered.size(); i++) {
			if (ordered.get(i) instanceof SystemXMLElementWrapper) {
				SystemXMLElementWrapper element = (SystemXMLElementWrapper) ordered.get(i);
				if (element != null)
					element.setOrder(i);
			}
			children.add(ordered.get(i));
		}
		return children;
	}

	/**
	 * For unique-name checking.
	 * Given a parent element XML node, returns the xml Element node with the given name attribute,
	 *  or null if not found.
	 */
	public static IPropertySet findChildByName(IPropertySet parentElement, IPropertySet xdoc, String tagName, String searchName) {
		IPropertySet match = null;
		IPropertySet[] subList = null;
		if (parentElement != null)
			subList = parentElement.getPropertySets();
		else
			subList = xdoc.getPropertySets();
		if (subList != null) {
			for (int idx = 0; (match == null) && (idx < subList.length); idx++) {
				IPropertySet sn = subList[idx];
				if (sn.getName().equals(searchName))
				{
					IProperty typeProperty = sn.getProperty(ISystemUDAConstants.TYPE_ATTR);
					if (typeProperty.getValue().equals(tagName))
					{
						match = sn;
					}
				}
			} // end for all subnodes
		} // end if sublist != null		
		return match;
	}
}
