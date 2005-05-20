/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.wizardPages;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

public class AlwaysPresentWizardPage extends MBSCustomPage
{

	private Composite composite;

	public AlwaysPresentWizardPage()
	{
		pageID = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.AlwaysPresentWizardPage";
	}

	public boolean canFlipToNextPage()
	{

		return (MBSCustomPageManager.getNextPage(pageID) != null);
	}

	public String getName()
	{
		return new String("Always Present Page");
	}


	public void createControl(Composite parent)
	{

		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text pageText = new Text(composite, SWT.CENTER);
		pageText.setBounds(composite.getBounds());
		pageText.setText("always present");
		pageText.setVisible(true);

	}

	public void dispose()
	{
		composite.dispose();

	}

	public Control getControl()
	{
		return composite;
	}

	public String getDescription()
	{
		return new String("My description");
	}

	public String getErrorMessage()
	{
		return new String("My error msg");
	}

	public Image getImage()
	{
		return wizard.getDefaultPageImage();
	}

	public String getMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle()
	{
		return new String("My Title");
	}

	public void performHelp()
	{
		// do nothing

	}

	public void setDescription(String description)
	{
		// do nothing

	}

	public void setImageDescriptor(ImageDescriptor image)
	{
		// do nothing

	}

	public void setTitle(String title)
	{
		// do nothing

	}

	public void setVisible(boolean visible)
	{
		composite.setVisible(visible);

	}

	protected boolean isCustomPageComplete()
	{
		return true;
	}

}
