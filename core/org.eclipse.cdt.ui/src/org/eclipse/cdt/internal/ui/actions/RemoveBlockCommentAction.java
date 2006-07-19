/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * Action that removes the enclosing comment marks from a Java block comment.
 * 
 * @since 3.0
 */
public class RemoveBlockCommentAction extends BlockCommentAction {

	/**
	 * Creates a new instance.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public RemoveBlockCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction#runInternal(org.eclipse.jface.text.ITextSelection, org.eclipse.jface.text.IDocumentExtension3, org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction.Edit.EditFactory)
	 */
	protected void runInternal(ITextSelection selection, IDocumentExtension3 docExtension, Edit.EditFactory factory) throws BadPartitioningException, BadLocationException {
		List edits= new LinkedList();
		int tokenLength= getCommentStart().length();
		
		int offset= selection.getOffset();
		int endOffset= offset + selection.getLength();

		ITypedRegion partition= docExtension.getPartition(IDocumentExtension3.DEFAULT_PARTITIONING, offset, false);
		int partOffset= partition.getOffset();
		int partEndOffset= partOffset + partition.getLength();
		
		while (partEndOffset < endOffset) {
			
			if (partition.getType() == ICPartitions.C_MULTI_LINE_COMMENT) {
				edits.add(factory.createEdit(partOffset, tokenLength, "")); //$NON-NLS-1$
				edits.add(factory.createEdit(partEndOffset - tokenLength, tokenLength, "")); //$NON-NLS-1$
			}
			
			partition= docExtension.getPartition(IDocumentExtension3.DEFAULT_PARTITIONING, partEndOffset, false);
			partOffset= partition.getOffset();
			partEndOffset= partOffset + partition.getLength();
		}

		if (partition.getType() == ICPartitions.C_MULTI_LINE_COMMENT) {
			edits.add(factory.createEdit(partOffset, tokenLength, "")); //$NON-NLS-1$
			edits.add(factory.createEdit(partEndOffset - tokenLength, tokenLength, "")); //$NON-NLS-1$
		}

		executeEdits(edits);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction#validSelection(org.eclipse.jface.text.ITextSelection)
	 */
	protected boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty() && selection.getLength() > 0;
	}


}
