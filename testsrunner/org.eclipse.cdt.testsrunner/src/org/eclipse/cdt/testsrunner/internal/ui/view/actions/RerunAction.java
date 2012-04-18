/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

/**
 * Restarts the last testing session (it may be run or debug session).
 */
public class RerunAction extends Action {

	private TestingSessionsManager testingSessionsManager;


	public RerunAction(TestingSessionsManager testingSessionsManager) {
		super(ActionsMessages.RerunAction_text);
		setToolTipText(ActionsMessages.RerunAction_tooltip);
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/rerun.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
		this.testingSessionsManager = testingSessionsManager;
	}

	@Override
	public void run() {
		ITestingSession activeSession = testingSessionsManager.getActiveSession();
		if (activeSession != null) {
			ILaunch launch = activeSession.getLaunch();
			DebugUITools.launch(launch.getLaunchConfiguration(), launch.getLaunchMode());
		} else {
			setEnabled(false);
		}
	}
	
}

