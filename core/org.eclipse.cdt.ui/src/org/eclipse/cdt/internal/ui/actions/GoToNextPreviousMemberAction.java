/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	P.Tomaszewski
 *  Anton Leherbauer (Wind River Systems)
 *  Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Gives possibility to move fast between member elements of the c/c++ source.
 *
 * @author P.Tomaszewski
 */
public class GoToNextPreviousMemberAction extends TextEditorAction {

	public static final String NEXT_MEMBER = "GotoNextMember"; //$NON-NLS-1$
	public static final String PREVIOUS_MEMBER = "GotoPrevMember"; //$NON-NLS-1$

	/** Determines should action take user to the next member or to the previous one. */
	private boolean fGotoNext;

	/**
	 * Creates new action.
	 * @param bundle Resource bundle.
	 * @param prefix Prefix.
	 * @param editor Editor.
	 * @param gotoNext Is it go to next or previous action.
	 */
	public GoToNextPreviousMemberAction(ResourceBundle bundle, String prefix, ITextEditor editor, boolean gotoNext) {
		super(bundle, prefix, editor);

		fGotoNext = gotoNext;
	}

	/**
	 * Creates new action.
	 * @param bundle Resource bundle.
	 * @param prefix Prefix.
	 * @param editor Editor.
	 * @param style UI style.
	 * @param gotoNext Is it go to next or previous action.
	 */
	public GoToNextPreviousMemberAction(ResourceBundle bundle, String prefix, ITextEditor editor, int style,
			boolean gotoNext) {
		super(bundle, prefix, editor, style);

		fGotoNext = gotoNext;
	}

	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#update()
	 */
	@Override
	public void update() {
		final ITextEditor editor = getTextEditor();
		setEnabled(editor instanceof CEditor && ((CEditor) editor).getInputCElement() != null);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		final CEditor editor = (CEditor) getTextEditor();
		final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		final IEditorInput editorInput = editor.getEditorInput();
		final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (workingCopy == null) {
			return;
		}
		try {
			ISourceReference next = fGotoNext ? getNextElement(workingCopy, selection.getOffset())
					: getPrevElement(workingCopy, selection.getOffset());
			if (next != null) {
				editor.selectAndReveal(next.getSourceRange().getIdStartPos(), 0);
			}
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
	}

	private ISourceReference getNextElement(IParent parent, int offset) throws CModelException {
		ICElement[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			ICElement element = children[i];
			if (element instanceof ISourceReference) {
				final ISourceReference candidate1 = (ISourceReference) element;
				final ISourceRange range = candidate1.getSourceRange();
				final int idpos1 = range.getIdStartPos();
				if (element instanceof IParent && range.getStartPos() + range.getLength() > offset) {
					ISourceReference candidate2 = getNextElement((IParent) element, offset);
					if (candidate2 != null) {
						final int idpos2 = candidate2.getSourceRange().getIdStartPos();
						if (idpos1 <= offset || idpos1 > idpos2) {
							return candidate2;
						}
					}
				}
				if (idpos1 > offset) {
					return candidate1;
				}
			}
		}
		return null;
	}

	private ISourceReference getPrevElement(IParent parent, int offset) throws CModelException {
		ICElement[] children = parent.getChildren();
		for (int i = children.length - 1; i >= 0; i--) {
			ICElement element = children[i];
			if (element instanceof ISourceReference) {
				final ISourceReference candidate1 = (ISourceReference) element;
				final ISourceRange range = candidate1.getSourceRange();
				final int idpos1 = range.getIdStartPos();
				if (element instanceof IParent && range.getStartPos() < offset) {
					ISourceReference candidate2 = getPrevElement((IParent) element, offset);
					if (candidate2 != null) {
						final int idpos2 = candidate2.getSourceRange().getIdStartPos();
						if (idpos1 >= offset || idpos1 < idpos2) {
							return candidate2;
						}
					}
				}
				if (idpos1 < offset) {
					return candidate1;
				}
			}
		}
		return null;
	}
}
