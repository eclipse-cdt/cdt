/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 16, 2002
 */
public class ChangeRegisterValueAction extends SelectionProviderAction
{
	protected Tree fTree;
	protected TreeEditor fTreeEditor;

	/**
	 * Constructor for ChangeRegisterValueAction.
	 * @param provider
	 * @param text
	 */
	public ChangeRegisterValueAction( Viewer viewer )
	{
		super( viewer, "Change Register Value" );
		setDescription( "Change Register Value" );
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_CHANGE_REGISTER_VALUE );
		fTree = ((TreeViewer)viewer).getTree();
		fTreeEditor = new TreeEditor( fTree );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.CHANGE_REGISTER_VALUE_ACTION );
	}
}
