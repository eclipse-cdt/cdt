/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Intel corporation    - customization for New Project model.
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class ConvertToMakeWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "WizardMakeProjectConversion.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "WizardMakeProjectConversion.description"; //$NON-NLS-1$
    
    protected CWizardHandler h_selected = null;
    // widgets
    private Button specifyProjectTypeButton;
    private Tree tree;
    private Composite right;
    private Button show_sup;
    private Label right_label;
    private Label left_label;

	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToMakeWizardPage(String pageName) {
		super(pageName);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		IStructuredSelection sel = ((BasicNewResourceWizard) getWizard())
				.getSelection();
		if (sel != null) {
			tableViewer.setCheckedElements(sel.toArray());
			setPageComplete(validatePage());
		}
	}

	protected void addToMainPage(Composite container) {
		super.addToMainPage(container);

		Group optionsGroup = new Group(container, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		optionsGroup.setLayout(new GridLayout(1, true));
		optionsGroup.setText(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.projectOptions.title")); //$NON-NLS-1$
		Composite c = new Composite(optionsGroup, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(2, true));

		specifyProjectTypeButton = new Button(c, SWT.CHECK);
		specifyProjectTypeButton.setText(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.projectOptions.projectType")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		specifyProjectTypeButton.setLayoutData(gd);
		specifyProjectTypeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableProjectTypeControls(specifyProjectTypeButton
						.getSelection());
			}
		});

		left_label = new Label(c, SWT.NONE);
		left_label.setText(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.projectOptions.projectTypeTable")); //$NON-NLS-1$
		left_label.setFont(container.getFont());
		left_label.setLayoutData(new GridData(GridData.BEGINNING));

		right_label = new Label(c, SWT.NONE);
		right_label.setFont(container.getFont());
		right_label.setLayoutData(new GridData(GridData.BEGINNING));

		tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0)
					return;
				switchTo((CWizardHandler) tis[0].getData(),
						(EntryDescriptor) tis[0]
								.getData(CDTMainWizardPage.DESC));
			}
		});
		right = new Composite(c, SWT.NONE);
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		right.setLayout(new PageLayout());

		show_sup = new Button(c, SWT.CHECK);
		show_sup.setSelection(true);
		show_sup.setText(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.projectOptions.showSuppressed")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		show_sup.setLayoutData(gd);
		final IWizardItemsListListener filter = new IWizardItemsListListener() {

			// Show only category
			public List<EntryDescriptor> filterItems(
					List<? extends EntryDescriptor> items) {
				List<EntryDescriptor> results = new ArrayList<EntryDescriptor>();

				for (EntryDescriptor entry : items) {
					if (entry.isCategory()) {
						results.add(entry);
					}
				}

				return results;
			}

			public boolean isCurrent() {
				return true;
			}

			public void toolChainListChanged(int count) {
				// Do nothing
			}
		};

		show_sup.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (h_selected != null)
					h_selected.setSupportedOnly(show_sup.getSelection());
				switchTo(CDTMainWizardPage.updateData(tree, right, show_sup,
						filter, null), CDTMainWizardPage.getDescriptor(tree));
			}
		});

		CDTPrefUtil.readPreferredTCs();
		switchTo(CDTMainWizardPage.updateData(tree, right, show_sup, filter,
				null), CDTMainWizardPage.getDescriptor(tree));

		specifyProjectTypeButton.setSelection(true);
		enableProjectTypeControls(true);
	}

	private void enableProjectTypeControls(boolean enabled) {
		left_label.setEnabled(enabled);
		right_label.setEnabled(enabled);
		tree.setEnabled(enabled);
		right.setEnabled(enabled);
		enabledCompositeChildren(right, enabled);
		show_sup.setEnabled(enabled);
	}

	private void enabledCompositeChildren(Composite composite, boolean enabled) {
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setEnabled(enabled);
			if (children[i] instanceof Composite) {
				enabledCompositeChildren((Composite) children[i], enabled);
			}
		}
	}

	private void switchTo(CWizardHandler h, EntryDescriptor ed) {
		if (h == null)
			h = ed.getHandler();
		try {
			if (h != null && ed != null)
				h.initialize(ed);
		} catch (CoreException e) {
			h = null;
		}
		if (h_selected != null)
			h_selected.handleUnSelection();
		h_selected = h;
		if (h == null)
			return;
		right_label.setText(h_selected.getHeader());
		h_selected.handleSelection();
		h_selected.setSupportedOnly(show_sup.getSelection());
	}

	/**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return MakeUIPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return MakeUIPlugin.getResourceString(WZ_DESC);
    }
       
    /**
     * Method isCandidate returns true for:
     * - non-CDT projects
     * - old style Make CDT projects
     * So new model projects and 
     * old style managed projects
     * are refused.
     */
    public boolean isCandidate(IProject project) {
    	boolean a = !AbstractPage.isCDTPrj(project); 
    	boolean b = ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project);
		return a || b; 
    }    

    public void convertProject(IProject project, String bsId, IProgressMonitor monitor) throws CoreException{
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 3); //$NON-NLS-1$
		try {
			if (ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project)) {
				ManagedBuilderCorePlugin.getDefault().convertOldStdMakeToNewStyle(project, monitor);
			} else { 
				super.convertProject(project, bsId, new SubProgressMonitor(monitor, 1));
				if (isSetProjectType()) {
					h_selected.convertProject(project, monitor);
				}
			}
		} finally {
			monitor.done();
		}
    }

	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(MakeUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 3); //$NON-NLS-1$
		try {
			if (ManagedBuilderCorePlugin.getDefault().isOldStyleMakeProject(project)) {
				ManagedBuilderCorePlugin.getDefault().convertOldStdMakeToNewStyle(project, monitor);
			} else { 
				super.convertProject(project, new SubProgressMonitor(monitor, 1), projectID);
				if (isSetProjectType()) {
					h_selected.convertProject(project, monitor);
				}
			}
		} finally {
			monitor.done();
		}
	}

	public boolean isSetProjectType() {
		return specifyProjectTypeButton != null && specifyProjectTypeButton.getSelection();
    }
}
