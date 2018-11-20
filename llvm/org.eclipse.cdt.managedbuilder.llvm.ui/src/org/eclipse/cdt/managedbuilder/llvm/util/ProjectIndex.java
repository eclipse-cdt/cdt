/*******************************************************************************
 * Copyright (c) 2011-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;

public class ProjectIndex {

	/**
	 * Rebuilts the index of the selected project in the workspace.
	 */
	public static void rebuiltIndex(IProject proj) {
		ICProject cproject = CoreModel.getDefault().getCModel().getCProject(proj.getName());
		CCorePlugin.getIndexManager().reindex(cproject);
	}

}
