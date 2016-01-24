/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
