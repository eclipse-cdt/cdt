/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.uda;

import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.rse.internal.useractions.UserActionsPersistenceUtil;
import org.eclipse.swt.graphics.Image;

/**
 * Instances of this class hold the UDA definitions unique to:
 * <ol>
 *   <li>The SystemProfile  - according to the subsystem
 *   <li>The SubSystem type - according to the subclassed SystemUDActionSubsystem
 * </ol>
 * Instances of this class will be linked to a SubSystem instance
 *
 *
 * Eventually, would hope to create a factory method for this class which will
 * return existing instances common to the subsystems of different connections
 * within the same profile.
 */
public class SystemUDActionManager extends SystemUDBaseManager
//       implements ErrorHandler,
		implements ITreeContentProvider {
	private static final String XE_ROOT = ISystemUDAConstants.ACTIONS_ROOT; 
	private static final String XE_ACTION = "Action"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public SystemUDActionManager(SystemUDActionSubsystem udas) {
		super(udas);
	}

	/**
	 * Return true if this is user actions, false if this is named types.
	 */
	protected boolean isUserActionsManager() {
		return true;
	}

	/**
	 * Get the icon to show in the tree views, for the "new" expandable item
	 */
	public Image getNewImage() {
		return UserActionsIcon.USERACTION_NEW.getImage();
	}

	/**
	 * Parent method override for returning the "New" icon label for the Work With dialog tree view.
	 * For us, we defer to the getActionSubSystem().{@link SystemUDActionSubsystem#getNewNodeActionLabel() getNewNodeActionLabel()}.
	 * Do not override this.
	 * @return translated value for "New" in new icon for WW action and type dialogs. Default is "New"
	 */
	protected String getNewNodeLabel() {
		return getActionSubSystem().getNewNodeActionLabel();
	}

	/**
	 * Overridable method for child classes to do migration of their document.
	 * This is called on first load of a document, which has a release stamp other than
	 * the current release
	 * @return true if any migration was done
	 */
	protected boolean doMigration(ISystemProfile profile, String oldRelease) {
		return getActionSubSystem().doActionsMigration(profile, oldRelease);
	}

	/**
	 * Get the document root tag name. 
	 * We return "Actions"
	 */
	public String getDocumentRootTagName() {
		return XE_ROOT; // "Actions"
	}

	/**
	 * Do we uppercase the value of the "Name" attribute?
	 * No, we don't for actions.
	 */
	protected boolean uppercaseName() {
		return false;
	}

	/**
	 * Return true if the elements managed by this class are scoped by
	 *  profile. Usually true for actions, false for types
	 */
	public boolean supportsProfiles() {
		return true;
	}

	/**
	 * Prime the given document with any default actions/types
	 * Should be overridden!
	 */
	public SystemXMLElementWrapper[] primeDocument(ISystemProfile profile) {
		if (profile.isDefaultPrivate()) // we only prime the user's private profile with default actions
			return getActionSubSystem().primeDefaultActions(this, profile);
		else
			return null;
	}

	/**
	 * Get the folder containing the xml file used to persist the actions,
	 *  for the given profile
	 */
	protected IFolder getDocumentFolder(ISubSystemConfiguration subsystemFactory, ISystemProfile profile) {
		return UserActionsPersistenceUtil.getUserActionsFolder(profile.getName(), subsystemFactory);
	}

	/**
	 * Intended for IMPORT actions only, where no Subsystem instance available:
	 */
	public void setFolder(String profileName, String factoryId) {
		importCaseFolder = UserActionsPersistenceUtil.getUserActionsFolder(profileName, factoryId);
	}

	/**
	 * Add a user-defined action
	 */
	public SystemUDActionElement addAction(ISystemProfile profile, String name, int domain) {
		return (SystemUDActionElement) super.addElement(profile, domain, name);
	}

	/**
	 * Return xml element wrapper objects for all actions, for the 
	 *  given domain, or for the whole document if domain is -1 (iff
	 *  domains not supported).
	 * @param v - existing vector to populate. If null passed, it is
	 *   not populated.
	 * @param profile - the profile to limit the search to
	 * @param domain - the integer representation of the given domain, 
	 *   or -1 iff supportsDomains() is false
	 * @return array of action objects
	 */
	public SystemUDActionElement[] getActions(Vector v, ISystemProfile profile, int domain) {
		v = super.getXMLWrappers(v, domain, profile);
		if (v == null) return new SystemUDActionElement[0];
		SystemUDActionElement[] actions = new SystemUDActionElement[v.size()];
		for (int idx = 0; idx < actions.length; idx++)
			actions[idx] = (SystemUDActionElement) v.elementAt(idx);
		return actions;
	}

	/**
	 * Return all the actions for the given profile, in all domains.
	 * @param v - existing vector to populate. If null passed, it is not populated.
	 * @param profile - the profile to limit the search to
	 * @return array of action objects
	 */
	public SystemUDActionElement[] getAllActions(Vector v, ISystemProfile profile) {
		if (!getActionSubSystem().supportsDomains()) return getActions(v, profile, -1);
		if (v == null) v = new Vector();
		int nbrDomains = getActionSubSystem().getMaximumDomain() + 1;
		for (int domain = 0; domain < nbrDomains; domain++) {
			super.getXMLWrappers(v, domain, profile);
		}
		SystemUDActionElement[] actions = new SystemUDActionElement[v.size()];
		for (int idx = 0; idx < actions.length; idx++)
			actions[idx] = (SystemUDActionElement) v.elementAt(idx);
		return actions;
	}

	// -----------------------------------------------------------	
	// ISystemXMLElementWrapperFactory
	// -----------------------------------------------------------	
	/**
	 * Return the tag name for our managed elements.
	 * Eg: will be "Action" for user actions, and "Type" for file types.
	 */
	public String getTagName() {
		return XE_ACTION;
	}

	/**
	 * Given an xml element node, create an instance of the appropriate
	 * subclass of SystemXMLElementWrapper to represent it.
	 */
	public SystemXMLElementWrapper createElementWrapper(IPropertySet xmlElementToWrap, ISystemProfile profile, int domain) {
		SystemUDActionElement elementWrapper = new SystemUDActionElement(xmlElementToWrap, this, profile, domain);
		return elementWrapper;
	}
}
