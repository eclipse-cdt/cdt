/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Provides the access to the register groups' management functions.
 */
public interface ICRegisterManager extends ICUpdateManager, IAdaptable {

	void addRegisterGroup( IRegisterGroup group );

	void removeRegisterGroup( IRegisterGroup group );

	void removeAllRegisterGroups();
}