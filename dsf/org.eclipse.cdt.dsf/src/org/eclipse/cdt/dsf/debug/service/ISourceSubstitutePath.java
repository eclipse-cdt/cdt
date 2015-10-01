/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

/**
 * @since 2.7
 */
public interface ISourceSubstitutePath extends IDsfService {

	/**
	 * DMC for operating Source Substitutions, normally the same as the
	 * ICommandControlDMContext
	 */
	public interface ISourceSubstituteDMContext extends IDMContext {
	}

	/**
	 * Set the Source Substitution paths on GDB to the mappings described by the
	 * containers.
	 * 
	 * @param sourceSubPathDmc
	 *            DMC to operate on
	 * @param containers
	 *            containers which describe the mappings
	 * @param requestMonitor
	 */
	void setSourceSubstitutePath(ISourceSubstituteDMContext sourceSubPathDmc,
			ISourceContainer[] containers, RequestMonitor requestMonitor);

}
