/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;

public class IBHistoryListAction extends Action {
	
	private class HistoryListDialog extends StatusDialog {
		private ListDialogField<ITranslationUnit> fHistoryList;
		private IStatus fHistoryStatus;
		private ITranslationUnit fResult;
		
		private HistoryListDialog(Shell shell, ITranslationUnit[] elements) {
			super(shell);
			setHelpAvailable(false);			
			setTitle(IBMessages.IBHistoryListAction_HistoryDialog_title); 
			String[] buttonLabels= new String[] { 
				IBMessages.IBHistoryListAction_Remove_label, 
			};
					
			IListAdapter<ITranslationUnit> adapter= new IListAdapter<ITranslationUnit>() {
				@Override
				public void customButtonPressed(ListDialogField<ITranslationUnit> field, int index) {
					doCustomButtonPressed();
				}
				@Override
				public void selectionChanged(ListDialogField<ITranslationUnit> field) {
					doSelectionChanged();
				}
				
				@Override
				public void doubleClicked(ListDialogField<ITranslationUnit> field) {
					doDoubleClicked();
				}				
			};
		
			ILabelProvider labelProvider= new CUILabelProvider(CElementLabels.APPEND_ROOT_PATH, CElementImageProvider.OVERLAY_ICONS);
			
			fHistoryList= new ListDialogField<ITranslationUnit>(adapter, buttonLabels, labelProvider);
			fHistoryList.setLabelText(IBMessages.IBHistoryListAction_HistoryList_label); 
			fHistoryList.setElements(Arrays.asList(elements));
			
			ISelection sel;
			if (elements.length > 0) {
				sel= new StructuredSelection(elements[0]);
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
			List<ITranslationUnit> selected= fHistoryList.getSelectedElements();
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
				
		public ITranslationUnit getResult() {
			return fResult;
		}
		
		public ITranslationUnit[] getRemaining() {
			List<ITranslationUnit> elems= fHistoryList.getElements();
			return elems.toArray(new ITranslationUnit[elems.size()]);
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
	
	private IBViewPart fView;
	
	public IBHistoryListAction(IBViewPart view) {
		fView= view;
		setText(IBMessages.IBHistoryListAction_label); 
	}
		
	/*
	 * @see IAction#run()
	 */
	@Override
	public void run() {
	    ITranslationUnit[] historyEntries= fView.getHistoryEntries();
		HistoryListDialog dialog= new HistoryListDialog(fView.getSite().getShell(), historyEntries);
		if (dialog.open() == Window.OK) {
			fView.setHistoryEntries(dialog.getRemaining());
			fView.setInput(dialog.getResult());
		}
	}
}

