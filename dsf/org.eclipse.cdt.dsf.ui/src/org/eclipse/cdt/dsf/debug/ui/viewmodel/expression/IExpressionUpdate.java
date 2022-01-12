/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * An update for an element based on the given expression.  The provider processing
 * this update needs to create an expression element based on the tree path and the
 * expression object in this update.
 */
public interface IExpressionUpdate extends IViewerUpdate {

	/**
	 * Returns the expression object for this update.
	 */
	public IExpression getExpression();

	/**
	 * Sets the element to the update.  The element is to be calculated by the provider
	 * handling the update.
	 */
	public void setExpressionElement(Object element);
}
