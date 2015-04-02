/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

/** Expressions interface with a way to control automatic update.
 *
 * @since 2.7
 */
public interface IExpressions4 extends IExpressions3 {

	/** Specify whether expression must be pro-actively updated on stop.
	 *
	 *  The IExpression interface allows to obtain values of expressions,
	 *  and a tree of subexpressions. It is typically reasonable, when
	 *  obtaining of expression, to also fetch values of entire tree
	 *  of expressions. For example, if a value of previously-known expression
	 *  is requested, after stop, GDB/MI will use -var-update to update values
	 *  of an entirely tree. Other implementations will behave similarly.
	 *
	 *  That approach is easy, but does not scale to large structures over
	 *  slow connections. This method allows UI to indicate that a value of
	 *  particular expression is not immediately necessary, and pre-fetching
	 *  it is undesirable. For example, if we expand tree element and then
	 *  collapse it, pre-fetching that tree element can be usually avoided.
	 *
	 *  The method works in 'reference counting' way. If it's never called,
	 *  default behaviour is in effect. If this method is ever called, then
	 *  calling with 'update' of true means UI prefers pre-fetch, and false
	 *  means UI prefers otherwise. If the number of calls with 'true' value
	 *  is larger than the number of calls with 'false' value, then pre-fetch
	 *  is desired.
	 *
	 * */
	void setAutomaticUpdate(IExpressionDMContext context, boolean update);

}
