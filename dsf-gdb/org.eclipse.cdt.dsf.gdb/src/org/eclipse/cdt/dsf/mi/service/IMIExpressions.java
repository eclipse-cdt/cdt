/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions3;

/**
 * An extension of {@link IExpressions} which became necessary because the MI
 * implementation of {@link IExpressions} has problems if asked for all
 * sub-expressions. Problems may arise if uninitialized data objects are
 * inspected. In the worst case, pretty printers may run into endless loops
 * (e.g. linked list that become cycle), and gdb never returns. But also in the
 * normal case of uninitialized collections, you easily end up with millions of
 * useless elements, damaging the responsiveness of the workbench.
 *
 * In order to avoid those problems, this extension lets the client specify a
 * maximum number of children that it is interested in.
 *
 * If you have an instance implementing {@link IExpressions}, you should always
 * check whether it implements this extension, and if so, use the methods of the
 * extension.
 *
 * @since 4.0
 */
public interface IMIExpressions extends IExpressions3 {

	/**
	 * A special constant that can be used in methods that expect a child count
	 * limit. If this constant is passed, the implementation will use the most
	 * recent child count limit for the expression. If such a limit was never
	 * specified before, at least one child will be fetched in order to tell
	 * whether an expression has children or not.
	 */
	public static final int CHILD_COUNT_LIMIT_UNSPECIFIED = -1;

	/**
	 * This method indicates whether the given expression can safely be asked
	 * for all its sub-expressions.
	 *
	 * If this method returns <code>false</code>, this has the following impact:
	 * <ul>
	 * <li>you should not call
	 * {@link IExpressions#getSubExpressionCount(IExpressionDMContext, DataRequestMonitor)},
	 * but
	 * {@link IMIExpressions#getSubExpressionCount(IExpressionDMContext, int, DataRequestMonitor)}
	 * instead.</li>
	 *
	 * <li>you should not call
	 * {@link IExpressions#getSubExpressions(IExpressionDMContext, DataRequestMonitor)},
	 * but
	 * {@link IExpressions#getSubExpressions(IExpressionDMContext, int, int, DataRequestMonitor)}
	 * </li>
	 * </ul>
	 *
	 * @param exprCtx
	 *            The data model context representing an expression.
	 *
	 * @param rm
	 *            Data Request monitor containing <code>true</code> if this expression can
	 *            safely fetch all its sub-expressions. <code>false</false> otherwise.
	 */
	public void safeToAskForAllSubExpressions(IExpressionDMContext exprCtx, DataRequestMonitor<Boolean> rm);

	/**
	 * This method is the same as
	 * {@link IExpressions#getSubExpressionCount(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, DataRequestMonitor)}
	 * , with the slight but important difference that this method allows to
	 * provide an upper limit of children we are interested in.
	 * As long as {@link #safeToAskForAllSubExpressions(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, DataRequestMonitor)}
	 * returns true, the original method can be called, and this method is not of further interest.
	 * However, if {@link #safeToAskForAllSubExpressions(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, DataRequestMonitor)}
	 * returns false, the original method must not be called, and this method must instead be used.
	 * Otherwise, the gdb response time may be very slow, or it even may hang.
	 *
	 * @param exprCtx
	 *            The data model context representing an expression.
	 *
	 * @param maxNumberOfChildren
	 *            The implementation needs not check whether there are more than
	 *            this number of children. However, if the implementation has
	 *            already knowledge of more children than this, or can obtain
	 *            them equally efficient, it might also return a higher count.
	 *
	 * @param rm
	 *            Request completion monitor containing the number of
	 *            sub-expressions of the specified expression
	 */
	void getSubExpressionCount(IExpressionDMContext exprCtx, int maxNumberOfChildren, DataRequestMonitor<Integer> rm);
}
