/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * Represents contents that are children of a container.
 * This is a singleton class representing a contents type.
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SystemChildrenContentsType implements ISystemContentsType {
	public static String CONTENTS_TYPE_CHILDREN = "contents_children"; //$NON-NLS-1$
	public static SystemChildrenContentsType _instance = new SystemChildrenContentsType();

	public static SystemChildrenContentsType getInstance() {
		return _instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.model.IRemoteContentsType#getType()
	 */
	public String getType() {
		return CONTENTS_TYPE_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.model.IRemoteContentsType#isPersistent()
	 */
	public boolean isPersistent() {
		return false;
	}

}
