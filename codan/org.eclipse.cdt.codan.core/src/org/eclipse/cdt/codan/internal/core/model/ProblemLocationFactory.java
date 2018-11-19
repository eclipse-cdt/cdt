/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Factory class that allows to create problem locations
 */
public class ProblemLocationFactory implements IProblemLocationFactory {
	@Override
	public IProblemLocation createProblemLocation(IFile file, int line) {
		return new CodanProblemLocation(file, line);
	}

	@Override
	public IProblemLocation createProblemLocation(IFile file, int startChar, int endChar, int line) {
		return new CodanProblemLocation(file, startChar, endChar, line);
	}

	public IProblemLocation createProblemLocation(IResource resource, int startChar, int endChar, int line) {
		return new CodanProblemLocation(resource, startChar, endChar, line);
	}
}
