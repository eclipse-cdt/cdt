/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 */
public class RawCommand extends MICommand<MIInfo> {

	String fRaw;

	public RawCommand(IDMContext ctx, String operation) {
		super(ctx, operation);
		fRaw = operation;
	}

	@Override
	public boolean supportsThreadAndFrameOptions() {
		return false;
	}

	@Override
	public boolean supportsThreadGroupOption() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.Command#getMIOutput()
	 */
	public MIOutput getMIOutput() {
		return new MIOutput();
	}
}
