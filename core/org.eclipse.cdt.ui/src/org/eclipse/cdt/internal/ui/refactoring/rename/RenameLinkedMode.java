/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.EditorHighlightingSynchronizer;
import org.eclipse.cdt.internal.ui.search.LinkedNamesFinder;
import org.eclipse.cdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;

public class RenameLinkedMode {

	private class FocusEditingSupport implements IEditingSupport {
		public boolean ownsFocusShell() {
			if (fInfoPopup == null)
				return false;
			if (fInfoPopup.ownsFocusShell()) {
				return true;
			}

			Shell editorShell= fEditor.getSite().getShell();
			Shell activeShell= editorShell.getDisplay().getActiveShell();
			if (editorShell == activeShell)
				return true;
			return false;
		}

		public boolean isOriginator(DocumentEvent event, IRegion subjectRegion) {
			return false; //leave on external modification outside positions
		}
	}

	private class EditorSynchronizer implements ILinkedModeListener {
		public void left(LinkedModeModel model, int flags) {
			linkedModeLeft();
			if ((flags & ILinkedModeListener.UPDATE_CARET) != 0) {
				doRename(fShowPreview);
			}
		}

		public void resume(LinkedModeModel model, int flags) {
		}

		public void suspend(LinkedModeModel model) {
		}
	}

	private class ExitPolicy extends DeleteBlockingExitPolicy {
		public ExitPolicy(IDocument document) {
			super(document);
		}

		@Override
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			fShowPreview= (event.stateMask & SWT.CTRL) != 0
							&& (event.character == SWT.CR || event.character == SWT.LF);
			return super.doExit(model, event, offset, length);
		}
	}


	private static RenameLinkedMode fgActiveLinkedMode;

	private final CEditor fEditor;
	
	private RenameInformationPopup fInfoPopup;

	private Point fOriginalSelection;
	private String fOriginalName;

	private LinkedPosition fNamePosition;
	private LinkedModeModel fLinkedModeModel;
	private LinkedPositionGroup fLinkedPositionGroup;
	private final FocusEditingSupport fFocusEditingSupport;
	private boolean fShowPreview;

	/**
	 * The operation on top of the undo stack when the rename is {@link #start()}ed, or
	 * <code>null</code> if rename has not been started or the undo stack was empty.
	 */
	private IUndoableOperation fStartingUndoOperation;
	private IRegion[] fLocations;

	public RenameLinkedMode(CEditor editor) {
		Assert.isNotNull(editor);
		fEditor= editor;
		fFocusEditingSupport= new FocusEditingSupport();
	}

	public static RenameLinkedMode getActiveLinkedMode() {
		if (fgActiveLinkedMode != null) {
			ISourceViewer viewer= fgActiveLinkedMode.fEditor.getViewer();
			if (viewer != null) {
				StyledText textWidget= viewer.getTextWidget();
				if (textWidget != null && !textWidget.isDisposed()) {
					return fgActiveLinkedMode;
				}
			}
			// Make sure we don't hold onto the active linked mode if anything went wrong with canceling.
			fgActiveLinkedMode= null;
		}
		return null;
	}

	public void start() {
		if (getActiveLinkedMode() != null) {
			// For safety; should already be handled in CRenameAction
			fgActiveLinkedMode.startFullDialog();
			return;
		}

		ISourceViewer viewer= fEditor.getViewer();
		fOriginalSelection= viewer.getSelectedRange();
		final int offset= fOriginalSelection.x;

		try {
			fLocations = null;
			Point selection= viewer.getSelectedRange();
			final int secectionOffset = selection.x;
			final int selectionLength = selection.y;
			final IDocument document= viewer.getDocument();

			ASTProvider.getASTProvider().runOnAST(fEditor.getInputCElement(), ASTProvider.WAIT_ACTIVE_ONLY,
					new NullProgressMonitor(), new ASTRunnable() {

				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
					if (astRoot == null)
						return Status.CANCEL_STATUS;
					
					IASTNodeSelector selector= astRoot.getNodeSelector(null);
					IASTName name= selector.findEnclosingName(secectionOffset, selectionLength);
					if (name != null) {
						fOriginalName = name.toString();
						fLocations = LinkedNamesFinder.findByName(astRoot, name);
					}
					return Status.OK_STATUS;
				}
			});

			if (fLocations == null || fLocations.length == 0) {
				return;
			}

			if (viewer instanceof ITextViewerExtension6) {
				IUndoManager undoManager= ((ITextViewerExtension6) viewer).getUndoManager();
				if (undoManager instanceof IUndoManagerExtension) {
					IUndoManagerExtension undoManagerExtension= (IUndoManagerExtension) undoManager;
					IUndoContext undoContext= undoManagerExtension.getUndoContext();
					IOperationHistory operationHistory= OperationHistoryFactory.getOperationHistory();
					fStartingUndoOperation= operationHistory.getUndoOperation(undoContext);
				}
			}

			// Sort the locations starting with the one @ offset.
			Arrays.sort(fLocations, new Comparator<IRegion>() {

				public int compare(IRegion n1, IRegion n2) {
					return rank(n1) - rank(n2);
				}

				/**
				 * Returns the absolute rank of a location. Location preceding <code>offset</code>
				 * are ranked last.
				 *
				 * @param location the location to compute the rank for
				 * @return the rank of the location with respect to the invocation offset
				 */
				private int rank(IRegion location) {
					int relativeRank= location.getOffset() + location.getLength() - offset;
					if (relativeRank < 0)
						return Integer.MAX_VALUE + relativeRank;
					else
						return relativeRank;
				}
			});
			
			fLinkedPositionGroup= new LinkedPositionGroup();
			for (int i= 0; i < fLocations.length; i++) {
				IRegion item= fLocations[i];
				LinkedPosition linkedPosition = new LinkedPosition(document, item.getOffset(), item.getLength(), i);
				if (i == 0) {
					fNamePosition= linkedPosition;
				}
				fLinkedPositionGroup.addPosition(linkedPosition);
			}

			fLinkedModeModel= new LinkedModeModel();
			fLinkedModeModel.addGroup(fLinkedPositionGroup);
			fLinkedModeModel.forceInstall();
			fLinkedModeModel.addLinkingListener(new EditorHighlightingSynchronizer(fEditor));
			fLinkedModeModel.addLinkingListener(new EditorSynchronizer());
			
			LinkedModeUI ui= new EditorLinkedModeUI(fLinkedModeModel, viewer);
			ui.setExitPolicy(new ExitPolicy(document));
			ui.setExitPosition(viewer, offset, 0, LinkedPositionGroup.NO_STOP);
			ui.enter();

			// By default full word is selected, restore original selection.
			viewer.setSelectedRange(selection.x, selection.y);

			if (viewer instanceof IEditingSupportRegistry) {
				IEditingSupportRegistry registry= (IEditingSupportRegistry) viewer;
				registry.register(fFocusEditingSupport);
			}

			openSecondaryPopup();
			fgActiveLinkedMode= this;
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	void doRename(boolean showPreview) {
		cancel();

		Image image= null;
		Label label= null;

		fShowPreview|= showPreview;
		try {
			ISourceViewer viewer= fEditor.getViewer();
			if (viewer instanceof SourceViewer) {
				SourceViewer sourceViewer= (SourceViewer) viewer;
				Control viewerControl= sourceViewer.getControl();
				if (viewerControl instanceof Composite) {
					Composite composite= (Composite) viewerControl;
					Display display= composite.getDisplay();

					// Flush pending redraw requests:
					while (!display.isDisposed() && display.readAndDispatch()) {
					}

					// Copy editor area:
					GC gc= new GC(composite);
					Point size;
					try {
						size= composite.getSize();
						image= new Image(gc.getDevice(), size.x, size.y);
						gc.copyArea(image, 0, 0);
					} finally {
						gc.dispose();
						gc= null;
					}

					// Persist editor area while executing refactoring:
					label= new Label(composite, SWT.NONE);
					label.setImage(image);
					label.setBounds(0, 0, size.x, size.y);
					label.moveAbove(null);
				}
			}

			String newName= fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return;
			RenameSupport renameSupport= undoAndCreateRenameSupport(newName);
			if (renameSupport == null)
				return;

			Shell shell= fEditor.getSite().getShell();
			boolean executed;
			if (fShowPreview) { // could have been updated by undoAndCreateRenameSupport(..)
				executed= renameSupport.openDialog(shell, true);
			} else {
				executed= renameSupport.perform(shell, fEditor.getSite().getWorkbenchWindow());
			}
			if (executed) {
				restoreFullSelection();
			}
			reconcile();
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			// canceling is OK -> redo text changes in that case?
		} catch (InvocationTargetException e) {
			CUIPlugin.log(e);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		} finally {
			if (label != null)
				label.dispose();
			if (image != null)
				image.dispose();
		}
	}

	public void cancel() {
		if (fLinkedModeModel != null) {
			fLinkedModeModel.exit(ILinkedModeListener.NONE);
		}
		linkedModeLeft();
	}

	private void restoreFullSelection() {
		if (fOriginalSelection.y != 0) {
			int originalOffset= fOriginalSelection.x;
			LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
			for (int i= 0; i < positions.length; i++) {
				LinkedPosition position= positions[i];
				if (!position.isDeleted() && position.includes(originalOffset)) {
					fEditor.getViewer().setSelectedRange(position.offset, position.length);
					return;
				}
			}
		}
	}

	private RenameSupport undoAndCreateRenameSupport(String newName) throws CoreException {
		// Assumption: the linked mode model should be shut down by now.
		final ISourceViewer viewer= fEditor.getViewer();

		try {
			if (!fOriginalName.equals(newName)) {
				fEditor.getSite().getWorkbenchWindow().run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if (viewer instanceof ITextViewerExtension6) {
							IUndoManager undoManager= ((ITextViewerExtension6) viewer).getUndoManager();
							if (undoManager instanceof IUndoManagerExtension) {
								IUndoManagerExtension undoManagerExtension= (IUndoManagerExtension) undoManager;
								IUndoContext undoContext= undoManagerExtension.getUndoContext();
								IOperationHistory operationHistory= OperationHistoryFactory.getOperationHistory();
								while (undoManager.undoable()) {
									if (fStartingUndoOperation != null &&
											fStartingUndoOperation.equals(operationHistory.getUndoOperation(undoContext)))
										return;
									undoManager.undo();
								}
							}
						}
					}
				});
			}
		} catch (InvocationTargetException e) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.getPluginId(),
					RenameMessages.RenameLinkedMode_error_saving_editor, e));
		} catch (InterruptedException e) {
			// canceling is OK
			return null;
		} finally {
			reconcile();
		}

		viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y);

		if (newName.length() == 0)
			return null;

		IWorkingCopy workingCopy = getWorkingCopy();
        IResource resource= workingCopy.getResource();
        if (!(resource instanceof IFile)) {
        	return null;
        }
    	CRefactoringArgument arg=
    		new CRefactoringArgument((IFile) resource, fOriginalSelection.x, fOriginalSelection.y);
        CRenameProcessor processor= new CRenameProcessor(CRefactory.getInstance(), arg);
        processor.setReplacementText(newName);
        CRenameRefactoringPreferences preferences = new CRenameRefactoringPreferences();
        processor.setSelectedOptions(preferences.getOptions());
        processor.setExhaustiveSearchScope(preferences.getScope());
        processor.setWorkingSetName(preferences.getWorkingSet());
		return RenameSupport.create(processor);
	}

	private void reconcile() throws CModelException {
		IWorkingCopy workingCopy = getWorkingCopy();
		synchronized (workingCopy) {
			workingCopy.reconcile();
		}
	}

	private IWorkingCopy getWorkingCopy() {
		IEditorInput input = fEditor.getEditorInput();
		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		return manager.getWorkingCopy(input);
	}

	public void startFullDialog() {
		cancel();

		try {
			String newName= fNamePosition.getContent();
			RenameSupport renameSupport= undoAndCreateRenameSupport(newName);
			if (renameSupport != null)
				renameSupport.openDialog(fEditor.getSite().getShell());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	private void linkedModeLeft() {
		fgActiveLinkedMode= null;
		if (fInfoPopup != null) {
			fInfoPopup.close();
		}

		ISourceViewer viewer= fEditor.getViewer();
		if (viewer instanceof IEditingSupportRegistry) {
			IEditingSupportRegistry registry= (IEditingSupportRegistry) viewer;
			registry.unregister(fFocusEditingSupport);
		}
	}

	private void openSecondaryPopup() {
		fInfoPopup= new RenameInformationPopup(fEditor, this);
		fInfoPopup.open();
	}

	public boolean isCaretInLinkedPosition() {
		return getCurrentLinkedPosition() != null;
	}

	public LinkedPosition getCurrentLinkedPosition() {
		Point selection= fEditor.getViewer().getSelectedRange();
		int start= selection.x;
		int end= start + selection.y;
		LinkedPosition[] positions= fLinkedPositionGroup.getPositions();
		for (int i= 0; i < positions.length; i++) {
			LinkedPosition position= positions[i];
			if (position.includes(start) && position.includes(end))
				return position;
		}
		return null;
	}

	public boolean isEnabled() {
		try {
			String newName= fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return false;
			return CConventions.validateIdentifier(newName, getLanguage()).isOK();
		} catch (BadLocationException e) {
			return false;
		}
	}

	private AbstractCLikeLanguage getLanguage() {
		ITranslationUnit tu = (ITranslationUnit) fEditor.getInputCElement();
		ILanguage language = null;
		try {
			language = tu.getLanguage();
		} catch (CoreException e) {
		}
		if (language instanceof AbstractCLikeLanguage) {
			return (AbstractCLikeLanguage) language;
		}
		return GPPLanguage.getDefault();
	}
	
	public boolean isOriginalName() {
		try {
			String newName= fNamePosition.getContent();
			return fOriginalName.equals(newName);
		} catch (BadLocationException e) {
			return false;
		}
	}
}
