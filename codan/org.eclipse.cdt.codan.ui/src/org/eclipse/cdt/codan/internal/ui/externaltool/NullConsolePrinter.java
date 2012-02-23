/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.externaltool;

import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;

/**
 * No-op implementation of <code>{@link IConsolePrinter}</code>.
 * 
 * @author alruiz@google.com (Alex Ruiz)
 */
class NullConsolePrinter implements IConsolePrinter {
	@Override
	public void clear() {
	}

	@Override
	public void println(String message) {
	}

	@Override
	public void println() {
	}

	@Override
	public void close() {
	}
}
