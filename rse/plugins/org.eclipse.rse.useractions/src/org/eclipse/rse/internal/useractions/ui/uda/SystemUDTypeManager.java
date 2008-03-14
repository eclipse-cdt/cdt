/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.swt.graphics.Image;

/**
 * Instances of this class hold the UDA Type definitions unique to
 *  the SubSystem type - according to the SubSystemFactory
 * 
 * Note that unlike user actions, types are not scoped by profile.
 * For each subsystem factory there is but a single master list of types.
 *
 * Instances of this class will be linked to a SubSystem instance for
 *  now, but should be linked to a subsystem factory instance in the future.
 *
 */
public class SystemUDTypeManager extends SystemUDBaseManager {
	private static final String XE_ROOT = ISystemUDAConstants.FILETYPES_ROOT;
	public static final String XE_TYPE = "Type"; //$NON-NLS-1$
	public static final String ALL_TYPE = "ALL"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public SystemUDTypeManager(SystemUDActionSubsystem udas) {
		super(udas);
	}

	/**
	 * Return true if this is user actions, false if this is named types.
	 */
	protected boolean isUserActionsManager() {
		return false;
	}

	/**
	 * Get the icon to show in the tree views, for the "new" expandable item
	 */
	public Image getNewImage() {
		return UserActionsIcon.USERTYPE_NEW.getImage();
	}


	/**
	 * Overridable extension point for child classes to do migration of their document.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done
	 */
	protected boolean doMigration(ISystemProfile profile, String oldRelease) {
		return getActionSubSystem().doTypesMigration(profile, oldRelease);
	}

	/**
	 * Parent method override for returning the "New" icon label for the Work With dialog tree view.
	 * For us, we defer to the getActionSubSystem().{@link SystemUDActionSubsystem#getNewNodeTypeLabel() getNewNodeTypeLabel()}.
	 * Do not override this.
	 * @return translated value for "New" in new icon for WW action and type dialogs. Default is "New"
	 */
	protected String getNewNodeLabel() {
		return getActionSubSystem().getNewNodeTypeLabel();
	}

	// -----------------------------------------------------------	
	// ISystemXMLElementWrapperFactory
	// -----------------------------------------------------------	
	/**
	 * Return the tag name for our managed elements.
	 * Eg: will be "Action" for user actions, and "Type" for file types.
	 */
	public String getTagName() {
		return XE_TYPE;
	}

	/**
	 * Given an xml element node, create an instance of the appropriate
	 * subclass of SystemXMLElementWrapper to represent it.
	 */
	public SystemXMLElementWrapper createElementWrapper(IPropertySet xmlElementToWrap, ISystemProfile profile, int domain) {
		SystemUDTypeElement elementWrapper = new SystemUDTypeElement(xmlElementToWrap, this, domain);
		return elementWrapper;
	}

	// -----------------------------------------------------------	
	// THE FOLLOWING ARE PARENT METHODS THAT ABSTRACT OUT THE 
	//  DIFFERENCES BETWEEN ACTIONS  AND TYPES
	// -----------------------------------------------------------
	/**
	 * Get the document root tag name. 
	 * We return "FileTypes"
	 */
	public String getDocumentRootTagName() {
		return XE_ROOT; // "FileTypes"
	}

	/**
	 * Do we uppercase the value of the "Name" attribute?
	 * Yes, we do for types
	 */
	protected boolean uppercaseName() {
		return true;
	}

	/**
	 * Return true if the elements managed by this class are scoped by
	 *  profile. Usually true for actions, false for types
	 */
	public boolean supportsProfiles() {
		return false;
	}

	/**
	 * Prime the given document with any default types
	 * Calls primeDefaultTypes in action subsystem.
	 */
	public SystemXMLElementWrapper[] primeDocument(ISystemProfile profile) {
		return getActionSubSystem().primeDefaultTypes(this);
	}





	/**
	 * Indicate data has changed for the given profile
	 */
	protected void dataChanged(ISystemProfile profile) {
		//  ADDED THIS LINE TO RESET THE RESOLVED TYPES WHEN A TYPE IS ADDED  	
		_udas.resetResolvedTypes();
	}

	// -----------------------------------------------------------	
	// TYPE-MANAGER UNIQUE METHODS...
	// -----------------------------------------------------------
	/**
	 * Given a type name and domain, find the named type and return
	 *  its types, or null if not found
	 */
	public String getTypesForTypeName(String typeName, int domain) {
		ISystemProfile profile = getSubSystem().getSystemProfile();
		SystemUDTypeElement element = (SystemUDTypeElement) findByName(profile, typeName, domain);
		if (element != null)
			return element.getTypes();
		else
			return null;
	}

	/**
	 * Return xml element wrapper objects for all types, for the 
	 *  given domain, or for the whole document if domain is -1 (iff
	 *  domains not supported).
	 * @param v - existing vector to populate. If null passed, it is
	 *   not populated.
	 * @param domain - the integer representation of the given domain, 
	 *   or -1 iff supportsDomains() is false
	 * @return array of type objects
	 */
	public SystemUDTypeElement[] getTypes(Vector v, int domain) {
		v = super.getXMLWrappers(v, domain, _udas.getSubsystem().getSystemProfile());
		if (v == null) return new SystemUDTypeElement[0];
		SystemUDTypeElement[] types = new SystemUDTypeElement[v.size()];
		for (int idx = 0; idx < types.length; idx++)
			types[idx] = (SystemUDTypeElement) v.elementAt(idx);
		return types;
	}

	/**
	 * Return list of names of types in the given domain, or in doc 
	 *  if domain is -1 (which must only happen if supportsDomains() is false!)
	 */
	public String[] getTypeNames(int domain) {
		Vector v = new Vector();
		// step 1: find the parent domain object, if any...
		if (domain != -1) {
			SystemUDTypeElement parentDomainElement = (SystemUDTypeElement) getDomainWrapper(null, domain);
			// step 1a: ask that parent to return its children names...
			v = parentDomainElement.getExistingNames();
			String[] names = new String[v.size()];
			for (int idx = 0; idx < names.length; idx++)
				names[idx] = (String) v.elementAt(idx);
			return names;
		}
		// step 2: no domain name given, so assume document roots are the types so find them and return their names
		else {
			v = SystemXMLElementWrapper.getExistingNames(null, getDocument(null), XE_TYPE);
			String[] names = new String[v.size()];
			for (int idx = 0; idx < names.length; idx++)
				names[idx] = (String) v.elementAt(idx);
			return names;
		}
	}

	/**
	 * Add a new user type.
	 * Creates the new XML node in the document,
	 *  and creates and returns a wrapper object for it.
	 * <p>
	 * Optimized flavour of addElement that does not require a profile,
	 *  and is typed to return SystemUDTypeElement
	 */
	public SystemUDTypeElement addType(int domain, String name) {
		ISystemProfile profile = getSubSystem().getSystemProfile();
		return (SystemUDTypeElement) super.addElement(profile, domain, name);
	}

	/**
	 * Delete a give user action or type, given its wrapper.
	 * Deletes the xml node from the document.
	 * <p>
	 * Optimized flavour of delete that does not require a profile,
	 *  and is typed to take SystemUDTypeElement
	 */
	public void delete(SystemUDTypeElement typeElement) {
		super.delete(null, typeElement);
	}

	// -------------------------------------------------------------------------
	// SPECIAL FLAVOURS OF PARENT METHODS, THAT DON'T REQUIRE A PROFILE PARM...
	// -------------------------------------------------------------------------
	/**
	 * Save user data
	 */
	public void saveUserData() {
		ISystemProfile profile = getActionSubSystem().getSubsystem().getSystemProfile();
		super.saveUserData(profile);
	}
	/*
	 * Get our xml document
	 *
	 protected Document getDocument()
	 {
	 return super.getDocument(null);
	 }*/
}
