package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.ui.texteditor.ITextEditor;

public interface ICEditorRulerAction extends IAction 
{
	void init( IVerticalRuler ruler, ITextEditor textEditor );
}

