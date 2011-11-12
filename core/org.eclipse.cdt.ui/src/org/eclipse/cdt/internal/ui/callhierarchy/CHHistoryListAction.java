/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;

public class CHHistoryListAction extends Action {
	
	private class HistoryListDialog extends StatusDialog {
		private ListDialogField<ICElement> fHistoryList;
		private IStatus fHistoryStatus;
		private ICElement fResult;
		
		private HistoryListDialog(Shell shell, ICElement[] historyEntries) {
			super(shell);
			setHelpAvailable(false);			
			setTitle(CHMessages.CHHistoryListAction_HistoryDialog_title); 
			String[] buttonLabels= new String[] { 
				CHMessages.CHHistoryListAction_Remove_label, 
			};
					
			IListAdapter<ICElement> adapter= new IListAdapter<ICElement>() {
				@Override
				public void customButtonPressed(ListDialogField<ICElement> field, int index) {
					doCustomButtonPressed();
				}
				@Override
				public void selectionChanged(ListDialogField<ICElement> field) {
					doSelectionChanged();
				}
				
				@Override
				public void doubleClicked(ListDialogField<ICElement> field) {
					doDoubleClicked();
				}				
			};
		
			ILabelProvider labelProvider= new CUILabelProvider(CHHistoryAction.LABEL_OPTIONS, CElementImageProvider.OVERLAY_ICONS);
			
			fHistoryList= new ListDialogField<ICElement>(adapter, buttonLabels, labelProvider);
			fHistoryList.setLabelText(CHMessages.CHHistoryListAction_HistoryList_label); 
			fHistoryList.setElements(Arrays.asList(historyEntries));
			
			ISelection sel;
			if (historyEntries.length > 0) {
				sel= new StructuredSelection(historyEntries[0]);
			} else {
				sel= new StructuredSelection();
			}
			
			fHistoryList.selectElements(sel);
		}
			
		/*
		 * @see Dialog#createDialogArea(Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);
			
			Composite composite= (Composite) super.createDialogArea(parent);
			
			Composite inner= new Composite(composite, SWT.NONE);
			inner.setFont(parent.getFont());
			
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));

			LayoutUtil.doDefaultLayout(inner, new DialogField[] { fHistoryList }, true, 0, 0);
			LayoutUtil.setHeightHint(fHistoryList.getListControl(null), convertHeightInCharsToPixels(12));
			LayoutUtil.setHorizontalGrabbing(fHistoryList.getListControl(null), true);

			applyDialogFont(composite);		
			return composite;
		}

		/**
		 * Method doCustomButtonPressed.
		 */
		private void doCustomButtonPressed() {
			fHistoryList.removeElements(fHistoryList.getSelectedElements());
		}
		
		private void doDoubleClicked() {
			if (fHistoryStatus.isOK()) {
				okPressed();
			}
		}

		private void doSelectionChanged() {
			StatusInfo status= new StatusInfo();
			List<ICElement> selected= fHistoryList.getSelectedElements();
			if (selected.size() != 1) {
				status.setError(""); //$NON-NLS-1$
				fResult= null;
			} else {
				fResult= selected.get(0);
			}
			fHistoryList.enableButton(0, fHistoryList.getSize() > selected.size() && selected.size() != 0);			
			fHistoryStatus= status;
			updateStatus(status);	
		}
				
		public ICElement getResult() {
			return fResult;
		}
		
		public ICElement[] getRemaining() {
			List<ICElement> elems= fHistoryList.getElements();
			return elems.toArray(new ICElement[elems.size()]);
		}	
		
		/*
		 * @see org.eclipse.jface.window.Window#configureShell(Shell)
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
//			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, ...);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#create()
		 */
		@Override
		public void create() {
			setShellStyle(getShellStyle() | SWT.RESIZE);
			super.create();
		}

	}
	
	private CHViewPart fView;
	
	public CHHistoryListAction(CHViewPart view) {
		fView= view;
		setText(CHMessages.CHHistoryListAction_OpenHistory_label); 
	}
		
	/*
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		ICElement[] historyEntries= CallHierarchyUI.getHistoryEntries();
		HistoryListDialog dialog= new HistoryListDialog(fView.getSite().getShell(), historyEntries);
		if (dialog.open() == Window.OK) {
			CallHierarchyUI.setHistoryEntries(dialog.getRemaining());
			fView.setInput(dialog.getResult());
		}
	}
}

