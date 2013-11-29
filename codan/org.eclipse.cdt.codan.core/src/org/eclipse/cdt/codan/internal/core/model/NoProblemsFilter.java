/*******************************************************************************
 * Copyright (c) 2013 Andreas Muelder and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Muelder  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import org.eclipse.cdt.codan.core.model.IProblemFilter;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.core.resources.IResource;

public class NoProblemsFilter implements IProblemFilter {
	@Override
	public boolean shouldIgnore(String problemId, IProblemLocation location) {
		return false;
	}

	@Override
	public void before(IResource resource) {
	}

	@Override
	public void after(IResource resource) {
	}
}
