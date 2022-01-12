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
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

/**
 * Handling of adding group of registers into expression view
 *
 * @since 2.4
 */
public class AddRegistersExpressionCommandHandler extends AddLocalsExpressionCommandHandler {
	@Override
	protected String getExpression() {
		return "=$*"; //$NON-NLS-1$
	}
}
