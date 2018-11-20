/*******************************************************************************
 * Copyright (c) 2009, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation9
 *******************************************************************************/

package org.eclipse.cdt.launch.internal.refactoring;

import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Common implementation of launch configuration changes.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public abstract class AbstractLaunchConfigChange extends Change {

	private ILaunchConfiguration launchConfig;

	/**
	 * Initializes me with the launch configuration that I change.
	 *
	 * @param launchConfig
	 *            my launch configuration
	 */
	public AbstractLaunchConfigChange(ILaunchConfiguration launchConfig) {
		this.launchConfig = launchConfig;
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return launchConfig;
	}

	@Override
	public Object getModifiedElement() {
		return getLaunchConfiguration();
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		// no-op
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {

		return new RefactoringStatus();
	}

	/**
	 * Chains changes, creating new composites or appending to existing composites, as appropriate.
	 * The pattern of usage is:
	 * <pre>
	 *   Change change = null;
	 *
	 *   for (<i>whatever</i>) {
	 *       change = AbstractLaunchConfigChange.append(change, createNextChange(...));
	 *   }
	 *
	 *   // do something with the change
	 * </pre>
	 *
	 * @param change a change to add to, or <code>null</code> to start a new (potentially conposite) change
	 * @param toAppend the change to add.  Must not be <code>null</code>
	 *
	 * @return the resulting change, which may or may not be a composite
	 */
	public static Change append(Change change, Change toAppend) {
		if (change == null) {
			return toAppend;
		} else if (change instanceof CompositeChange) {
			((CompositeChange) change).add(toAppend);
			return change;
		} else {
			return new CompositeChange(LaunchMessages.AbstractChange_compositeName0, new Change[] { change, toAppend });
		}
	}
}
