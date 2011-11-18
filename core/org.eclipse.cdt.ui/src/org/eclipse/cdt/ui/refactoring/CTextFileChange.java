/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.core.model.TranslationUnit;

import org.eclipse.cdt.internal.ui.refactoring.DocumentAdapter;
import org.eclipse.cdt.internal.ui.refactoring.UndoCTextFileChange;

/**
 * A TextFileChange that uses a working copy in order to generate CModel events.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CTextFileChange extends TextFileChange {
    // "c2" is the extension which the CContentViewerCreator is registered
    // with the extension point "org.eclipse.compare.contentMergeViewers"
    private static final String TEXT_TYPE = "c2"; //$NON-NLS-1$
	private ITranslationUnit fTranslationUnit;
    private IWorkingCopy fWorkingCopy;
    private int fAquireCount;
    
    public CTextFileChange(String name, IFile file) {
        super(name, file);
        ICElement element = CoreModel.getDefault().create(file);
        if (element instanceof ITranslationUnit) {
            fTranslationUnit = (ITranslationUnit) element;
            setTextType(TEXT_TYPE);
        }
    }
    
    /**
	 * @since 5.1
	 */
    public CTextFileChange(String name, ITranslationUnit tu) {
        super(name, getFile(tu));
        fTranslationUnit = tu;
        setTextType(TEXT_TYPE);
    }

    @Override
	protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
        IDocument doc= super.acquireDocument(pm);
        if (++fAquireCount == 1) {
            if (fTranslationUnit instanceof TranslationUnit && fWorkingCopy == null) {
                fWorkingCopy= ((TranslationUnit) fTranslationUnit).getWorkingCopy(null, DocumentAdapter.FACTORY);
                if (!fTranslationUnit.isOpen()) {
                    fTranslationUnit.open(null);
                }
            }
        }
        return doc;
    }
       
    @Override
	protected void commit(final IDocument document, final IProgressMonitor pm) throws CoreException {
        if (fWorkingCopy == null) {
        	super.commit(document, pm);
        } else if (needsSaving()) {
        	fWorkingCopy.commit(false, pm);
        }
    }
    
    @Override
	protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
        super.releaseDocument(document, pm);
        if (--fAquireCount == 0) {
            if (fWorkingCopy != null) {
                fWorkingCopy.destroy();
                fWorkingCopy= null;
            }
        }
    }

    @Override
	protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) {
        return new UndoCTextFileChange(getName(), getFile(), edit, stampToRestore, getSaveMode());
    }

	private static IFile getFile(ITranslationUnit tu) {
		return (IFile) tu.getResource();
	}
}
