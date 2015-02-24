/*******************************************************************************
 * Copyright (c) 2014 Broadcom Corp
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Mason (Broadcom Corp.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.core.resources.IProject;

/**
 * This can be implemented by MBSWizardOperations to allow the
 * project that has just been created to be passed in before
 * running the operation
 *
 * @since 8.3
 */
public interface IProjectSettable {
	/**
	 * Method to store the newly created project in the wizard page operation
	 *
	 * @param proj  Project created by new project wizard upon which operation will act
	 */
	void setProject(IProject proj);
}
