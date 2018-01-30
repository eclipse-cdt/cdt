/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui;

import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.jface.wizard.Wizard;

public class NewMesonToolChainFileWizard extends Wizard {

	private IMesonToolChainFile newFile;
	private NewMesonToolChainFilePage page;

	@Override
	public void addPages() {
		page = new NewMesonToolChainFilePage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		newFile = page.getNewFile();
		return true;
	}

	public IMesonToolChainFile getNewFile() {
		return newFile;
	}

}
