/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueRetriever;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;

/**
 * @since 1.0
 */
public class BreakpointHitUpdatePolicy extends DebugManualUpdatePolicy {

	public static String BREAKPOINT_HIT_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.debug.ui.viewmodel.update.breakpointHitUpdatePolicy"; //$NON-NLS-1$

	/**
	 * Creates a breakpoint hit update policy for debug views.
	 */
	public BreakpointHitUpdatePolicy() {
		super();
	}

	/**
	 * Creates a breakpoint hit update policy for debug views for models that
	 * retrieve multiple formatted values for each view entry.  The given
	 * prefixes distinguish the formatted values properties from each other.
	 *
	 * @see FormattedValueRetriever
	 * @see FormattedValueVMUtil#getPropertyForFormatId(String, String)
	 *
	 * @param prefixes Prefixes to use when flushing the active formatted value
	 * from VM cache.
	 */
	public BreakpointHitUpdatePolicy(String[] prefixes) {
		super(prefixes);
	}

	@Override
	public String getID() {
		return BREAKPOINT_HIT_UPDATE_POLICY_ID;
	}

	@Override
	public String getName() {
		return MessagesForVMUpdate.BreakpointHitUpdatePolicy_name;
	}

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		if (event instanceof ISuspendedDMEvent) {
			ISuspendedDMEvent suspendedEvent = (ISuspendedDMEvent) event;
			if (suspendedEvent.getReason().equals(StateChangeReason.BREAKPOINT)) {
				return super.getElementUpdateTester(REFRESH_EVENT);
			}
		}
		return super.getElementUpdateTester(event);
	}
}
