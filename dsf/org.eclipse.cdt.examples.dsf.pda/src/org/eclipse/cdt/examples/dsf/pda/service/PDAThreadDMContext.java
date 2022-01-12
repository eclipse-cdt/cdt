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
package org.eclipse.cdt.examples.dsf.pda.service;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * Context representing a PDA thread.
 */
public class PDAThreadDMContext extends AbstractDMContext implements IExecutionDMContext {
	final private Integer fID;

	public PDAThreadDMContext(String sessionId, PDAVirtualMachineDMContext vmCtx, int id) {
		super(sessionId, new IDMContext[] { vmCtx });
		fID = id;
	}

	public int getID() {
		return fID;
	}

	@Override
	public String toString() {
		return super.baseToString() + ".thread[" + fID + "]";
	}

	@Override
	public int hashCode() {
		return baseHashCode() + fID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return baseEquals(obj) && ((PDAThreadDMContext) obj).fID.equals(fID);
	}
}
