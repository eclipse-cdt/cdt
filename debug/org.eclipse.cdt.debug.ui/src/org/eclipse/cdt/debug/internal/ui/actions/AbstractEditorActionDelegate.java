/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

/**
 *
 * Enter type comment.
 *
 * @since Sep 19, 2002
 */
public abstract class AbstractEditorActionDelegate extends ActionDelegate
		implements IWorkbenchWindowActionDelegate, IEditorActionDelegate, IPartListener, INullSelectionListener {
	private IAction fAction;
	private IWorkbenchWindow fWorkbenchWindow;
	private IWorkbenchPart fTargetPart;
	private IDebugTarget fDebugTarget = null;

	/**
	 * Constructor for AbstractEditorActionDelegate.
	 */
	public AbstractEditorActionDelegate() {
	}

	@Override
	public void dispose() {
		IWorkbenchWindow win = getWorkbenchWindow();
		if (win != null) {
			win.getPartService().removePartListener(this);
			win.getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		setWorkbenchWindow(window);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			setTargetPart(page.getActivePart());
		}
		window.getPartService().addPartListener(this);
		window.getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		initializeDebugTarget();
		update();
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setAction(action);
		if (getWorkbenchWindow() == null) {
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			setWorkbenchWindow(window);
			if (window != null) {
				window.getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			}
		}
		setTargetPart(targetEditor);
		initializeDebugTarget();
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		setTargetPart(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == getTargetPart()) {
			setTargetPart(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	@Override
	public abstract void selectionChanged(IWorkbenchPart part, ISelection selection);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	@Override
	public abstract void run(IAction action);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		update();
	}

	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart part) {
		fTargetPart = part;
	}

	protected ISelection getTargetSelection() {
		IWorkbenchPart part = getTargetPart();
		if (part != null) {
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if (provider != null) {
				return provider.getSelection();
			}
		}
		return null;
	}

	protected void setDebugTarget(IDebugTarget target) {
		fDebugTarget = target;
	}

	protected IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
		fWorkbenchWindow = workbenchWindow;
	}

	protected void update() {
		IAction action = getAction();
		if (action != null) {
			action.setEnabled(getDebugTarget() != null && getTargetPart() != null);
		}
	}

	protected abstract void initializeDebugTarget();
}
