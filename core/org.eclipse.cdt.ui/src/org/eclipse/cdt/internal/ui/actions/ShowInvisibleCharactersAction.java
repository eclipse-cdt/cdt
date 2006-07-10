/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.cdt.internal.ui.InvisibleCharacterPainter;

/**
 * This action toggles the visibility of whitespace characters by
 * attaching/detaching an {@link InvisibleCharacterPainter} to the
 * active (text-)editor.
 * 
 * @author anton.leherbauer@windriver.com
 */
public class ShowInvisibleCharactersAction implements IEditorActionDelegate, IWorkbenchWindowActionDelegate, IUpdate {

	/**
	 * The PartListener to act on changes of the active editor.
	 */
	private class PartListener implements IPartListener2 {

		/*
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) instanceof IEditorPart) {
				updateActiveEditor();
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) instanceof IEditorPart) {
				updateActiveEditor();
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partOpened(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) instanceof IEditorPart) {
				updateActiveEditor();
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) instanceof IEditorPart) {
				updateActiveEditor();
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

	}

	private IPainter fInvisibleCharPainter;
	private IWorkbenchWindow fWindow;
	private IPartListener2 fPartListener;
	private ITextEditor fTextEditor;
	private boolean fIsChecked;
	private IAction fAction;

	public ShowInvisibleCharactersAction() {
	}

	/**
	 * Add the painter to the current editor.
	 */
	private void addPainter() {
		ITextEditor editor= getTextEditor();
		ITextViewer viewer= getTextViewer(editor);
		if (viewer instanceof ITextViewerExtension2) {
			ITextViewerExtension2 viewerExt2= (ITextViewerExtension2)viewer;
			if (fInvisibleCharPainter == null) {
				fInvisibleCharPainter= new InvisibleCharacterPainter(viewer);
			}
			viewerExt2.addPainter(fInvisibleCharPainter);
		}
	}

	/**
	 * Get the ITextViewer from an ITextEditor by adapting
	 * it to a ITextOperationTarget.
	 * @param editor  the ITextEditor
	 * @return  the text viewer or <code>null</code>
	 */
	private ITextViewer getTextViewer(ITextEditor editor) {
		Object target= editor.getAdapter(ITextOperationTarget.class);
		if (target instanceof ITextViewer) {
			return (ITextViewer)target;
		}
		return null;
	}

	/**
	 * Remove the painter from the current editor.
	 */
	private void removePainter() {
		ITextEditor editor= getTextEditor();
		ITextViewer viewer= getTextViewer(editor);
		if (viewer instanceof ITextViewerExtension2) {
			ITextViewerExtension2 viewerExt2= (ITextViewerExtension2)viewer;
			viewerExt2.removePainter(fInvisibleCharPainter);
		}
	}

	private void setEditor(ITextEditor editor) {
		if (editor != null && editor == getTextEditor()) {
			return;
		}
		if (fInvisibleCharPainter != null) {
			removePainter();
			fInvisibleCharPainter.deactivate(false);
			fInvisibleCharPainter.dispose();
			fInvisibleCharPainter= null;
		}
		fTextEditor= editor;
		update();
		if (fTextEditor != null && fIsChecked) {
			addPainter();
		}
	}

	private ITextEditor getTextEditor() {
		return fTextEditor;
	}

	/*
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fAction= action;
		if (targetEditor instanceof ITextEditor) {
			setEditor((ITextEditor)targetEditor);
		} else {
			setEditor(null);
		}
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
		fPartListener= new PartListener();
		fWindow.getActivePage().addPartListener(fPartListener);
		updateActiveEditor();
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		if (fWindow != null) {
			IWorkbenchPage activePage= fWindow.getActivePage();
			if (activePage != null) {
				activePage.removePartListener(fPartListener);
			}
			fPartListener= null;
			fWindow= null;
		}
		fAction= null;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		fAction= action;
		fIsChecked= action.isChecked();
		if (fIsChecked) {
			addPainter();
		} else if (fInvisibleCharPainter != null) {
			removePainter();
			fInvisibleCharPainter.deactivate(true);
		}
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction= action;
		fIsChecked= action.isChecked();
		update();
	}

	/**
	 * Update the active editor.
	 */
	protected void updateActiveEditor() {
		IWorkbenchPage page= fWindow.getActivePage();
		if (page != null) {
			IEditorPart editorPart= page.getActiveEditor();
			if (editorPart instanceof ITextEditor) {
				setEditor((ITextEditor)editorPart);
			} else {
				setEditor(null);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (fAction != null) {
			fAction.setEnabled(getTextEditor() != null);
		}
	}

}
