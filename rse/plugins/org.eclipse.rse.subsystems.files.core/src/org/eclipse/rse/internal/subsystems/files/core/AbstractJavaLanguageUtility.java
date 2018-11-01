/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * This class is a Java language utility.
 */
public abstract class AbstractJavaLanguageUtility extends AbstractLanguageUtility implements IJavaLanguageUtility {

	/**
	 * Constructor.
	 * @param subsystem the subsystem with which the utility is associated.
	 * @param language the language.
	 */
	public AbstractJavaLanguageUtility(IRemoteFileSubSystem subsystem, String language) {
		super(subsystem, language);
	}
}