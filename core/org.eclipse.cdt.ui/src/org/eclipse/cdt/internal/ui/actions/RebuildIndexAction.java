/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;

public class RebuildIndexAction extends AbstractUpdateIndexAction {
	
	@Override
	protected void doRun(ICProject[] projects) {
		for (ICProject proj : projects) {
			if(proj != null) {
				CCorePlugin.getIndexManager().reindex(proj);
			}
		}
	}

	@Override
	protected int getUpdateOptions() {
		return 0;
	}
}
