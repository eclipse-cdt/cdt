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

package org.eclipse.cdt.dsf.ui.viewmodel.datamodel;

import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions4;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.AbstractElementVMProvider;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/** VM Provider for the special case of IExpressionDMContext elements.
 *
 *  In particular, has methods to notify expressions whether they
 *  are still used in UI.
 */
public class ExpressionBasedDMVMProvider extends AbstractElementVMProvider {

	public ExpressionBasedDMVMProvider(AbstractVMAdapter adapter,
			IPresentationContext presentationContext, DsfSession session) {
		super(adapter, presentationContext, session);
	}

	public void handleExpandCollapse(final IExpressionDMContext context, final boolean expanded) {

		DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(),
				context.getSessionId());
		IExpressions xexpressions = tracker.getService(IExpressions.class);
		tracker.dispose();

		if (!(xexpressions instanceof IExpressions4))
			return;

		final IExpressions4 expressions = (IExpressions4)xexpressions;

		expressions.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				expressions.setAutomaticUpdate(context, expanded);
			}
		});

	}
}
