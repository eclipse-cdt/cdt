/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction.proposals;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.corext.util.Resources;
import org.eclipse.cdt.internal.corext.util.Strings;
import org.eclipse.cdt.internal.ui.CUIStatus;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.LinkedProposalModelPresenter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A proposal for quick fixes and quick assist that work on a single compilation unit.
 * Either a {@link TextChange text change} is directly passed in the constructor or method
 * {@link #addEdits(IDocument, TextEdit)} is overridden to provide the text edits that are
 * applied to the document when the proposal is evaluated.
 * <p>
 * The proposal takes care of the preview of the changes as proposal information.
 *
 * @since 5.1
 */
public class TUCorrectionProposal extends ChangeCorrectionProposal {
	private ITranslationUnit fTranslationUnit;
	private LinkedProposalModel fLinkedProposalModel;

	/**
	 * Constructs a correction proposal working on a compilation unit with a given text change
	 *
	 * @param name the name that is displayed in the proposal selection dialog.
	 * @param tu the compilation unit on that the change works.
	 * @param change the change that is executed when the proposal is applied or {@code null}
	 *     if implementors override {@link #addEdits(IDocument, TextEdit)} to provide
	 *     the text edits or {@link #createTextChange()} to provide a text change.
	 * @param relevance the relevance of this proposal.
	 * @param image the image that is displayed for this proposal or {@code null} if no
	 * image is desired.
	 */
	public TUCorrectionProposal(String name, ITranslationUnit tu, TextChange change, int relevance, Image image) {
		super(name, change, relevance, image);
		if (tu == null) {
			throw new IllegalArgumentException("Translation unit must not be null"); //$NON-NLS-1$
		}
		fTranslationUnit = tu;
	}

	/**
	 * Constructs a correction proposal working on a compilation unit.
	 * <p>
	 * Users have to override {@link #addEdits(IDocument, TextEdit)} to provide
	 * the text edits or {@link #createTextChange()} to provide a text change.
	 *
	 * @param name The name that is displayed in the proposal selection dialog.
	 * @param tu The compilation unit on that the change works.
	 * @param relevance The relevance of this proposal.
	 * @param image The image that is displayed for this proposal or {@code null} if no
	 * image is desired.
	 */
	protected TUCorrectionProposal(String name, ITranslationUnit tu, int relevance, Image image) {
		this(name, tu, null, relevance, image);
	}

	/**
	 * Called when the {@link CTextFileChange} is initialized. Subclasses can override to
	 * add text edits to the root edit of the change. Implementors must not access the proposal,
	 * e.g getting the change.
	 * <p>The default implementation does not add any edits</p>
	 *
	 * @param document content of the underlying compilation unit. To be accessed read only.
	 * @param editRoot The root edit to add all edits to
	 * @throws CoreException can be thrown if adding the edits is failing.
	 */
	protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
		//		if (false) {
		//			throw new CoreException(CUIStatus.createError(IStatus.ERROR, "Implementors can throw an exception", null)); //$NON-NLS-1$
		//		}
	}

	protected LinkedProposalModel getLinkedProposalModel() {
		if (fLinkedProposalModel == null) {
			fLinkedProposalModel = new LinkedProposalModel();
		}
		return fLinkedProposalModel;
	}

	public void setLinkedProposalModel(LinkedProposalModel model) {
		fLinkedProposalModel = model;
	}

	@Override
	public String getAdditionalProposalInfo() {
		final StringBuilder buf = new StringBuilder();

		try {
			final TextChange change = getTextChange();

			change.setKeepPreviewEdits(true);
			final IDocument previewContent = change.getPreviewDocument(new NullProgressMonitor());
			final TextEdit rootEdit = change.getPreviewEdit(change.getEdit());

			class EditAnnotator extends TextEditVisitor {
				private int fWrittenToPos = 0;

				public void unchangedUntil(int pos) {
					if (pos > fWrittenToPos) {
						appendContent(previewContent, fWrittenToPos, pos, buf, true);
						fWrittenToPos = pos;
					}
				}

				@Override
				public boolean visit(MoveTargetEdit edit) {
					return true; //rangeAdded(edit);
				}

				@Override
				public boolean visit(CopyTargetEdit edit) {
					return true; //return rangeAdded(edit);
				}

				@Override
				public boolean visit(InsertEdit edit) {
					return rangeAdded(edit);
				}

				@Override
				public boolean visit(ReplaceEdit edit) {
					if (edit.getLength() > 0)
						return rangeAdded(edit);
					return rangeRemoved(edit);
				}

				@Override
				public boolean visit(MoveSourceEdit edit) {
					return rangeRemoved(edit);
				}

				@Override
				public boolean visit(DeleteEdit edit) {
					return rangeRemoved(edit);
				}

				private boolean rangeRemoved(TextEdit edit) {
					unchangedUntil(edit.getOffset());
					return false;
				}

				private boolean rangeAdded(TextEdit edit) {
					unchangedUntil(edit.getOffset());
					buf.append("<b>"); //$NON-NLS-1$
					appendContent(previewContent, edit.getOffset(), edit.getExclusiveEnd(), buf, false);
					buf.append("</b>"); //$NON-NLS-1$
					fWrittenToPos = edit.getExclusiveEnd();
					return false;
				}
			}
			EditAnnotator ea = new EditAnnotator();
			rootEdit.accept(ea);

			// Final pre-existing region
			ea.unchangedUntil(previewContent.getLength());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return buf.toString();
	}

	private final int surroundLines = 1;

	private void appendContent(IDocument text, int startOffset, int endOffset, StringBuilder buf,
			boolean surroundLinesOnly) {
		try {
			int startLine = text.getLineOfOffset(startOffset);
			int endLine = text.getLineOfOffset(endOffset);

			boolean dotsAdded = false;
			if (surroundLinesOnly && startOffset == 0) { // No surround lines for the top no-change range
				startLine = Math.max(endLine - surroundLines, 0);
				buf.append("...<br>"); //$NON-NLS-1$
				dotsAdded = true;
			}

			for (int i = startLine; i <= endLine; i++) {
				if (surroundLinesOnly) {
					if ((i - startLine > surroundLines) && (endLine - i > surroundLines)) {
						if (!dotsAdded) {
							buf.append("...<br>"); //$NON-NLS-1$
							dotsAdded = true;
						} else if (endOffset == text.getLength()) {
							return; // No surround lines for the bottom no-change range
						}
						continue;
					}
				}

				IRegion lineInfo = text.getLineInformation(i);
				int start = lineInfo.getOffset();
				int end = start + lineInfo.getLength();

				int from = Math.max(start, startOffset);
				int to = Math.min(end, endOffset);
				String content = text.get(from, to - from);
				if (surroundLinesOnly && from == start && Strings.containsOnlyWhitespaces(content)) {
					continue; // Ignore empty lines except when range started in the middle of a line
				}
				for (int k = 0; k < content.length(); k++) {
					char ch = content.charAt(k);
					if (ch == '<') {
						buf.append("&lt;"); //$NON-NLS-1$
					} else if (ch == '>') {
						buf.append("&gt;"); //$NON-NLS-1$
					} else {
						buf.append(ch);
					}
				}
				if (to == end && to != endOffset) { // New line when at the end of the line, and not end of range
					buf.append("<br>"); //$NON-NLS-1$
				}
			}
		} catch (BadLocationException e) {
			// Ignore.
		}
	}

	@Override
	public void apply(IDocument document) {
		try {
			ITranslationUnit unit = getTranslationUnit();
			IEditorPart part = null;
			if (unit.getResource().exists()) {
				boolean canEdit = performValidateEdit(unit);
				if (!canEdit) {
					return;
				}
				part = EditorUtility.isOpenInEditor(unit);
				if (part == null) {
					part = EditorUtility.openInEditor(unit);
					if (part != null) {
						document = CUIPlugin.getDefault().getDocumentProvider().getDocument(part.getEditorInput());
					}
				}
				IWorkbenchPage page = CUIPlugin.getActivePage();
				if (page != null && part != null) {
					page.bringToTop(part);
				}
				if (part != null) {
					part.setFocus();
				}
			}
			performChange(part, document);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, CorrectionMessages.TUCorrectionProposal_error_title,
					CorrectionMessages.TUCorrectionProposal_error_message);
		}
	}

	private boolean performValidateEdit(ITranslationUnit unit) {
		IStatus status = Resources.makeCommittable(unit.getResource(), CUIPlugin.getActiveWorkbenchShell());
		if (!status.isOK()) {
			String label = CorrectionMessages.TUCorrectionProposal_error_title;
			String message = CorrectionMessages.TUCorrectionProposal_error_message;
			ErrorDialog.openError(CUIPlugin.getActiveWorkbenchShell(), label, message, status);
			return false;
		}
		return true;
	}

	@Override
	protected void performChange(IEditorPart part, IDocument document) throws CoreException {
		try {
			super.performChange(part, document);
			if (part == null) {
				return;
			}

			if (fLinkedProposalModel != null) {
				if (fLinkedProposalModel.hasLinkedPositions() && part instanceof CEditor) {
					// enter linked mode
					ITextViewer viewer = ((CEditor) part).getViewer();
					new LinkedProposalModelPresenter().enterLinkedMode(viewer, part, fLinkedProposalModel);
				} else if (part instanceof ITextEditor) {
					LinkedProposalPositionGroup.PositionInformation endPosition = fLinkedProposalModel.getEndPosition();
					if (endPosition != null) {
						// select a result
						int pos = endPosition.getOffset() + endPosition.getLength();
						((ITextEditor) part).selectAndReveal(pos, 0);
					}
				}
			}
		} catch (BadLocationException e) {
			throw new CoreException(CUIStatus.createError(IStatus.ERROR, e));
		}
	}

	/**
	 * Creates the text change for this proposal.
	 * This method is only called once and only when no text change has been passed in
	 * {@link #TUCorrectionProposal(String, ITranslationUnit, TextChange, int, Image)}.
	 *
	 * @return returns the created text change.
	 * @throws CoreException thrown if the creation of the text change failed.
	 */
	protected TextChange createTextChange() throws CoreException {
		ITranslationUnit tu = getTranslationUnit();
		String name = getName();
		TextChange change;
		if (!tu.getResource().exists()) {
			String source;
			try {
				source = tu.getSource();
			} catch (CModelException e) {
				CUIPlugin.log(e);
				source = ""; //$NON-NLS-1$
			}
			Document document = new Document(source);
			document.setInitialLineDelimiter(StubUtility.getLineDelimiterUsed(tu));
			change = new DocumentChange(name, document);
		} else {
			CTextFileChange tuChange = new CTextFileChange(name, tu);
			tuChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
			change = tuChange;
		}
		TextEdit rootEdit = new MultiTextEdit();
		change.setEdit(rootEdit);

		// Initialize text change.
		IDocument document = change.getCurrentDocument(new NullProgressMonitor());
		addEdits(document, rootEdit);
		return change;
	}

	@Override
	protected final Change createChange() throws CoreException {
		return createTextChange(); // make sure that only text changes are allowed here
	}

	/**
	 * Returns the text change that is invoked when the change is applied.
	 *
	 * @return returns the text change that is invoked when the change is applied.
	 * @throws CoreException throws an exception if accessing the change failed
	 */
	public final TextChange getTextChange() throws CoreException {
		return (TextChange) getChange();
	}

	/**
	 * The compilation unit on that the change works.
	 *
	 * @return the compilation unit on that the change works.
	 */
	public final ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	/**
	 * Creates a preview of the content of the compilation unit after applying the change.
	 *
	 * @return returns the preview of the changed compilation unit.
	 * @throws CoreException thrown if the creation of the change failed.
	 */
	public String getPreviewContent() throws CoreException {
		return getTextChange().getPreviewContent(new NullProgressMonitor());
	}

	@Override
	public String toString() {
		try {
			return getPreviewContent();
		} catch (CoreException e) {
		}
		return super.toString();
	}
}
