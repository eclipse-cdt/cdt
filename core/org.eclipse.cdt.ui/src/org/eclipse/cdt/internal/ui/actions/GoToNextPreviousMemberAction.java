/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	P.Tomaszewski
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
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
    public GoToNextPreviousMemberAction(ResourceBundle bundle, String prefix, ITextEditor editor, int style, boolean gotoNext) {
        super(bundle, prefix, editor, style);

        fGotoNext = gotoNext;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        final CEditor editor = (CEditor) getTextEditor();
        final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        final IEditorInput editorInput = editor.getEditorInput();
        final WorkingCopy workingCopy =  (WorkingCopy) CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
        try {
            final ICElement[] elements =  workingCopy.getChildren();
            final Integer[] elementOffsets = createSourceIndexes(elements);
            final ICElement selectedElement = workingCopy.getElementAtOffset(selection.getOffset());
            if (selectedElement != null && selectedElement instanceof ISourceReference) {
                final int offset = ((ISourceReference) selectedElement).getSourceRange().getStartPos(); 
                final int offsetToSelect = fGotoNext ? getNextOffset(elementOffsets, offset) : getPreviousOffset(elementOffsets, offset);
                editor.selectAndReveal(offsetToSelect, 0);
            } else if (selectedElement == null) {
                final int offset = selection.getOffset(); 
                final int offsetToSelect = fGotoNext ? getNextOffset(elementOffsets, offset) : getPreviousOffset(elementOffsets, offset);
                editor.selectAndReveal(offsetToSelect, 0);
            } else {
               //System.out.println("Selected element class:" + selectedElement.getClass()); //$NON-NLS-1$
            }
        } catch (CModelException e) {
        	CUIPlugin.getDefault().log(e);
            //System.out.println("Exception:" + e.getMessage()); //$NON-NLS-1$
        }
    }

    /**
     * Searches for next offset within array of offsets.
     * @param offsets Offsets to search.
     * @param actualOffset Actual offsets.
     * @return Found offset or actual.
     */
    private static int getNextOffset(Integer[] offsets, int actualOffset) {
    	if (offsets.length > 0) {
        	if (actualOffset < offsets[0].intValue())
        	{
        		return offsets[0].intValue();
        	}
        }
        for (int i = 0; i < offsets.length - 1; i++) {
            if (offsets[i].intValue() == actualOffset) {
                return offsets[i + 1].intValue();
            } else if ((actualOffset > offsets[i].intValue()) 
                            && (actualOffset < offsets[i + 1].intValue())) {
                return offsets[i + 1].intValue();
            }
        }
        return actualOffset;
    }

    /**
     * Searches for previous offset within array of offsets.
     * @param offsets Offsets to search.
     * @param actualOffset Actual offset.
     * @return Found offset or actual.
     */
    private static int getPreviousOffset(Integer[] offsets, int actualOffset) {
    	if (offsets.length > 0) {
    		if (actualOffset > offsets[offsets.length - 1].intValue())
    		{
    			return offsets[offsets.length - 1].intValue();
    		}
    	}
        for (int i = 1; i < offsets.length; i++) {
            if (offsets[i].intValue() == actualOffset) {
                return offsets[i - 1].intValue();
            } else if ((actualOffset > offsets[i - 1].intValue())
                            && (actualOffset < offsets[i].intValue())) {
                return offsets[i - 1].intValue();                
            }
        }
        return actualOffset;
    }

    /**
     * Creates array in indexes from ICElements.
     * @param elements Elements to retrieve needed data.
     * @return indexes.
     * @throws CModelException Thrown if source range not found.
     */
    private static Integer[] createSourceIndexes(ICElement[] elements) throws CModelException
    {
        final List indexesList = new LinkedList();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof ISourceReference) {
                indexesList.add(new Integer(((ISourceReference) elements[i]).getSourceRange().getStartPos()));
            }
        }
        //System.out.println("Indexes list:" + indexesList); //$NON-NLS-1$
        final Integer[] indexes = new Integer[indexesList.size()];
        indexesList.toArray(indexes);
        return indexes;
    }
}
