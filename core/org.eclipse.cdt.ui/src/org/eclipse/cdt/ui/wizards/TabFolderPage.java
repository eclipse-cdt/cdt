package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.ui.wizards.swt.MGridData;
import org.eclipse.cdt.internal.ui.wizards.swt.MGridLayout;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

/**
 * @deprecated
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class TabFolderPage extends WizardPage implements IValidation {

	private static final String WZ_TITLE= "TabFolderPage.title";
	private static final String WZ_DESC= "TabFolderPage.desc";

	protected TabFolder tabFolder;
	protected CProjectWizard wizard;

	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);

		MGridLayout layout= new MGridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 5;
		layout.minimumWidth= 450;
		layout.minimumHeight= 350;
		layout.numColumns= 1;
		composite.setLayout(layout);

		tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());

		MGridData gd= new MGridData();
		gd.horizontalAlignment= MGridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.verticalAlignment= MGridData.FILL;
		gd.grabExcessVerticalSpace= true;
		tabFolder.setLayoutData(gd);

		wizard.addTabItems(tabFolder);

		//tabFolder.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent e) {
		//		//tabChanged(e.item);
		//	}
		//});

		setControl(composite);
	}

	public void setComplete(boolean complete) {
		setPageComplete(isPageComplete());
	}

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		IWizardTab[] items = wizard.getTabItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setVisible(visible);
		}
	}
	
	public boolean isPageComplete() {
		IWizardTab[] items = wizard.getTabItems();
		for (int i = 0; i < items.length; i++) {
			if (!(items[i].isValid()))
				return false;
		}
		return true;
	}

	public TabFolderPage(CProjectWizard wizard) {
		super("CProjectWizardTabPage");
		this.wizard = wizard;
	}
}
