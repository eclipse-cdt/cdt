/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IResource;

/**
 * Implementation of IChecker that works with translation unit without locking
 * the index
 *
 * Clients may extend this class.
 * @since 3.3
 */
public abstract class AbstractCElementChecker extends AbstractCheckerWithProblemPreferences implements ICIndexChecker {
	@Override
	public synchronized boolean processResource(IResource resource) {
		ICElement model = CoreModel.getDefault().create(resource);
		if (!(model instanceof ITranslationUnit))
			return true; // not a C/C++ file
		ITranslationUnit tu = (ITranslationUnit) model;
		processTranslationUnitUnlocked(tu);
		return false;
	}

	protected void processTranslationUnitUnlocked(ITranslationUnit tu) {
		processUnit(tu);
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		getTopLevelPreference(problem); // initialize
		getLaunchModePreference(problem).enableInLaunchModes(CheckerLaunchMode.RUN_ON_FILE_OPEN,
				CheckerLaunchMode.RUN_ON_FILE_SAVE, CheckerLaunchMode.RUN_ON_DEMAND,
				CheckerLaunchMode.RUN_ON_FULL_BUILD, CheckerLaunchMode.RUN_ON_INC_BUILD);
	}

	@Override
	public boolean runInEditor() {
		return false;
	}
}
