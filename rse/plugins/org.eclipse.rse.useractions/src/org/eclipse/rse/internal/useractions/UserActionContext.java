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
 * The model object for a user action context. A user action context is what a user action can apply to.
 * An example of a user action context is a named list of file types for which a certain user action applies.
 * Since the context can consist of different properties (file types, process names, etc.), the properties that make up the
 * context can be specified through property sets of the context.
 */
public class UserActionContext extends RSEModelObject implements IUserActionContext {

	// persistent properties
	private ISystemProfile profile;
	private ISubSystemConfiguration subsysConfig;
	private String name;
	private String description;
	private String supplier;
	private boolean isModifiable;
	
	/**
	 * Creates a modifiable user action context.
	 * @param profile the parent profile for which the user action context applies.
	 * @param subsysConfig the subsystem configuration for which the user action context applies.
	 * @param name the name of the user action context.
	 */
	public UserActionContext(ISystemProfile profile, ISubSystemConfiguration subsysConfig, String name) {
		super();
		this.profile = profile;
		this.subsysConfig = subsysConfig;
		this.name = name;
		this.isModifiable = true;
	}

	/**
	 * Creates a user action context.
	 * @param profile the parent profile for which the user action context applies.
	 * @param subsysConfig the subsystem configuration for which the user action context applies.
	 * @param name the name of the user action context.
	 * @param description the description of the user action context.
	 * @param supplier the supplier of the user action context.
	 * @param sets the property sets associated with the user action context. The actual list of object types - for example, file types - that the context
	 * represents can be specified through this parameter.
	 * @param isModifiable <code>true</code> if the user action context is modifiable, <code>false</code> otherwise.
	 */
	public UserActionContext(ISystemProfile profile, ISubSystemConfiguration subsysConfig, String name, String description, String supplier, IPropertySet[] sets, boolean isModifiable) {
		super();
		this.profile = profile;
		this.subsysConfig = subsysConfig;
		this.name = name;
		this.description = description;
		this.supplier = supplier;
		addPropertySets(sets);
		this.isModifiable = isModifiable;
	}
	
	/**
	 * Returns the profile that the user action context belongs to.
	 * @return the profile that the user action context belongs to.
	 */
	public ISystemProfile getParentProfile() {
		return profile;
	}
	
	/**
	 * Returns the subsystem configuration that the user action context is applicable for.
	 * @return the subsystem configuration that the user action context is applicable for.
	 */
	public ISubSystemConfiguration getParentConfiguration() {
		return subsysConfig;
	}
	
	/**
	 * Sets the name of the user action context. It has no effect if the user action context is not modifiable.
	 * @param name the name of the user action context.
	 */
	public void setName(String name) {
		
		if (isModifiable()) {
			this.name = name;
		}
	}
	
	/**
	 * Returns the name of the user action context.
	 * @return the name of the user action context.
	 */ 
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the description of the user action context. It has no effect if the user action context is not modifiable.
	 * @param description the description of the user action context.
	 */
	public void setDescription(String description) {
		
		if (isModifiable()) {
			this.description = description;
		}
	}

	/**
	 * Returns the description of the user action context.
	 * @return the description of the user action context.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the supplier of the user action context.
	 * @param supplier the supplier of the user action context.
	 */
	public void setSupplier(String supplier) {
		
		if (isModifiable()) {
			this.supplier = supplier;
		}
	}
	
	/**
	 * Returns the supplier of the user action context.
	 * @return the supplier of the user action context.
	 */
	public String getSupplier() {
		return supplier;
	}

	/**
	 * Returns whether the user action context is modifiable.
	 * @return <code>true</code> if the user action context is modifiable, <code>false</code> otherwise.
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
		return IRSEPersistableContainer.NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}
}