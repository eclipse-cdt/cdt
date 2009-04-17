/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.checkers.sample;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;

public class QuickFixAssignmentInCondition implements IMarkerResolution {

       @Override
       public String getLabel() {
               return "Change first occurence '=' in the line to condition '=='";
       }

       @Override
       public void run(IMarker marker) {
               // See if there is an open editor on the file containing the marker
               IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
               if (w == null) {
                       return;
               }

               IWorkbenchPage page = w.getActivePage();
               if (page == null) {
                       return;
               }

               IFileEditorInput input = new FileEditorInput((IFile) marker.getResource());
               IEditorPart editorPart = page.findEditor(input);

               if (editorPart == null) {
                       // open an editor
                       try {
                               editorPart = IDE.openEditor(page, (IFile) marker.getResource(), true);
                       } catch (PartInitException e) {
                               e.printStackTrace();
                       }
               }
               if (editorPart == null) {
                       return;
               }

               if (editorPart instanceof ITextEditor) {
                       ITextEditor editor = (ITextEditor) editorPart;
                       IDocument doc = editor.getDocumentProvider().getDocument(
                                       editor.getEditorInput());

                       int line = marker.getAttribute(IMarker.LINE_NUMBER, -1)-1;

                       FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(doc);
                       try {
                               dad.find(doc.getLineOffset(line), "=", /*forwardSearch*/ true, /*caseSensitive*/ false,
                                               /*wholeWord*/ false, /*regExSearch*/ false);
                               dad.replace("==", /*regExReplace*/ false);
                               marker.delete();
                       } catch (BadLocationException e) {
                               // TODO: log the error
                               e.printStackTrace();
                       } catch (CoreException e) {
                               // TODO: log the error
                               e.printStackTrace();
                       }

               }
       }

}
