/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * The model object for a user action. A user action applies to objects in subsystems belonging to a subsystem configuration in a user profile.
 */
public class UserActionModel extends RSEModelObject implements IUserActionModel {

	// persistent properties
	private ISystemProfile profile;
	private ISubSystemConfiguration subsysConfig;
	private String type;
	private String name;
	private String description;
	private String supplier;
	private boolean isModifiable;

	/**
	 * Creates a user action model object.
	 * @param profile the parent profile for which the user action applies.
	 * @param subsysConfig the subsystem configuration for which the user action applies.
	 * @param type the type of the user action.
	 * @param name the name of the user action.
	 * @param description the description of the user action.
	 * @param supplier the supplier of the user action.
	 * @param sets the property sets associated with the user action.
	 * @param isModifiable <code>true</code> if the user action is modifiable, <code>false</code> otherwise.
	 */
	public UserActionModel(ISystemProfile profile, ISubSystemConfiguration subsysConfig, String type, String name, String description, String supplier, IPropertySet[] sets, boolean isModifiable) {
		super();
		this.profile = profile;
		this.subsysConfig = subsysConfig;
		this.type = type;
		this.name = name;
		this.description = description;
		this.supplier = supplier;
		addPropertySets(sets);
		this.isModifiable = isModifiable;
	}

	/**
	 * Returns the profile that the user action belongs to.
	 * @return the profile that the user action belongs to.
	 */
	public ISystemProfile getParentProfile() {
		return profile;
	}
	
	/**
	 * Returns the subsystem configuration that the user action is applicable for.
	 * @return the subsystem configuration that the user action is applicable for.
	 */
	public ISubSystemConfiguration getParentConfiguration() {
		return subsysConfig;
	}
	
	/**
	 * Sets the type of the user action.
	 * @param type the type of the user action.
	 */
	public void setType(String type) {
		
		if (isModifiable()) {
			this.type = type;
		}
	}
	
	/**
	 * The type of the user action.
	 * @return the type of the user action.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the name of the user action. It has no effect if the user action is not modifiable.
	 * @param name the name of the user action.
	 */
	public void setName(String name) {
		
		if (isModifiable()) {
			this.name = name;
		}
	}
	
	/**
	 * Returns the name of the user action.
	 * @return the name of the user action.
	 */ 
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the description of the user action. It has no effect if the user action is not modifiable.
	 * @param description the description of the user action.
	 */
	public void setDescription(String description) {
		
		if (isModifiable()) {
			this.description = description;
		}
	}

	/**
	 * Returns the description of the user action.
	 * @return the description of the user action.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the supplier of the user action.
	 * @param supplier the supplier of the user action.
	 */
	public void setSupplier(String supplier) {
		
		if (isModifiable()) {
			this.supplier = supplier;
		}
	}
	
	/**
	 * Returns the supplier of the user action.
	 * @return the supplier of the user action.
	 */
	public String getSupplier() {
		return supplier;
	}

	/**
	 * Returns whether the user action is modifiable.
	 * @return <code>true</code> if the user action is modifiable, <code>false</code> otherwise.
	 */
	public boolean isModifiable() {
		return isModifiable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableChildren()
	 */
	public IRSEPersistableContainer[] getPersistableChildren() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}
}