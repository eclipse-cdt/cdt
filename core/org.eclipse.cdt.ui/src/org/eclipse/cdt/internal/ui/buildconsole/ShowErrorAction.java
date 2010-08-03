/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

/**
 * Set whether to show error in editor when moving to next/prev error in Build Console
 */
public class ShowErrorAction extends Action {

	private BuildConsolePage fConsolePage;
	
	public ShowErrorAction(BuildConsolePage page) {
		super(ConsoleMessages.ShowErrorAction_Tooltip); 
		fConsolePage = page;
		setChecked(true);
		setToolTipText(ConsoleMessages.ShowErrorAction_Tooltip); 
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();		
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED_DISABLED));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		super.run();
		if ( isChecked() ) {
			IProject project = fConsolePage.getProject();
			if (project == null) return;
			
			IBuildConsoleManager consoleManager = CUIPlugin.getDefault().getConsoleManager();
			IConsole console = consoleManager.getConsole(project);
			if ( console instanceof BuildConsolePartitioner) {
				BuildConsolePartitioner par = (BuildConsolePartitioner)console;
				fConsolePage.showError(par, true );
			}
		}
	}	
	
}
