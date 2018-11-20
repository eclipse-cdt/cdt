/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.jface.wizard.Wizard;

public class NewCMakeToolChainFileWizard extends Wizard {

	private ICMakeToolChainFile newFile;
	private NewCMakeToolChainFilePage page;

	@Override
	public void addPages() {
		page = new NewCMakeToolChainFilePage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		newFile = page.getNewFile();
		return true;
	}

	public ICMakeToolChainFile getNewFile() {
		return newFile;
	}

}
