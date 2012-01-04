/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This class serves as an adapter for actions contributed to the vertical ruler's
 * context menu. This adapter provides the contributed actions access to their disassembly part
 * and the disassembly part's vertical ruler. These actions gain only limited access to the vertical
 * ruler as defined by <code>IVerticalRulerInfo</code>.  The adapter updates the
 * adapter (inner) action on menu and mouse action on the vertical ruler.<p>
 * Extending classes must implement the factory method
 * <code>createAction(IDisassemblyPart, IVerticalRulerInfo)</code>.
 * 
 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate
 */
public abstract class AbstractDisassemblyRulerActionDelegate extends ActionDelegate implements IEditorActionDelegate, IViewActionDelegate, MouseListener, IMenuListener {

	/** The disassembly part. */
	private IDisassemblyPart fDisassemblyPart;
	/** The action calling the action delegate. */
	private IAction fCallerAction;
	/** The underlying action. */
	private IAction fAction;

	/**
	 * The factory method creating the underlying action.
	 *
	 * @param disassemblyPart  the disassembly part the action to be created will work on
	 * @param rulerInfo  the vertical ruler the action to be created will work on
	 * @return the created action
	 */
	protected abstract IAction createAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo);

	/*
	 * @see IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		setTargetPart(callerAction, targetEditor);
	}

    /*
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
	public void init(IViewPart view) {
    	setTargetPart(fCallerAction, view);
    }

	@Override
	public void init(IAction action) {
		fCallerAction= action;
	}

	private void setTargetPart(IAction callerAction, IWorkbenchPart targetPart) {
		if (fDisassemblyPart != null) {
			IVerticalRulerInfo rulerInfo= (IVerticalRulerInfo) fDisassemblyPart.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				Control control= rulerInfo.getControl();
				if (control != null && !control.isDisposed())
					control.removeMouseListener(this);
			}

			fDisassemblyPart.removeRulerContextMenuListener(this);
		}

		fDisassemblyPart= (IDisassemblyPart)(targetPart == null ? null : targetPart.getAdapter(IDisassemblyPart.class));
		fCallerAction= callerAction;
		fAction= null;

		if (fDisassemblyPart != null) {
				fDisassemblyPart.addRulerContextMenuListener(this);

			IVerticalRulerInfo rulerInfo= (IVerticalRulerInfo) fDisassemblyPart.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				fAction= createAction(fDisassemblyPart, rulerInfo);
				update();

				Control control= rulerInfo.getControl();
				if (control != null && !control.isDisposed())
					control.addMouseListener(this);
			}
		}
	}

	@Override
	public void run(IAction callerAction) {
		if (fAction != null)
			fAction.run();
	}
	
	@Override
	public void runWithEvent(IAction action, Event event) {
		if (fAction != null)
			fAction.runWithEvent(event);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		/*
		 * This is a ruler action - don't update on selection.
		 */
	}

	/**
	 * Updates to the current state.
	 */
	private void update() {
		if (fAction instanceof IUpdate) {
			((IUpdate) fAction).update();
			if (fCallerAction != null) {
				fCallerAction.setText(fAction.getText());
				fCallerAction.setEnabled(fAction.isEnabled());
			}
		}
	}

	/*
	 * @see IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void menuAboutToShow(IMenuManager manager) {
		update();
	}

	/*
	 * @see MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	/*
	 * @see MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		update();
	}

	/*
	 * @see MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {
	}
}
