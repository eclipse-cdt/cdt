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
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import org.eclipse.cdt.ui.actions.CdtActionConstants;

import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.FindWordAction;
import org.eclipse.cdt.internal.ui.actions.GoToNextPreviousMemberAction;
import org.eclipse.cdt.internal.ui.actions.GotoNextBookmarkAction;

public class CEditorActionContributor extends TextEditorActionContributor {
	
	private RetargetTextEditorAction fContentAssist;
	private RetargetTextEditorAction fContextInformation;
	private TogglePresentationAction fTogglePresentation;
	private GotoAnnotationAction fPreviousAnnotation;
	private GotoAnnotationAction fNextAnnotation;
	private RetargetTextEditorAction fGotoMatchingBracket;
	private RetargetTextEditorAction fGotoNextBookmark;
	private RetargetTextEditorAction fGotoNextMemberAction;
	private RetargetTextEditorAction fGotoPreviousMemberAction;
	private RetargetTextEditorAction fToggleInsertModeAction;
	private RetargetTextEditorAction fShowOutline;
	private RetargetTextEditorAction fToggleSourceHeader;
	private ToggleMarkOccurrencesAction fToggleMarkOccurrencesAction;
	private RetargetTextEditorAction fFindWord;
	
	public CEditorActionContributor() {
		super();
		
		ResourceBundle bundle = ConstructedCEditorMessages.getResourceBundle();
	
		fContentAssist = new RetargetTextEditorAction(bundle, "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssist.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

		fContextInformation = new RetargetTextEditorAction(bundle, "ContentAssistContextInformation."); //$NON-NLS-1$
		fContextInformation.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);

		// actions that are "contributed" to editors, they are considered belonging to the active editor
		fTogglePresentation= new TogglePresentationAction();

		fToggleMarkOccurrencesAction= new ToggleMarkOccurrencesAction();

		fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
		fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$

		fGotoMatchingBracket= new RetargetTextEditorAction(bundle, "GotoMatchingBracket."); //$NON-NLS-1$
		fGotoMatchingBracket.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);

		fGotoNextBookmark = new RetargetTextEditorAction(bundle, "GotoNextBookmark."); //$NON-NLS-1$
		fGotoNextBookmark.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_BOOKMARK);

		fGotoNextMemberAction= new RetargetTextEditorAction(bundle, "GotoNextMember."); //$NON-NLS-1$
		fGotoNextMemberAction.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
		fGotoPreviousMemberAction= new RetargetTextEditorAction(bundle, "GotoPreviousMember."); //$NON-NLS-1$
		fGotoPreviousMemberAction.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);

		fToggleInsertModeAction= new RetargetTextEditorAction(bundle, "ToggleInsertMode.", IAction.AS_CHECK_BOX); //$NON-NLS-1$
		fToggleInsertModeAction.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_INSERT_MODE);

		fShowOutline= new RetargetTextEditorAction(bundle, "OpenOutline."); //$NON-NLS-1$
		fShowOutline.setActionDefinitionId(ICEditorActionDefinitionIds.OPEN_OUTLINE);

		fToggleSourceHeader= new RetargetTextEditorAction(bundle, "ToggleSourceHeader."); //$NON-NLS-1$
		fToggleSourceHeader.setActionDefinitionId(ICEditorActionDefinitionIds.TOGGLE_SOURCE_HEADER);

		fFindWord = new RetargetTextEditorAction(bundle, "FindWord."); //$NON-NLS-1$
		fFindWord.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_WORD);
	}	

	/*
	 * @see org.eclipse.ui.texteditor.BasicTextEditorActionContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void contributeToMenu(IMenuManager menu) {
		
		super.contributeToMenu(menu);
		
		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {	
			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_ASSIST, fContentAssist);
			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_ASSIST, fContextInformation);

			editMenu.prependToGroup(IWorkbenchActionConstants.FIND_EXT, fFindWord);

//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, fShiftRight);
//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, fShiftLeft);
//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, fFormatter);
//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, new Separator());
//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, fAddInclude);
//			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_GENERATE, new Separator());

			editMenu.appendToGroup(IContextMenuConstants.GROUP_ADDITIONS, fToggleInsertModeAction);
		}
		
		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fToggleSourceHeader);

			navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);

			IMenuManager gotoMenu= navigateMenu.findMenuUsingPath(IWorkbenchActionConstants.GO_TO);
			if (gotoMenu != null) {
				gotoMenu.add(new Separator("additions2"));  //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoPreviousMemberAction); //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoNextMemberAction); //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoMatchingBracket); //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoNextBookmark); //$NON-NLS-1$
			}
		}

	}
	
	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		super.init(bars);

		// register actions that have a dynamic editor. 
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, fNextAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, fPreviousAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);
		bars.setGlobalActionHandler(ICEditorActionDefinitionIds.TOGGLE_MARK_OCCURRENCES, fToggleMarkOccurrencesAction);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		
		super.setActiveEditor(part);
		
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
		
		fTogglePresentation.setEditor(textEditor);
		fToggleMarkOccurrencesAction.setEditor(textEditor);
		fPreviousAnnotation.setEditor(textEditor);
		fNextAnnotation.setEditor(textEditor);

		fContentAssist.setAction(getAction(textEditor, "ContentAssistProposal")); //$NON-NLS-1$
		fContextInformation.setAction(getAction(textEditor, "ContentAssistContextInformation")); //$NON-NLS-1$

		fGotoMatchingBracket.setAction(getAction(textEditor, GotoMatchingBracketAction.GOTO_MATCHING_BRACKET));
		fGotoNextBookmark.setAction(getAction(textEditor, GotoNextBookmarkAction.NEXT_BOOKMARK));
		fGotoNextMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.NEXT_MEMBER));
		fGotoPreviousMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.PREVIOUS_MEMBER));

		fShowOutline.setAction(getAction(textEditor, "OpenOutline")); //$NON-NLS-1$
		fToggleSourceHeader.setAction(getAction(textEditor, "ToggleSourceHeader")); //$NON-NLS-1$
		fToggleInsertModeAction.setAction(getAction(textEditor, ITextEditorActionConstants.TOGGLE_INSERT_MODE));
		fFindWord.setAction(getAction(textEditor, FindWordAction.FIND_WORD));

		// Source menu.
		IActionBars bars= getActionBars();
		bars.setGlobalActionHandler(CdtActionConstants.SHIFT_RIGHT, getAction(textEditor, "ShiftRight")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.SHIFT_LEFT, getAction(textEditor, "ShiftLeft")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.COMMENT, getAction(textEditor, "Comment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.UNCOMMENT, getAction(textEditor, "Uncomment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.TOGGLE_COMMENT, getAction(textEditor, "ToggleComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.ADD_BLOCK_COMMENT, getAction(textEditor, "AddBlockComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.REMOVE_BLOCK_COMMENT, getAction(textEditor, "RemoveBlockComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.INDENT, getAction(textEditor, "Indent")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.ADD_INCLUDE, getAction(textEditor, "AddIncludeOnSelection")); //$NON-NLS-1$
		bars.setGlobalActionHandler(CdtActionConstants.SORT_LINES, getAction(textEditor, "SortLines")); //$NON-NLS-1$

		IAction action= getAction(textEditor, ITextEditorActionConstants.REFRESH);
		bars.setGlobalActionHandler(ITextEditorActionConstants.REFRESH, action);

		bars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), getAction(textEditor, IDEActionFactory.ADD_TASK.getId()));
		bars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), getAction(textEditor, IDEActionFactory.BOOKMARK.getId()));

		bars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), getAction(textEditor, IDEActionFactory.OPEN_PROJECT.getId()));
		bars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), getAction(textEditor, IDEActionFactory.CLOSE_PROJECT.getId()));
		bars.setGlobalActionHandler(IDEActionFactory.CLOSE_UNRELATED_PROJECTS.getId(), getAction(textEditor, IDEActionFactory.CLOSE_UNRELATED_PROJECTS.getId()));

		if (part instanceof CEditor) {
			CEditor cEditor= (CEditor) part;
			cEditor.fillActionBars(bars);
		}

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
	 */
	@Override
	public void dispose() {
		setActiveEditor(null);
		super.dispose();
	}
}
