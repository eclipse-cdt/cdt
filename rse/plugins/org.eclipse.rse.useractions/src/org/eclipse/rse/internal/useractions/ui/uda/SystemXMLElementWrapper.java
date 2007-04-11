/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemUDAConstants
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This is a base class for classes that wrapper xml elements.
 * Eg, there are child classes to represent action xml elements, and
 *  type xml elements.
 */
public abstract class SystemXMLElementWrapper implements IAdaptable {
	//parameters
	protected Element elm;
	private boolean isDomainElement;
	private SystemUDBaseManager database; // For setChanged()
	private ISystemProfile profile;
	private int domainType;
	// constants
	/**
	 * What we store in XML document for TRUE
	 */
	private static final String XML_TRUE = "True"; //$NON-NLS-1$
	/**
	 * What we store in XML document for FALSE
	 */
	private static final String XML_FALSE = "False"; //$NON-NLS-1$
	/**
	 * The XML attribute name for the "IBM-Supplied" attribute
	 */
	private static final String XML_ATTR_VENDOR = "Vendor"; //$NON-NLS-1$
	/**
	 * The XML attribute name for the "User-Changed" attribute
	 */
	private static final String XML_ATTR_CHANGED = "UserChanged"; //$NON-NLS-1$
	/**
	 * The value we place in the Vendor attribute for IBM-supplied actions/types
	 */
	private static final String VENDOR_IBM = "IBM"; //$NON-NLS-1$

	/**
	 * Constructor
	 * @param elm - The actual xml document element for this action/type
	 * @param mgr - The parent manager of these actions/types
	 * @param profile - The system profile which owns this action
	 * @param domainType - The integer representation of the domain this is in (or this is, for a domain element)
	 */
	public SystemXMLElementWrapper(Element elm, SystemUDBaseManager mgr, ISystemProfile profile, int domainType) {
		super();
		this.elm = elm;
		this.isDomainElement = elm.getTagName().equals(ISystemUDAConstants.XE_DOMAIN);
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
	 * Get the XML document element this node wraps
	 */
	public Element getElement() {
		return elm;
	}

	/**
	 * Get the document this element is a part of
	 */
	public Document getDocument() {
		// this method added by phil.
		// this allows getChildren in xxxmanager classes to avoid deducing the document
		return elm.getOwnerDocument();
	}

	/**
	 * Get the parent xml domain element of this element.
	 * If domains aren't supported, this will return null
	 */
	public Element getParentDomainElement() {
		Element parent = getParentElement();
		if ((parent != null) && parent.getTagName().equals(ISystemUDAConstants.XE_DOMAIN))
			return parent;
		else
			return null;
	}

	/**
	 * Get the parent xml element of this element.
	 * Only returns null if this is the root, which should never happen.
	 */
	public Element getParentElement() {
		Node parent = elm.getParentNode();
		if (parent instanceof Element)
			return (Element) parent;
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
	 * Return the value of this node's "Name" attribute
	 */
	public String getName() {
		return elm.getAttribute(ISystemUDAConstants.NAME_ATTR);
	}

	/**
	 * Set the value of this tag's "Name" attribute.
	 * If this is an IBM-supplied user action, then it will cause an addition attribute to
	 *  be created named "OriginalName", containing the IBM-supplied name.
	 */
	public void setName(String s) {
		if (isIBM()) {
			String orgName = elm.getAttribute(ISystemUDAConstants.ORIGINAL_NAME_ATTR);
			if ((orgName != null) && (orgName.length() > 0)) {
				// no need to do anything, as its already set.
			} else
				elm.setAttribute(ISystemUDAConstants.ORIGINAL_NAME_ATTR, getName());
		}
		setAttribute(ISystemUDAConstants.NAME_ATTR, s);
		setUserChanged(true);
	}

	/**
	 * For IBM-supplied elements that have been edited, returns the original IBM-supplied name
	 */
	public String getOriginalName() {
		String s = elm.getAttribute(ISystemUDAConstants.ORIGINAL_NAME_ATTR);
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
		String vendor = elm.getAttribute(XML_ATTR_VENDOR);
		if (vendor == null)
			return false;
		else
			return vendor.equals(VENDOR_IBM);
	}

	/**
	 * Set the name of the vendor who supplied this user action or type
	 */
	public void setVendor(String vendor) {
		setAttribute(XML_ATTR_VENDOR, vendor);
	}

	/**
	 * Get the name of the vendor who supplied this user action or type.
	 * May be null, if created by a user
	 */
	public String getVendor() {
		return elm.getAttribute(XML_ATTR_VENDOR);
	}

	/**
	 * Set the value of this tag's "Vendor" attribute to "IBM",
	 * or clear the IBM attribute (after a duplication action for example).
	 */
	public void setIBM(boolean isFromIBM) {
		if (isFromIBM)
			setAttribute(XML_ATTR_VENDOR, VENDOR_IBM);
		else
			setAttribute(XML_ATTR_VENDOR, null);
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
			changed = getBooleanAttribute(XML_ATTR_CHANGED, false);
		//System.out.println("Inside isUserChanged, returning "+changed+": isIBM()="+isIBM()+", isDomainElement="+isDomainElement);
		return changed;
	}

	/**
	 * Set the value of this tag's "user-changed" attribute
	 */
	public void setUserChanged(boolean isUserChanged) {
		if (isIBM() && !isDomainElement) setBooleanAttribute(XML_ATTR_CHANGED, isUserChanged);
	}

	/**
	 * Delete this element from the document
	 */
	public void deleteElement() {
		// Not intended for root.  Only for Actions
		elm.getParentNode().removeChild(elm);
	}

	// --------------------------
	// INTERNAL HELPER METHODS...
	// --------------------------
	/**
	 * Given the name of a child xml tag, return the data for that tag
	 */
	protected String getTextNode(String tagname) {
		Element tag = getChildTag(tagname, false);
		if (null != tag) {
			Node n = tag.getFirstChild();
			if (null != n) {
				if (n instanceof Text) {
					Text tn = (Text) n;
					return tn.getData();
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Given the name of a child xml tag and a data value,
	 *  update the data of that tag
	 */
	protected void setTextNode(String tagname, String val) {
		Element tag = getChildTag(tagname, true);
		if (null != tag) {
			database.setChanged(profile);
			// ?? Loop on all children, removing?
			Node n = tag.getFirstChild();
			if (null != n) {
				if (n instanceof Text) {
					Text tn = (Text) n;
					tn.setData(val);
					return;
				}
				// ?? Loop on all children, removing?
				tag.removeChild(n);
			}
			tag.appendChild(elm.getOwnerDocument().createTextNode(val));
			return;
		}
	}

	/**
	 * Given a tag name, return the xml node for that child tag
	 * @param tagname - the name of the tag to find
	 * @param create - true if tag is to be created if not found
	 */
	protected Element getChildTag(String tagname, boolean create) {
		NodeList subList = elm.getChildNodes();
		if (null != subList) {
			for (int i = 0; i < subList.getLength(); i++) {
				Node sn = subList.item(i);
				if (sn instanceof Element) {
					Element se = (Element) sn;
					if (tagname.equals(se.getTagName())) return se;
				}
			}
		}
		if (create) {
			Element newchild = elm.getOwnerDocument().createElement(tagname);
			elm.appendChild(newchild);
			return newchild;
		}
		return null;
	}

	/**
	 * Set the value of a boolean attribute
	 */
	public void setBooleanAttribute(String attr, boolean b) {
		elm.setAttribute(attr, (b) ? XML_TRUE : XML_FALSE);
		database.setChanged(profile);
	}

	/**
	 * Return the boolean value of a given attribute. It must exist!
	 * @param attr - name of the attribute to query
	 */
	public boolean getBooleanAttribute(String attr) {
		String val = elm.getAttribute(attr);
		if (XML_TRUE.equals(val)) return true;
		return false;
	}

	/**
	 * Return the boolean value of a given attribute. 
	 * @param attr - name of the attribute to query
	 * @param defaultValue - value to return if the attribute is not found
	 */
	public boolean getBooleanAttribute(String attr, boolean defaultValue) {
		String val = elm.getAttribute(attr);
		if (val == null) return defaultValue;
		if (XML_TRUE.equals(val)) return true;
		return false;
	}

	/**
	 * Set the text value of the given attribute.
	 * Specify a default value to return if the attribute is not found
	 */
	public String getAttribute(String attr, String defaultValue) {
		String value = elm.getAttribute(attr);
		if (value == null) value = defaultValue;
		return value;
	}

	/**
	 * Set the text value of the given attribute to a given value
	 */
	public void setAttribute(String attr, String value) {
		if (value != null)
			elm.setAttribute(attr, value);
		else
			elm.removeAttribute(attr);
		database.setChanged(profile);
	}

	/**
	 * For unique-name checking.
	 * If this is a domain element, returns all child action names.
	 * If this is an action/tag element, returns all sibling action names, minus this one.
	 * Always returns a non-null vector, although it may be empty
	 */
	public Vector getExistingNames() {
		Element parentElement = null;
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
	public Vector getExistingNames(Element parentElement, Document xdoc) {
		return getExistingNames(parentElement, xdoc, getTagName());
	}

	/**
	 * For unique-name checking.
	 * Given a parent element XML node, returns all child action names.
	 * Always returns a non-null vector of Strings, although it may be empty
	 */
	public static Vector getExistingNames(Element parentElement, Document xdoc, String tagName) {
		Vector nameList = new Vector();
		Element se = null;
		NodeList subList = null;
		if (parentElement != null)
			subList = parentElement.getChildNodes();
		else
			subList = xdoc.getElementsByTagName(tagName);
		if (subList != null) {
			for (int idx = 0; idx < subList.getLength(); idx++) {
				Node sn = subList.item(idx);
				if (sn instanceof Element) {
					se = (Element) sn;
					if (se.getTagName().equals(tagName)) {
						nameList.add(se.getAttribute(ISystemUDAConstants.NAME_ATTR));
					}
				}
			} // end for all subnodes
		} // end if sublist != null		
		return nameList;
	}

	/**
	 * Returns element wrappers of children (if this is a domain) or siblings
	 */
	public Vector getChildren(Vector children, ISystemProfile profile) {
		Element parentElement = null;
		if (isDomain())
			parentElement = this.getElement();
		else
			parentElement = getParentElement();
		children = getChildren(children, parentElement, getDocument(), profile);
		return children;
	}

	/**
	 * Given a parent element XML node, returns wrappers of all child tags of which we are interested
	 * Always returns a non-null vector, although it may be empty
	 */
	public Vector getChildren(Vector children, Element parentElement, Document xdoc, ISystemProfile profile) {
		return getChildren(children, parentElement, xdoc, profile, database, getDomain());
	}

	/**
	 * Given a parent element XML node, returns wrappers all child tag elements with the given tag name
	 * Always returns a non-null vector, although it may be empty.
	 * If the parentElement is null, uses the roots of the given document. Should only be true if domains not supported!
	 * @return Vector of SystemXMLElementWrapper objects
	 */
	public static Vector getChildren(Vector children, Element parentElement, Document xdoc, ISystemProfile profile, ISystemXMLElementWrapperFactory factory, int domain) {
		if (children == null) children = new Vector();
		String tagName = factory.getTagName();
		Element se = null;
		NodeList subList = null;
		if (parentElement != null)
			subList = parentElement.getChildNodes();
		else
			subList = xdoc.getElementsByTagName(tagName);
		if (subList != null) {
			for (int idx = 0; idx < subList.getLength(); idx++) {
				Node sn = subList.item(idx);
				if (sn instanceof Element) {
					se = (Element) sn;
					if (se.getTagName().equals(tagName)) {
						children.add(factory.createElementWrapper(se, profile, domain));
					}
				}
			} // end for all subnodes
		} // end if sublist != null		
		return children;
	}

	/**
	 * For unique-name checking.
	 * Given a parent element XML node, returns the xml Element node with the given name attribute,
	 *  or null if not found.
	 */
	public static Element findChildByName(Element parentElement, Document xdoc, String tagName, String searchName) {
		Element match = null;
		NodeList subList = null;
		if (parentElement != null)
			subList = parentElement.getChildNodes();
		else
			subList = xdoc.getElementsByTagName(tagName);
		if (subList != null) {
			for (int idx = 0; (match == null) && (idx < subList.getLength()); idx++) {
				Node sn = subList.item(idx);
				if (sn instanceof Element) {
					if (((Element) sn).getTagName().equals(tagName)) {
						if (((Element) sn).getAttribute(ISystemUDAConstants.NAME_ATTR).equals(searchName)) match = (Element) sn;
					}
				}
			} // end for all subnodes
		} // end if sublist != null		
		return match;
	}
}
