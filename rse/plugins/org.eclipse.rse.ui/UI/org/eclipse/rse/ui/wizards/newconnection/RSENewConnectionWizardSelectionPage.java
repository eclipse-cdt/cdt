/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * Uwe Stieber      (Wind River) - [209193] RSE new connection wizard shows empty categories if typing something into the filter
 * Martin Oberhuber (Wind River) - [235197][api] Unusable wizard after cancelling on first page
 * Uwe Stieber (Wind River) - [237816][api] restrictToSystemType does not work for RSEMainNewConnectionWizard
 *******************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardCategory;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeContentProvider;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeElement;
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
 * <p>
 * <b>Note:</b> The page allows filtering of the presented wizard list by adapting
 * the associated system type to<br>
 * <ul>
 * <li><code>org.eclipse.jface.viewers.ViewerFilter</code>: calling <code>ViewerFilter.select(...)</code> and double-check via.</li>
 * <li><code>org.eclipse.rse.ui.RSESystemTypeAdapter</code>: calling <code>RSESystemTypeAdapter.isEnabled(...)</code>.</li>
 * </ul>
 */
public class RSENewConnectionWizardSelectionPage extends WizardPage {
	private final String helpId = RSEUIPlugin.HELPPREFIX + "wncc0000"; //$NON-NLS-1$;

	private static final String EXPANDED_CATEGORIES_SETTINGS_ID = "filteredTree.expandedCatogryIds"; //$NON-NLS-1$
	private static final String[] DEFAULT_EXPANDED_CATEGORY_IDS = new String[] { "org.eclipse.rse.ui.wizards.newconnection.default.category" }; //$NON-NLS-1$

	private IRSESystemType[] restrictedSystemTypes;

	private RSENewConnectionWizardRegistry wizardRegistry;
	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;
	private ViewerFilter filteredTreeWizardStateFilter;
	private RSENewConnectionWizardSelectionTreeDataManager filteredTreeDataManager;

	/**
	 * Internal class. The wizard state filter is responsible to filter
	 * out any not enabled or filtered wizard from the tree.
	 */
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

				// First, adapt the system type to a viewer filter and pass on the select request
				// to the viewer filter adapter if available
				ViewerFilter filter = (ViewerFilter)(systemType.getAdapter(ViewerFilter.class));
				if (filter != null && !filter.select(viewer, parentElement, element)) {
					return false;
				}

				// Second, double check if the system type passed the viewer filter but is disabled.
				if (!systemType.isEnabled()) return false;
			}

			// In all other cases, the element passes the filter
			return true;
		}
	}

	/**
	 * Internal class. The wizard viewer comparator is responsible for
	 * the sorting in the tree. Current implementation is not prioritizing
	 * categories.
	 */
	private class NewConnectionWizardViewerComparator extends ViewerComparator {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isSorterProperty(Object element, String property) {
			// The comparator is affected if the label of the elements should change.
      return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

 	/**
	 * Constructor.
     * @since org.eclipse.rse.ui 3.0
	 */
	public RSENewConnectionWizardSelectionPage(RSENewConnectionWizardRegistry wizardRegistry) {
		super("RSENewConnectionWizardSelectionPage"); //$NON-NLS-1$
		setTitle(getDefaultTitle());
		setDescription(getDefaultDescription());
		this.wizardRegistry = wizardRegistry;
	}

	/**
	 * Constructor.
	 *
	 * @deprecated Use
	 *             {@link #RSENewConnectionWizardSelectionPage(RSENewConnectionWizardRegistry)}
	 *             to control the lifetime of the wizard registry
	 */
	public RSENewConnectionWizardSelectionPage() {
		this(RSENewConnectionWizardRegistry.getInstance());
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
	 * @since 3.0 made protected method public
	 */
	public IRSESystemType[] getRestrictToSystemTypes() {
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
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 325; layoutData.widthHint = 450;
		filteredTree.setLayoutData(layoutData);

		final TreeViewer treeViewer = filteredTree.getViewer();
		treeViewer.setContentProvider(new RSEWizardSelectionTreeContentProvider());
		// Explicitly allow the tree items to get decorated!!!
		treeViewer.setLabelProvider(new DecoratingLabelProvider(new RSEWizardSelectionTreeLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		treeViewer.setComparator(new NewConnectionWizardViewerComparator());

		filteredTreeWizardStateFilter = new NewConnectionWizardStateFilter();
		treeViewer.addFilter(filteredTreeWizardStateFilter);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// Double-click on a connection type is triggering the sub wizard
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					// The tree is single selection, so look for the first element only.
					Object element = selection.getFirstElement();
					if (element instanceof RSENewConnectionWizardSelectionTreeElement) {
						// Double-click on a connection type is triggering the sub wizard
						if (canFlipToNextPage()) getWizard().getContainer().showPage(getNextPage());
					} else if (event.getViewer() instanceof TreeViewer) {
						TreeViewer viewer = (TreeViewer)event.getViewer();
						if (viewer.isExpandable(element)) {
							viewer.setExpandedState(element, !viewer.getExpandedState(element));
						}
					}
				}
			}
		});

		filteredTreeDataManager = new RSENewConnectionWizardSelectionTreeDataManager(wizardRegistry);
		treeViewer.setInput(filteredTreeDataManager);

		// apply the standard dialog font
		Dialog.applyDialogFont(composite);

		setControl(composite);

		// Restore the expanded state of the category items within the tree
		// before initializing the selection.
		restoreWidgetValues();

		// Initialize the tree selection
		initializeSelection(treeViewer);

		// Initialize the selection in the tree
		filteredTree.getFilterControl().setFocus();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), helpId);

	}

	/**
	 * Set the initial tree selection for the given tree viewer instance.
	 *
	 * @param treeViewer The tree viewer instance.
	 *
	 * @since 3.1
	 */
	protected void initializeSelection(TreeViewer treeViewer) {
		if (treeViewer == null) return;

		if (getWizard() instanceof ISelectionProvider) {
			ISelectionProvider selectionProvider = (ISelectionProvider)getWizard();
			if (selectionProvider.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
				if (selection.getFirstElement() instanceof IRSESystemType) {
					IRSESystemType systemType = (IRSESystemType)selection.getFirstElement();
					RSENewConnectionWizardSelectionTreeElement treeElement = filteredTreeDataManager.getTreeElementForSystemType(systemType);
					if (treeElement != null) treeViewer.setSelection(new StructuredSelection(treeElement), true);
				}
			}
		}

	}

	/**
	 * Called from the selection listener to propage the current
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
			} else {
				selectionProvider.setSelection(null);
			}
		}

		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		// If the wizard is set and returns dialog settings, we re-use them here
		IDialogSettings settings = super.getDialogSettings();
		// If the dialog settings could not set from the wizard, fallback to the plugins
		// dialog settings store.
		if (settings == null) settings = RSEUIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		settings = settings.getSection(sectionName);

		return settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// if the page will become hidden, save the expansion state of
		// the tree elements.
		if (!visible) saveWidgetValues();
		// If the page will become visible, refresh the viewer
		// content -> The listed system types might have changed.
		else if (filteredTree != null && filteredTree.getViewer() != null) {
			filteredTree.getViewer().refresh();
			initializeSelection(filteredTree.getViewer());
		}
	}

	/**
	 * Restore the tree state from the dialog settings.
	 */
	public void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] expandedCategories = settings.getArray(EXPANDED_CATEGORIES_SETTINGS_ID);
			// by default we expand always the "General" category.
			if (expandedCategories == null) expandedCategories = DEFAULT_EXPANDED_CATEGORY_IDS;
			if (expandedCategories != null) {
				List expanded = new ArrayList();
				for (int i = 0; i < expandedCategories.length; i++) {
					String categoryId = expandedCategories[i];
					if (categoryId != null && !"".equals(categoryId.trim())) { //$NON-NLS-1$
						IRSEWizardRegistryElement registryElement = wizardRegistry.findElementById(categoryId);
						if (registryElement instanceof IRSEWizardCategory) {
							RSEWizardSelectionTreeElement treeElement = filteredTreeDataManager.getTreeElementForCategory((IRSEWizardCategory)registryElement);
							if (treeElement != null) expanded.add(treeElement);
						}
					}
				}

				if (expanded.size() > 0) filteredTree.getViewer().setExpandedElements(expanded.toArray());
			}
		}
	}

	/**
	 * Saves the tree state to the dialog settings.
	 */
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			List expandedCategories = new ArrayList();
			Object[] expanded = filteredTree.getViewer().getVisibleExpandedElements();
			for (int i = 0; i < expanded.length; i++) {
				if (expanded[i] instanceof RSEWizardSelectionTreeElement) {
					IRSEWizardRegistryElement registryElement = ((RSEWizardSelectionTreeElement)expanded[i]).getWizardRegistryElement();
					if (registryElement instanceof IRSEWizardCategory) {
						expandedCategories.add(((IRSEWizardCategory)registryElement).getId());
					}
				}
			}
			settings.put(EXPANDED_CATEGORIES_SETTINGS_ID, (String[])expandedCategories.toArray(new String[expandedCategories.size()]));
		}
	}
}