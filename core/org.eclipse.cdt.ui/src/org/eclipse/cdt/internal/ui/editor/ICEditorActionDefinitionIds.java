/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
	 * Action definition ID of the source -> format action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.format"</code>).
	 */
	public static final String FORMAT = "org.eclipse.cdt.ui.edit.text.c.format"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> add include action
	 * (value <code>"org.eclipse.cdt.ui.edit.text.c.add.include"</code>).
	 */
	public static final String ADD_INCLUDE= "org.eclipse.cdt.ui.edit.text.c.add.include"; //$NON-NLS-1$

}
