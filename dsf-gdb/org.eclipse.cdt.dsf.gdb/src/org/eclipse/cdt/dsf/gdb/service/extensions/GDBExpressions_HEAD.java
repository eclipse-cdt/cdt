/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.extensions;

import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Top-level class in the version hierarchy of implementations of {@link IExpressions}.
 * <br>
 * Extenders should subclass this class for their special needs, which will allow
 * them to always extend the most recent version of the service.
 * For example, if GDB<Service>_7_9 is added, this GDB<Service>_HEAD class
 * will be changed to extend it instead of the previous version, therefore
 * automatically allowing extenders to be extending the new class.
 *
 * NOTE: Older versions of GDB that were already using an extending class,
 *       will automatically start using the new service version, which may
 *       not be desirable.  Extenders should update how they extend
 *       GdbDebugServicesFactory to properly choose the version of the
 *       service that should be used for older GDBs.
 *
 *       On the contrary, not using GDB<Service>_HEAD requires the
 *       extender to update how they extend GdbDebugServicesFactory
 *       whenever a new GDB<Service> version is added.
 *
 *       Extenders that prefer to focus on the latest GDB version are
 *       encouraged to extend GDB<Service>_HEAD.
 *
 * @since 4.8
 */
public class GDBExpressions_HEAD extends MIExpressions {
	public GDBExpressions_HEAD(DsfSession session) {
		super(session);

		validateGdbVersion(session);
	}

	protected String getMinGDBVersionSupported() {
		return GdbDebugServicesFactory.GDB_7_1_VERSION;
	}

	protected void validateGdbVersion(DsfSession session) {
		GdbDebugServicesFactory.validateGdbVersion(session, getMinGDBVersionSupported(), this);
	}
}
