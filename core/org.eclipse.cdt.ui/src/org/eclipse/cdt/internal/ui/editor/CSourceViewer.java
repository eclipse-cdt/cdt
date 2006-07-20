/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.ResourceUtil;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor.ITextConverter;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;

/**
 * Adapted source viewer for CEditor
 */

public class CSourceViewer extends ProjectionViewer implements ITextViewerExtension {

    /** Show outline operation id. */
    public static final int SHOW_OUTLINE = 101;
    
	/** Editor. */
    private final CEditor editor;
    /** Presents outline. */
    private IInformationPresenter fOutlinePresenter;

    private List fTextConverters;
    
	/**
     * Creates new source viewer. 
     * @param editor
     * @param parent
     * @param ruler
     * @param styles
     * @param fOverviewRuler
     * @param isOverviewRulerShowing
	 */
    public CSourceViewer(
    		CEditor editor, Composite parent,
    		IVerticalRuler ruler,
    		int styles,
    		IOverviewRuler fOverviewRuler,
    		boolean isOverviewRulerShowing) {
		super(parent, ruler, fOverviewRuler, isOverviewRulerShowing, styles);
        this.editor = editor;
	}
    
	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}
    
	public ILanguage getLanguage() {
		ICElement element = editor.getInputCElement();
		if (element instanceof ITranslationUnit) {
			try {
				return ((ITranslationUnit)element).getLanguage();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		} else {
			// compute the language from the plain editor input
			IContentType contentType = null;
			IEditorInput input = editor.getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				contentType = CCorePlugin.getContentType(file.getProject(), file.getName());
			} else if (input instanceof IPathEditorInput) {
				IPath path = ((IPathEditorInput)input).getPath();
				contentType = CCorePlugin.getContentType(path.lastSegment());
			} else {
				ILocationProvider locationProvider = (ILocationProvider)input.getAdapter(ILocationProvider.class);
				if (locationProvider != null) {
					IPath path = locationProvider.getPath(input);
					contentType = CCorePlugin.getContentType(path.lastSegment());
				}
			}
			if (contentType != null) {
				try {
					return LanguageManager.getInstance().getLanguage(contentType);
				} catch (CoreException exc) {
					CUIPlugin.getDefault().log(exc.getStatus());
				}
			}
		}
		return null;
	}
	
    /**
     * @see org.eclipse.jface.text.source.SourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
     */
    public void configure(SourceViewerConfiguration configuration)
    {
        super.configure(configuration);
        if (configuration instanceof CSourceViewerConfiguration)
        {            
            fOutlinePresenter = ((CSourceViewerConfiguration) configuration).getOutlinePresenter(editor);
            fOutlinePresenter.install(this);
        }
    }

    /**
     * @see org.eclipse.jface.text.source.SourceViewer#unconfigure()
     */
    public void unconfigure()
    {
        if (fOutlinePresenter != null) {
            fOutlinePresenter.uninstall();  
            fOutlinePresenter= null;
        }
        super.unconfigure();
    }
    
	/**
     * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
	 */
    public void doOperation(int operation) {

		if (getTextWidget() == null) {
			return;
		}
		switch (operation) {
			case CONTENTASSIST_PROPOSALS:
            {
				String msg= fContentAssistant.showPossibleCompletions();
				this.editor.setStatusLineErrorMessage(msg);
				return;
            }
            case SHOW_OUTLINE:
            {
                fOutlinePresenter.showInformation();
                return;
            }
		}
		super.doOperation(operation);
	}

    /**
     * @see org.eclipse.jface.text.source.projection.ProjectionViewer#canDoOperation(int)
     */
    public boolean canDoOperation(int operation)
    {
        if (operation == SHOW_OUTLINE)
        {
            return fOutlinePresenter != null;
        }
        return super.canDoOperation(operation);
    }

	public void insertTextConverter(ITextConverter textConverter, int index) {
		throw new UnsupportedOperationException();
	}

	public void addTextConverter(ITextConverter textConverter) {
		if (fTextConverters == null) {
			fTextConverters = new ArrayList(1);
			fTextConverters.add(textConverter);
		} else if (!fTextConverters.contains(textConverter))
			fTextConverters.add(textConverter);
	}

	public void removeTextConverter(ITextConverter textConverter) {
		if (fTextConverters != null) {
			fTextConverters.remove(textConverter);
			if (fTextConverters.size() == 0)
				fTextConverters = null;
		}
	}

	/*
	 * @see TextViewer#customizeDocumentCommand(DocumentCommand)
	 */
	protected void customizeDocumentCommand(DocumentCommand command) {
		super.customizeDocumentCommand(command);
		if (fTextConverters != null) {
			for (Iterator e = fTextConverters.iterator(); e.hasNext();)
				 ((ITextConverter) e.next()).customizeDocumentCommand(getDocument(), command);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.ISourceViewer#setRangeIndication(int, int, boolean)
	 */
	public void setRangeIndication(int offset, int length, boolean moveCursor) {
		// Fixin a bug in the ProjectViewer implemenation
		// PR: https://bugs.eclipse.org/bugs/show_bug.cgi?id=72914
		if (isProjectionMode()) {
			super.setRangeIndication(offset, length, moveCursor);
		} else {
			super.setRangeIndication(offset, length, false);
		}
	}
}
