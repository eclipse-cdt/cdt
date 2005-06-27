/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.internal.corext.refactoring.base.Change;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.ICompositeChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.ViewerPane;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.PageBook;

/**
 * Presents the changes made by the refactoring.
 * Consists of a tree of changes and a compare viewer that shows the differences. 
 */
public class PreviewWizardPage extends RefactoringWizardPage implements IPreviewWizardPage {
	// Dummy root node if input element isn't a composite change.
	private static class DummyRootNode extends Change implements ICompositeChange {
		private IChange[] fChildren;
		
		public DummyRootNode(IChange change) {
			fChildren= new IChange[] { change };
		}
		public IChange[] getChildren() {
			return fChildren;
		}
		public String getName() {
			return null;
		}
		public Object getModifiedLanguageElement() {
			return null;
		}
		public IChange getUndoChange() {
			return null;
		}
		public void perform(ChangeContext context, IProgressMonitor pm) {
		}
	}
	
	private static class NullPreviewer implements IChangePreviewViewer {
		private Label fLabel;
		public void createControl(Composite parent) {
			fLabel= new Label(parent, SWT.CENTER | SWT.FLAT);
			fLabel.setText(RefactoringMessages.getString("PreviewWizardPage.no_preview")); //$NON-NLS-1$
		}
		public void refresh() {
		}
		public Control getControl() {
			return fLabel;
		}
		public void setInput(Object input) throws CoreException {
		}
	}
	
	private class NextChange extends Action {
		public NextChange() {
			setImageDescriptor(CompareUI.DESC_ETOOL_NEXT);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_NEXT);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_NEXT);
			setToolTipText(RefactoringMessages.getString("PreviewWizardPage.next_Change")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, ICHelpContextIds.NEXT_CHANGE_ACTION);			
		}
		public void run() {
			fTreeViewer.revealNext();	
		}
	}
	
	private class PreviousChange extends Action {
		public PreviousChange() {
			setImageDescriptor(CompareUI.DESC_ETOOL_PREV);
			setDisabledImageDescriptor(CompareUI.DESC_DTOOL_PREV);
			setHoverImageDescriptor(CompareUI.DESC_CTOOL_PREV);
			setToolTipText(RefactoringMessages.getString("PreviewWizardPage.previous_Change")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, ICHelpContextIds.PREVIOUS_CHANGE_ACTION);			
		}	
		public void run() {
			fTreeViewer.revealPrevious();
		}
	}
	
	private IChange fChange;		
	private ChangeElement fCurrentSelection;
	private PageBook fPageContainer;
	private Control fStandardPage;
	private Control fNullPage;
	private ChangeElementTreeViewer fTreeViewer;
//////
//	private PageBook fPreviewContainer;
//	private ChangePreviewViewerDescriptor fCurrentDescriptor;
//	private IChangePreviewViewer fCurrentPreviewViewer;
//	private IChangePreviewViewer fNullPreviewer;
//////	
	public PreviewWizardPage() {
		super(PAGE_NAME);
		setDescription(RefactoringMessages.getString("PreviewWizardPage.description")); //$NON-NLS-1$
	}

	public void setChange(IChange change) {
		if (fChange == change)
			return;
		
		fChange= change;	
		setTreeViewerInput();
	}

	protected ChangeElementTreeViewer createTreeViewer(Composite parent) {
		return new ChangeElementTreeViewer(parent);
	}
	
	protected ITreeContentProvider createTreeContentProvider() {
		return new ChangeElementContentProvider();
	}
	
	protected ILabelProvider createTreeLabelProvider() {
		return new ChangeElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT | CElementLabelProvider.SHOW_SMALL_ICONS);
	}
	
	protected boolean performFinish() {
		return getRefactoringWizard().performFinish(new PerformChangeOperation(fChange));
	} 
	
	public boolean canFlipToNextPage() {
		return false;
	}
	
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		fPageContainer= new PageBook(parent, SWT.NONE);
		fStandardPage= createStandardPreviewPage(fPageContainer);
		fNullPage= createNullPage(fPageContainer);
		setControl(fPageContainer);
		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.REFACTORING_PREVIEW_WIZARD_PAGE);
	}

	private Composite createStandardPreviewPage(Composite parent) {
		// XXX The composite is needed to limit the width of the SashForm. See http://bugs.eclipse.org/bugs/show_bug.cgi?id=6854
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		result.setLayout(layout);
		
		SashForm sashForm= new SashForm(result, SWT.HORIZONTAL);
		
		ViewerPane pane= new ViewerPane(sashForm, SWT.BORDER | SWT.FLAT);
		pane.setText(RefactoringMessages.getString("PreviewWizardPage.changes")); //$NON-NLS-1$
		ToolBarManager tbm= pane.getToolBarManager();
		tbm.add(new NextChange());
		tbm.add(new PreviousChange());
		tbm.update(true);
		
		fTreeViewer= createTreeViewer(pane);
		fTreeViewer.setContentProvider(createTreeContentProvider());
		fTreeViewer.setLabelProvider(createTreeLabelProvider());
		fTreeViewer.addSelectionChangedListener(createSelectionChangedListener());
		fTreeViewer.addCheckStateListener(createCheckStateListener());
		pane.setContent(fTreeViewer.getControl());
		setTreeViewerInput();
////////		
//		fPreviewContainer= new PageBook(sashForm, SWT.NONE);
//		fNullPreviewer= new NullPreviewer();
//		fNullPreviewer.createControl(fPreviewContainer);
//		fPreviewContainer.showPage(fNullPreviewer.getControl());
//		fCurrentPreviewViewer= fNullPreviewer;
//		fCurrentDescriptor= null;		
//		sashForm.setWeights(new int[]{33, 67});
////////		
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		sashForm.setLayoutData(gd);
		Dialog.applyDialogFont(result);
		return result;
	}
	
	private Control createNullPage(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		result.setLayout(layout);
		Label label= new Label(result, SWT.CENTER);
		label.setText(RefactoringMessages.getString("PreviewWizardPage.no_source_code_change")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Dialog.applyDialogFont(result);
		return result;
	}
	
	public void setVisible(boolean visible) {
		fCurrentSelection= null;
		if (hasChanges()) {
			fPageContainer.showPage(fStandardPage);
			ChangeElement treeViewerInput= (ChangeElement)fTreeViewer.getInput();
			if (visible && treeViewerInput != null) {
				IStructuredSelection selection= (IStructuredSelection)fTreeViewer.getSelection();
				if (selection.isEmpty()) {
					ITreeContentProvider provider= (ITreeContentProvider)fTreeViewer.getContentProvider();
					Object[] elements= provider.getElements(treeViewerInput);
					if (elements != null && elements.length > 0) {
						Object element= elements[0];
						if (getRefactoringWizard().getExpandFirstNode()) {
							Object[] subElements= provider.getElements(element);
							if (subElements != null && subElements.length > 0) {
								fTreeViewer.expandToLevel(element, 999);
							}
						}
						fTreeViewer.setSelection(new StructuredSelection(element));
					}
				}
			}
			super.setVisible(visible);
			fTreeViewer.getControl().setFocus();
		} else {
			fPageContainer.showPage(fNullPage);
			super.setVisible(visible);
		}
		getRefactoringWizard().setPreviewShown(visible);
	}
	
	private void setTreeViewerInput() {
		ChangeElement input;
		IChange change= computeChangeInput();
		if (change == null) {
			input= null;
		} else if (change instanceof ICompositeChange && !(change instanceof TextChange)) {
			input= new DefaultChangeElement(null, change);
		} else {
			input= new DefaultChangeElement(null, new DummyRootNode(change));
		}
		if (fTreeViewer != null) {
			fTreeViewer.setInput(input);
		}
	}
	
	private IChange computeChangeInput() {
		IChange result= fChange;
		if (result == null)
			return result;
		while (true) {
			if (result instanceof ICompositeChange) {
				IChange[] children= ((ICompositeChange)result).getChildren();
				if (children.length == 1) {
					result= children[0];
				} else {
					return result;
				}
			} else {
				return result;
			}
		}
	}

	private ICheckStateListener createCheckStateListener() {
		return new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event){
				ChangeElement element= (ChangeElement)event.getElement();
				if (isChild(fCurrentSelection, element) || isChild(element, fCurrentSelection)) {
					showPreview(fCurrentSelection);
				}
			}
			private boolean isChild(ChangeElement element, ChangeElement child) {
				while (child != null) {
					if (child == element)
						return true;
					child= child.getParent();
				}
				return false;
			}
		};
	}
		
	private ISelectionChangedListener createSelectionChangedListener() {
		return new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				if (sel.size() == 1) {
					ChangeElement newSelection= (ChangeElement)sel.getFirstElement();
					if (newSelection != fCurrentSelection) {
						fCurrentSelection= newSelection;
						showPreview(newSelection);
					}
				} else {
					showPreview(null);
				}
			}
		};
	}	

	private void showPreview(ChangeElement element) {
/////////////		
//		try {
//			if (element == null) {
				showNullPreviewer();
//			} else {
//				ChangePreviewViewerDescriptor descriptor= element.getChangePreviewViewer();
//				if (fCurrentDescriptor != descriptor) {
//					IChangePreviewViewer newViewer;
//					if (descriptor != null) {
//						newViewer= descriptor.createViewer();
//						newViewer.createControl(fPreviewContainer);
//					} else {
//						newViewer= fNullPreviewer;
//					}
//					fCurrentDescriptor= descriptor;
//					element.feedInput(newViewer);
//					if (fCurrentPreviewViewer != null && fCurrentPreviewViewer != fNullPreviewer)
//						fCurrentPreviewViewer.getControl().dispose();
//					fCurrentPreviewViewer= newViewer;				
//					fPreviewContainer.showPage(fCurrentPreviewViewer.getControl());
//				} else {
//					element.feedInput(fCurrentPreviewViewer);
//				}
//		}
//		} catch (CoreException e) {
//			showNullPreviewer();
//		}
///////////
	}
	
	private void showNullPreviewer() {
/////////
//		fCurrentDescriptor= null;
//		fCurrentPreviewViewer= fNullPreviewer;
//		fPreviewContainer.showPage(fCurrentPreviewViewer.getControl());
/////////		
	}

	public boolean hasChanges() {
		if (fChange == null)
			return false;
		if (fChange instanceof ICompositeChange)
			return ((ICompositeChange)fChange).getChildren().length > 0;
		return true;
	}
	
}
