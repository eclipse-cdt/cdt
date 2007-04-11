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
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCommand;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * This class represents a compile command node in the Team view. 
 */
public class SystemTeamViewCompileCommandNode implements IAdaptable {
	//private String mementoHandle;
	private SystemCompileCommand command;
	private SystemTeamViewCompileTypeNode parentType;

	/**
	 * Constructor
	 */
	public SystemTeamViewCompileCommandNode(SystemTeamViewCompileTypeNode parentType, SystemCompileCommand command) {
		super();
		this.command = command;
		this.parentType = parentType;
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
	 * Return this node's image
	 * @return the image to show in the tree, for this node
	 */
	public ImageDescriptor getImageDescriptor() {
		return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_COMPILE_ID);
	}

	/**
	 * Return this node's label
	 * @return the translated label to show in the tree, for this node
	 */
	public String getLabel() {
		return command.getLabel();
	}

	/**
	 * @return profile this category is associated with
	 */
	public ISystemProfile getProfile() {
		return parentType.getProfile();
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
	 * Return the compile command this node represents
	 */
	public SystemCompileCommand getCompileCommand() {
		return command;
	}

	/**
	 * Set the compile command this node represents
	 */
	public void setCompileCommand(SystemCompileCommand command) {
		this.command = command;
	}

	/**
	 * Return the parent compile type this is a child of.
	 */
	public SystemTeamViewCompileTypeNode getParentCompileType() {
		return parentType;
	}
}
