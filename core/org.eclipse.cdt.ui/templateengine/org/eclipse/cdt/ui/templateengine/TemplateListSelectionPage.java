/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 *  TemplateListSelectionPage
 */
class TemplateListSelectionPage extends WizardSelectionPage implements ISelectionChangedListener {

	private String labelText;
	private FormBrowser descriptionBrowser;
	private TreeViewer wizardSelectionTreeViewer = null;
	private TableViewer wizardSelectionTableViewer = null;
	private StructuredViewer wizardSelectionViewer = null;
	private TemplatesChoiceWizard parentWizard;
	private Template[] templates;

	public TemplateListSelectionPage(TemplatesChoiceWizard parentWizard) {
		super("Template Selection"); //$NON-NLS-1$
		setTitle(parentWizard.getListSelectionTitle());
		setDescription(parentWizard.getListSelectionDescription());
		this.labelText = parentWizard.getListSelectionLabel();
		descriptionBrowser = new FormBrowser();
		descriptionBrowser.setText(""); //$NON-NLS-1$
		this.parentWizard = parentWizard;
	}

	public void createDescriptionIn(Composite composite) {
		descriptionBrowser.createControl(composite);
		Control c = descriptionBrowser.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 200;
		c.setLayoutData(gd);
	}

	public String getLabel() {
		return labelText;
	}

	public void setDescriptionText(String text) {
		descriptionBrowser.setText(text);
	}

	public void setDescriptionEnabled(boolean enabled) {
		Control control = descriptionBrowser.getControl();
		if (control != null) {
			control.setEnabled(enabled);
		}
	}

	public void moveToNextPage() {
		getContainer().showPage(getNextPage());
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(container, SWT.NONE);
		label.setText(getLabel());
		GridData gd = new GridData();
		label.setLayoutData(gd);

		SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 300;
		gd.minimumHeight = 230;
		sashForm.setLayoutData(gd);

		boolean useTree = parentWizard.showTemplatesInTreeView();

		if (useTree)
		{
			wizardSelectionTreeViewer = new TreeViewer(sashForm, SWT.BORDER);
			wizardSelectionTreeViewer.setContentProvider(parentWizard);
			wizardSelectionTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					selectionChanged(new SelectionChangedEvent(wizardSelectionTreeViewer, wizardSelectionTreeViewer.getSelection()));
					moveToNextPage();
				}
			});
			wizardSelectionTreeViewer.setInput(templates);
			wizardSelectionTreeViewer.addSelectionChangedListener(this);
			wizardSelectionTreeViewer.getTree().setData("name", "templates"); //$NON-NLS-1$ //$NON-NLS-2$
			wizardSelectionViewer = wizardSelectionTreeViewer;

		}
		else
		{
			wizardSelectionTableViewer = new TableViewer(sashForm, SWT.BORDER);
			wizardSelectionTableViewer.setContentProvider(parentWizard);
			wizardSelectionTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					selectionChanged(new SelectionChangedEvent(wizardSelectionTableViewer, wizardSelectionTableViewer.getSelection()));
					moveToNextPage();
				}
			});
			wizardSelectionTableViewer.setInput(templates);
			wizardSelectionTableViewer.addSelectionChangedListener(this);
			wizardSelectionTableViewer.getTable().setData("name", "templates"); //$NON-NLS-1$ //$NON-NLS-2$
			wizardSelectionViewer = wizardSelectionTableViewer;

		}
		wizardSelectionViewer.getControl().setData(".uid", "wizardSelectionViewer"); //$NON-NLS-1$ //$NON-NLS-2$

		createDescriptionIn(sashForm);
		sashForm.setWeights(new int[] {75, 25});

		Dialog.applyDialogFont(container);
		setControl(container);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setErrorMessage(null);
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Template currentWizardSelection = null;
		Object selectedObject = null;
		Iterator<?> iter = selection.iterator();
		if (iter.hasNext()) {
			selectedObject = iter.next();
			if (selectedObject instanceof Template)
				currentWizardSelection = (Template) selectedObject;
		}
		if (currentWizardSelection == null) {
			setDescriptionText(parentWizard.getDescription(selectedObject));
			setSelectedNode(null);
			return;
		}
		final Template finalSelection = currentWizardSelection;
		setSelectedNode(new WizardNode(this, finalSelection));
		setDescriptionText(parentWizard.getDescription(finalSelection));
		getContainer().updateButtons();
	}

	public Template getTemplate() {
		IWizardNode selectedNode = getSelectedNode();
		if (selectedNode != null) {
			return ((WizardNode)selectedNode).getTemplate();
		}
		return null;
	}

	public IWizardPage getNextPage(boolean shouldCreate) {
		if (!shouldCreate) {
			return super.getNextPage();
		}
		IWizardNode selectedNode = getSelectedNode();
		selectedNode.dispose();
		IWizard wizard = selectedNode.getWizard();
		if (wizard == null) {
			super.setSelectedNode(null);
			return null;
		}
		if (shouldCreate) {
			wizard.addPages();
		}

		return wizard.getStartingPage();
	}

	@Override
	public boolean canFlipToNextPage() {
		IStructuredSelection ssel = (IStructuredSelection)wizardSelectionViewer.getSelection();
		return ssel != null && !ssel.isEmpty() && (ssel.getFirstElement() instanceof Template);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			Template[] templates = parentWizard.getTemplates();
			if (templatesHaveChanged(templates)) {
				this.templates = templates;
				wizardSelectionViewer.setInput(templates);
				wizardSelectionViewer.refresh();
				if (wizardSelectionTreeViewer != null) {
					wizardSelectionTreeViewer.expandAll();
				}

				// select the first element by default
				if (wizardSelectionTableViewer != null) {
					wizardSelectionTableViewer.setSelection(new StructuredSelection(wizardSelectionTableViewer.getElementAt(0)), true);
				}
				if (wizardSelectionTreeViewer != null) {
					wizardSelectionTreeViewer.setSelection(new StructuredSelection(wizardSelectionTreeViewer.getTree().getItem(0).getData()), true);
				}
			}
		}
		super.setVisible(visible);
		if (visible) {
			if (wizardSelectionTreeViewer != null) {
				wizardSelectionTreeViewer.getTree().setFocus();
			}
			if (wizardSelectionTableViewer != null) {
				wizardSelectionTableViewer.getTable().setFocus();
			}
		}
	}

	private boolean templatesHaveChanged(Template[] newTemplates) {
		// doing this rather than an array compare because even when
		// the templates are the same the objects are not.  we really
		// just need to compare the template info.
		boolean changed = false;
		if (newTemplates != null && templates != null && newTemplates.length == templates.length) {
			for (int i=0; i<templates.length; i++) {
				if (!newTemplates[i].getTemplateInfo().equals(templates[i].getTemplateInfo())) {
					changed = true;
					break;
				}
			}
		} else {
			changed = true;
		}

		return changed;
	}

	Map<String, String> getDataInPreviousPages() {
		return parentWizard.getAllDataInNonTemplatePages();
	}

	public IWizardDataPage[] getPagesAfterTemplatePages()
	{
		return parentWizard.getPagesAfterTemplatePages();
	}

	public IWizardDataPage[] getPagesAfterTemplateSelection() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		return parentWizard.getPagesAfterTemplateSelectionWithExtraPages(getTemplate());
	}

	public void adjustTemplateValues(Template template) {
		parentWizard.adjustTemplateValues(template);
	}

}
