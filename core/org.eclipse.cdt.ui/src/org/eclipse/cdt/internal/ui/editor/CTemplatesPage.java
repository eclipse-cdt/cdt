/*******************************************************************************
 * Copyright (c) 2007, 2010 Dakshinamurthy Karra, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *     Marc-Andre Laperle - CDT version
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.template.c.CContext;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.ui.preferences.CSourcePreviewerUpdater;
import org.eclipse.cdt.internal.ui.preferences.EditTemplateDialog;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.SimpleCSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.template.TemplateVariableProcessor;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.templates.AbstractTemplatesPage;

/**
 * The templates page for the C editor.
 *
 * @since 5.2
 */
public class CTemplatesPage extends AbstractTemplatesPage {

	private static final String PREFERENCE_PAGE_ID = "org.eclipse.cdt.ui.preferences.TemplatePreferencePage"; //$NON-NLS-1$
	private static final TemplateStore TEMPLATE_STORE = CUIPlugin.getDefault().getTemplateStore();
	private static final IPreferenceStore PREFERENCE_STORE = CUIPlugin.getDefault().getPreferenceStore();
	private static final ContextTypeRegistry TEMPLATE_CONTEXT_REGISTRY = CUIPlugin.getDefault()
			.getTemplateContextRegistry();

	private TemplateVariableProcessor fTemplateProcessor;
	private CEditor fCEditor;

	/**
	 * Create a new AbstractTemplatesPage for the CEditor
	 *
	 * @param cEditor the C editor
	 */
	public CTemplatesPage(CEditor cEditor) {
		super(cEditor, cEditor.getViewer());
		fCEditor = cEditor;
		fTemplateProcessor = new TemplateVariableProcessor();
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#insertTemplate(org.eclipse.jface.text.templates.Template, org.eclipse.jface.text.IDocument)
	 */
	@Override
	protected void insertTemplate(Template template, IDocument document) {
		if (!fCEditor.validateEditorInputState())
			return;

		ISourceViewer contextViewer = fCEditor.getViewer();
		ITextSelection textSelection = (ITextSelection) contextViewer.getSelectionProvider().getSelection();
		if (!isValidTemplate(document, template, textSelection.getOffset(), textSelection.getLength()))
			return;
		beginCompoundChange(contextViewer);
		/*
		 * The Editor checks whether a completion for a word exists before it allows for the template to be
		 * applied. We pickup the current text at the selection position and replace it with the first char
		 * of the template name for this to succeed.
		 * Another advantage by this method is that the template replaces the selected text provided the
		 * selection by itself is not used in the template pattern.
		 */
		String savedText;
		try {
			savedText = document.get(textSelection.getOffset(), textSelection.getLength());
			if (savedText.length() == 0) {
				String prefix = getIdentifierPart(document, template, textSelection.getOffset(),
						textSelection.getLength());
				if (prefix.length() > 0 && !template.getName().startsWith(prefix.toString())) {
					return;
				}
				if (prefix.length() > 0) {
					contextViewer.setSelectedRange(textSelection.getOffset() - prefix.length(), prefix.length());
					textSelection = (ITextSelection) contextViewer.getSelectionProvider().getSelection();
				}
			}
			document.replace(textSelection.getOffset(), textSelection.getLength(), template.getName().substring(0, 1));
		} catch (BadLocationException e) {
			endCompoundChange(contextViewer);
			return;
		}
		Position position = new Position(textSelection.getOffset() + 1, 0);
		Region region = new Region(textSelection.getOffset() + 1, 0);
		contextViewer.getSelectionProvider().setSelection(new TextSelection(textSelection.getOffset(), 1));
		ITranslationUnit compilationUnit = (ITranslationUnit) EditorUtility.getEditorInputCElement(fCEditor);

		TemplateContextType type = getContextTypeRegistry().getContextType(template.getContextTypeId());
		DocumentTemplateContext context = ((CContextType) type).createContext(document, position, compilationUnit);
		context.setVariable("selection", savedText); //$NON-NLS-1$
		if (context.getKey().length() == 0) {
			try {
				document.replace(textSelection.getOffset(), 1, savedText);
			} catch (BadLocationException e) {
				endCompoundChange(contextViewer);
				return;
			}
		}
		TemplateProposal proposal = new TemplateProposal(template, context, region, null);
		fCEditor.getSite().getPage().activate(fCEditor);
		proposal.apply(fCEditor.getViewer(), ' ', 0, region.getOffset());
		endCompoundChange(contextViewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#getContextTypeRegistry()
	 */
	@Override
	protected ContextTypeRegistry getContextTypeRegistry() {
		return TEMPLATE_CONTEXT_REGISTRY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#getTemplatePreferenceStore()
	 */
	@Override
	protected IPreferenceStore getTemplatePreferenceStore() {
		return PREFERENCE_STORE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#getTemplateStore()
	 */
	@Override
	public TemplateStore getTemplateStore() {
		return TEMPLATE_STORE;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TextEditorTemplatesPage#isValidTemplate(org.eclipse.jface.text.templates.Template, int, int)
	 */
	@Override
	protected boolean isValidTemplate(IDocument document, Template template, int offset, int length) {
		String[] contextIds = getContextTypeIds(document, offset);
		for (int i = 0; i < contextIds.length; i++) {
			if (contextIds[i].equals(template.getContextTypeId())) {
				DocumentTemplateContext context = getContext(document, template, offset, length);
				return context.canEvaluate(template) || isTemplateAllowed(context, template);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.TextEditorTemplatesPage#createPatternViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected SourceViewer createPatternViewer(Composite parent) {
		IDocument document = new Document();
		CTextTools tools = CUIPlugin.getDefault().getTextTools();
		tools.setupCDocumentPartitioner(document, ICPartitions.C_PARTITIONING, null);
		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		CSourceViewer viewer = new CSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL, store);
		SimpleCSourceViewerConfiguration configuration = new SimpleCSourceViewerConfiguration(tools.getColorManager(),
				store, null, ICPartitions.C_PARTITIONING, false);
		viewer.configure(configuration);
		viewer.setEditable(false);
		viewer.setDocument(document);

		Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		new CSourcePreviewerUpdater(viewer, configuration, store);

		Control control = viewer.getControl();
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		control.setLayoutData(data);

		viewer.setEditable(false);
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#getImageForTemplate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	protected Image getImage(Template template) {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TEMPLATE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TextEditorTemplatesPage#editTemplate(org.eclipse.jface.text.templates.Template, boolean, boolean)
	 */
	@Override
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		EditTemplateDialog dialog = new EditTemplateDialog(getSite().getShell(), template, edit, isNameModifiable,
				false, getContextTypeRegistry());
		if (dialog.open() == Window.OK)
			return dialog.getTemplate();
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TextEditorTemplatesPage#updatePatternViewer(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	protected void updatePatternViewer(Template template) {
		if (template == null) {
			getPatternViewer().getDocument().set(""); //$NON-NLS-1$
			return;
		}
		String contextId = template.getContextTypeId();
		TemplateContextType type = getContextTypeRegistry().getContextType(contextId);
		fTemplateProcessor.setContextType(type);

		IDocument doc = getPatternViewer().getDocument();

		String start = ""; //$NON-NLS-1$
		doc.set(start + template.getPattern());
		int startLen = start.length();
		getPatternViewer().setDocument(doc, startLen, doc.getLength() - startLen);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.AbstractTemplatesPage#getPreferencePageId()
	 */
	@Override
	protected String getPreferencePageId() {
		return PREFERENCE_PAGE_ID;
	}

	/**
	 * Undomanager - end compound change
	 *
	 * @param viewer the viewer
	 */
	private void endCompoundChange(ISourceViewer viewer) {
		if (viewer instanceof ITextViewerExtension)
			((ITextViewerExtension) viewer).getRewriteTarget().endCompoundChange();
	}

	/**
	 * Undomanager - begin a compound change
	 *
	 * @param viewer the viewer
	 */
	private void beginCompoundChange(ISourceViewer viewer) {
		if (viewer instanceof ITextViewerExtension)
			((ITextViewerExtension) viewer).getRewriteTarget().beginCompoundChange();
	}

	/**
	 * Check whether the template is allowed even though the context can't evaluate it. This is
	 * needed because the Dropping of a template is more lenient than ctl-space invoked code assist.
	 *
	 * @param context the template context
	 * @param template the template
	 * @return true if the template is allowed
	 */
	private boolean isTemplateAllowed(DocumentTemplateContext context, Template template) {
		int offset;
		try {
			return ((offset = context.getCompletionOffset()) > 0
					&& !isTemplateNamePart(context.getDocument().getChar(offset - 1)));
		} catch (BadLocationException e) {
		}
		return false;
	}

	/**
	 * Checks whether the character is a valid character in C template names
	 *
	 * @param ch the character
	 * @return <code>true</code> if the character is part of a template name
	 */
	private boolean isTemplateNamePart(char ch) {
		return !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';';
	}

	/**
	 * Get context
	 *
	 * @param document the document
	 * @param template the template
	 * @param offset the offset
	 * @param length the length
	 * @return the context
	 */
	private DocumentTemplateContext getContext(IDocument document, Template template, final int offset, int length) {
		DocumentTemplateContext context = new CContext(
				getContextTypeRegistry().getContextType(template.getContextTypeId()), document,
				new Position(offset, length), (ITranslationUnit) EditorUtility.getEditorInputCElement(fCEditor));
		return context;
	}

	/**
	 * Get the active contexts for the given position in the document.
	 * <p>
	 * FIXME: should trigger code assist to get the context.
	 * </p>
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return an array of valid context id
	 */
	@Override
	protected String[] getContextTypeIds(IDocument document, int offset) {
		return new String[] { CContextType.ID };
	}

	/**
	 * Get the C identifier terminated at the given offset
	 *
	 * @param document the document
	 * @param template the template
	 * @param offset the offset
	 * @param length the length
	 * @return the identifier part the C identifier
	 */
	private String getIdentifierPart(IDocument document, Template template, int offset, int length) {
		return getContext(document, template, offset, length).getKey();
	}
}
