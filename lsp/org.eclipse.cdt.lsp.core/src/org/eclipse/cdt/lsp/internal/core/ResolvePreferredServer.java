/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.core;

import java.util.function.Function;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.SupportedLanguageServers;
import org.eclipse.core.runtime.ServiceCaller;

public final class ResolvePreferredServer implements Function<Class<?>, LanguageServerConfiguration> {

	@Override
	public LanguageServerConfiguration apply(Class<?> caller) {
		LanguageServerConfiguration[] configs = new LanguageServerConfiguration[1];
		ServiceCaller.callOnce(caller, SupportedLanguageServers.class, x -> configs[0] = x.preferred());
		return configs[0];
	}

}
