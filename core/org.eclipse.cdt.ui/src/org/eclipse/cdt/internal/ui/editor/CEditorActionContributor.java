/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.GoToNextPreviousMemberAction;

public class CEditorActionContributor extends TextEditorActionContributor {
	
	protected CEditor fCEditor;
	protected RetargetTextEditorAction fContentAssist;
	protected RetargetTextEditorAction fFormatter;
	protected RetargetTextEditorAction fAddInclude;
//	protected RetargetTextEditorAction fOpenOnSelection;
	protected RetargetTextEditorAction fShiftLeft;
	protected RetargetTextEditorAction fShiftRight;
	private TogglePresentationAction fTogglePresentation;
	private GotoAnnotationAction fPreviousAnnotation;
	private GotoAnnotationAction fNextAnnotation;
	private RetargetTextEditorAction fGotoMatchingBracket;
	private RetargetTextEditorAction fGotoNextMemberAction;
	private RetargetTextEditorAction fGotoPreviousMemberAction;
	private RetargetTextEditorAction fToggleInsertModeAction;
	
	public CEditorActionContributor() {
		super();
		
		ResourceBundle bundle = CEditorMessages.getResourceBundle();
	
		fShiftRight= new RetargetTextEditorAction(bundle, "ShiftRight.", ITextOperationTarget.SHIFT_RIGHT);		 //$NON-NLS-1$
		fShiftRight.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_RIGHT);
		CPluginImages.setImageDescriptors(fShiftRight, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_RIGHT);

		fShiftLeft= new RetargetTextEditorAction(bundle, "ShiftLeft.", ITextOperationTarget.SHIFT_LEFT); //$NON-NLS-1$
		fShiftLeft.setActionDefinitionId(ITextEditorActionDefinitionIds.SHIFT_LEFT);
		CPluginImages.setImageDescriptors(fShiftLeft, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SHIFT_LEFT);
		
		fContentAssist = new RetargetTextEditorAction(bundle, "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssist.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

		fFormatter = new RetargetTextEditorAction(bundle, "Format."); //$NON-NLS-1$
		fFormatter.setActionDefinitionId(ICEditorActionDefinitionIds.FORMAT);
		
		fAddInclude = new RetargetTextEditorAction(bundle, "AddIncludeOnSelection."); //$NON-NLS-1$
		fAddInclude.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_INCLUDE);

//		fOpenOnSelection = new RetargetTextEditorAction(bundle, "OpenOnSelection."); //$NON-NLS-1$

		// actions that are "contributed" to editors, they are considered belonging to the active editor
		fTogglePresentation= new TogglePresentationAction();
		fTogglePresentation.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
		
		//fToggleTextHover= new ToggleTextHoverAction();

		fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
		fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$

		fGotoMatchingBracket= new RetargetTextEditorAction(bundle, "GotoMatchingBracket."); //$NON-NLS-1$
		fGotoMatchingBracket.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);

		fGotoNextMemberAction= new RetargetTextEditorAction(bundle, "GotoNextMember."); //$NON-NLS-1$
		fGotoNextMemberAction.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
		fGotoPreviousMemberAction= new RetargetTextEditorAction(bundle, "GotoPreviousMember."); //$NON-NLS-1$
		fGotoPreviousMemberAction.setActionDefinitionId(ICEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);

		fToggleInsertModeAction= new RetargetTextEditorAction(bundle, "ToggleInsertMode.", IAction.AS_CHECK_BOX); //$NON-NLS-1$
		fToggleInsertModeAction.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_INSERT_MODE);
	}	


	/*
	 * @see org.eclipse.ui.texteditor.BasicTextEditorActionContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		
		super.contributeToMenu(menu);
		
		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {	
			editMenu.add(fShiftRight);
			editMenu.add(fShiftLeft);

			editMenu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fContentAssist);
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fAddInclude);
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fFormatter);
//			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, fOpenOnSelection);

			editMenu.appendToGroup(IContextMenuConstants.GROUP_ADDITIONS, fToggleInsertModeAction);

			IMenuManager gotoMenu= menu.findMenuUsingPath("navigate/goTo"); //$NON-NLS-1$
			if (gotoMenu != null) {
				gotoMenu.add(new Separator("additions2"));  //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoPreviousMemberAction); //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoNextMemberAction); //$NON-NLS-1$
				gotoMenu.appendToGroup("additions2", fGotoMatchingBracket); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);

		// register actions that have a dynamic editor. 
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, fNextAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, fPreviousAnnotation);
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);
	}

	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		
		super.setActiveEditor(part);
		
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
		
		fShiftRight.setAction(getAction(textEditor, ITextEditorActionConstants.SHIFT_RIGHT));
		fShiftLeft.setAction(getAction(textEditor, ITextEditorActionConstants.SHIFT_LEFT));

		fTogglePresentation.setEditor(textEditor);
		fPreviousAnnotation.setEditor(textEditor);
		fNextAnnotation.setEditor(textEditor);

		fContentAssist.setAction(getAction(textEditor, "ContentAssistProposal")); //$NON-NLS-1$
		fAddInclude.setAction(getAction(textEditor, "AddIncludeOnSelection")); //$NON-NLS-1$
//		fOpenOnSelection.setAction(getAction(textEditor, "OpenOnSelection")); //$NON-NLS-1$
		fFormatter.setAction(getAction(textEditor, "Format")); //$NON-NLS-1$

		fGotoMatchingBracket.setAction(getAction(textEditor, GotoMatchingBracketAction.GOTO_MATCHING_BRACKET));
		fGotoNextMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.NEXT_MEMBER));
		fGotoPreviousMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.PREVIOUS_MEMBER));
		
		fToggleInsertModeAction.setAction(getAction(textEditor, ITextEditorActionConstants.TOGGLE_INSERT_MODE));

		if (part instanceof CEditor) {
			CEditor cEditor= (CEditor) part;
			cEditor.fillActionBars(getActionBars());
		}

	}
	
	/*
	 * @see EditorActionBarContributor#contributeToStatusLine(IStatusLineManager)
	 *
	 * More code here only until we move to 2.0...
	 */
	public void contributeeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		setActiveEditor(null);
		super.dispose();
	}
}