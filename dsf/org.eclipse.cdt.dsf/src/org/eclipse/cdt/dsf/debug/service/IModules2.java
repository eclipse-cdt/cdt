/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;

/**
 * Extends to IModules interface to supporting loading symbols.
 * @since 2.6
 */
public interface IModules2 extends IModules {

	/**
	 * Event indicating a change in the symbols of certain modules of the symbol context.
	 */
	public interface ISymbolsChangedDMEvent extends IDMEvent<ISymbolDMContext> {
		public IModuleDMContext[] getModules();
	}

	/**
	 * Indicates symbols were loaded for some modules.
	 */
	public interface ISymbolsLoadedDMEvent extends ISymbolsChangedDMEvent {
	}

	/**
	 * Indicates symbols were unloaded for some modules.
	 */
	public interface ISymbolsUnloadedDMEvent extends ISymbolsChangedDMEvent {
	}

	/**
	 * Load symbols for all modules of the specified symbol context
	 */
	void loadSymbolsForAllModules(ISymbolDMContext symCtx, RequestMonitor rm);

	/**
	 *  Load symbols for the specified module
	 */
	void loadSymbols(IModuleDMContext dmc, RequestMonitor rm);
}
