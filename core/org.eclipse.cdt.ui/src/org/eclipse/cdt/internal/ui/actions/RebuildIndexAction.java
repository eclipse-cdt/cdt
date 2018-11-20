/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

public class RebuildIndexAction extends AbstractUpdateIndexAction {
	@Override
	protected void doRun(ICElement[] elements) {
		Set<ICProject> projects = new LinkedHashSet<>();
		for (ICElement element : elements) {
			if (element != null) {
				projects.add(element.getCProject());
			}
		}
		for (ICProject project : projects) {
			CCorePlugin.getIndexManager().reindex(project);
		}
	}

	@Override
	protected int getUpdateOptions() {
		return 0;
	}
}
