/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tests whether an active C/C++ application is debugging. And selection inside Debug View is present on it.
 *
 * @author Raghunandana Murthappa
 */
public class CDTDebugPropertyTester extends PropertyTester {

	private static final String IS_CDT_DEBUGGING = "isCDTDebugging"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (IS_CDT_DEBUGGING.equals(property)) {
			return isCdtLaunchConfigDebugMode();
		}
		return false;
	}

	private boolean isCdtLaunchConfigDebugMode() {
		ISteppingModeTarget gdbTarget = getSteppingModeTarget();
		return gdbTarget != null && gdbTarget.supportsInstructionStepping();
	}

	/**
	 * Debug View can contain many targets at given point of time. This will check if {@code ISteppingModeTarget} present and it is selected. If yes returns it.
	 *
	 * @return Instruction stepping mode target.
	 */
	public static ISteppingModeTarget getSteppingModeTarget() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow == null) {
			return null;
		}

		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		if (workbenchPage == null) {
			return null;
		}

		IViewPart debugView = workbenchPage.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (debugView == null) {
			return null;
		}

		IDebugView debugViewClazz = debugView.getAdapter(IDebugView.class);
		ISelection selection = debugViewClazz.getViewer().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}

		Object element = ((IStructuredSelection) selection).getFirstElement();

		return getTargetFromSelection(element);
	}

	public static ISteppingModeTarget getTargetFromSelection(Object element) {
		ISteppingModeTarget target = null;
		if (element instanceof IDebugElement) {
			IDebugTarget debugTarget = ((IDebugElement) element).getDebugTarget();
			if (debugTarget instanceof ISteppingModeTarget) {
				target = (ISteppingModeTarget) debugTarget;
			}
		}
		if (target == null) {
			if (element instanceof IAdaptable) {
				target = ((IAdaptable) element).getAdapter(ISteppingModeTarget.class);
			}
		}
		return target;
	}
}
