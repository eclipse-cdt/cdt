/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Defines the definition IDs for the C editor actions.
 * 
 * <p>
 * This interface is not intended to be implemented or extended.
 * </p>.
 * 
 * @since 2.1
 */
public interface ICEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	/**
	 * Action definition ID of the source -> toggle comment action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.toggle.comment"</code>).
	 * @since 4.0.0
	 */
	public static final String TOGGLE_COMMENT= "org.eclipse.cdt.ui.edit.text.c.toggle.comment"; //$NON-NLS-1$
	
	
	/**
	 * Action definition ID of the source -> add block comment action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.add.block.comment"</code>).
	 * @since 3.0
	 */
	public static final String ADD_BLOCK_COMMENT= "org.eclipse.cdt.ui.edit.text.c.add.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> remove block comment action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.remove.block.comment"</code>).
	 * @since 3.0
	 */
	public static final String REMOVE_BLOCK_COMMENT= "org.eclipse.cdt.ui.edit.text.c.remove.block.comment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> join lines action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.join.lines"</code>).
	 * @since 3.0.2
	 */
	public static final String JOIN_LINES= "org.eclipse.cdt.ui.edit.text.c.join.lines"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> indent action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.indent"</code>).
	 */
	public static final String INDENT = "org.eclipse.cdt.ui.edit.text.c.indent"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> format action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.format"</code>).
	 */
	public static final String FORMAT = "org.eclipse.cdt.ui.edit.text.c.format"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> add include action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.add.include"</code>).
	 */
	public static final String ADD_INCLUDE= "org.eclipse.cdt.ui.edit.text.c.add.include"; //$NON-NLS-1$	

	/**
	 * Action definition ID of the open declaration action
	 * (value <code>"org.eclipse.cdt.ui.edit.opendecl"</code>).
	 */
	public static final String OPEN_DECL= "org.eclipse.cdt.ui.edit.opendecl"; //$NON-NLS-1$
    
	/**
	 * Action definition ID of the show in C/C++ Projects View action
	 * (value <code>"org.eclipse.cdt.ui.edit.opencview"</code>).
	 */
	public static final String OPEN_CVIEW= "org.eclipse.cdt.ui.edit.opencview"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the refactor -> rename element action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.rename.element"</code>).
	 */
	public static final String RENAME_ELEMENT= "org.eclipse.cdt.ui.edit.text.rename.element"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> extract constant action
	 * (value <code>"org.eclipse.cdt.ui.refactor.extract.constant"</code>).
	 */
	public static final String EXTRACT_CONSTANT= "org.eclipse.cdt.ui.refactor.extract.constant"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> extract local variable action
	 * (value <code>"org.eclipse.cdt.ui.refactor.extract.local.variable"</code>).
	 */
	public static final String EXTRACT_LOCAL_VARIABLE= "org.eclipse.cdt.ui.refactor.extract.local.variable"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the refactor -> extract function action (value
	 * <code>"org.eclipse.cdt.ui.refactor.extract.function"</code>).
	 */
	public static final String EXTRACT_FUNCTION = "org.eclipse.cdt.ui.refactor.extract.function"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> hide method action
	 * (value <code>"org.eclipse.cdt.ui.refactor.hide.method"</code>).
	 */
	public static final String HIDE_METHOD= "org.eclipse.cdt.ui.refactor.hide.method"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the refactor -> implement method action
	 * (value <code>"org.eclipse.cdt.ui.refactor.implement.method"</code>).
	 */
	public static final String IMPLEMENT_METHOD= "org.eclipse.cdt.ui.refactor.implement.method"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> generate getters and setters
	 * (value <code>"org.eclipse.cdt.ui.refactor.getters.and.setters"</code>).
	 */
	public static final String GETTERS_AND_SETTERS= "org.eclipse.cdt.ui.refactor.getters.and.setters"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> undo action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.undo.action"</code>).
	 */
	public static final String UNDO_ACTION= "org.eclipse.cdt.ui.edit.text.undo.action"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the refactor -> redo action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.redo.action"</code>).
	 */
	public static final String REDO_ACTION= "org.eclipse.cdt.ui.edit.text.redo.action"; //$NON-NLS-1$	

	/**
	 * Action definition ID of the find references in workspace action
	 * (value <code>"org.eclipse.cdt.ui.search.findrefs"</code>).
	 */
	public static final String FIND_REFS= "org.eclipse.cdt.ui.search.findrefs"; //$NON-NLS-1$	
	
	/**
	 * Action definition ID of the find declarations in workspace action
	 * (value <code>"org.eclipse.cdt.ui.search.finddecl"</code>).
	 */
	public static final String FIND_DECL= "org.eclipse.cdt.ui.search.finddecl"; //$NON-NLS-1$	

	/**
	 * Action definition ID of the navigate -> open type hierarchy action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.open.type.hierarchy"</code>).
	 */
	public static final String OPEN_TYPE_HIERARCHY= "org.eclipse.cdt.ui.edit.open.type.hierarchy"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> open type hierarchy action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.open.quick.type.hierarchy"</code>).
	 */
	public static final String OPEN_QUICK_TYPE_HIERARCHY= "org.eclipse.cdt.ui.edit.open.quick.type.hierarchy"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> open action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.open.editor"</code>).
	 */
	public static final String OPEN_EDITOR= "org.eclipse.cdt.ui.edit.text.c.open.editor"; //$NON-NLS-1$

	/**
	 * Action definition ID of the open quick outline.
	 * (value <code>"org.eclipse.cdt.ui.edit.open.outline"</code>).
	 */
	public static final String OPEN_OUTLINE= "org.eclipse.cdt.ui.edit.open.outline"; //$NON-NLS-1$

	/**
	 * Action definition ID for opening the call hierarchy.
	 * (value <code>"org.eclipse.cdt.ui.edit.open.call.hierarchy"</code>).
	 */
	public static final String OPEN_CALL_HIERARCHY= "org.eclipse.cdt.ui.edit.open.call.hierarchy"; //$NON-NLS-1$

	/**
	 * Action definition ID for opening the include browser.
	 * (value <code>"org.eclipse.cdt.ui.edit.open.include.browser"</code>).
	 */
	public static final String OPEN_INCLUDE_BROWSER= "org.eclipse.cdt.ui.edit.open.include.browser"; //$NON-NLS-1$

	/**
	 * Action definition ID for go to next c member.
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.goto.next.memeber"</code>)
	 */
	public static final String GOTO_NEXT_MEMBER = "org.eclipse.cdt.ui.edit.text.c.goto.next.member"; //$NON-NLS-1$

	/**
	 * Action definition ID for go to previous c member.
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.goto.prev.memeber"</code>)
	 */
	public static final String GOTO_PREVIOUS_MEMBER = "org.eclipse.cdt.ui.edit.text.c.goto.prev.member"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to matching bracket action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.goto.matching.bracket"</code>).
	 *
	 * @since 3.0
	 */
	public static final String GOTO_MATCHING_BRACKET= "org.eclipse.cdt.ui.edit.text.c.goto.matching.bracket"; //$NON-NLS-1$

	/**
	 * Action definition ID for goto next bookmark action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.goto.next.bookmark"</code>).
	 */
	public static final String GOTO_NEXT_BOOKMARK = "org.eclipse.cdt.ui.edit.text.c.goto.next.bookmark"; //$NON-NLS-1$	

	/**
	 * Action definition ID for find word action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.find.word"</code>).
	 */
	public static final String FIND_WORD = "org.eclipse.cdt.ui.edit.text.c.find.word"; //$NON-NLS-1$	

	/**
	 * Action definition ID for toggle source/header action.
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.toggle.source.header"</code>)
	 * 
	 * @since 4.0
	 */
	public static final String TOGGLE_SOURCE_HEADER = "org.eclipse.cdt.ui.edit.text.c.toggle.source.header"; //$NON-NLS-1$

	/**
	 * Action definition id of toggle mark occurrences action
	 * (value: <code>"org.eclipse.cdt.ui.edit.text.c.toggleMarkOccurrences"</code>).
	 * @since 5.0
	 */
	public static final String TOGGLE_MARK_OCCURRENCES= "org.eclipse.cdt.ui.edit.text.c.toggleMarkOccurrences"; //$NON-NLS-1$

	/**
	 * Action definition ID of the open macro explorer quick view action
	 * (value <code>"org.eclipse.cdt.ui.edit.open.quick.macro.explorer"</code>).
	 * @since 5.0
	 */
	public static final String OPEN_QUICK_MACRO_EXPLORER = "org.eclipse.cdt.ui.edit.open.quick.macro.explorer"; //$NON-NLS-1$

	/**
	 * Action definition id of sort lines action.
	 * (value: <code>"org.eclipse.cdt.ui.edit.text.c.sort.lines"</code>).
	 * @since 5.2
	 */
	public static final String SORT_LINES = "org.eclipse.cdt.ui.edit.text.c.sort.lines"; //$NON-NLS-1$
}
