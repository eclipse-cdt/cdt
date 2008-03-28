/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [208951] new priority field
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * Martin Oberhuber (Wind River) - [219975] Fix implementations of clone()
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core.model;

import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping;


/**
 * An internal class.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SystemFileTransferModeMapping implements ISystemFileTransferModeMapping, Cloneable {

	public static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;
	private String name;
	private String extension;
	private boolean isBinary = true;
	private int priority = DEFAULT_PRIORITY;

	/**
	 * Constructor for SystemFileTransferModeMapping. The name is set to <code>*</code>.
	 * @param extension the extension. Can be <code>null</code>.
	 */
	public SystemFileTransferModeMapping(String extension) {
		this("*", extension); //$NON-NLS-1$
	}

	/**
	 * Constructor for SystemFileTransferModeMapping.
	 * @param name the name. If the name is <code>null</code> or if it is an empty string, it is set to <code>*</code>.
	 * @param extension the extension. Can be <code>null</code>.
	 */
	public SystemFileTransferModeMapping(String name, String extension) {

		if ((name == null) || (name.length() < 1)) {
			setName("*"); //$NON-NLS-1$
		}
		else {
			setName(name);
		}

		setExtension(extension);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#getExtension()
	 */
	public String getExtension() {
		return extension;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#getLabel()
	 */
	public String getLabel() {

		if (extension != null) {
			return (name + "." + extension);  //$NON-NLS-1$
		}
		else {
			return name;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#isBinary()
	 */
	public boolean isBinary() {
		return isBinary;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#isText()
	 */
	public boolean isText() {
		return !isBinary();
	}

	/**
	 * Set whether transfer mode is binary
	 */
	public void setAsBinary() {
		isBinary = true;
	}

	/**
	 * Set whether transfer mode is text
	 */
	public void setAsText() {
		isBinary = false;
	}

	/**
	 * Set the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the extension
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * Set the priority - the smaller the number, the higher priority
	 * @param priority priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping#getPriority()
	 */
	public int getPriority()
	{
		return this.priority;
	}

	/**
	 * Clone this object.
	 * 
	 * Subclasses must ensure that such a deep copy operation is always
	 * possible, so their state must always be cloneable.
	 */
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			//assert false; //can never happen
			throw new RuntimeException(e);
		}
	}
}
