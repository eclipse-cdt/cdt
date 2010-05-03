/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Axel Mueller - [289339] Surround with
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension7;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.actions.TextSearchGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextNavigationAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.GenerateActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.cdt.ui.refactoring.actions.CRefactoringActionGroup;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.AddBlockCommentAction;
import org.eclipse.cdt.internal.ui.actions.FindWordAction;
import org.eclipse.cdt.internal.ui.actions.FoldingActionGroup;
import org.eclipse.cdt.internal.ui.actions.GoToNextPreviousMemberAction;
import org.eclipse.cdt.internal.ui.actions.GotoNextBookmarkAction;
import org.eclipse.cdt.internal.ui.actions.IndentAction;
import org.eclipse.cdt.internal.ui.actions.RemoveBlockCommentAction;
import org.eclipse.cdt.internal.ui.actions.SurroundWithActionGroup;
import org.eclipse.cdt.internal.ui.search.OccurrencesFinder;
import org.eclipse.cdt.internal.ui.search.IOccurrencesFinder.OccurrenceLocation;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CPairMatcher;
import org.eclipse.cdt.internal.ui.text.CSourceViewerScalableConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.CWordIterator;
import org.eclipse.cdt.internal.ui.text.DocumentCharacterIterator;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.internal.ui.text.Symbols;
import org.eclipse.cdt.internal.ui.text.TabsToSpacesConverter;
import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.util.CUIHelp;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.viewsupport.ISelectionListenerWithAST;
import org.eclipse.cdt.internal.ui.viewsupport.SelectionListenerWithASTManager;

/**
 * C/C++ source editor.
 */
public class CEditor extends TextEditor implements ISelectionChangedListener, ICReconcilingListener {

	/** Marker used for synchronization from Problems View to the editor on double-click. */
	private IMarker fSyncProblemsViewMarker = null;

	/**
	 * A slightly modified implementation of IGotomarker compared to AbstractDecoratedTextEditor.
	 * 
	 * @since 5.0
	 */
	private final class GotoMarkerAdapter implements IGotoMarker {
		/*
		 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#gotoMarker(org.eclipse.core.resources.IMarker)
		 */
		public void gotoMarker(IMarker marker) {
			if (fIsUpdatingMarkerViews)
				return;

			if (getSourceViewer() == null)
				return;

			int start= MarkerUtilities.getCharStart(marker);
			int end= MarkerUtilities.getCharEnd(marker);
			
			boolean selectLine= start < 0 || end < 0;

			// look up the current range of the marker when the document has been edited
			IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
			if (model instanceof AbstractMarkerAnnotationModel) {

				AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) model;
				Position pos= markerModel.getMarkerPosition(marker);
				if (pos != null && !pos.isDeleted()) {
					// use position instead of marker values
					start= pos.getOffset();
					end= pos.getOffset() + pos.getLength();
					// use position as is
					selectLine= false;
				}

				if (pos != null && pos.isDeleted()) {
					// do nothing if position has been deleted
					return;
				}
			}

			IDocument document= getDocumentProvider().getDocument(getEditorInput());

			if (selectLine) {
				int line;
				try {
					if (start >= 0) {
						IRegion lineInfo= document.getLineInformationOfOffset(start);
						start= lineInfo.getOffset();
						end= start + lineInfo.getLength();
					} else {
						line= MarkerUtilities.getLineNumber(marker);
						// Marker line numbers are 1-based
						-- line;
						IRegion lineInfo= document.getLineInformation(line);
						start= lineInfo.getOffset();
						end= start + lineInfo.getLength();
					}
				} catch (BadLocationException e) {
					return;
				}
			}

			int length= document.getLength();
			if (end - 1 < length && start < length) {
				fSyncProblemsViewMarker = marker;
				selectAndReveal(start, end - start);
			}
		}
	}

	class AdaptedSourceViewer extends CSourceViewer  {

		public AdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				                   boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
		}

		@Override
		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		}

		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		@Override
		public void doOperation(int operation) {
			if (getTextWidget() == null)
				return;

			switch (operation) {
				case CONTENTASSIST_PROPOSALS:
					String msg= fContentAssistant.showPossibleCompletions();
					setStatusLineErrorMessage(msg);
					return;
				case QUICK_ASSIST:
					/*
					 * XXX: We can get rid of this once the SourceViewer has a way to update the status line
					 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
					 */
					msg= fQuickAssistAssistant.showPossibleQuickAssists();
					setStatusLineErrorMessage(msg);
					return;
			}

			super.doOperation(operation);
		}

		/*
		 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
		 */
		@Override
		public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester);
		}

		/*
		 * @see IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper, int)
		 * @since 3.0
		 */
		@Override
		public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester, priority);
		}

		/*
		 * @see org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
		 * @since 3.0
		 */
		@Override
		public IFormattingContext createFormattingContext() {
			IFormattingContext context= new FormattingContext();

			Map<String, Object> preferences;
			ICElement inputCElement= getInputCElement();
			ICProject cProject= inputCElement != null ? inputCElement.getCProject() : null;
			if (cProject == null)
				preferences= new HashMap<String, Object>(CCorePlugin.getOptions());
			else
				preferences= new HashMap<String, Object>(cProject.getOptions(true));

			if (inputCElement instanceof ITranslationUnit) {
				ITranslationUnit tu= (ITranslationUnit) inputCElement;
				ILanguage language= null;
				try {
					language= tu.getLanguage();
				} catch (CoreException exc) {
					// use fallback CPP
					language= GPPLanguage.getDefault();
				}
				preferences.put(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT, tu);
		        preferences.put(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE, language);
				preferences.put(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE, tu.getResource());
			}

			if (cProject == null) {
				// custom formatter specified?
				String customFormatterId= getPreferenceStore().getString(CCorePreferenceConstants.CODE_FORMATTER);
				if (customFormatterId != null) {
					preferences.put(CCorePreferenceConstants.CODE_FORMATTER, customFormatterId);
				}
			}
			context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

			return context;
		}
	}

	private class ExitPolicy implements IExitPolicy {
		final char fExitCharacter;
		final char fEscapeCharacter;
		final Stack<BracketLevel> fStack;
		final int fSize;

		public ExitPolicy(char exitCharacter, char escapeCharacter, Stack<BracketLevel> stack) {
			fExitCharacter = exitCharacter;
			fEscapeCharacter = escapeCharacter;
			fStack = stack;
			fSize = fStack.size();
		}

		/*
		 * @see org.eclipse.jface.text.link.LinkedModeUI$IExitPolicy#doExit(org.eclipse.jface.text.link.LinkedModeModel, org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			if (fSize == fStack.size() && !isMasked(offset)) {
				if (event.character == fExitCharacter) {
					BracketLevel level = fStack.peek();
					if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset)
						return null;
					if (level.fSecondPosition.offset == offset && length == 0)
						// don't enter the character if if its the closing peer
						return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				}
				// when entering an anonymous class between the parenthesis', we don't want
				// to jump after the closing parenthesis when return is pressed
				if (event.character == SWT.CR && offset > 0) {
					IDocument document = getSourceViewer().getDocument();
					try {
						if (document.getChar(offset - 1) == '{')
							return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
					} catch (BadLocationException e) {
					}
				}
			}
			return null;
		}

		private boolean isMasked(int offset) {
			IDocument document = getSourceViewer().getDocument();
			try {
				return fEscapeCharacter == document.getChar(offset - 1);
			} catch (BadLocationException e) {
			}
			return false;
		}
	}

	private static class BracketLevel {
//		int fOffset;
//		int fLength;
		LinkedModeUI fUI;
		Position fFirstPosition;
		Position fSecondPosition;
	}

	/**
	 * Position updater that takes any changes at the borders of a position to not belong to the position.
	 *
	 * @since 4.0
	 */
	private static class ExclusivePositionUpdater implements IPositionUpdater {

		/** The position category. */
		private final String fCategory;

		/**
		 * Creates a new updater for the given <code>category</code>.
		 *
		 * @param category the new category.
		 */
		public ExclusivePositionUpdater(String category) {
			fCategory = category;
		}

		/*
		 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
		 */
		public void update(DocumentEvent event) {
			int eventOffset = event.getOffset();
			int eventOldLength = event.getLength();
			int eventNewLength = event.getText() == null ? 0 : event.getText().length();
			int deltaLength = eventNewLength - eventOldLength;

			try {
				Position[] positions = event.getDocument().getPositions(fCategory);

				for (int i = 0; i != positions.length; i++) {

					Position position = positions[i];

					if (position.isDeleted())
						continue;

					int offset = position.getOffset();
					int length = position.getLength();
					int end = offset + length;

					if (offset >= eventOffset + eventOldLength) {
						// position comes
						// after change - shift
						position.setOffset(offset + deltaLength);
				    } else if (end <= eventOffset) {
						// position comes way before change -
						// leave alone
					} else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
						// event completely internal to the position - adjust length
						position.setLength(length + deltaLength);
					} else if (offset < eventOffset) {
						// event extends over end of position - adjust length
						int newEnd = eventOffset;
						position.setLength(newEnd - offset);
					} else if (end > eventOffset + eventOldLength) {
						// event extends from before position into it - adjust offset
						// and length
						// offset becomes end of event, length adjusted accordingly
						int newOffset = eventOffset + eventNewLength;
						position.setOffset(newOffset);
						position.setLength(end - newOffset);
					} else {
						// event consumes the position - delete it
						position.delete();
					}
				}
			} catch (BadPositionCategoryException e) {
				// ignore and return
			}
		}

//		/**
//		 * Returns the position category.
//		 *
//		 * @return the position category
//		 */
//		public String getCategory() {
//			return fCategory;
//		}
	}

	private class BracketInserter implements VerifyKeyListener, ILinkedModeListener {

		private boolean fCloseBrackets = true;
		private boolean fCloseStrings = true;
		private boolean fCloseAngularBrackets = true;
		private final String CATEGORY = toString();
		private IPositionUpdater fUpdater = new ExclusivePositionUpdater(CATEGORY);
		private Stack<BracketLevel> fBracketLevelStack = new Stack<BracketLevel>();

		public void setCloseBracketsEnabled(boolean enabled) {
			fCloseBrackets = enabled;
		}

		public void setCloseStringsEnabled(boolean enabled) {
			fCloseStrings = enabled;
		}

		public void setCloseAngularBracketsEnabled(boolean enabled) {
			fCloseAngularBrackets = enabled;
		}

		private boolean isAngularIntroducer(String identifier) {
			return identifier.length() > 0 && (Character.isUpperCase(identifier.charAt(0))
					|| angularIntroducers.contains(identifier)
					|| identifier.endsWith("_ptr") //$NON-NLS-1$
					|| identifier.endsWith("_cast")); //$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {

			// early pruning to slow down normal typing as little as possible
			if (!event.doit || getInsertMode() != SMART_INSERT)
				return;
			switch (event.character) {
				case '(':
				case '<':
				case '[':
				case '\'':
				case '\"':
					break;
				default:
					return;
			}

			final ISourceViewer sourceViewer = getSourceViewer();
			IDocument document = sourceViewer.getDocument();

			final Point selection = sourceViewer.getSelectedRange();
			final int offset = selection.x;
			final int length = selection.y;
			try {
				IRegion startLine = document.getLineInformationOfOffset(offset);
				IRegion endLine = document.getLineInformationOfOffset(offset + length);
				if (startLine != endLine && isBlockSelectionModeEnabled()) {
					return;
				}

				ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, true);
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
						&& !ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
					return;
				}

				CHeuristicScanner scanner = new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, partition.getType());
				int nextToken = scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
				String next = nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
				int prevToken = scanner.previousToken(offset - 1, startLine.getOffset());
				int prevTokenOffset = scanner.getPosition() + 1;
				String previous = prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();

				switch (event.character) {
					case '(':
						if (!fCloseBrackets
								|| nextToken == Symbols.TokenLPAREN
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '<':
						if (!(fCloseAngularBrackets && fCloseBrackets)
								|| nextToken == Symbols.TokenLESSTHAN
								|| prevToken != Symbols.TokenIDENT 
								|| !isAngularIntroducer(previous))
							return;
						break;

					case '[':
						if (!fCloseBrackets
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '\'':
					case '"':
						if (!fCloseStrings
								|| nextToken == Symbols.TokenIDENT
								|| next != null && (next.length() > 1 || next.charAt(0) == event.character)
								|| isInsideStringInPreprocessorDirective(partition, document, offset))
							return;
						break;

					default:
						return;
				}

				if (!validateEditorInputState())
					return;

				final char character = event.character;
				final char closingCharacter = getPeerCharacter(character);
				final StringBuilder buffer = new StringBuilder(3);
				buffer.append(character);
				buffer.append(closingCharacter);
				if (closingCharacter == '>' && nextToken != Symbols.TokenEOF
						&& document.getChar(offset + length) == '>') {
					// Insert a space to avoid two consecutive closing angular brackets.
					buffer.append(' ');
				}

				document.replace(offset, length, buffer.toString());

				BracketLevel level = new BracketLevel();
				fBracketLevelStack.push(level);

				LinkedPositionGroup group = new LinkedPositionGroup();
				group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));

				LinkedModeModel model = new LinkedModeModel();
				model.addLinkingListener(this);
				model.addGroup(group);
				model.forceInstall();

//				level.fOffset = offset;
//				level.fLength = 2;

				// set up position tracking for our magic peers
				if (fBracketLevelStack.size() == 1) {
					document.addPositionCategory(CATEGORY);
					document.addPositionUpdater(fUpdater);
				}
				level.fFirstPosition = new Position(offset, 1);
				level.fSecondPosition = new Position(offset + 1, 1);
				document.addPosition(CATEGORY, level.fFirstPosition);
				document.addPosition(CATEGORY, level.fSecondPosition);

				level.fUI = new EditorLinkedModeUI(model, sourceViewer);
				level.fUI.setSimpleMode(true);
				level.fUI.setExitPolicy(new ExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter), fBracketLevelStack));
				level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
				level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
				level.fUI.enter();

				IRegion newSelection = level.fUI.getSelectedRegion();
				sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());

				event.doit = false;
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			} catch (BadPositionCategoryException e) {
				CUIPlugin.log(e);
			}
		}

		private boolean isInsideStringInPreprocessorDirective(ITypedRegion partition, IDocument document, int offset) throws BadLocationException {
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType()) && offset < document.getLength()) {
				// use temporary document to test whether offset is inside non-default partition
				String directive = document.get(partition.getOffset(), offset - partition.getOffset() + 1);
				int hashIdx = directive.indexOf('#');
				if (hashIdx >= 0) {
					IDocument tmp = new Document(directive.substring(hashIdx + 1));
					new CDocumentSetupParticipant().setup(tmp);
					String type = TextUtilities.getContentType(tmp, ICPartitions.C_PARTITIONING, offset - (partition.getOffset() + hashIdx + 1), true);
					if (!type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
		 */
		public void left(LinkedModeModel environment, int flags) {

			final BracketLevel level = fBracketLevelStack.pop();

			if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION)
				return;

			// remove brackets
			final ISourceViewer sourceViewer = getSourceViewer();
			final IDocument document = sourceViewer.getDocument();
			if (document instanceof IDocumentExtension) {
				IDocumentExtension extension = (IDocumentExtension) document;
				extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {

					public void perform(IDocument d, IDocumentListener owner) {
						if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0)
								&& !level.fSecondPosition.isDeleted
								&& level.fSecondPosition.offset == level.fFirstPosition.offset) {
							try {
								document.replace(level.fSecondPosition.offset,
												 level.fSecondPosition.length,
												 null);
							} catch (BadLocationException e) {
								CUIPlugin.log(e);
							}
						}

						if (fBracketLevelStack.size() == 0) {
							document.removePositionUpdater(fUpdater);
							try {
								document.removePositionCategory(CATEGORY);
							} catch (BadPositionCategoryException e) {
								CUIPlugin.log(e);
							}
						}
					}
				});
			}
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
		 */
		public void suspend(LinkedModeModel environment) {
		}

		/*
		 * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
		 */
		public void resume(LinkedModeModel environment, int flags) {
		}
	}

	/**
	 * Updates the C outline page selection and this editor's range indicator.
	 *
	 * @since 3.0
	 */
	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {

		/**
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			// XXX: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=56161
			CEditor.this.selectionChanged();
		}
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * @since 4.0
	 */
	protected abstract class NextSubWordAction extends TextNavigationAction {

		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new next sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected NextSubWordAction(int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		@Override
		public void run() {
			// Check whether sub word navigation is enabled.
			final IPreferenceStore store = getPreferenceStore();
			if (!store.getBoolean(SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int next = findNextPosition(position);
			try {
				if (isBlockSelectionModeEnabled() && document.getLineOfOffset(next) != document.getLineOfOffset(position)) {
					super.run(); // may navigate into virtual white space
				} else if (next != BreakIterator.DONE) {
					setCaretPosition(next);
					getTextWidget().showSelection();
					fireSelectionChanged();
				}
			} catch (BadLocationException x) {
				// ignore
			}
		}

		/**
		 * Finds the next position after the given position.
		 *
		 * @param position the current position
		 * @return the next position
		 */
		protected int findNextPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.following(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with <code>position</code>.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class NavigateNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new navigate next sub-word action.
		 */
		public NavigateNextSubWordAction() {
			super(ST.WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class DeleteNextSubWordAction extends NextSubWordAction implements IUpdate {

		/**
		 * Creates a new delete next sub-word action.
		 */
		public DeleteNextSubWordAction() {
			super(ST.DELETE_WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(final int position) {
			if (!validateEditorInputState())
				return;

			final ISourceViewer viewer = getSourceViewer();
			StyledText text= viewer.getTextWidget();
			Point widgetSelection= text.getSelection();
			if (isBlockSelectionModeEnabled() && widgetSelection.y != widgetSelection.x) {
				final int caret= text.getCaretOffset();
				final int offset= modelOffset2WidgetOffset(viewer, position);

				if (caret == widgetSelection.x)
					text.setSelectionRange(widgetSelection.y, offset - widgetSelection.y);
				else
					text.setSelectionRange(widgetSelection.x, offset - widgetSelection.x);
				text.invokeAction(ST.DELETE_NEXT);
			} else {
				Point selection= viewer.getSelectedRange();
				final int caret, length;
				if (selection.y != 0) {
					caret= selection.x;
					length= selection.y;
				} else {
					caret= widgetOffset2ModelOffset(viewer, text.getCaretOffset());
					length= position - caret;
				}

				try {
					viewer.getDocument().replace(caret, length, ""); //$NON-NLS-1$
				} catch (BadLocationException exception) {
					// Should not happen
				}
			}
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#findNextPosition(int)
		 */
		@Override
		protected int findNextPosition(int position) {
			return fIterator.following(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the next sub-word.
	 *
	 * @since 4.0
	 */
	protected class SelectNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new select next sub-word action.
		 */
		public SelectNextSubWordAction() {
			super(ST.SELECT_WORD_NEXT);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.NextSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected abstract class PreviousSubWordAction extends TextNavigationAction {

		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new previous sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected PreviousSubWordAction(final int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		@Override
		public void run() {
			// Check whether sub word navigation is enabled.
			final IPreferenceStore store = getPreferenceStore();
			if (!store.getBoolean(SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int previous = findPreviousPosition(position);
			try {
				if (isBlockSelectionModeEnabled() && document.getLineOfOffset(previous) != document.getLineOfOffset(position)) {
					super.run(); // may navigate into virtual white space
				} else if (previous != BreakIterator.DONE) {
					setCaretPosition(previous);
					getTextWidget().showSelection();
					fireSelectionChanged();
				}
			} catch (BadLocationException x) {
				// ignore - getLineOfOffset failed
			}
		}

		/**
		 * Finds the previous position before the given position.
		 *
		 * @param position the current position
		 * @return the previous position
		 */
		protected int findPreviousPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.preceding(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with <code>position</code>.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new navigate previous sub-word action.
		 */
		public NavigatePreviousSubWordAction() {
			super(ST.WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class DeletePreviousSubWordAction extends PreviousSubWordAction implements IUpdate {

		/**
		 * Creates a new delete previous sub-word action.
		 */
		public DeletePreviousSubWordAction() {
			super(ST.DELETE_WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(int position) {
			if (!validateEditorInputState())
				return;

			final int length;
			final ISourceViewer viewer = getSourceViewer();
			StyledText text= viewer.getTextWidget();
			Point widgetSelection= text.getSelection();
			if (isBlockSelectionModeEnabled() && widgetSelection.y != widgetSelection.x) {
				final int caret= text.getCaretOffset();
				final int offset= modelOffset2WidgetOffset(viewer, position);

				if (caret == widgetSelection.x)
					text.setSelectionRange(widgetSelection.y, offset - widgetSelection.y);
				else
					text.setSelectionRange(widgetSelection.x, offset - widgetSelection.x);
				text.invokeAction(ST.DELETE_PREVIOUS);
			} else {
				Point selection= viewer.getSelectedRange();
				if (selection.y != 0) {
					position= selection.x;
					length= selection.y;
				} else {
					length= widgetOffset2ModelOffset(viewer, text.getCaretOffset()) - position;
				}

				try {
					viewer.getDocument().replace(position, length, ""); //$NON-NLS-1$
				} catch (BadLocationException exception) {
					// Should not happen
				}
			}
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#findPreviousPosition(int)
		 */
		@Override
		protected int findPreviousPosition(int position) {
			return fIterator.preceding(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the previous sub-word.
	 *
	 * @since 4.0
	 */
	protected class SelectPreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new select previous sub-word action.
		 */
		public SelectPreviousSubWordAction() {
			super(ST.SELECT_WORD_PREVIOUS);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.editor.CEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		@Override
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	/**
	 * The editor selection changed listener.
	 *
	 * @since 3.0
	 */
	private EditorSelectionChangedListener fEditorSelectionChangedListener;

	/** The outline page */
	protected CContentOutlinePage fOutlinePage;

	/** Search actions **/
	private ActionGroup fSelectionSearchGroup;
	private ActionGroup fTextSearchGroup;
	private CRefactoringActionGroup fRefactoringActionGroup;
	private ActionGroup fOpenInViewGroup;

	/** Generate action group filling the "Source" submenu */
	private GenerateActionGroup fGenerateActionGroup;
	
	/** Generate action group filling the "Surround with" submenu */
	private SurroundWithActionGroup fSurroundWithActionGroup;

	/** Pairs of brackets, used to match. */
    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };

	/** Matches the brackets. */
    protected CPairMatcher fBracketMatcher = new CPairMatcher(BRACKETS);

	/** The bracket inserter. */
	private BracketInserter fBracketInserter = new BracketInserter();

	/** Listener to annotation model changes that updates the error tick in the tab image */
	private CEditorErrorTickUpdater fCEditorErrorTickUpdater;

	/** Preference key for sub-word navigation, aka smart caret positioning */
	public final static String SUB_WORD_NAVIGATION = "subWordNavigation"; //$NON-NLS-1$
	/** Preference key for matching brackets */
	public final static String MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$
	/** Preference key for matching brackets color */
	public final static String MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$
	/** Preference key for inactive code painter enablement */
	public static final String INACTIVE_CODE_ENABLE = "inactiveCodeEnable"; //$NON-NLS-1$
	/** Preference key for inactive code painter color */
	public static final String INACTIVE_CODE_COLOR = "inactiveCodeColor"; //$NON-NLS-1$
	/** Preference key for automatically closing strings */
	private final static String CLOSE_STRINGS = PreferenceConstants.EDITOR_CLOSE_STRINGS;
	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS = PreferenceConstants.EDITOR_CLOSE_BRACKETS;
	/** Preference key for automatically closing angular brackets */
	private final static String CLOSE_ANGULAR_BRACKETS = PreferenceConstants.EDITOR_CLOSE_ANGULAR_BRACKETS;

    /** Preference key for compiler task tags */
    private final static String TODO_TASK_TAGS = CCorePreferenceConstants.TODO_TASK_TAGS;

	/**
	 * This editor's projection support
	 */
	protected ProjectionSupport fProjectionSupport;
	/**
	 * This editor's projection model updater
	 */
	private ICFoldingStructureProvider fProjectionModelUpdater;

	/**
	 * The action group for folding.
	 */
	private FoldingActionGroup fFoldingGroup;

	/**
	 * AST reconciling listeners.
	 * @since 4.0
	 */
	private ListenerList fReconcilingListeners= new ListenerList(ListenerList.IDENTITY);

	/**
	 * Semantic highlighting manager
	 * @since 4.0
	 */
	private SemanticHighlightingManager fSemanticManager;

	/**
	 * True if editor is opening a large file.
	 * @since 5.0
	 */
	private boolean fEnableScalablilityMode = false;

	/**
	 * Flag indicating wheter the reconciler is currently running.
	 */
	private volatile boolean fIsReconciling;

	private CTemplatesPage fTemplatesPage;

	private static final Set<String> angularIntroducers = new HashSet<String>();
	static {
		angularIntroducers.add("template"); //$NON-NLS-1$
		angularIntroducers.add("vector"); //$NON-NLS-1$
		angularIntroducers.add("deque"); //$NON-NLS-1$
		angularIntroducers.add("list"); //$NON-NLS-1$
		angularIntroducers.add("slist"); //$NON-NLS-1$
		angularIntroducers.add("map"); //$NON-NLS-1$
		angularIntroducers.add("set"); //$NON-NLS-1$
		angularIntroducers.add("multimap"); //$NON-NLS-1$
		angularIntroducers.add("multiset"); //$NON-NLS-1$
		angularIntroducers.add("hash_map"); //$NON-NLS-1$
		angularIntroducers.add("hash_set"); //$NON-NLS-1$
		angularIntroducers.add("hash_multimap"); //$NON-NLS-1$
		angularIntroducers.add("hash_multiset"); //$NON-NLS-1$
		angularIntroducers.add("pair"); //$NON-NLS-1$
		angularIntroducers.add("include"); //$NON-NLS-1$
	}

	/**
	 * Default constructor.
	 */
	public CEditor() {
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());

		setEditorContextMenuId("#CEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#CEditorOutlinerContext"); //$NON-NLS-1$

		fCEditorErrorTickUpdater = new CEditorErrorTickUpdater(this);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor() {
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer= getSourceViewer();
		if (!(sourceViewer instanceof ISourceViewerExtension2)) {
			setPreferenceStore(createCombinedPreferenceStore(input));
			internalDoSetInput(input);
			updateScalabilityMode(input);
			return;
		}

		getDocumentProvider().connect(input);
		try {
			// uninstall & unregister preference store listener
			getSourceViewerDecorationSupport(sourceViewer).uninstall();
			((ISourceViewerExtension2) sourceViewer).unconfigure();

			setPreferenceStore(createCombinedPreferenceStore(input));
			updateScalabilityMode(input);

			// install & register preference store listener
			sourceViewer.configure(getSourceViewerConfiguration());
			getSourceViewerDecorationSupport(sourceViewer).install(getPreferenceStore());

			internalDoSetInput(input);
		} finally {
			getDocumentProvider().disconnect(input);
		}
	}

	private void internalDoSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer= getSourceViewer();
		CSourceViewer cSourceViewer= null;
		if (sourceViewer instanceof CSourceViewer) {
			cSourceViewer= (CSourceViewer) sourceViewer;
		}

		IPreferenceStore store= getPreferenceStore();
		if (cSourceViewer != null && isFoldingEnabled() && (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS)))
			cSourceViewer.prepareDelayedProjection();

		super.doSetInput(input);

		setOutlinePageInput(fOutlinePage, input);

		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.initialize();
		}
		if (fCEditorErrorTickUpdater != null) {
			fCEditorErrorTickUpdater.updateEditorImage(getInputCElement());
		}
		ICElement element= getInputCElement();
		if (element instanceof ITranslationUnit) {
			fBracketMatcher.configure(((ITranslationUnit) element).getLanguage());
		} else {
			fBracketMatcher.configure(null);
		}
	}

	private void updateScalabilityMode(IEditorInput input) {
		int lines = getDocumentProvider().getDocument(input).getNumberOfLines();
		boolean wasEnabled = fEnableScalablilityMode;
		fEnableScalablilityMode = lines > getPreferenceStore().getInt(PreferenceConstants.SCALABILITY_NUMBER_OF_LINES);
		if (fEnableScalablilityMode && !wasEnabled) {
			//Alert users that scalability mode should be turned on
			if (getPreferenceStore().getBoolean(PreferenceConstants.SCALABILITY_ALERT)) {
				MessageDialogWithToggle dialog = new MessageDialogWithToggle(
						Display.getCurrent().getActiveShell(),
						CEditorMessages.Scalability_info,  
						null,
						CEditorMessages.Scalability_message,  
						MessageDialog.INFORMATION,
						new String[] {IDialogConstants.OK_LABEL}, 0,
						CEditorMessages.Scalability_reappear,  
						false) {
					{
						setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.ON_TOP | getDefaultOrientation());
					}
					@Override
					protected void buttonPressed(int buttonId) {
						PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.SCALABILITY_ALERT, !getToggleState());
						super.buttonPressed(buttonId);
					}
				};
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @since 5.0
	 */
	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		SourceViewerConfiguration sourceViewerConfiguration= getSourceViewerConfiguration();
		if (!(sourceViewerConfiguration instanceof CSourceViewerConfiguration)) {
			CTextTools textTools= CUIPlugin.getDefault().getTextTools();
			setSourceViewerConfiguration(new CSourceViewerScalableConfiguration(textTools.getColorManager(), store, this, ICPartitions.C_PARTITIONING));
		}

		if (getSourceViewer() instanceof CSourceViewer)
			((CSourceViewer) getSourceViewer()).setPreferenceStore(store);

		fMarkOccurrenceAnnotations= store.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
		fStickyOccurrenceAnnotations= store.getBoolean(PreferenceConstants.EDITOR_STICKY_OCCURRENCES);
	}

	/**
	 * Update the title image.
     * @param image Title image.
	 */
	public void updatedTitleImage(Image image) {
		setTitleImage(image);
	}

	/**
	 * Returns the C element wrapped by this editors input.
	 *
	 * @return the C element wrapped by this editors input.
	 * @since 3.0
	 */
	public ICElement getInputCElement () {
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(getEditorInput());
	}

	/**
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
    @Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Gets the outline page of the c-editor.
     * @return Outline page.
	 */
	public CContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage = new CContentOutlinePage(this);
			fOutlinePage.addSelectionChangedListener(this);
		}
		setOutlinePageInput(fOutlinePage, getEditorInput());
		return fOutlinePage;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		} else if (required == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, IPageLayout.ID_RES_NAV };
				}
			};
		} else if (required == IShowInSource.class) {
			ICElement ce= null;
			ce= getElementAt(getSourceViewer().getSelectedRange().x, false);
			if (ce instanceof ITranslationUnit) {
				ce = null;
			}
			final ISelection selection= ce != null ? new StructuredSelection(ce) : null;
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					return new ShowInContext(getEditorInput(), selection);
				}
			};
		} else if (ProjectionAnnotationModel.class.equals(required)) {
			if (fProjectionSupport != null) {
				Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
				if (adapter != null)
					return adapter;
			}
		} else if (IContextProvider.class.equals(required)) {
			return new CUIHelp.CUIHelpContextProvider(this);
		} else if (IGotoMarker.class.equals(required)) {
			IGotoMarker gotoMarker= new GotoMarkerAdapter();
			return gotoMarker;
		} else if (ITemplatesPage.class.equals(required)) {
			if (fTemplatesPage == null) {
				fTemplatesPage = new CTemplatesPage(this);
				return fTemplatesPage;
			}
		}
		return super.getAdapter(required);
	}
	
	/**
	 * Handles a property change event describing a change
	 * of the editor's preference store and updates the preference
	 * related editor properties.
	 *
	 * @param event the property change event
	 */
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
			/*
			 * Ignore tab setting since we rely on the formatter preferences.
			 * We do this outside the try-finally block to avoid that EDITOR_TAB_WIDTH
			 * is handled by the base-class (AbstractDecoratedTextEditor).
			 */
			return;
		}
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(property)) {
			// Ignore spaces-for-tab setting since we rely on the formatter preferences.
			return;
		}

		try {
			final AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();

			if (asv != null) {

				boolean newBooleanValue= false;
				Object newValue= event.getNewValue();
				if (newValue != null)
					newBooleanValue= Boolean.valueOf(newValue.toString()).booleanValue();

				if (CLOSE_BRACKETS.equals(property)) {
					fBracketInserter.setCloseBracketsEnabled(newBooleanValue);
					return;
				}

				if (CLOSE_ANGULAR_BRACKETS.equals(property)) {
					fBracketInserter.setCloseAngularBracketsEnabled(newBooleanValue);
					return;
				}

				if (CLOSE_STRINGS.equals(property)) {
					fBracketInserter.setCloseStringsEnabled(newBooleanValue);
					return;
				}

				if (PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS.equals(property))
					updateHoverBehavior();

				((CSourceViewerConfiguration) getSourceViewerConfiguration()).handlePropertyChangeEvent(event);

				if (PreferenceConstants.EDITOR_SMART_TAB.equals(property)) {
					if (newBooleanValue) {
						setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
					} else {
						removeActionActivationCode("IndentOnTab"); //$NON-NLS-1$
					}
					return;
				}

				if (TODO_TASK_TAGS.equals(event.getProperty())) {
					ISourceViewer sourceViewer = getSourceViewer();
					if (sourceViewer != null && affectsTextPresentation(event))
						sourceViewer.invalidateTextPresentation();
					return;
				}

				if (PreferenceConstants.EDITOR_FOLDING_PROVIDER.equals(property)) {
					if (fProjectionModelUpdater != null) {
						fProjectionModelUpdater.uninstall();
					}
					// either freshly enabled or provider changed
					fProjectionModelUpdater = CUIPlugin.getDefault().getFoldingStructureProviderRegistry().getCurrentFoldingProvider();
					if (fProjectionModelUpdater != null) {
						fProjectionModelUpdater.install(this, asv);
					}
					return;
				}

				if (DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE.equals(property)
						|| DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE.equals(property)
						|| DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR.equals(property)) {
					StyledText textWidget= asv.getTextWidget();
					int tabWidth= getSourceViewerConfiguration().getTabWidth(asv);
					if (textWidget.getTabs() != tabWidth)
						textWidget.setTabs(tabWidth);
					uninstallTabsToSpacesConverter();
					if (isTabsToSpacesConversionEnabled()) {
						installTabsToSpacesConverter();
					} else {
						updateIndentationMode();
					}
					return;
				}

				if (PreferenceConstants.EDITOR_MARK_OCCURRENCES.equals(property)) {
					if (newBooleanValue != fMarkOccurrenceAnnotations) {
						fMarkOccurrenceAnnotations= newBooleanValue;
						if (!fMarkOccurrenceAnnotations)
							uninstallOccurrencesFinder();
						else
							installOccurrencesFinder(true);
					}
					return;
				}
				if (PreferenceConstants.EDITOR_STICKY_OCCURRENCES.equals(property)) {
					fStickyOccurrenceAnnotations= newBooleanValue;
					return;
				}

				if (SemanticHighlightings.affectsEnablement(getPreferenceStore(), event)
						|| (isEnableScalablilityMode() && PreferenceConstants.SCALABILITY_SEMANTIC_HIGHLIGHT.equals(property))) {
					if (isSemanticHighlightingEnabled()) {
						installSemanticHighlighting();
						fSemanticManager.refresh();
					} else {
						uninstallSemanticHighlighting();
					}
					return;
				}
				
				// For Scalability
				if (isEnableScalablilityMode()) {
					if (PreferenceConstants.SCALABILITY_RECONCILER.equals(property) ||
							PreferenceConstants.SCALABILITY_SYNTAX_COLOR.equals(property)) {
						BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
							public void run() {
								setOutlinePageInput(fOutlinePage, getEditorInput());
								asv.unconfigure();
								asv.configure(getSourceViewerConfiguration());
							}});
						return;
					}
				}
				
				IContentAssistant c = asv.getContentAssistant();
				if (c instanceof ContentAssistant) {
					ContentAssistPreference.changeConfiguration((ContentAssistant) c, getPreferenceStore(), event);
				}
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#initializeViewerColors(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	protected void initializeViewerColors(ISourceViewer viewer) {
		// is handled by CSourceViewer
	}

	/*
	 * Update the hovering behavior depending on the preferences.
	 */
	private void updateHoverBehavior() {
		SourceViewerConfiguration configuration= getSourceViewerConfiguration();
		String[] types= configuration.getConfiguredContentTypes(getSourceViewer());

		for (String t : types) {

			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer instanceof ITextViewerExtension2) {
				// Remove existing hovers
				((ITextViewerExtension2) sourceViewer).removeTextHovers(t);

				int[] stateMasks= configuration.getConfiguredTextHoverStateMasks(getSourceViewer(), t);

				if (stateMasks != null) {
					for (int stateMask : stateMasks) {
						ITextHover textHover= configuration.getTextHover(sourceViewer, t, stateMask);
						((ITextViewerExtension2) sourceViewer).setTextHover(textHover, t, stateMask);
					}
				} else {
					ITextHover textHover= configuration.getTextHover(sourceViewer, t);
					((ITextViewerExtension2) sourceViewer).setTextHover(textHover, t, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
				}
			} else {
				sourceViewer.setTextHover(configuration.getTextHover(sourceViewer, t), t);
			}
		}
	}

	/**
	 * React to changed selection in the editor.
	 *
	 * @since 3.0
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
	 * Computes and returns the source reference that includes the caret and
	 * serves as provider for the outline page selection and the editor range
	 * indication.
	 *
	 * @return the computed source reference
	 * @since 4.0
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
			ITextViewerExtension5 extension= (ITextViewerExtension5) sourceViewer;
			caret= extension.widgetOffset2ModelOffset(styledText.getSelection().x);
		} else {
			int offset= sourceViewer.getVisibleRegion().getOffset();
			caret= offset + styledText.getSelection().x;
		}

		ICElement element= getElementAt(caret, false);

		if (!(element instanceof ISourceReference))
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
		ITranslationUnit unit= (ITranslationUnit) getInputCElement();

		if (unit != null) {
			try {
				if (reconcile && unit instanceof IWorkingCopy) {
					synchronized (unit) {
						((IWorkingCopy) unit).reconcile();
					}
					return unit.getElementAtOffset(offset);
				} else if (unit.isStructureKnown() && unit.isConsistent() && !fIsReconciling) {
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
	 * Synchronizes the outline view selection with the given element
	 * position in the editor.
	 *
	 * @since 4.0
	 */
	protected void synchronizeOutlinePage() {
		if (fOutlinePage != null && fOutlinePage.isLinkingEnabled()) {
			fOutlinePage.removeSelectionChangedListener(this);
			fOutlinePage.synchronizeSelectionWithEditor();
			fOutlinePage.addSelectionChangedListener(this);
		}
	}

	/**
	 * React to changed selection in the outline view.
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
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

		if (element == null) {
			return;
		}

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
				// Binary elements return the first executable statement so we have to substract -1
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
				if (start > -1 && getSourceViewer() != null) {
					getSourceViewer().revealRange(start, length);
					getSourceViewer().setSelectedRange(start, length);
				}
				updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
			}
			return;
		} catch (IllegalArgumentException x) {
            // No information to the user
		} catch (BadLocationException e) {
            // No information to the user
		}

		if (moveCursor)
			resetHighlightRange();
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

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#installTabsToSpacesConverter()
	 * @since 4.0
	 */
	@Override
	protected void installTabsToSpacesConverter() {
		ISourceViewer sourceViewer= getSourceViewer();
		SourceViewerConfiguration config= getSourceViewerConfiguration();
		if (config != null && sourceViewer instanceof ITextViewerExtension7) {
			int tabWidth= config.getTabWidth(sourceViewer);
			TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
			tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
			IDocumentProvider provider= getDocumentProvider();
			if (provider instanceof CDocumentProvider) {
				CDocumentProvider cProvider= (CDocumentProvider) provider;
				tabToSpacesConverter.setLineTracker(cProvider.createLineTracker(getEditorInput()));
			} else {
				tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
			}
			((ITextViewerExtension7) sourceViewer).setTabsToSpacesConverter(tabToSpacesConverter);
			updateIndentationMode();
		}
	}

	private void updateIndentationMode() {
		ISourceViewer sourceViewer= getSourceViewer();
		if (sourceViewer instanceof CSourceViewer) {
			CSourceViewer cSourceVieer= (CSourceViewer) sourceViewer;
			ICElement element= getInputCElement();
			ICProject project= element == null ? null : element.getCProject();
			final int indentWidth= CodeFormatterUtil.getIndentWidth(project);
			final boolean useSpaces= isTabsToSpacesConversionEnabled();
			cSourceVieer.configureIndentation(indentWidth, useSpaces);
		}
		super.updateIndentPrefixes();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isTabsToSpacesConversionEnabled()
	 * @since 4.0
	 */
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		ICElement element= getInputCElement();
		ICProject project= element == null ? null : element.getCProject();
		String option;
		if (project == null)
			option= CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		else
			option= project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);
		return CCorePlugin.SPACE.equals(option);
	}

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    @Override
	public void dispose() {

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(fBracketInserter);

		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.uninstall();
			fProjectionModelUpdater = null;
		}

		if (fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport = null;
		}

		// cancel possible running computation
		fMarkOccurrenceAnnotations= false;
		uninstallOccurrencesFinder();

    	uninstallSemanticHighlighting();

		if (fCEditorErrorTickUpdater != null) {
			fCEditorErrorTickUpdater.dispose();
			fCEditorErrorTickUpdater = null;
		}

        if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher = null;
		}

		if (fOutlinePage != null) {
			fOutlinePage.dispose();
			fOutlinePage = null;
		}

		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup = null;
		}

		if (fTextSearchGroup != null) {
			fTextSearchGroup.dispose();
			fTextSearchGroup = null;
		}

		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup = null;
		}

		if (fOpenInViewGroup != null) {
			fOpenInViewGroup.dispose();
			fOpenInViewGroup = null;
		}

		if (fGenerateActionGroup != null) {
			fGenerateActionGroup.dispose();
			fGenerateActionGroup= null;
		}
		
		if (fSurroundWithActionGroup != null) {
			fSurroundWithActionGroup.dispose();
			fSurroundWithActionGroup= null;
		}
		
		if (fEditorSelectionChangedListener != null)  {
			fEditorSelectionChangedListener.uninstall(getSelectionProvider());
			fEditorSelectionChangedListener = null;
		}

		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#canHandleMove(org.eclipse.ui.IEditorInput, org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected boolean canHandleMove(IEditorInput originalElement, IEditorInput movedElement) {
		String oldLanguage = ""; //$NON-NLS-1$
		if (originalElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) originalElement).getFile();
			if (file != null) {
				IContentType type = CCorePlugin.getContentType(file.getProject(), file.getName());
				if (type != null) {
					oldLanguage = type.getId();
				}
				if (oldLanguage == null) {
					return false;
				}
			}
		}

		String newLanguage = ""; //$NON-NLS-1$
		if (movedElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) movedElement).getFile();
			if (file != null) {
				IContentType type = CCorePlugin.getContentType(file.getProject(), file.getName());
				if (type != null) {
					newLanguage = type.getId();
				}
				if (newLanguage == null) {
					return false;
				}
			}
		}
		return oldLanguage.equals(newLanguage);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();

		fFoldingGroup = new FoldingActionGroup(this, getSourceViewer());

		// Default text editing menu items
		IAction action= new GotoMatchingBracketAction(this);
		action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction(GotoMatchingBracketAction.GOTO_MATCHING_BRACKET, action);
		
		final ResourceBundle bundle = ConstructedCEditorMessages.getResourceBundle();
		action = new GotoNextBookmarkAction(bundle, "GotoNextBookmark.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_BOOKMARK);
		setAction(GotoNextBookmarkAction.NEXT_BOOKMARK, action);

		action = new FindWordAction(bundle, "FindWord.", this, getSourceViewer()); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_WORD);
		setAction(FindWordAction.FIND_WORD, action);
		markAsStateDependentAction(FindWordAction.FIND_WORD, true);
		markAsSelectionDependentAction(FindWordAction.FIND_WORD, true);

		action = new ToggleCommentAction(bundle, "ToggleComment.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.TOGGLE_COMMENT);
		setAction("ToggleComment", action); //$NON-NLS-1$
		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
		configureToggleCommentAction();

		action = new AddBlockCommentAction(bundle, "AddBlockComment.", this);  //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
		setAction("AddBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$
		//WorkbenchHelp.setHelp(action, ICHelpContextIds.ADD_BLOCK_COMMENT_ACTION);

		action = new RemoveBlockCommentAction(bundle, "RemoveBlockComment.", this);  //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
		setAction("RemoveBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		//WorkbenchHelp.setHelp(action, ICHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);

		action = new IndentAction(bundle, "Indent.", this, false); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.INDENT);
		setAction("Indent", action); //$NON-NLS-1$
		markAsStateDependentAction("Indent", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(action, ICHelpContextIds.INDENT_ACTION);

		action = new IndentAction(bundle, "Indent.", this, true); //$NON-NLS-1$
		setAction("IndentOnTab", action); //$NON-NLS-1$
		markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
		markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$

		if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
			setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
		}

		action = new TextOperationAction(bundle, "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$

		action = new SortLinesAction(this);
		action.setActionDefinitionId(ICEditorActionDefinitionIds.SORT_LINES);
		setAction("SortLines", action); //$NON-NLS-1$
		markAsStateDependentAction("SortLines", true); //$NON-NLS-1$
		markAsSelectionDependentAction("SortLines", true); //$NON-NLS-1$

		action = new ContentAssistAction(bundle, "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$

		action= new TextOperationAction(bundle, "ContentAssistContextInformation.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistContextInformation", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistContextInformation", true); //$NON-NLS-1$

        action = new TextOperationAction(bundle, "OpenOutline.", this, CSourceViewer.SHOW_OUTLINE, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_OUTLINE);
        setAction("OpenOutline", action); //$NON-NLS-1$*/

        action = new TextOperationAction(bundle, "OpenHierarchy.", this, CSourceViewer.SHOW_HIERARCHY, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_QUICK_TYPE_HIERARCHY);
        setAction("OpenHierarchy", action); //$NON-NLS-1$*/

        action = new GoToNextPreviousMemberAction(bundle, "GotoNextMember.", this, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
        setAction(GoToNextPreviousMemberAction.PREVIOUS_MEMBER, action);

        action = new GoToNextPreviousMemberAction(bundle, "GotoPreviousMember.", this, false); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);
        setAction(GoToNextPreviousMemberAction.NEXT_MEMBER, action);

        action= new ToggleSourceAndHeaderAction(bundle, "ToggleSourceHeader.", this); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.TOGGLE_SOURCE_HEADER);
        setAction("ToggleSourceHeader", action); //$NON-NLS-1$

        action = new TextOperationAction(bundle, "OpenMacroExplorer.", this, CSourceViewer.SHOW_MACRO_EXPLORER, true); //$NON-NLS-1$
        action.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_QUICK_MACRO_EXPLORER);
        setAction("OpenMacroExplorer", action); //$NON-NLS-1$*/

        // Assorted action groupings
		fSelectionSearchGroup = createSelectionSearchGroup();
		fTextSearchGroup= new TextSearchGroup(this);
		fRefactoringActionGroup= new CRefactoringActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
		fOpenInViewGroup= createOpenViewActionGroup();
		fGenerateActionGroup= new GenerateActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
		fSurroundWithActionGroup = new SurroundWithActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
		
		action = getAction(ITextEditorActionConstants.SHIFT_RIGHT);
		if (action != null) {
			action.setId(ITextEditorActionConstants.SHIFT_RIGHT);
			CPluginImages.setImageDescriptors(action, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_RIGHT);
		}
		action = getAction(ITextEditorActionConstants.SHIFT_LEFT);
		if (action != null) {
			action.setId(ITextEditorActionConstants.SHIFT_LEFT);
			CPluginImages.setImageDescriptors(action, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_LEFT);
		}
	}

	protected ActionGroup createSelectionSearchGroup() {
		return new SelectionSearchGroup(this);
	}
	
	protected ActionGroup createOpenViewActionGroup() {
		return new OpenViewActionGroup(this);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		// marker for contributions to the top
		menu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
		// separator for debug related actions (similar to ruler context menu)
		menu.add(new Separator(IContextMenuConstants.GROUP_DEBUG));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_DEBUG+".end")); //$NON-NLS-1$

		super.editorContextMenuAboutToShow(menu);

		// remove shift actions added by base class
		menu.remove(ITextEditorActionConstants.SHIFT_LEFT);
		menu.remove(ITextEditorActionConstants.SHIFT_RIGHT);

		menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(IContextMenuConstants.GROUP_SHOW));

		final boolean hasCElement= getInputCElement() != null;
		if (hasCElement) {
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenDeclarations"); //$NON-NLS-1$
	        addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenDefinition"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenTypeHierarchy"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenCallHierarchy"); //$NON-NLS-1$

			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenOutline"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenHierarchy"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "OpenMacroExplorer"); //$NON-NLS-1$
			addAction(menu, IContextMenuConstants.GROUP_OPEN, "ToggleSourceHeader"); //$NON-NLS-1$
		}

		ActionContext context= new ActionContext(getSelectionProvider().getSelection());
		fGenerateActionGroup.setContext(context);
		fGenerateActionGroup.fillContextMenu(menu);
		fGenerateActionGroup.setContext(null);

		fSurroundWithActionGroup.setContext(context);
		fSurroundWithActionGroup.fillContextMenu(menu);
		fSurroundWithActionGroup.setContext(null);
		
		if (hasCElement) {
			fSelectionSearchGroup.fillContextMenu(menu);
		}
		fTextSearchGroup.fillContextMenu(menu);

		if (hasCElement) {
			fRefactoringActionGroup.fillContextMenu(menu);
			fOpenInViewGroup.fillContextMenu(menu);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#rulerContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		IMenuManager foldingMenu= new MenuManager(CEditorMessages.CEditor_menu_folding, "projection");  //$NON-NLS-1$
		menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);

		IAction action= getAction("FoldingToggle"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingExpandAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingCollapseAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action= getAction("FoldingRestore"); //$NON-NLS-1$
		foldingMenu.add(action);
	}

	/**
     * Sets an input for the outline page.
	 * @param page Page to set the input.
	 * @param input Input to set.
	 */
	public static void setOutlinePageInput(CContentOutlinePage page, IEditorInput input) {
		if (page != null) {
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			page.setInput(manager.getWorkingCopy(input));
		}
	}

	/**
     * Determines if folding is enabled.
	 * @return <code>true</code> if folding is enabled, <code>false</code> otherwise.
	 */
	boolean isFoldingEnabled() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
	}

	/*
	 * @see org.eclipse.ui.part.WorkbenchPart#getOrientation()
	 * @since 4.0
	 */
	@Override
	public int getOrientation() {
		// C/C++ editors are always left to right by default
		return SWT.LEFT_TO_RIGHT;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// bug 291008 - register custom help listener
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
		parent.addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				IContextProvider provider = (IContextProvider) CEditor.this.getAdapter(IContextProvider.class);
				if (provider != null) {
					IContext context = provider.getContext(CEditor.this);
					if (context != null) {
						helpSystem.displayHelp(context);
						return;
					}
				}
				helpSystem.displayHelp(ICHelpContextIds.CEDITOR_VIEW);
			}});
		
		fEditorSelectionChangedListener = new EditorSelectionChangedListener();
		fEditorSelectionChangedListener.install(getSelectionProvider());

		if (isSemanticHighlightingEnabled())
			installSemanticHighlighting();

		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean closeBrackets = preferenceStore.getBoolean(CLOSE_BRACKETS);
		boolean closeAngularBrackets = preferenceStore.getBoolean(CLOSE_ANGULAR_BRACKETS);
		boolean closeStrings = preferenceStore.getBoolean(CLOSE_STRINGS);

		fBracketInserter.setCloseBracketsEnabled(closeBrackets);
		fBracketInserter.setCloseStringsEnabled(closeStrings);
		fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(fBracketInserter);

		if (isMarkingOccurrences())
			installOccurrencesFinder(false);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getSourceViewerDecorationSupport(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(ISourceViewer viewer) {
		if (fSourceViewerDecorationSupport == null) {
			fSourceViewerDecorationSupport= new CSourceViewerDecorationSupport(this, viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors());
			configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
		}
		return fSourceViewerDecorationSupport;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#configureSourceViewerDecorationSupport(org.eclipse.ui.texteditor.SourceViewerDecorationSupport)
	 */
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		//Enhance the stock source viewer decorator with a bracket matcher
		support.setCharacterPairMatcher(fBracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);
		((CSourceViewerDecorationSupport) support).setInactiveCodePainterPreferenceKeys(INACTIVE_CODE_ENABLE, INACTIVE_CODE_COLOR);
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel the annotation model
	 * @return the annotation model's lock object
	 * @since 5.0
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock= ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {

		ISourceViewer sourceViewer = getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection = getSignedSelection(sourceViewer);

		int selectionLength = Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(CEditorMessages.GotoMatchingBracket_error_invalidSelection);	
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		// #26314
		int sourceCaretOffset = selection.getOffset() + selection.getLength();
		if (isSurroundedByBrackets(document, sourceCaretOffset))
			sourceCaretOffset -= selection.getLength();

		IRegion region = fBracketMatcher.match(document, sourceCaretOffset);
		if (region == null) {
			setStatusLineErrorMessage(CEditorMessages.GotoMatchingBracket_error_noMatchingBracket);	
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		int offset = region.getOffset();
		int length = region.getLength();

		if (length < 1)
			return;

		int anchor = fBracketMatcher.getAnchor();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
		int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1: offset + length;

		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(CEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);	
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() > 0)
			targetOffset -= selection.getLength();

		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	protected void updateStatusLine() {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
		Annotation annotation = getAnnotation(selection.getOffset(), selection.getLength(), fSyncProblemsViewMarker);
		setStatusLineErrorMessage(null);
		setStatusLineMessage(null);
		if (annotation != null) {
			if (fSyncProblemsViewMarker == null) {
				updateMarkerViews(annotation);
			}
			if (annotation instanceof ICAnnotation && ((ICAnnotation) annotation).isProblem())
				setStatusLineMessage(annotation.getText());
		}
		fSyncProblemsViewMarker = null;
	}

	/**
	 * Returns the annotation overlapping with the given range or <code>null</code>.
	 *
	 * @param offset the region offset
	 * @param length the region length
	 * @param marker associated marker or <code>null</code> of not available
	 * @return the found annotation or <code>null</code>
	 */
	private Annotation getAnnotation(int offset, int length, IMarker marker) {
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		if (model == null)
			return null;
		
		@SuppressWarnings("rawtypes")
		Iterator parent;
		if (model instanceof IAnnotationModelExtension2) {
			parent= ((IAnnotationModelExtension2) model).getAnnotationIterator(offset, length, true, true);
		} else {
			parent= model.getAnnotationIterator();
		}

		@SuppressWarnings("unchecked")
		Iterator<Annotation> e= new CAnnotationIterator(parent, false);
		Annotation annotation = null;
		while (e.hasNext()) {
			Annotation a = e.next();
			if (!isNavigationTarget(a))
				continue;
			
			Position p = model.getPosition(a);
			if (p != null && p.overlapsWith(offset, length)) {
				if (annotation == null) {
					annotation = a;
					if (marker == null)
						break;
				}
				if (a instanceof MarkerAnnotation) {
					if (((MarkerAnnotation) a).getMarker().equals(marker)) {
						annotation = a;
						break;
					}
				}
			}
		}
		
		return annotation;
	}

	/*
	 * Get the dektop's StatusLineManager
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
	 * Configures the toggle comment action
	 *
	 * @since 4.0.0
	 */
	private void configureToggleCommentAction() {
		IAction action = getAction("ToggleComment"); //$NON-NLS-1$
		if (action instanceof ToggleCommentAction) {
			ISourceViewer sourceViewer = getSourceViewer();
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			((ToggleCommentAction) action).configure(sourceViewer, configuration);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createNavigationActions()
	 */
	@Override
	protected void createNavigationActions() {
		super.createNavigationActions();

		final StyledText textWidget = getSourceViewer().getTextWidget();

		IAction action = new NavigatePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

		action = new NavigateNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

		action = new SelectPreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT, SWT.NULL);

		action = new SelectNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT, SWT.NULL);

		action = new DeletePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.BS, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, true);

		action = new DeleteNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.DEL, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, true);
	}

	public final ISourceViewer getViewer() {
		return getSourceViewer();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		IPreferenceStore store= getPreferenceStore();
		ISourceViewer sourceViewer =
				new AdaptedSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);

		CSourceViewer cSourceViewer= null;
		if (sourceViewer instanceof CSourceViewer) {
			cSourceViewer= (CSourceViewer) sourceViewer;
		}

		/*
		 * This is a performance optimization to reduce the computation of
		 * the text presentation triggered by {@link #setVisibleDocument(IDocument)}
		 */
		if (cSourceViewer != null && isFoldingEnabled() && (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS)))
			cSourceViewer.prepareDelayedProjection();

		ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;

		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.search.results"); //$NON-NLS-1$
		fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				return new SourceViewerInformationControl(shell, false, getOrientation(), null);
			}
		});
		fProjectionSupport.install();

		fProjectionModelUpdater = CUIPlugin.getDefault().getFoldingStructureProviderRegistry().getCurrentFoldingProvider();
		if (fProjectionModelUpdater != null)
			fProjectionModelUpdater.install(this, projectionViewer);

		if (isFoldingEnabled())
			projectionViewer.doOperation(ProjectionViewer.TOGGLE);

		getSourceViewerDecorationSupport(sourceViewer);

		return sourceViewer;
	}

	/** Outliner context menu Id */
	protected String fOutlinerContextMenuId;

	/**
	 * Holds the current occurrence annotations.
	 * @since 5.0
	 */
	private Annotation[] fOccurrenceAnnotations= null;
	/**
	 * Tells whether all occurrences of the element at the
	 * current caret location are automatically marked in
	 * this editor.
	 * @since 5.0
	 */
	private boolean fMarkOccurrenceAnnotations;
	/**
	 * Tells whether the occurrence annotations are sticky
	 * i.e. whether they stay even if there's no valid Java
	 * element at the current caret position.
	 * Only valid if {@link #fMarkOccurrenceAnnotations} is <code>true</code>.
	 * @since 5.0
	 */
	private boolean fStickyOccurrenceAnnotations;
	/**
	 * The selection used when forcing occurrence marking
	 * through code.
	 * @since 5.0
	 */
	private ISelection fForcedMarkOccurrencesSelection;
	/**
	 * The document modification stamp at the time when the last
	 * occurrence marking took place.
	 * @since 5.0
	 */
	private long fMarkOccurrenceModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	/**
	 * The region of the word under the caret used to when
	 * computing the current occurrence markings.
	 * @since 5.0
	 */
	private IRegion fMarkOccurrenceTargetRegion;

	private OccurrencesAnnotationUpdaterJob fOccurrencesAnnotationUpdaterJob;
	private OccurrencesFinderJobCanceler fOccurrencesFinderJobCanceler;
	private ISelectionListenerWithAST fPostSelectionListenerWithAST;

	/**
	 * Sets the outliner's context menu ID.
	 */
	protected void setOutlinerContextMenuId(String menuId) {
		fOutlinerContextMenuId = menuId;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
	 */
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String [] { "org.eclipse.cdt.ui.cEditorScope" }); //$NON-NLS-1$
	}

	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 */
	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof CSourceViewerConfiguration) {
			return ((CSourceViewerConfiguration) configuration).affectsTextPresentation(event);
		}
		return false;
	}

	/**
	 * Returns the folding action group, or <code>null</code> if there is none.
	 *
	 * @return the folding action group, or <code>null</code> if there is none
	 */
	protected FoldingActionGroup getFoldingActionGroup() {
		return fFoldingGroup;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performRevert()
	 */
	@Override
	protected void performRevert() {
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {

			boolean projectionMode = projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.uninstall();
			}

			super.performRevert();

			if (projectionMode) {
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.install(this, projectionViewer);
				projectionViewer.enableProjection();
			}

		} finally {
			projectionViewer.setRedraw(true);
		}
	}

    /**
     * Sets the given message as error message to this editor's status line.
     *
     * @param msg message to be set
     */
    @Override
	protected void setStatusLineErrorMessage(String msg) {
    	IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
    	if (statusLine != null)
    		statusLine.setMessage(true, msg, null);
    }

	/**
	 * Sets the given message as message to this editor's status line.
	 *
	 * @param msg message to be set
	 * @since 3.0
	 */
	@Override
	protected void setStatusLineMessage(String msg) {
		IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(false, msg, null);
	}

	/**
	 * Returns the signed current selection.
	 * The length will be negative if the resulting selection
	 * is right-to-left (RtoL).
	 * <p>
	 * The selection offset is model based.
	 * </p>
	 *
	 * @param sourceViewer the source viewer
	 * @return a region denoting the current signed selection, for a resulting RtoL selections length is < 0
	 */
	protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
		StyledText text = sourceViewer.getTextWidget();
		Point selection = text.getSelectionRange();

		if (text.getCaretOffset() == selection.x) {
			selection.x = selection.x + selection.y;
			selection.y = -selection.y;
		}

		selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);

		return new Region(selection.x, selection.y);
	}

	private static boolean isBracket(char character) {
		for (int i = 0; i != BRACKETS.length; ++i) {
			if (character == BRACKETS[i])
				return true;
		}
		return false;
	}

	private static boolean isSurroundedByBrackets(IDocument document, int offset) {
		if (offset == 0 || offset == document.getLength())
			return false;

		try {
			return isBracket(document.getChar(offset - 1)) &&
					isBracket(document.getChar(offset));
		} catch (BadLocationException e) {
			return false;
		}
	}

	private static char getEscapeCharacter(char character) {
		switch (character) {
			case '"':
			case '\'':
				return '\\';
			default:
				return 0;
		}
	}

	private static char getPeerCharacter(char character) {
		switch (character) {
			case '(':
				return ')';

			case ')':
				return '(';

			case '<':
				return '>';

			case '>':
				return '<';

			case '[':
				return ']';

			case ']':
				return '[';

			case '"':
				return character;

			case '\'':
				return character;

			default:
				throw new IllegalArgumentException();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#collectContextMenuPreferencePages()
	 */
	@Override
	protected String[] collectContextMenuPreferencePages() {
		// Add C/C++ Editor relevant pages
		String[] parentPrefPageIds = super.collectContextMenuPreferencePages();
		String[] prefPageIds = new String[parentPrefPageIds.length + 12];
		int nIds = 0;
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CEditorPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeAssistPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeAssistPreferenceAdvanced"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.HoverPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.FoldingPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.MarkOccurrencesPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeColoringPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.TemplatePreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.SmartTypingPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CodeFormatterPreferencePage"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.CScalabilityPreferences"; //$NON-NLS-1$
		prefPageIds[nIds++] = "org.eclipse.cdt.ui.preferences.SaveActionsPreferencePage"; //$NON-NLS-1$
		System.arraycopy(parentPrefPageIds, 0, prefPageIds, nIds, parentPrefPageIds.length);
		return prefPageIds;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 * @since 4.0
	 */
	public void aboutToBeReconciled() {
		fIsReconciling= true;

		// Notify AST provider
		CUIPlugin.getDefault().getASTProvider().aboutToBeReconciled(getInputCElement());

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length= listeners.length; i < length; ++i) {
			((ICReconcilingListener) listeners[i]).aboutToBeReconciled();
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, boolean, IProgressMonitor)
	 * @since 4.0
	 */
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		fIsReconciling= false;

		CUIPlugin cuiPlugin= CUIPlugin.getDefault();
		if (cuiPlugin == null)
			return;

		// Always notify AST provider
		cuiPlugin.getASTProvider().reconciled(ast, getInputCElement(), progressMonitor);

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length= listeners.length; i < length; ++i) {
			((ICReconcilingListener) listeners[i]).reconciled(ast, force, progressMonitor);
		}
	}

	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	The reconcile listener to be added
	 * @since 4.0
	 */
	final public void addReconcileListener(ICReconcilingListener listener) {
		fReconcilingListeners.add(listener);
	}

	/**
	 * Removes the given listener.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener	the reconcile listener to be removed
	 * @since 4.0
	 */
	final public void removeReconcileListener(ICReconcilingListener listener) {
		fReconcilingListeners.remove(listener);
	}

	/**
	 * @return <code>true</code> if Semantic Highlighting is enabled.
	 *
	 * @since 4.0
	 */
	private boolean isSemanticHighlightingEnabled() {
		return SemanticHighlightings.isEnabled(getPreferenceStore()) && !(isEnableScalablilityMode() && getPreferenceStore().getBoolean(PreferenceConstants.SCALABILITY_SEMANTIC_HIGHLIGHT));
	}

	/**
	 * Install Semantic Highlighting.
	 *
	 * @since 4.0
	 */
	private void installSemanticHighlighting() {
		if (fSemanticManager == null) {
			fSemanticManager= new SemanticHighlightingManager();
			fSemanticManager.install(this, (CSourceViewer) getSourceViewer(), CUIPlugin.getDefault().getTextTools().getColorManager(), getPreferenceStore());
		}
	}

	/**
	 * Uninstall Semantic Highlighting.
	 *
	 * @since 4.0
	 */
	private void uninstallSemanticHighlighting() {
		if (fSemanticManager != null) {
			fSemanticManager.uninstall();
			fSemanticManager= null;
		}
	}

	/**
	 * Called whenever the editor is activated and allows for registering
	 * action handlers.
	 */
	public void fillActionBars(IActionBars actionBars) {
		fOpenInViewGroup.fillActionBars(actionBars);
		fRefactoringActionGroup.fillActionBars(actionBars);
		fGenerateActionGroup.fillActionBars(actionBars);
		fFoldingGroup.updateActionBars();
		fSurroundWithActionGroup.fillActionBars(actionBars);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updateStateDependentActions()
	 */
	@Override
	protected void updateStateDependentActions() {
		super.updateStateDependentActions();
		fGenerateActionGroup.editorStateChanged();
	}

	/**
	 * Resets the foldings structure according to the folding
	 * preferences.
	 *
	 * @since 4.0
	 */
	public void resetProjection() {
		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.initialize();
		}
	}

	/**
	 * Updates occurrence annotations.
	 *
	 * @since 5.0
	 */
	class OccurrencesAnnotationUpdaterJob extends Job {

		private final IDocument fDocument;
		private final ISelection fSelection;
		private final ISelectionValidator fPostSelectionValidator;
		private boolean fCanceled= false;
		private final OccurrenceLocation[] fLocations;

		public OccurrencesAnnotationUpdaterJob(IDocument document, OccurrenceLocation[] locations, ISelection selection, ISelectionValidator validator) {
			super(CEditorMessages.CEditor_markOccurrences_job_name); 
			fDocument= document;
			fSelection= selection;
			fLocations= locations;
			fPostSelectionValidator= validator;
		}

		// cannot use cancel() because it is declared final
		void doCancel() {
			fCanceled= true;
			cancel();
		}

		private boolean isCanceled(IProgressMonitor progressMonitor) {
			return fCanceled || progressMonitor.isCanceled()
				||  fPostSelectionValidator != null && !(fPostSelectionValidator.isValid(fSelection) || fForcedMarkOccurrencesSelection == fSelection)
				|| LinkedModeModel.hasInstalledModel(fDocument);
		}

		/*
		 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public IStatus run(IProgressMonitor progressMonitor) {
			if (isCanceled(progressMonitor))
				return Status.CANCEL_STATUS;

			ITextViewer textViewer= getViewer();
			if (textViewer == null)
				return Status.CANCEL_STATUS;

			IDocument document= textViewer.getDocument();
			if (document == null)
				return Status.CANCEL_STATUS;

			IDocumentProvider documentProvider= getDocumentProvider();
			if (documentProvider == null)
				return Status.CANCEL_STATUS;

			IAnnotationModel annotationModel= documentProvider.getAnnotationModel(getEditorInput());
			if (annotationModel == null)
				return Status.CANCEL_STATUS;

			// Add occurrence annotations
			int length= fLocations.length;
			Map<Annotation, Position> annotationMap= new HashMap<Annotation, Position>(length);
			for (int i= 0; i < length; i++) {
				if (isCanceled(progressMonitor))
					return Status.CANCEL_STATUS;

				OccurrenceLocation location= fLocations[i];
				Position position= new Position(location.getOffset(), location.getLength());

				String description= location.getDescription();
				String annotationType= "org.eclipse.cdt.ui.occurrences"; //$NON-NLS-1$

				annotationMap.put(new Annotation(annotationType, false, description), position);
			}

			if (isCanceled(progressMonitor))
				return Status.CANCEL_STATUS;

			synchronized (getLockObject(annotationModel)) {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, annotationMap);
				} else {
					removeOccurrenceAnnotations();
					Iterator<Map.Entry<Annotation, Position>> iter= annotationMap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry<Annotation, Position> mapEntry= iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
				fOccurrenceAnnotations= annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
			}

			return Status.OK_STATUS;
		}
	}

	/**
	 * Cancels the occurrences finder job upon document changes.
	 *
	 * @since 5.0
	 */
	class OccurrencesFinderJobCanceler implements IDocumentListener, ITextInputListener {

		public void install() {
			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;

			StyledText text= sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			sourceViewer.addTextInputListener(this);

			IDocument document= sourceViewer.getDocument();
			if (document != null)
				document.addDocumentListener(this);
		}

		public void uninstall() {
			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer != null)
				sourceViewer.removeTextInputListener(this);

			IDocumentProvider documentProvider= getDocumentProvider();
			if (documentProvider != null) {
				IDocument document= documentProvider.getDocument(getEditorInput());
				if (document != null)
					document.removeDocumentListener(this);
			}
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (fOccurrencesAnnotationUpdaterJob != null)
				fOccurrencesAnnotationUpdaterJob.doCancel();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput == null)
				return;

			oldInput.removeDocumentListener(this);
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (newInput == null)
				return;
			newInput.addDocumentListener(this);
		}
}

	/**
	 * Updates the occurrences annotations based
	 * on the current selection.
	 *
	 * @param selection the text selection
	 * @param astRoot the compilation unit AST
	 * @since 5.0
	 */
	protected void updateOccurrenceAnnotations(ITextSelection selection, IASTTranslationUnit astRoot) {
		if (fOccurrencesAnnotationUpdaterJob != null)
			fOccurrencesAnnotationUpdaterJob.cancel();

		if (!fMarkOccurrenceAnnotations)
			return;

		if (astRoot == null || selection == null)
			return;

		IDocument document= getSourceViewer().getDocument();
		if (document == null)
			return;

		ISelectionValidator validator= null;
		if (fForcedMarkOccurrencesSelection != selection && getSelectionProvider() instanceof ISelectionValidator) {
			validator= (ISelectionValidator) getSelectionProvider();
			if (!validator.isValid(selection)) {
				return;
			}
		}

		boolean hasChanged= false;
		if (document instanceof IDocumentExtension4) {
			int offset= selection.getOffset();
			long currentModificationStamp= ((IDocumentExtension4) document).getModificationStamp();
			IRegion markOccurrenceTargetRegion= fMarkOccurrenceTargetRegion;
			hasChanged= currentModificationStamp != fMarkOccurrenceModificationStamp;
			if (markOccurrenceTargetRegion != null && !hasChanged) {
				if (markOccurrenceTargetRegion.getOffset() <= offset && offset <= markOccurrenceTargetRegion.getOffset() + markOccurrenceTargetRegion.getLength())
					return;
			}
			fMarkOccurrenceTargetRegion= CWordFinder.findWord(document, offset);
			fMarkOccurrenceModificationStamp= currentModificationStamp;
		}

		OccurrenceLocation[] locations= null;

		IASTNodeSelector selector= astRoot.getNodeSelector(null);
		IASTName name= selector.findEnclosingName(selection.getOffset(), selection.getLength());
		if (name == null)
			name = selector.findEnclosingImplicitName(selection.getOffset(), selection.getLength());

		if (validator != null && !validator.isValid(selection)) {
			return;
		}

		if (name != null) {
//			try {
//				IASTFileLocation location= name.getFileLocation();
//				if (!document.get(location.getNodeOffset(), location.getNodeLength()).equals(name.toString())) {
//					System.err.println("CEditor.updateOccurrenceAnnotations() invalid ast");
//					return;
//				}
//			} catch (BadLocationException exc) {
//			}
			IBinding binding= name.resolveBinding();
			if (binding != null) {
				OccurrencesFinder occurrencesFinder= new OccurrencesFinder();
				if (occurrencesFinder.initialize(astRoot, name) == null) {
					locations= occurrencesFinder.getOccurrences();
				}
			}
		}

		if (locations == null || locations.length == 0) {
			if (!fStickyOccurrenceAnnotations)
				removeOccurrenceAnnotations();
			else if (hasChanged) // check consistency of current annotations
				removeOccurrenceAnnotations();
			return;
		}

		fOccurrencesAnnotationUpdaterJob= new OccurrencesAnnotationUpdaterJob(document, locations, selection, validator);
		// we are already in a background job
		//fOccurrencesFinderJob.setPriority(Job.DECORATE);
		//fOccurrencesFinderJob.setSystem(true);
		//fOccurrencesFinderJob.schedule();
		fOccurrencesAnnotationUpdaterJob.run(new NullProgressMonitor());
	}

	protected void installOccurrencesFinder(boolean forceUpdate) {
		fMarkOccurrenceAnnotations= true;

		fPostSelectionListenerWithAST= new ISelectionListenerWithAST() {
			public void selectionChanged(IEditorPart part, ITextSelection selection, IASTTranslationUnit astRoot) {
				updateOccurrenceAnnotations(selection, astRoot);
			}
		};
		SelectionListenerWithASTManager.getDefault().addListener(this, fPostSelectionListenerWithAST);
		if (forceUpdate && getSelectionProvider() != null) {
			fForcedMarkOccurrencesSelection= getSelectionProvider().getSelection();
			ASTProvider.getASTProvider().runOnAST(getInputCElement(), ASTProvider.WAIT_NO, getProgressMonitor(), new ASTRunnable() {
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					updateOccurrenceAnnotations((ITextSelection) fForcedMarkOccurrencesSelection, ast);
					return Status.OK_STATUS;
				}});
		}

		if (fOccurrencesFinderJobCanceler == null) {
			fOccurrencesFinderJobCanceler= new OccurrencesFinderJobCanceler();
			fOccurrencesFinderJobCanceler.install();
		}
	}

	protected void uninstallOccurrencesFinder() {
		fMarkOccurrenceAnnotations= false;

		if (fOccurrencesAnnotationUpdaterJob != null) {
			fOccurrencesAnnotationUpdaterJob.cancel();
			fOccurrencesAnnotationUpdaterJob= null;
		}

		if (fOccurrencesFinderJobCanceler != null) {
			fOccurrencesFinderJobCanceler.uninstall();
			fOccurrencesFinderJobCanceler= null;
		}

		if (fPostSelectionListenerWithAST != null) {
			SelectionListenerWithASTManager.getDefault().removeListener(this, fPostSelectionListenerWithAST);
			fPostSelectionListenerWithAST= null;
		}

		removeOccurrenceAnnotations();
	}

	protected boolean isMarkingOccurrences() {
		IPreferenceStore store= getPreferenceStore();
		return store != null && store.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
	}

	void removeOccurrenceAnnotations() {
		fMarkOccurrenceModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		fMarkOccurrenceTargetRegion= null;

		IDocumentProvider documentProvider= getDocumentProvider();
		if (documentProvider == null)
			return;

		IAnnotationModel annotationModel= documentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
			} else {
				for (Annotation occurrenceAnnotation : fOccurrenceAnnotations)
					annotationModel.removeAnnotation(occurrenceAnnotation);
			}
			fOccurrenceAnnotations= null;
		}
	}

	/**
	 * Creates and returns the preference store for this editor with the given input.
	 *
	 * @param input The editor input for which to create the preference store
	 * @return the preference store for this editor
	 */
	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List<IPreferenceStore> stores= new ArrayList<IPreferenceStore>(3);

		ICProject project= EditorUtility.getCProject(input);
		if (project != null) {
			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project.getProject()), CCorePlugin.PLUGIN_ID));
		}

		stores.add(CUIPlugin.getDefault().getPreferenceStore());
		stores.add(new ScopedPreferenceStore(new InstanceScope(), CCorePlugin.PLUGIN_ID));
		stores.add(EditorsUI.getPreferenceStore());

		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}

	/**
	 * @return <code>true</code> if parser based Content Assist proposals are disabled.
	 *
	 * @since 5.0
	 */
	public boolean isParserBasedContentAssistDisabled() {
		return getPreferenceStore().getBoolean(PreferenceConstants.SCALABILITY_PARSER_BASED_CONTENT_ASSIST);
	}
	
	/**
	 * @return <code>true</code> if Content Assist auto activation is disabled.
	 *
	 * @since 5.0
	 */
	public boolean isContentAssistAutoActivartionDisabled() {
		return getPreferenceStore().getBoolean(PreferenceConstants.SCALABILITY_CONTENT_ASSIST_AUTO_ACTIVATION);
	}
	
	/**
	 * @return <code>true</code> if the number of lines in the file exceed
	 * the line number for scalability mode in the preference.
	 *
	 * @since 5.0
	 */
	public boolean isEnableScalablilityMode() {
		return fEnableScalablilityMode;
	}

	@Override
	protected boolean isPrefQuickDiffAlwaysOn() {
		// enable only if not in scalability mode
		// workaround for http://bugs.eclipse.org/75555
		return super.isPrefQuickDiffAlwaysOn() && !isEnableScalablilityMode();
	}
}
