/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
