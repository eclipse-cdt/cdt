/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alvaro Sanchez-Leon (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.io.IOException;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorActionBarContributor;

public class GDBBreakpointsDetailPane2 implements IDetailPane {

	static final String ID = "GDBBreakpointsDetailPane"; //$NON-NLS-1$
	static final String NAME = "Detail Pane For GDB Breakpoints"; //$NON-NLS-1$
	static final String DESCRIPTION = "Displays the details of the target breakpoint"; //$NON-NLS-1$
    private static final String PAGE_ID_COMMON = "org.eclipse.cdt.debug.ui.propertypages.breakpoint.common"; //$NON-NLS-1$ 
	
	private Composite fControlParent;
	private IWorkbenchPart fPart; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
	 */
	@Override
	public void init(IWorkbenchPartSite partSite) {
		fPart = partSite.getPart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		fControlParent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		fControlParent.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fControlParent.setLayoutData(gd);

		return fControlParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#dispose()
	 */
	@Override
	public void dispose() {
		fControlParent.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void display(IStructuredSelection selection) {
		ICBreakpoint breakpoint = (ICBreakpoint) selection.getFirstElement();
		
		IPreferenceNode[] nodes = PreferencesUtil.propertiesContributorsFor(breakpoint);

		IPreferenceNode bpCommonPage = null;
		if (nodes != null) {
			for (int i = 0; i < nodes.length; i++) {
				if (PAGE_ID_COMMON.equals(nodes[i].getId())) {
					// we found the common page
					bpCommonPage = nodes[i];
					break;
				}
			}
		}

		//remove previous content as it may no longer be applicable
		Control[] children = fControlParent.getChildren();
		if (children != null && children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				children[i].dispose();
			}
			fControlParent.pack();
			fControlParent.redraw();
		}
		
		//populate with corresponding break point common page information
		if (bpCommonPage != null) {
			assert(bpCommonPage instanceof IWorkbenchPropertyPage);
			bpCommonPage.createPage();
			CBreakpointContext bpContext = new CBreakpointContext(breakpoint, DebugUITools.getDebugContextForPart(fPart));
			
			final IPreferencePage preferencePage = bpCommonPage.getPage();
			((IWorkbenchPropertyPage)preferencePage).setElement(bpContext); 
			preferencePage.createControl(fControlParent);
			
			//Add a Save button to allow editing from the the Detail Pane
			final Button button = new Button(fControlParent, SWT.NONE);
			button.setText("Save"); //$NON-NLS-1$
			button.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					IStatusLineManager statusLine = getStatusLine();
					if (statusLine != null) {
						statusLine.setErrorMessage(null);
					}
					
					if(preferencePage.isValid()) {
						preferencePage.performOk();
						handleSave(preferencePage);
					} else {
						//focus on first invalid editor widget
						preferencePage.setVisible(true);
		
						String errorMessage = preferencePage.getErrorMessage();
						if (errorMessage != null) {
							Display.getCurrent().beep();
							System.out.println(errorMessage);
							if(statusLine != null) {
								statusLine.setErrorMessage(errorMessage);								
							}
						}
					}
					
					button.setSelection(false);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
				
			});
			
			fControlParent.pack();
			fControlParent.redraw();
		}

	}

	protected void handleSave(IPreferencePage page) {
			if (page instanceof PreferencePage) {
				// Save now in case tHe workbench does not shutdown cleanly
				IPreferenceStore store = ((PreferencePage) page).getPreferenceStore();
				if (store != null && store.needsSaving()
						&& store instanceof IPersistentPreferenceStore) {
					try {
						((IPersistentPreferenceStore) store).save();
					} catch (IOException e) {
						String message =JFaceResources.format(
                                "PreferenceDialog.saveErrorMessage", new Object[] { page.getTitle(), e.getMessage() }); //$NON-NLS-1$
			            Policy.getStatusHandler().show(
			                    new Status(IStatus.ERROR, Policy.JFACE, message, e),
			                    JFaceResources.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$
										
					}
				}
			}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints.IGDBBreakpointsDetailPane#setFocus()
	 */
	@Override
	public boolean setFocus() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints.IGDBBreakpointsDetailPane#getID()
	 */
	@Override
	public String getID() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints.IGDBBreakpointsDetailPane#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints.IGDBBreakpointsDetailPane#getDescription()
	 */
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go through the active part
		if (fPart.getSite() instanceof IViewSite) {
			IViewSite site= (IViewSite) fPart.getSite();
			IWorkbenchPage page= site.getPage();
			IWorkbenchPart activePart= page.getActivePart();

			if (activePart instanceof IViewPart) {
				IViewPart activeViewPart= (IViewPart)activePart;
				IViewSite activeViewSite= activeViewPart.getViewSite();
				return activeViewSite.getActionBars().getStatusLineManager();
			}
	
			if (activePart instanceof IEditorPart) {
				IEditorPart activeEditorPart= (IEditorPart)activePart;
				IEditorActionBarContributor contributor= activeEditorPart.getEditorSite().getActionBarContributor();
				if (contributor instanceof EditorActionBarContributor)
					return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
			}
			// no active part
			return site.getActionBars().getStatusLineManager();
		}
		return null;
	}
	
}
