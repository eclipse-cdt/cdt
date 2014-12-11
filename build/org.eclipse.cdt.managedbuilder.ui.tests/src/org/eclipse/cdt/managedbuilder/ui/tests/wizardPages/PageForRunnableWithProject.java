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

	public PageForRunnableWithProject()
	{
		pageID = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ProjectStorageInterfacePage";
	}


	@Override
	public String getName() {
		return new String("Operation that stores project page");
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text pageText = new Text(composite, SWT.CENTER);
		pageText.setBounds(composite.getBounds());
		pageText.setText("Operation that stores project WizardPage");
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
