/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;

/**
 * The C element working set page allows the user to create 
 * and edit a C element working set.
 * <p>
 * Working set elements are presented as a C element tree.
 * </p>
 * 
 */
public class CElementWorkingSetPage extends WizardPage implements IWorkingSetPage {

	final private static String PAGE_TITLE= WorkingSetMessages.CElementWorkingSetPage_title; 
	final private static String PAGE_ID= "CElementWorkingSetPage"; //$NON-NLS-1$

	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private Text fWorkingSetName;
	private CheckboxTreeViewer fTree;
	private IWorkingSet fWorkingSet;
	private boolean fFirstCheck;		// set to true if selection is set in setSelection
	private ITreeContentProvider fTreeContentProvider;

	/**
	 * Creates a new instance of the receiver.
	 */
	public CElementWorkingSetPage() {
		super(PAGE_ID, PAGE_TITLE, CPluginImages.DESC_WIZABAN_C_APP);
		setDescription(WorkingSetMessages.CElementWorkingSetPage_description); 
		fFirstCheck= true;
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setText(WorkingSetMessages.CElementWorkingSetPage_name); 
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fWorkingSetName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fWorkingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fWorkingSetName.addModifyListener(
			new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		fWorkingSetName.setFocus();
		
		label = new Label(composite, SWT.WRAP);
		label.setText(WorkingSetMessages.CElementWorkingSetPage_content); 
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fTree = new CheckboxTreeViewer(composite);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		gd.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		fTree.getControl().setLayoutData(gd);

		fTreeContentProvider= new CElementWorkingSetPageContentProvider();
		fTree.setContentProvider(fTreeContentProvider);

		AppearanceAwareLabelProvider cElementLabelProvider= 
			new AppearanceAwareLabelProvider(
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | CElementImageProvider.SMALL_ICONS
			);
		
		fTree.setLabelProvider(new DecoratingCLabelProvider(cElementLabelProvider));
		fTree.setSorter(new CElementSorter());
		fTree.setUseHashlookup(true);

		fTree.setInput(CoreModel.create(CUIPlugin.getWorkspace().getRoot()));

		fTree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		fTree.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element = event.getElement();
				if (fTree.getGrayed(element) == false)
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
						@Override
						public void run() {
							setSubtreeChecked(element, fTree.getChecked(element), false);
						}
					});
			}
		});

		if (fWorkingSet != null) {
			fWorkingSetName.setText(fWorkingSet.getName());
		}
		initializeCheckedState();
		validateInput();

		Dialog.applyDialogFont(composite);
		// TODO Set help for the page 
//		CUIHelp.setHelp(fTree, ICHelpContextIds.C_WORKING_SET_PAGE);
	}

	/*
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	@Override
	public void finish() {
		String workingSetName= fWorkingSetName.getText();
		ArrayList<Object> elements= new ArrayList<Object>(10);
		findCheckedElements(elements, fTree.getInput());
		if (fWorkingSet == null) {
			IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
			fWorkingSet= workingSetManager.createWorkingSet(workingSetName, elements.toArray(new IAdaptable[elements.size()]));
		} else {
			// Add inaccessible resources
			IAdaptable[] oldItems= fWorkingSet.getElements();
			HashSet<IProject> closedWithChildren= new HashSet<IProject>(elements.size());
			for (IAdaptable oldItem : oldItems) {
				IResource oldResource= null;
				if (oldItem instanceof IResource) {
					oldResource= (IResource)oldItem;
				} else {
					oldResource= (IResource)oldItem.getAdapter(IResource.class);
				}
				if (oldResource != null && oldResource.isAccessible() == false) {
					IProject project= oldResource.getProject();
					if (closedWithChildren.contains(project) || elements.contains(project)) {
						elements.add(oldItem);
						elements.remove(project);
						closedWithChildren.add(project);
					}
				}
			}
			fWorkingSet.setName(workingSetName);
			fWorkingSet.setElements(elements.toArray(new IAdaptable[elements.size()]));
		}
	}	

	/*
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	@Override
	public IWorkingSet getSelection() {
		return fWorkingSet;
	}

	/**
	 * Called when the checked state of a tree item changes.
	 * 
	 * @param event the checked state change event.
	 */
	void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			@Override
			public void run() {
				IAdaptable element= (IAdaptable) event.getElement();
				boolean state = event.getChecked();
				
				fTree.setGrayed(element, false);
				if (isExpandable(element)) {
					setSubtreeChecked(element, state, true);
				}
				updateParentState(element, state);
				validateInput();
			}
		});
	}
	
	private boolean isExpandable(Object element) {
		return (element instanceof ICProject || element instanceof ICContainer
				|| element instanceof CElementGrouping
				|| element instanceof ICModel || element instanceof IContainer);
	}

	private void updateParentState(Object child, boolean baseChildState) {
		if (child == null)
			return;
		if (child instanceof IAdaptable) {
			IResource resource= (IResource)((IAdaptable)child).getAdapter(IResource.class);
			if (resource != null && !resource.isAccessible())
				return;
		}
		Object parent= fTreeContentProvider.getParent(child);
		if (parent == null)
			return;
		
		updateObjectState(parent, baseChildState);
	}

	private void updateObjectState(Object element, boolean baseChildState) {		

		boolean allSameState= true;
		Object[] children= fTreeContentProvider.getChildren(element);

		for (int i= children.length -1; i >= 0; i--) {
			if (fTree.getChecked(children[i]) != baseChildState || fTree.getGrayed(children[i])) {
				allSameState= false;
				break;
			}
		}
	
		fTree.setGrayed(element, !allSameState);
		fTree.setChecked(element, !allSameState || baseChildState);
		
		updateParentState(element, baseChildState);
	}

	/**
	 * Sets the checked state of tree items based on the initial 
	 * working set, if any.
	 */
	private void initializeCheckedState() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			@Override
			public void run() {
				Object[] elements;
				if (fWorkingSet == null) {
					// Use current part's selection for initialization
					IWorkbenchPage page= CUIPlugin.getActivePage();
					if (page == null)
						return;
					
					IWorkbenchPart part= CUIPlugin.getActivePage().getActivePart();
					if (part == null)
						return;
					
					try {
						elements= SelectionConverter.getStructuredSelection(part).toArray();
						for (int i= 0; i < elements.length; i++) {
							if (elements[i] instanceof IResource) {
								ICElement ce= (ICElement)((IResource)elements[i]).getAdapter(ICElement.class);
								if (ce != null && ce.exists() &&  ce.getCProject().isOnSourceRoot((IResource)elements[i]))
									elements[i]= ce;
							}
						}
					} catch (CModelException e) {
						return;
					}
				}
				else
					elements= fWorkingSet.getElements();

				for (int i = 0; i < elements.length; i++) {
					Object element = elements[i];
					if (element instanceof IResource) {
						IProject project= ((IResource)element).getProject();
						if (!project.isAccessible()) {
							elements[i]= project;
						} else {
							// for backwards compatibility: adapt to ICElement if possible
							if(CoreModel.hasCNature(project)) {
								ICElement cElement= CoreModel.getDefault().create((IResource)element);
								if (cElement != null) {
									elements[i]= cElement;
								}
							}
						}
					} else if (element instanceof ICElement) {
						ICProject cProject= ((ICElement)element).getCProject();
						if (cProject != null && !cProject.getProject().isAccessible()) 
							elements[i]= cProject.getProject();
					}
				}
				fTree.setCheckedElements(elements);
				HashSet<Object> parents = new HashSet<Object>();
				for (Object element : elements) {
					if (isExpandable(element))
						setSubtreeChecked(element, true, true);
						
					if (element instanceof IAdaptable) {
						IResource resource= (IResource) ((IAdaptable)element).getAdapter(IResource.class);
						if (resource != null && !resource.isAccessible())
							continue;
					}
					Object parent= fTreeContentProvider.getParent(element);
					if (parent != null)
						parents.add(parent);
				}
				
				for (Object object : parents)
					updateObjectState(object, true);
			}
		});
	}

	/*
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public void setSelection(IWorkingSet workingSet) {
		if (workingSet == null) {
			throw new IllegalArgumentException("Working set must not be null"); //$NON-NLS-1$
		}
		fWorkingSet = workingSet;
		if (getContainer() != null && fWorkingSetName != null) {
			fFirstCheck = false;
			fWorkingSetName.setText(workingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}	
	/**
	 * Sets the checked state of the container's members.
	 * 
	 * @param parent the parent whose children should be checked/unchecked
	 * @param state true=check all members in the container. false=uncheck all 
	 * 	members in the container.
	 * @param checkExpandedState true=recurse into sub-containers and set the 
	 * 	checked state. false=only set checked state of members of this container
	 */
	private void setSubtreeChecked(Object parent, boolean state, boolean checkExpandedState) {
		if (!(parent instanceof IAdaptable))
			return;
		IContainer container= (IContainer)((IAdaptable)parent).getAdapter(IContainer.class);
		if ((!fTree.getExpandedState(parent) && checkExpandedState) || (container != null && !container.isAccessible()))
			return;
		
		Object[] children= fTreeContentProvider.getChildren(parent);
		for (int i= children.length - 1; i >= 0; i--) {
			Object element= children[i];
			if (state) {
				fTree.setChecked(element, true);
				fTree.setGrayed(element, false);
			}
			else
				fTree.setGrayChecked(element, false);
			if (isExpandable(element))
				setSubtreeChecked(element, state, true);
		}
	}

	/**
	 * Validates the working set name and the checked state of the 
	 * resource tree.
	 */
	void validateInput() {
		String errorMessage = null;
		String newText = fWorkingSetName.getText();

		if (newText.equals(newText.trim()) == false) {
			errorMessage = WorkingSetMessages.CElementWorkingSetPage_warning_nameMustNotBeEmpty; 
		}
		if (newText.equals("")) { //$NON-NLS-1$
			if (fFirstCheck) {
				setPageComplete(false);
				fFirstCheck= false;
				return;
			}
			errorMessage = WorkingSetMessages.CElementWorkingSetPage_warning_nameMustNotBeEmpty; 
		}

		fFirstCheck= false;

		if (errorMessage == null && (fWorkingSet == null || newText.equals(fWorkingSet.getName()) == false)) {
			IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
			for (IWorkingSet workingSet : workingSets) {
				if (newText.equals(workingSet.getName())) {
					errorMessage = WorkingSetMessages.CElementWorkingSetPage_warning_workingSetExists; 
				}
			}
		}
		
		if (errorMessage == null && fTree.getCheckedElements().length == 0) {
			String infoMessage = WorkingSetMessages.CElementWorkingSetPage_warning_resourceMustBeChecked; 
			setMessage(infoMessage, INFORMATION);
		}
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	/**
	 * Collects all checked elements of the given parent.
	 * 
	 * @param checkedElements the output, list of checked elements
	 * @param parent the parent to collect checked elements in
	 */
	private void findCheckedElements(List<Object> checkedElements, Object parent) {
		Object[] children= fTreeContentProvider.getChildren(parent);
		for (Object element : children) {
			if (fTree.getGrayed(element))
				findCheckedElements(checkedElements, element);
			else if (fTree.getChecked(element))
				checkedElements.add(element);
		}
	}
}
