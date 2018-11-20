/*******************************************************************************
 * Copyright (c) 2008, 2013 Broadcom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.net.URI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.IPath;

/**
 * This class computes a relevance for files in case we have to select
 * from multiple files for the same file-system location.
 */
public class FileRelevance {
	private static final int PREFERRED_PROJECT = 0x40;
	private static final int CDT_PROJECT = 0x20;
	private static final int ON_SOURCE_ROOT = 0x10;

	// Penalty for undesirable attributes
	private static final int LINK_PENALTY = 1;
	private static final int INACCESSIBLE_SHIFT = 4;

	/**
	 * Compute a relevance for the given file. The higher the score the more relevant the
	 * file. It is determined by the following criteria: <br>
	 * - file belongs to preferred project <br>
	 * - file belongs to a cdt-project <br>
	 * - file belongs to a source folder of a cdt-project <br>
	 * - file is accessible
	 * - file is not a link
	 * @param f the file to compute the relevance for
	 * @return integer representing file relevance. Larger numbers are more relevant
	 */
	public static int getRelevance(IFile f, IProject preferredProject) {
		return getRelevance(f, preferredProject, true, null);
	}

	/**
	 * Compute a relevance for the given file. The higher the score the more relevant the
	 * file. It is determined by the following criteria: <br>
	 * - file belongs to preferred project <br>
	 * - file belongs to a cdt-project <br>
	 * - file belongs to a source folder of a cdt-project <br>
	 * - file is accessible
	 * - file is not a link
	 * - file matches the original location
	 * @param f the file to compute the relevance for
	 * @return integer representing file relevance. Larger numbers are more relevant
	 */
	public static int getRelevance(IFile f, IProject preferredProject, boolean degradeSymLinks,
			Object originalLocation) {
		int result = 0;
		IProject p = f.getProject();
		if (p.equals(preferredProject))
			result += PREFERRED_PROJECT;

		if (CoreModel.hasCNature(p)) {
			result += CDT_PROJECT;
			ICProject cproject = CModelManager.getDefault().create(p);
			if (cproject.isOnSourceRoot(f))
				result += ON_SOURCE_ROOT;
		}

		if (!f.isAccessible()) {
			result >>= INACCESSIBLE_SHIFT;
		} else if (f.isLinked()) {
			result -= LINK_PENALTY;
		} else if (degradeSymLinks) {
			ResourceAttributes ra = f.getResourceAttributes();
			if (ra != null && ra.isSymbolicLink())
				result -= LINK_PENALTY;
		} else {
			// Symbolic links are not degraded, prefer the original location
			if (originalLocation instanceof URI) {
				if (originalLocation.equals(f.getLocationURI()))
					result += LINK_PENALTY;
			} else if (originalLocation instanceof IPath) {
				if (originalLocation.equals(f.getLocation()))
					result += LINK_PENALTY;
			}
		}
		return result;
	}
}
