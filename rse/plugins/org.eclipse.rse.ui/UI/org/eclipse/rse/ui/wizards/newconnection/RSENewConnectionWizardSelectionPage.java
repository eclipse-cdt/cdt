/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeContentProvider;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeLabelProvider;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreePatternFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The New Connection Wizard main page that allows selection of system type.
 */
public class RSENewConnectionWizardSelectionPage extends WizardPage {
	private final String helpId = RSEUIPlugin.HELPPREFIX + "wncc0000"; //$NON-NLS-1$;
	
	private IRSESystemType[] restrictedSystemTypes;

	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;
	private ViewerFilter filteredTreeWizardStateFilter;
	private RSENewConnectionWizardSelectionTreeDataManager filteredTreeDataManager;
	
	private class NewConnectionWizardStateFilter extends ViewerFilter {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider()).getChildren(element);
			if (children.length > 0) {
				return filter(viewer, element, children).length > 0;
			}
			
			if (element instanceof RSENewConnectionWizardSelectionTreeElement) {
				// the system type must be enabled, otherwise it is filtered out
				IRSESystemType systemType = ((RSENewConnectionWizardSelectionTreeElement)element).getSystemType();
				if (systemType == null) return false;
				
				// if the page is restricted to a set of system types, check on them first
				IRSESystemType[] restricted = getRestrictToSystemTypes();
				if (restricted != null && restricted.length > 0) {
					if (!Arrays.asList(restricted).contains(systemType)) return false;
				}
				
				RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemType.getAdapter(IRSESystemType.class));
				if (adapter != null) {
					return adapter.isEnabled(systemType);
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Constructor.
	 */
	public RSENewConnectionWizardSelectionPage() {
		super("RSENewConnectionWizardSelectionPage"); //$NON-NLS-1$
		setTitle(getDefaultTitle());
		setDescription(getDefaultDescription());
	}

	/**
	 * Returns the default page title.
	 * 
	 * @return The default page title. Must be never <code>null</code>.
	 */
	protected String getDefaultTitle() {
		return SystemResources.RESID_NEWCONN_MAIN_PAGE_TITLE;
	}
	
	/**
	 * Returns the default page description.
	 * 
	 * @return The default page description. Must be never <code>null</code>.
	 */
	protected String getDefaultDescription() {
		return SystemResources.RESID_NEWCONN_MAIN_PAGE_DESCRIPTION;
	}

	/**
	 * Restrict the selectable wizards to the given set of system types.
	 * 
	 * @param systemTypes The list of the system types to restrict the page to or <code>null</code>.
	 */
	public void restrictToSystemTypes(IRSESystemType[] systemTypes) {
		this.restrictedSystemTypes = systemTypes;
	}
	
	/**
	 * Returns the list of system types the page is restricted to.
	 * 
	 * @return The list of system types the page is restricted to or <code>null</code>.
	 */
	protected IRSESystemType[] getRestrictToSystemTypes() {
		return restrictedSystemTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
    Label label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.setText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL + ":"); //$NON-NLS-1$    
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    filteredTreeFilter = new RSEWizardSelectionTreePatternFilter();
    filteredTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filteredTreeFilter);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

    final TreeViewer treeViewer = filteredTree.getViewer();
    treeViewer.setContentProvider(new RSEWizardSelectionTreeContentProvider());
    // Explicitly allow the tree items to get decorated!!!
		treeViewer.setLabelProvider(new DecoratingLabelProvider(new RSEWizardSelectionTreeLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
//		treeViewer.setComparator(NewWizardCollectionComparator.INSTANCE);
//		treeViewer.addSelectionChangedListener(this);
		treeViewer.setAutoExpandLevel(2);

		filteredTreeWizardStateFilter = new NewConnectionWizardStateFilter();
		treeViewer.addFilter(filteredTreeWizardStateFilter);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
//				onSelectionChanged();
				if (canFlipToNextPage()) getWizard().getContainer().showPage(getNextPage());
			}
		});
		
		filteredTreeDataManager = new RSENewConnectionWizardSelectionTreeDataManager();
		treeViewer.setInput(filteredTreeDataManager);
		
		// apply the standard dialog font
		Dialog.applyDialogFont(composite);
		
		setControl(composite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpId);
	}

	/**
	 * Called from the selection and double-click listener to propage the current
	 * system type selection to the underlaying wizard.
	 */
	protected void onSelectionChanged() {
		IWizard wizard = getWizard();
		if (wizard instanceof ISelectionProvider && filteredTree.getViewer().getSelection() instanceof IStructuredSelection) {
			ISelectionProvider selectionProvider = (ISelectionProvider)wizard;
			IStructuredSelection filteredTreeSelection = (IStructuredSelection)filteredTree.getViewer().getSelection();
			if (filteredTreeSelection.getFirstElement() instanceof RSENewConnectionWizardSelectionTreeElement) {
				RSENewConnectionWizardSelectionTreeElement element = (RSENewConnectionWizardSelectionTreeElement)filteredTreeSelection.getFirstElement();
				selectionProvider.setSelection(new StructuredSelection(element.getSystemType()));
				if (element.getDescription() != null) {
					setDescription(element.getDescription());
				} else {
					if (!getDefaultDescription().equals(getDescription())) setDescription(getDefaultDescription());
				}
			}
		}
	}
	
	/**
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizardPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
//	public Control createContents(Composite parent) {
//		
//		int nbrColumns = 2;
//		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
//		SystemWidgetHelpers.setCompositeHelp(composite_prompts, parentHelpId);
//		
//		String temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL);
//		
//		Label labelSystemType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
//		labelSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
//		
//		if (restrictedSystemTypes == null) {
//			textSystemType = SystemWidgetHelpers.createSystemTypeListBox(parent, null);
//		}
//		else {
//			String[] systemTypeNames = new String[restrictedSystemTypes.length];
//			
//			for (int i = 0; i < restrictedSystemTypes.length; i++) {
//				systemTypeNames[i] = restrictedSystemTypes[i].getName();
//			}
//			
//			textSystemType = SystemWidgetHelpers.createSystemTypeListBox(parent, null, systemTypeNames);
//		}
//		
//		textSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
//		SystemWidgetHelpers.setHelp(textSystemType, RSEUIPlugin.HELPPREFIX + "ccon0003"); //$NON-NLS-1$
//		
//		textSystemType.addSelectionListener(this);
//		
//		descriptionSystemType = SystemWidgetHelpers.createMultiLineTextField(parent,null,30);
//		descriptionSystemType.setEditable(false);
//
//		widgetSelected(null);
//		
//		return composite_prompts;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizardPage#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}


}