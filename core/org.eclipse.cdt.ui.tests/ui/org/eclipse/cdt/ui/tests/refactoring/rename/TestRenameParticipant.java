/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class TestRenameParticipant extends RenameParticipant {
	private static Object sElement;
	private static RenameArguments sArguments;
	private static int sConditionCheck;
	private static int sCreateChange;

	public static int getConditionCheckCount() {
		return sConditionCheck;
	}

	public static int getCreateChangeCount() {
		return sCreateChange;
	}

	public static Object getElement() {
		return sElement;
	}

	public static RenameArguments staticGetArguments() {
		return sArguments;
	}

	public static void reset() {
		sElement = null;
		sArguments = null;
		sConditionCheck = 0;
		sCreateChange = 0;
	}

	@Override
	protected boolean initialize(Object element) {
		sElement = element;
		return true;
	}

	@Override
	public String getName() {
		return "TestRenameParticipant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		sConditionCheck++;
		sArguments = getArguments();
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		sCreateChange++;
		return null;
	}
}
