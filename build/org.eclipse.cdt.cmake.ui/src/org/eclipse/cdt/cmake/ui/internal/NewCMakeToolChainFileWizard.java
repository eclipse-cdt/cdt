/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
