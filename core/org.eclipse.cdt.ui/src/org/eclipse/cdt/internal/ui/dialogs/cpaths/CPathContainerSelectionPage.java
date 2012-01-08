/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;

/**
 * @deprecated as of CDT 4.0. This page was used for CPathContainerWizard
 * for 3.X style projects.
  */
@Deprecated
public class CPathContainerSelectionPage extends WizardPage {

	private static final String DIALOGSTORE_SECTION= "CPathContainerSelectionPage"; //$NON-NLS-1$
	private static final String DIALOGSTORE_CONTAINER_IDX= "index"; //$NON-NLS-1$


	private static class CPathContainerLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			return ((IContainerDescriptor) element).getName();
		}
		
		@Override
		public Image getImage(Object element) {
			return ((IContainerDescriptor) element).getImage();
		}
	}

	private static class CPathContainerSorter extends ViewerSorter {
		
		@Override
		public int category(Object element) {
			if ( element instanceof ProjectContainerDescriptor) {
				return 0;
			}
			return 1;
		}
	}

	private TableViewer fListViewer;
	private IContainerDescriptor[] fContainers;
	private IDialogSettings fDialogSettings;

	/**
	 * Constructor for ClasspathContainerWizardPage.
	 * @param containerPages
	 */
	protected CPathContainerSelectionPage(IContainerDescriptor[] containerPages) {
		super("CPathContainerWizardPage"); //$NON-NLS-1$
		setTitle(CPathEntryMessages.CPathContainerSelectionPage_title); 
		setDescription(CPathEntryMessages.CPathContainerSelectionPage_description); 
		setImageDescriptor(CPluginImages.DESC_WIZBAN_ADD_LIBRARY);

		fContainers= containerPages;

		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings();
		fDialogSettings= settings.getSection(DIALOGSTORE_SECTION);
		if (fDialogSettings == null) {
			fDialogSettings= settings.addNewSection(DIALOGSTORE_SECTION);
			fDialogSettings.put(DIALOGSTORE_CONTAINER_IDX, 0);
		}
		validatePage();
	}

	/* (non-Javadoc)
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		fListViewer= new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
		fListViewer.setLabelProvider(new CPathContainerLabelProvider());
		fListViewer.setContentProvider(new ListContentProvider());
		fListViewer.setSorter(new CPathContainerSorter());
		fListViewer.setInput(Arrays.asList(fContainers));
		fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
		});
		fListViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doDoubleClick();
			}
		});		
		
		int selectionIndex= fDialogSettings.getInt(DIALOGSTORE_CONTAINER_IDX);
		if (selectionIndex >= fContainers.length) {
			selectionIndex= 0;
		}
		fListViewer.getTable().select(selectionIndex);
		validatePage();
		setControl(fListViewer.getTable());
		Dialog.applyDialogFont(fListViewer.getTable());
	}

	/**
	 * Method validatePage.
	 */
	void validatePage() {
		setPageComplete(getSelected() != null);
	}


	public IContainerDescriptor getSelected() {
		if (fListViewer != null) {
			ISelection selection= fListViewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) 
					return (IContainerDescriptor) ss.getFirstElement();
			}
		}
		return null;
	}
	
	protected void doDoubleClick() {
		if (canFlipToNextPage()) {
			getContainer().showPage(getNextPage());
		}
	}	

	/* (non-Javadoc)
	 * @see IWizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete(); // avoid the getNextPage call to prevent potential plugin load
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (!visible && fListViewer != null) {
			fDialogSettings.put(DIALOGSTORE_CONTAINER_IDX, fListViewer.getTable().getSelectionIndex());
		}
		super.setVisible(visible);
	}

}
