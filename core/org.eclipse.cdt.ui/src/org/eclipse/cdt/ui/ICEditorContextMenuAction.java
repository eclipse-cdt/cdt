package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.ITextEditor;

public interface ICEditorContextMenuAction extends IAction 
{
	void init( ITextEditor textEditor );
	String getMenuPath();
}

