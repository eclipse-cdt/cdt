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
