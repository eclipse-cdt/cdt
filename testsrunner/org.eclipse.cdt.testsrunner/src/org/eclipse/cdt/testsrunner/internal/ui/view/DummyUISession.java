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
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProviderInfo;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.debug.core.ILaunch;

/**
 * Represents a simple testing session which is used for UI when there is no
 * "real" testing sessions to show (e.g. when there was no launched testing
 * session or when all of them were cleared).
 */
public class DummyUISession implements ITestingSession {

	@Override
	public int getCurrentCounter() {
		return 0;
	}

	@Override
	public int getTotalCounter() {
		return 0;
	}
	
	@Override
	public int getCount(ITestItem.Status status) {
		return 0;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

	@Override
	public boolean wasStopped() {
		return false;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public ITestModelAccessor getModelAccessor() {
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public ITestsRunnerProviderInfo getTestsRunnerProviderInfo() {
		return null;
	}

	@Override
	public String getStatusMessage() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return "<dummy>"; //$NON-NLS-1$
	}

	@Override
	public void stop() {
	}

}
