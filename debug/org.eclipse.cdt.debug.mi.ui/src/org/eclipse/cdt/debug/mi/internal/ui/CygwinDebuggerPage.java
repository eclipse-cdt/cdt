/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.internal.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.cdt.debug.mi.internal.ui.MIUIPlugin;


public class CygwinDebuggerPage extends GDBDebuggerPage 
{
	public String getName() 
	{
		return MIUIPlugin.getResourceString("internal.ui.CygwinDebuggerPage.Cygwin_GDB_Debugger_Options"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#createSolibBlock(org.eclipse.swt.widgets.Composite)
	 */
	public GDBSolibBlock createSolibBlock(Composite parent)
	{
		GDBSolibBlock block = new GDBSolibBlock();
		block.createBlock( parent, true, false, true );
		return block;
	}
}
