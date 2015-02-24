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

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class PageForRunnableWithProject extends MBSCustomPage {

	private Composite composite;

	public PageForRunnableWithProject() {
		pageID = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ProjectStorageInterfacePage";
	}

	@Override
	public String getName() {
		return new String("Page for operation that gets a reference to the created project");
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text pageText = new Text(composite, SWT.CENTER);
		pageText.setBounds(composite.getBounds());
		pageText.setText("WizardPage with operation that gets a reference to the created project");
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
		return new String("My description");
	}

	@Override
	public String getErrorMessage() {
		return new String("My error message");
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
		return new String("My title");
	}

	@Override
	public void performHelp() {
		// Do nothing
	}

	@Override
	public void setDescription(String description) {
		// Do nothing
	}

	@Override
	public void setImageDescriptor(ImageDescriptor image) {
		// Do nothing
	}

	@Override
	public void setTitle(String title) {
		// Do nothing
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
