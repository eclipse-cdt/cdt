/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * @since 2.6
 */
public interface IModules2 extends IModules {
	void loadSymbolsForAllModules(ISymbolDMContext symCtx, RequestMonitor rm);

	void loadSymbols(IModuleDMContext dmc, RequestMonitor rm, String name);
}
