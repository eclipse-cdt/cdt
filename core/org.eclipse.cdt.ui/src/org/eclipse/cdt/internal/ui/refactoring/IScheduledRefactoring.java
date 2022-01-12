/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface IScheduledRefactoring {

	/**
	 * The scheduling rule used to perform the
	 * refactoring.
	 *
	 * @return {@link ISchedulingRule} not null
	 */
	public ISchedulingRule getSchedulingRule();

}
