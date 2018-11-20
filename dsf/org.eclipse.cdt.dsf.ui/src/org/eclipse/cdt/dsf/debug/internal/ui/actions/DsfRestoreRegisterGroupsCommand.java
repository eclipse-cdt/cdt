/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command performing adding a register group.
 * @since 2.4
 */
public class DsfRestoreRegisterGroupsCommand extends AbstractDsfRegisterGroupActions {
	@Override
	public void setEnabled(Object evaluationContext) {
		boolean state = false;
		if (evaluationContext instanceof IEvaluationContext) {
			Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
			Object p = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_PART_NAME);
			if (s instanceof IStructuredSelection && p instanceof IWorkbenchPart) {
				state = canRestoreDefaultGroups((IWorkbenchPart) p, (IStructuredSelection) s);
			}
		}
		setBaseEnabled(state);
	}

	@Override
	public Object execute(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (selection instanceof IStructuredSelection) {
			restoreDefaultGroups(part, (IStructuredSelection) selection);
		}
		return null;
	}
}
