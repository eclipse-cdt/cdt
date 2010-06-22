/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IResource;

/**
 * Abstract implementation of a IProblemReporter
 * 
 * @since 1.1
 */
public abstract class AbstractProblemReporter implements IProblemReporter {
	public void reportProblem(String id, IProblemLocation loc, Object... args) {
		IResource file = loc.getFile();
		if (file == null)
			throw new NullPointerException("file"); //$NON-NLS-1$
		if (id == null)
			throw new NullPointerException("id"); //$NON-NLS-1$
		IProblem problem = CheckersRegistry.getInstance()
				.getResourceProfile(file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered:" + id); //$NON-NLS-1$
		if (problem.isEnabled() == false)
			return; // skip
		ICodanProblemMarker codanProblemMarker = new CodanProblemMarker(
				problem, loc, args);
		reportProblem(codanProblemMarker);
	}

	/**
	 * @param codanProblemMarker
	 */
	protected abstract void reportProblem(ICodanProblemMarker codanProblemMarker);
}
