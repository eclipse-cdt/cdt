/*******************************************************************************
 *  Copyright (c) 2005, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Wind River Systems
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;


import java.util.Iterator;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.AsmSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;

import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.editor.AbstractCModelOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CAnnotationIterator;
import org.eclipse.cdt.internal.ui.editor.ICAnnotation;


/**
 * Assembly text editor.
 */
public class AsmTextEditor extends TextEditor implements ISelectionChangedListener {	

	/**
	 * Updates the outline page selection and this editor's range indicator.
	 */
	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {
		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			AsmTextEditor.this.selectionChanged();
		}
	}
	
	private AbstractCModelOutlinePage fOutlinePage;
	private EditorSelectionChangedListener fEditorSelectionChangedListener;

	/**
	 * Creates a new assembly text editor.
	 */
	public AsmTextEditor() {
		super();
	}
	
	/**
	 * Initializes this editor.
	 */
	@Override
	protected void initializeEditor() {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		// FIXME: Should this editor have a different preference store ?
		// For now we are sharing with the CEditor and any changes in the
		// setting of the CEditor will be reflected in this editor.
		setPreferenceStore(store);
		final IColorManager colorManager = CDTUITools.getColorManager();
		setSourceViewerConfiguration(new AsmSourceViewerConfiguration(colorManager, store, this, ICPartitions.C_PARTITIONING));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
		setEditorContextMenuId("#ASMEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ASMEditorRulerContext"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#collectContextMenuPreferencePages()
	 */
	@Override
	protected String[] collectContextMenuPreferencePages() {
		// Add Assembly Editor relevant pages
		String[] parentPrefPageIds = super.collectContextMenuPreferencePages();
		String[] prefPageIds = new String[parentPrefPageIds.length + 1];
		int nIds = 0;
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeColoringPreferencePage"; //$NON-NLS-1$
		System.arraycopy(parentPrefPageIds, 0, prefPageIds, nIds, parentPrefPageIds.length);
		return prefPageIds;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			return getOutlinePage();
		}
		return super.getAdapter(adapter);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICHelpContextIds.ASMEDITOR_VIEW);

		fEditorSelectionChangedListener = new EditorSelectionChangedListener();
		fEditorSelectionChangedListener.install(getSelectionProvider());
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#dispose()
	 */
	@Override
	public void dispose() {
		if (fOutlinePage != null) {
			fOutlinePage.dispose();
			fOutlinePage = null;
		}
		if (fEditorSelectionChangedListener != null)  {
			fEditorSelectionChangedListener.uninstall(getSelectionProvider());
			fEditorSelectionChangedListener = null;
		}
		super.dispose();
	}

	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 * Pulled in from 2.0
	 */
	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof AsmSourceViewerConfiguration) {
			return ((AsmSourceViewerConfiguration)configuration).affectsTextPresentation(event);
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof AsmSourceViewerConfiguration) {
			((AsmSourceViewerConfiguration)configuration).handlePropertyChangeEvent(event);
		}
		super.handlePreferenceStoreChanged(event);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		// marker for contributions to the top
		menu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
		// separator for debug related actions (similar to ruler context menu)
		menu.add(new Separator(IContextMenuConstants.GROUP_DEBUG));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_DEBUG+".end")); //$NON-NLS-1$

		super.editorContextMenuAboutToShow(menu);
	}

	/**
	 * Gets the outline page for this editor.
     * @return Outline page.
	 */
	public AbstractCModelOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage = new AsmContentOutlinePage(this);
			fOutlinePage.addSelectionChangedListener(this);
		}
		setOutlinePageInput(fOutlinePage, getEditorInput());
		return fOutlinePage;
	}

	/**
     * Sets an input for the outline page.
	 * @param page Page to set the input.
	 * @param input Input to set.
	 */
	public static void setOutlinePageInput(AbstractCModelOutlinePage page, IEditorInput input) {
		if (page != null) {
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			page.setInput(manager.getWorkingCopy(input));
		}
	}

	/**
	 * React to changed selection in the editor.
	 */
	protected void selectionChanged() {
		if (getSelectionProvider() == null)
			return;
		ISourceReference element= computeHighlightRangeSourceReference();
		updateStatusLine();
		synchronizeOutlinePage();
		setSelection(element, false);
	}

	/**
	 * Synchronizes the outline view selection with the given element
	 * position in the editor.
	 */
	protected void synchronizeOutlinePage() {
		if(fOutlinePage != null && fOutlinePage.isLinkingEnabled()) {
			fOutlinePage.removeSelectionChangedListener(this);
			fOutlinePage.synchronizeSelectionWithEditor();
			fOutlinePage.addSelectionChangedListener(this);
		}
	}	
	
	protected void updateStatusLine() {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
		Annotation annotation = getAnnotation(selection.getOffset(), selection.getLength());
		setStatusLineErrorMessage(null);
		setStatusLineMessage(null);
		if (annotation != null) {
			updateMarkerViews(annotation);
			if (annotation instanceof ICAnnotation && ((ICAnnotation) annotation).isProblem())
				setStatusLineMessage(annotation.getText());
		}
	}

	/**
	 * Returns the annotation overlapping with the given range or <code>null</code>.
	 * 
	 * @param offset the region offset
	 * @param length the region length
	 * @return the found annotation or <code>null</code>
	 */
	private Annotation getAnnotation(int offset, int length) {
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		if (model == null)
			return null;
		
		@SuppressWarnings("rawtypes")
		Iterator parent;
		if (model instanceof IAnnotationModelExtension2) {
			parent= ((IAnnotationModelExtension2)model).getAnnotationIterator(offset, length, true, true);
		} else {
			parent= model.getAnnotationIterator();
		}

		@SuppressWarnings("unchecked")
		Iterator<Annotation> e= new CAnnotationIterator(parent, false);
		while (e.hasNext()) {
			Annotation a = e.next();
			if (!isNavigationTarget(a))
				continue;
				
			Position p = model.getPosition(a);
			if (p != null && p.overlapsWith(offset, length))
				return a;
		}
		
		return null;
	}

	/**
	 * Get the StatusLineManager.
	 */
	@Override
	protected IStatusLineManager getStatusLineManager() {
		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		return null;
	}
	

	/**
	 * Computes and returns the source reference that includes the caret and
	 * serves as provider for the outline page selection and the editor range
	 * indication.
	 *
	 * @return the computed source reference
	 */
	protected ISourceReference computeHighlightRangeSourceReference() {
		ISourceViewer sourceViewer= getSourceViewer();
		if (sourceViewer == null)
			return null;

		StyledText styledText= sourceViewer.getTextWidget();
		if (styledText == null)
			return null;

		int caret= 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)sourceViewer;
			caret= extension.widgetOffset2ModelOffset(styledText.getSelection().x);
		} else {
			int offset= sourceViewer.getVisibleRegion().getOffset();
			caret= offset + styledText.getSelection().x;
		}

		ICElement element= getElementAt(caret, false);

		if ( !(element instanceof ISourceReference))
			return null;

		return (ISourceReference) element;
	}

	/**
	 * Returns the most narrow element including the given offset.  If <code>reconcile</code>
	 * is <code>true</code> the editor's input element is reconciled in advance. If it is
	 * <code>false</code> this method only returns a result if the editor's input element
	 * does not need to be reconciled.
	 *
	 * @param offset the offset included by the retrieved element
	 * @param reconcile <code>true</code> if working copy should be reconciled
	 * @return the most narrow element which includes the given offset
	 */
	protected ICElement getElementAt(int offset, boolean reconcile) {
		ITranslationUnit unit= (ITranslationUnit)getInputCElement();

		if (unit != null) {
			try {
				if (reconcile && unit instanceof IWorkingCopy) {
					synchronized (unit) {
						((IWorkingCopy) unit).reconcile();
					}
					return unit.getElementAtOffset(offset);
				} else if (unit.isConsistent()) {
					return unit.getElementAtOffset(offset);
				}
			} catch (CModelException x) {
				CUIPlugin.log(x.getStatus());
				// nothing found, be tolerant and go on
			}
		}

		return null;
	}

	/**
	 * Returns the C element wrapped by this editors input.
	 *
	 * @return the C element wrapped by this editors input.
	 */
	public ICElement getInputCElement () {
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(getEditorInput());
	}

	/**
	 * React to changed selection in the outline view.
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object obj = selection.getFirstElement();
			if (obj instanceof ISourceReference) {
				try {
					ISourceRange range = ((ISourceReference) obj).getSourceRange();
					if (range != null) {
						setSelection(range, !isActivePart());
					}
				} catch (CModelException e) {
                    // Selection change not applied.
				}
			}
		}
	}

	/**
     * Checks is the editor active part. 
     * @return <code>true</code> if editor is the active part of the workbench.
	 */
    private boolean isActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		return (this == service.getActivePart());
	}

	/**
     * Sets selection for C element. 
     * @param element Element to select.
	 */
    public void setSelection(ICElement element) {
		if (element instanceof ISourceReference && !(element instanceof ITranslationUnit)) {
			ISourceReference reference = (ISourceReference) element;
			// set hightlight range
			setSelection(reference, true);
		}
	}

    /**
     * Sets selection for source reference.
     * @param element Source reference to set.
     * @param moveCursor Should cursor be moved.
     */
    public void setSelection(ISourceReference element, boolean moveCursor) {
		if (element != null) {
			StyledText  textWidget = null;
			
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				textWidget = sourceViewer.getTextWidget();
			
			if (textWidget == null)
				return;

			try {
				setSelection(element.getSourceRange(), moveCursor);
			} catch (CModelException e) {
                // Selection not applied.
			}
		}
	}

	/**
	 * Sets the current editor selection to the source range. Optionally
	 * sets the current editor position.
	 *
	 * @param element the source range to be shown in the editor, can be null.
	 * @param moveCursor if true the editor is scrolled to show the range.
	 */
	public void setSelection(ISourceRange element, boolean moveCursor) {
		if (getSelectionProvider() == null)
			return;

		ISelection selection= getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) selection;
			// PR 39995: [navigation] Forward history cleared after going back in navigation history:
			// mark only in navigation history if the cursor is being moved (which it isn't if
			// this is called from a PostSelectionEvent that should only update the magnet)
			if (moveCursor && (textSelection.getOffset() != 0 || textSelection.getLength() != 0))
				markInNavigationHistory();
		}

		if (element != null) {
			
			StyledText textWidget= null;
			
			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;
			
			textWidget= sourceViewer.getTextWidget();
			if (textWidget == null)
				return;

			try {
				IRegion alternateRegion = null;
				int start = element.getStartPos();
				int length = element.getLength();
	
				// Sanity check sometimes the parser may throw wrong numbers.
				if (start < 0 || length < 0) {
					start = 0;
					length = 0;
				}
	
				// 0 length and start and non-zero start line says we know
				// the line for some reason, but not the offset.
				if (length == 0 && start == 0 && element.getStartLine() > 0) {
					// We have the information in term of lines, we can work it out.
					// Binary elements return the first executable statement so we have to subtract -1
					start = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getStartLine() - 1);
					if (element.getEndLine() > 0) {
						length = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getEndLine()) - start;
					} else {
						length = start;
					}
					// create an alternate region for the keyword highlight.
					alternateRegion = getDocumentProvider().getDocument(getEditorInput()).getLineInformation(element.getStartLine() - 1);
					if (start == length || length < 0) {
						if (alternateRegion != null) {
							start = alternateRegion.getOffset();
							length = alternateRegion.getLength();
						}
					}
				}
				setHighlightRange(start, length, moveCursor);
	
				if (moveCursor) {
					start = element.getIdStartPos();
					length = element.getIdLength();
					if (start == 0 && length == 0 && alternateRegion != null) {
						start = alternateRegion.getOffset();
						length = alternateRegion.getLength();
					}
					if (start > -1 && length > 0) {
						try  {
							textWidget.setRedraw(false);
							sourceViewer.revealRange(start, length);
							sourceViewer.setSelectedRange(start, length);
						} finally {
							textWidget.setRedraw(true);
						}
						markInNavigationHistory();
					}
					updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
				}
			} catch (IllegalArgumentException x) {
	            // No information to the user
			} catch (BadLocationException e) {
	            // No information to the user
			}
		} else if (moveCursor) {
			resetHighlightRange();
			markInNavigationHistory();
		}
	}

}
