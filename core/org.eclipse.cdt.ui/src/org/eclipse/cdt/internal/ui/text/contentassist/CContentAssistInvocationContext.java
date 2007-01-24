/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;


/**
 * Describes the context of a content assist invocation in a C/C++ editor.
 * <p>
 * Clients may use but not subclass this class.
 * </p>
 * 
 * @since 4.0
 */
public class CContentAssistInvocationContext extends ContentAssistInvocationContext {
	
	private final IEditorPart fEditor;
	private ITranslationUnit fTU= null;
	private boolean fTUComputed= false;

	/**
	 * Creates a new context.
	 * 
	 * @param viewer the viewer used by the editor
	 * @param offset the invocation offset
	 * @param editor the editor that content assist is invoked in
	 */
	public CContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor) {
		super(viewer, offset);
		Assert.isNotNull(editor);
		fEditor= editor;
	}
	
	/**
	 * Creates a new context.
	 * 
	 * @param unit the translation unit in <code>document</code>
	 */
	public CContentAssistInvocationContext(ITranslationUnit unit) {
		super();
		fTU= unit;
		fTUComputed= true;
		fEditor= null;
	}
	
	/**
	 * Returns the translation unit that content assist is invoked in, <code>null</code> if there
	 * is none.
	 * 
	 * @return the translation unit that content assist is invoked in, possibly <code>null</code>
	 */
	public ITranslationUnit getTranslationUnit() {
		if (!fTUComputed) {
			fTUComputed= true;
			fTU= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		}
		return fTU;
	}
	
	/**
	 * Returns the project of the translation unit that content assist is invoked in,
	 * <code>null</code> if none.
	 * 
	 * @return the current C project, possibly <code>null</code>
	 */
	public ICProject getProject() {
		ITranslationUnit unit= getTranslationUnit();
		return unit == null ? null : unit.getCProject();
	}
	
	/**
	 * Get the editor content assist is invoked in.
	 * 
	 * @return the editor, may be <code>null</code>
	 */
	public IEditorPart getEditor() {
		return fEditor;
	}
}
