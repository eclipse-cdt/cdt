/*******************************************************************************
 * Copyright (c) 2009,2012 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IResource;

/**
 * Abstract implementation of a IProblemReporter
 *
 * @since 2.0
 */
public abstract class AbstractProblemReporter implements IProblemReporter {
	@Override
	public void reportProblem(String id, IProblemLocation loc, Object... args) {
		IResource file = loc.getFile();
		if (file == null)
			throw new NullPointerException("file"); //$NON-NLS-1$
		if (id == null)
			throw new NullPointerException("id"); //$NON-NLS-1$
		IProblem problem = CheckersRegistry.getInstance().getResourceProfile(file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered:" + id); //$NON-NLS-1$
		if (!problem.isEnabled())
			return; // skip
		ICodanProblemMarker codanProblemMarker = new CodanProblemMarker(problem, loc, args);
		reportProblem(codanProblemMarker);
	}

	/**
	 * @param codanProblemMarker
	 */
	protected abstract void reportProblem(ICodanProblemMarker codanProblemMarker);
}
