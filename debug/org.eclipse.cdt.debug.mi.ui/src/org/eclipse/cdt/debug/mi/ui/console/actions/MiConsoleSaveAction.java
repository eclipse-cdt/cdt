/*******************************************************************************
 * Copyright (c) 2006 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * STMicroelectronics - Process console enhancements
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.ui.console.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.debug.mi.internal.ui.MIUIPlugin;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

/**
 * Save console content
 *
 */
public class MiConsoleSaveAction extends Action{

	private IConsole fConsole;
	private String fileName;
	
	public MiConsoleSaveAction(IConsole console) {
        super();
        setToolTipText(MiConsoleMessages.saveActionTooltip);
        setImageDescriptor(MIUIPlugin.imageDescriptorFromPlugin(MIUIPlugin.PLUGIN_ID,IMiConsoleImagesConst.IMG_SAVE_CONSOLE));
        fConsole = console;
	}
	
	@Override
	public void run() {
		
		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		fileName = fileDialog.open();
		if(fileName==null) {
			return;
		}

		Runnable saveJob = new Runnable() {
			@Override
			public void run() {
				saveContent();
			}
		};
		BusyIndicator.showWhile(Display.getCurrent(), saveJob);
		
	}
	
	protected void saveContent() 	{
		boolean confirmed = true;
		
		try {
			File f = new File(fileName);
			if(f.exists()) {
				confirmed = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Confirm overwrite", MiConsoleMessages.confirmOverWrite);
			}
			if(confirmed) {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
				out.write(fConsole.getDocument().get());
				out.close();
			}
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),"Error",MiConsoleMessages.infoIOError);
		}

	}

}
