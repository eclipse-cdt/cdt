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
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

/**
 * IProblemFilter - interface to suppress problems.
 *
 * It is recommented to extend {@link AbstractProblemFilter} instead
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 *
 * @since 3.3
 *
 */
public interface IProblemFilter {
	public boolean shouldIgnore(String problemId, IProblemLocation location);

	public void before(IResource resource);

	public void after(IResource resource);
}
