/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.text.MakefileColorManager;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileWordDetector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class MakefileEditor extends TextEditor implements ISelectionChangedListener{

	/**
	 * The page that shows the outline.
	 */
	protected MakefileContentOutlinePage page;
	private FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;

	public MakefileContentOutlinePage getOutlinePage() {
		if (page == null) {
			page = new MakefileContentOutlinePage(this);
			page.addSelectionChangedListener(this);
			page.setInput(getEditorInput());
		}
		return page;
	}

	public MakefileEditor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	protected void initializeEditor() {

		setSourceViewerConfiguration(new MakefileSourceConfiguration(new MakefileColorManager(), this));
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#MakefileEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#MakefileRulerContext"); //$NON-NLS-1$
		setDocumentProvider(MakeUIPlugin.getDefault().getMakefileDocumentProvider());
		setPreferenceStore(MakeUIPlugin.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * Method declared on IAdaptable
	 */
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getOutlinePage();
		}
		return super.getAdapter(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null) {
			page.update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		ResourceBundle bundle = MakeUIPlugin.getDefault().getResourceBundle();

		IAction a = new TextOperationAction(bundle, "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$

		a = new TextOperationAction(bundle, "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a); //$NON-NLS-1$

		a = new TextOperationAction(bundle, "Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.COMMENT);
		setAction("Comment", a); //$NON-NLS-1$
		markAsStateDependentAction("Comment", true); //$NON-NLS-1$
 
		a = new TextOperationAction(bundle, "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.UNCOMMENT);
		setAction("Uncomment", a); //$NON-NLS-1$
		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$

		a = new OpenDeclarationAction(this);
		a.setActionDefinitionId(IMakefileEditorActionDefinitionIds.OPEN_DECLARATION);
		setAction("OpenDeclarationAction", a); //$NON-NLS-1$
		markAsStateDependentAction("OpenDeclarationAction", true); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			resetHighlightRange();
		} else if (selection instanceof IStructuredSelection){                                                                                                                         
			if (!isActivePart() && MakeUIPlugin.getActivePage() != null) {
				MakeUIPlugin.getActivePage().bringToTop(this);
			}                                                                                                                 
			Object element =  ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IDirective) {
				IDirective statement = (IDirective)element;
				setSelection(statement, !isActivePart());
			}
		}
	}

	/**
	 * Returns whether the editor is active.
	 */
	private boolean isActivePart() {
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		IPartService service= window.getPartService();
		IWorkbenchPart part= service.getActivePart();
		return part != null && part.equals(this);
	}

	/**
	 * Returns the find/replace document adapter.
	 * 
	 * @return the find/replace document adapter.
	 */
	private FindReplaceDocumentAdapter getFindRepalceDocumentAdapter() {
		if (fFindReplaceDocumentAdapter == null) {
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(doc);
		}
		return fFindReplaceDocumentAdapter;
	}

	public void setSelection(IDirective directive, boolean moveCursor) {
		int startLine = directive.getStartLine() - 1;
		int endLine = directive.getEndLine() - 1;
		try {
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			int start = doc.getLineOffset(startLine);
			int len = doc.getLineLength(endLine) - 1;
			int length = (doc.getLineOffset(endLine) + len) - start;
			setHighlightRange(start, length, true);
			if (moveCursor) {
				// Let see if we can move the cursor at the position also
				String var = directive.toString().trim();
				IWordDetector detector = new MakefileWordDetector();
				for (len = 0; len < var.length(); len++) {
					char c = var.charAt(len);
					//if (! (Character.isLetterOrDigit(c) || c == '.' || c == '_')) {
					if (!(detector.isWordPart(c) || detector.isWordStart(c) || c == '-' || c == '_')) {
						break;
					}
				}
				if (len > 0) {
					var = var.substring(0, len);
				}
				IRegion region = getFindRepalceDocumentAdapter().find(start, var, true, true, true, false);

				if (region != null) {
					len = region.getOffset();
					length = region.getLength();
					getSourceViewer().revealRange(len, length);
					// Selected region begins one index after offset
					getSourceViewer().setSelectedRange(len, length);
				}

			}
		} catch (IllegalArgumentException x) {
			resetHighlightRange();
		} catch (BadLocationException e) {
			resetHighlightRange();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Comment"); //$NON-NLS-1$
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Uncomment"); //$NON-NLS-1$
		//addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "OpenDeclarationAction"); //$NON-NLS-1$
	}

}
