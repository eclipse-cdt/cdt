/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.console.IConsole;

/**
 * Fake part to use as keys in page book for debugger console pages
 */
public class DebuggerConsoleWorkbenchPart implements IWorkbenchPart {

	private IConsole fConsole;
	private IWorkbenchPartSite fSite;

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DebuggerConsoleWorkbenchPart)
				&& fConsole.equals(((DebuggerConsoleWorkbenchPart) obj).fConsole);
	}

	@Override
	public int hashCode() {
		return fConsole.hashCode();
	}

	/**
	 * Constructs a part for the given console that binds to the given
	 * site
	 */
	public DebuggerConsoleWorkbenchPart(IConsole console, IWorkbenchPartSite site) {
		fConsole = console;
		fSite = site;
	}

	@Override
	public void addPropertyListener(IPropertyListener listener) {
	}

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return fSite;
	}

	@Override
	public String getTitle() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getTitleImage() {
		return null;
	}

	@Override
	public String getTitleToolTip() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void removePropertyListener(IPropertyListener listener) {
	}

	@Override
	public void setFocus() {
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}
}
