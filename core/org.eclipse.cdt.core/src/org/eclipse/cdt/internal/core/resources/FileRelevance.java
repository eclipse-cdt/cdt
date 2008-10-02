/*******************************************************************************
 * Copyright (c) 2008 Broadcom and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     James Blackburn (Broadcom) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * This class computes a relevance for files in case we have to select
 * from multiple files for the same file-system location.
 */
public class FileRelevance {
	private static final int PREFERRED_PROJECT 		= 0x40;
	private static final int CDT_PROJECT 			= 0x20;
	private static final int ON_SOURCE_ROOT			= 0x10;

	/**
	 * Compute a relevance for the given file. The higher the score the more relevant the
	 * file. It is determined by the following criteria: <br>
	 * - file belongs to preferred project <br>
	 * - file belongs to a cdt-project <br>
	 * - file belongs to a source folder of a cdt-project <br>
	 * @param f the file to compute the relevance for
	 * @return -1 if f1 is preferable, 1 if f2 is preferable, 0 if there is no difference.
	 */
	public static int getRelevance(IFile f, IProject preferredProject) {
		int result= 0;
		IProject p= f.getProject();
		if (p.equals(preferredProject)) 
			result+= PREFERRED_PROJECT;

		if (CoreModel.hasCNature(p)) {
			result+= CDT_PROJECT;
			ICProject cproject= CModelManager.getDefault().create(p);
			if (cproject.isOnSourceRoot(f)) 
				result+= ON_SOURCE_ROOT;
		}
		return result;
	}
}
