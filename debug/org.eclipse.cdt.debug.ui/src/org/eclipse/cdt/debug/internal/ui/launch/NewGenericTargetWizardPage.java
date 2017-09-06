/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class NewGenericTargetWizardPage extends WizardPage {

	private GenericTargetPropertiesBlock propertiesBlock;

	public NewGenericTargetWizardPage() {
		super(NewGenericTargetWizardPage.class.getName());
		setTitle("Generic Target");
		setDescription("Enter name and properties for the target.");
	}

	@Override
	public void createControl(Composite parent) {
		propertiesBlock = new GenericTargetPropertiesBlock(parent, SWT.NONE);
		setControl(propertiesBlock);
	}

	public String getTargetName() {
		return propertiesBlock.getTargetName();
	}

	public String getOS() {
		return propertiesBlock.getOS();
	}

	public String getArch() {
		return propertiesBlock.getArch();
	}

}
