/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;

/**
 * Manual update policy which selectively clears the cache when the expressions
 * in the expression manager are modified.
 */
public class ExpressionsBreakpointHitUpdatePolicy extends BreakpointHitUpdatePolicy {

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		if (event instanceof ExpressionsChangedEvent) {
			return new ExpressionsChangedUpdateTester((ExpressionsChangedEvent) event);
		}
		return super.getElementUpdateTester(event);
	}
}
