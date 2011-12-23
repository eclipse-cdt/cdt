/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.console.TextConsole;

/**
 * An action to save the gdb traces. Inspired by MiConsoleSaveAction
 * 
 * @since 2.1
 */
public class ConsoleSaveAction extends Action{
	private TextConsole fConsole;
	
	public ConsoleSaveAction(TextConsole console) {
        super();
        setToolTipText( ConsoleMessages.ConsoleMessages_save_action_tooltip);
        setImageDescriptor(GdbUIPlugin.imageDescriptorFromPlugin(GdbUIPlugin.PLUGIN_ID,IConsoleImagesConst.IMG_SAVE_CONSOLE));
        fConsole = console;
	}
	
	@Override
	public void run() {
		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		final String fileName = fileDialog.open();
		if(fileName==null) {
			return;
		}

		Runnable saveJob = new Runnable() {
            @Override
			public void run() {
				saveContent(fileName);
			}
		};
		BusyIndicator.showWhile(Display.getCurrent(), saveJob);
	}
	
	/**
	 * Save the content from the tracing console to a file
	 * 
	 * @param fileName The fileName of the File to save to
	 */
	protected void saveContent(String fileName) 	{
		try {
			boolean confirmed = true;
			
			File file = new File(fileName);
			if(file.exists()) {
				confirmed = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), 
						ConsoleMessages.ConsoleMessages_save_confirm_overwrite_title, 
						ConsoleMessages.ConsoleMessages_save_confirm_overwrite_desc);
			}
			if(confirmed) {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
				out.write(fConsole.getDocument().get());
				out.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					ConsoleMessages.ConsoleMessages_save_info_io_error_title, 
					ConsoleMessages.ConsoleMessages_save_info_io_error_desc);
		}

	}
}
