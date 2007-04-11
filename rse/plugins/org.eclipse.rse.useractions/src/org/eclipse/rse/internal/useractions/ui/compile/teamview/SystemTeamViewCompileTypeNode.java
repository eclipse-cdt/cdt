/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile.teamview;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewCategoryNode;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewSubSystemConfigurationNode;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileType;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

//import com.ibm.etools.systems.subsystems.SubSystemFactory;
/**
 * This class represents a compile type node in the Team view. 
 */
public class SystemTeamViewCompileTypeNode implements IAdaptable {
	//private String mementoHandle;
	private SystemCompileType type;
	private SystemTeamViewSubSystemConfigurationNode parentSSF;

	/**
	 * Constructor
	 */
	public SystemTeamViewCompileTypeNode(SystemTeamViewSubSystemConfigurationNode parentSSF, SystemCompileType type) {
		super();
		this.type = type;
		this.parentSSF = parentSSF;
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Compare this node to another. 
	 */
	public boolean equals(Object o) {
		if (o instanceof SystemTeamViewCompileTypeNode) {
			SystemTeamViewCompileTypeNode other = (SystemTeamViewCompileTypeNode) o;
			if ((type == other.getCompileType()) && parentSSF.equals(other.getParentSubSystemFactory()))
				return true;
			else
				return false;
		} else
			return super.equals(o);
	}

	/**
	 * Return this node's image
	 * @return the image to show in the tree, for this node
	 */
	public ImageDescriptor getImageDescriptor() {
		//return RSEUIPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_FILE_ID);
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE); //
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel() {
		return type.toString();
	}

	/**
	 * @return profile this category is associated with
	 */
	public ISystemProfile getProfile() {
		return parentSSF.getProfile();
	}

	/*
	 * @return the untranslated value to store in the memento, to uniquely identify this node
	 *
	 public String getMementoHandle()
	 {
	 return mementoHandle;
	 }*/
	/*
	 * Set the untranslated value to store in the memento, to uniquely identify this node
	 * @param string - untranslated value
	 *
	 public void setMementoHandle(String string)
	 {
	 mementoHandle = string;
	 }*/
	/**
	 * Return the compile type this node represents
	 */
	public SystemCompileType getCompileType() {
		return type;
	}

	/**
	 * Set the compile type this node represents
	 */
	public void setCompileType(SystemCompileType type) {
		this.type = type;
	}

	/**
	 * Return the grandparent category this is a grandchild of.
	 */
	public SystemTeamViewCategoryNode getParentCategory() {
		return parentSSF.getParentCategory();
	}

	/**
	 * Return the parent subsystem factory this is a child of.
	 */
	public SystemTeamViewSubSystemConfigurationNode getParentSubSystemFactory() {
		return parentSSF;
	}

	/**
	 * Set the parent subsystem factory this is a child of.
	 */
	public void setParentSubSystemFactory(SystemTeamViewSubSystemConfigurationNode factory) {
		parentSSF = factory;
	}
}
