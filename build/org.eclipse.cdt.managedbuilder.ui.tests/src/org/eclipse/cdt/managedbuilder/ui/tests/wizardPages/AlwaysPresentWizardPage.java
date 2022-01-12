/*******************************************************************************
 * Copyright (c) 2005, 2016 Texas Instruments Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.wizardPages;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class AlwaysPresentWizardPage extends MBSCustomPage {

	private Composite composite;

	public AlwaysPresentWizardPage() {
		pageID = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.AlwaysPresentWizardPage";
	}

	@Override
	public boolean canFlipToNextPage() {

		return (MBSCustomPageManager.getNextPage(pageID) != null);
	}

	@Override
	public String getName() {
		return "Always Present Page";
	}

	@Override
	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text pageText = new Text(composite, SWT.CENTER);
		pageText.setBounds(composite.getBounds());
		pageText.setText("This page is a test page provided by the org.eclipse.cdt.managedbuilder.ui.tests plugin.");
		pageText.setVisible(true);

	}

	@Override
	public void dispose() {
		composite.dispose();

	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public String getDescription() {
		return "This page is for testing, please ignore it.";
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		return "Test Page";
	}

	@Override
	public void performHelp() {
		// do nothing

	}

	@Override
	public void setDescription(String description) {
		// do nothing

	}

	@Override
	public void setImageDescriptor(ImageDescriptor image) {
		// do nothing

	}

	@Override
	public void setTitle(String title) {
		// do nothing

	}

	@Override
	public void setVisible(boolean visible) {
		composite.setVisible(visible);

	}

	@Override
	protected boolean isCustomPageComplete() {
		return true;
	}

}
