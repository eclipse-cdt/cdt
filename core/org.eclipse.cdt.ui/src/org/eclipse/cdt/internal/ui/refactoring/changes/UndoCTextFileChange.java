/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.changes;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.DocumentAdapter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.UndoTextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

/**
 * UndoCTextFileChange that uses a working copy in order to generate CModel events.
 * @author janees
 */
public class UndoCTextFileChange extends UndoTextFileChange {

	UndoEdit mUndoEdit = null;

	public UndoCTextFileChange(String name, IFile file, UndoEdit undo, ContentStamp stamp, int saveMode) {
		super(name, file, undo, stamp, saveMode);
		mUndoEdit = undo;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (pm == null) {
			pm = new NullProgressMonitor();
		}
		Object obj = getModifiedElement();
		if (!(obj instanceof IFile)) {
			throw new IllegalArgumentException();
		}
		final IFile file = (IFile) obj;
		ICElement element = CoreModel.getDefault().create(file);
		if (!(element instanceof TranslationUnit)) {
			return super.perform(pm);
		}

		final TranslationUnit tu = (TranslationUnit) element;
		IWorkingCopy wc = tu.getWorkingCopy(pm, DocumentAdapter.FACTORY);
		final IBuffer buffer = wc.getBuffer();
		assert buffer instanceof DocumentAdapter;
		if (buffer instanceof DocumentAdapter) {
			IDocument document = ((DocumentAdapter) buffer).getDocument();
			try {
				UndoEdit redo = mUndoEdit.apply(document, TextEdit.CREATE_UNDO);
				wc.commit(false, pm);
				return new UndoCTextFileChange(getName(), file, redo, null, getSaveMode());
			} catch (MalformedTreeException e) {
				CUIPlugin.log(e);
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			} finally {
				wc.destroy();
			}
		}
		return null;
	}
}
