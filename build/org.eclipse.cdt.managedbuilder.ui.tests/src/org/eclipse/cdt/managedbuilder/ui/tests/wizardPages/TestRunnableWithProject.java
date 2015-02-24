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

package org.eclipse.cdt.managedbuilder.ui.tests.wizardPages;

import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.IProjectSettable;
import org.eclipse.core.resources.IProject;

public class TestRunnableWithProject implements Runnable, IProjectSettable {

	IProject project;

	@Override
	public void setProject(IProject proj) {
		project = proj;
	}

	@Override
	public void run() {
		TestCustomPageManager.testProjectName = project.getName();
	}
}
