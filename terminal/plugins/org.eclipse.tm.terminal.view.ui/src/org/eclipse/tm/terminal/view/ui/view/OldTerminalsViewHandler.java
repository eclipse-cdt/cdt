/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.terminal.view.ui.interfaces.IUIConstants;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Old terminals view handler implementation.
 * <p>
 * If invoked, the view implementation opens the new terminals view and
 * closes itself afterwards.
 */
public class OldTerminalsViewHandler extends ViewPart {

	boolean fReplaced;
	IPartListener2 fPartlistener;

	/**
	 * Constructor.
	 */
	public OldTerminalsViewHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		replaceWithTerminalsView();
	}

	protected void replaceWithTerminalsView() {
		if (fReplaced)
			return;
	    IViewSite site = getViewSite();
		final IWorkbenchPage page = site.getPage();

		site.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (fReplaced)
					return;
				if (!page.isPageZoomed() || page.getActivePart() instanceof TerminalsView) {
					fReplaced = true;
					// Show the new view
					try {
						page.showView(IUIConstants.ID, null, IWorkbenchPage.VIEW_CREATE);
					}
					catch (PartInitException e) { /* ignored on purpose */ }

					// Hide ourself in the current perspective
					page.hideView(OldTerminalsViewHandler.this);
				} else if (fPartlistener == null) {
					final IWorkbenchPart maximizedPart = page.getActivePart();
					page.addPartListener(fPartlistener = new IPartListener2() {
						@Override
						public void partVisible(IWorkbenchPartReference partRef) {
							if (partRef.getPart(false) == OldTerminalsViewHandler.this) {
								page.removePartListener(this);
								fPartlistener = null;
								replaceWithTerminalsView();
							}
						}
						@Override
						public void partOpened(IWorkbenchPartReference partRef) {
						}
						@Override
						public void partInputChanged(IWorkbenchPartReference partRef) {
						}
						@Override
						public void partHidden(IWorkbenchPartReference partRef) {
						}
						@Override
						public void partDeactivated(IWorkbenchPartReference partRef) {
						}
						@Override
						public void partClosed(IWorkbenchPartReference partRef) {
							if (partRef.getPart(false) == OldTerminalsViewHandler.this) {
								page.removePartListener(this);
								fPartlistener = null;
							} else if (partRef.getPart(false) == maximizedPart) {
								page.removePartListener(this);
								fPartlistener = null;
								replaceWithTerminalsView();
							}
						}
						@Override
						public void partBroughtToTop(IWorkbenchPartReference partRef) {
						}
						@Override
						public void partActivated(IWorkbenchPartReference partRef) {
						}
					});
				}
			}
		});
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// should not happen, but just in case - replace on focus
		replaceWithTerminalsView();
	}

}
