/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
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

/**
 * Factory class that allows to create problem locations
 */
public class ProblemLocationFactory implements IProblemLocationFactory {
	@Override
	public IProblemLocation createProblemLocation(IFile file, int line) {
		return new CodanProblemLocation(file, line);
	}

	@Override
	@Deprecated
	public IProblemLocation createProblemLocation(IFile file, int startChar, int endChar) {
		return new CodanProblemLocation(file, startChar, endChar);
	}

	@Override
	public IProblemLocation createProblemLocation(IFile file, int startChar, int endChar, int line) {
		return new CodanProblemLocation(file, startChar, endChar, line);
	}
}
