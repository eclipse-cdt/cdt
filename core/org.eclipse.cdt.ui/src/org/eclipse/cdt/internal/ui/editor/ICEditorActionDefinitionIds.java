/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
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
	 * Action definition ID of the source -> comment action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.comment"</code>).
	 */
	public static final String COMMENT = "org.eclipse.cdt.ui.edit.text.c.comment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> uncomment action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.uncomment"</code>).
	 */
	public static final String UNCOMMENT = "org.eclipse.cdt.ui.edit.text.c.uncomment"; //$NON-NLS-1$

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
	 * Action definition ID of the toggle presentation toolbar button action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.java.toggle.presentation"</code>).
	 */
	public static final String TOGGLE_PRESENTATION= "org.eclipse.cdt.ui.edit.text.c.toggle.presentation"; //$NON-NLS-1$
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
	public static final String OPEN_TYPE_HIERARCHY= "org.eclipse.cdt.ui.edit.text.c.open.type.hierarchy"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> open action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.open.editor"</code>).
	 */
	public static final String OPEN_EDITOR= "org.eclipse.cdt.ui.edit.text.c.open.editor"; //$NON-NLS-1$

    /**
     * Action definition ID of the open outline dialog.
     * (value <code>"org.eclipse.cdt.ui.edit.open.outline"</code>).
     */
    public static final String OPEN_OUTLINE= "org.eclipse.cdt.ui.edit.open.outline"; //$NON-NLS-1$
}
