/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.console.IConsole;

/**
 * Fake part to use as keys in page book for console pages
 */
public class GdbConsoleWorkbenchPart implements IWorkbenchPart {

	private IConsole fConsole = null;
	private IWorkbenchPartSite fSite = null;

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof GdbConsoleWorkbenchPart) &&
			fConsole.equals(((GdbConsoleWorkbenchPart)obj).fConsole);
	}

	@Override
	public int hashCode() {
		return fConsole.hashCode();
	}

	/**
	 * Constructs a part for the given console that binds to the given
	 * site
	 */
	public GdbConsoleWorkbenchPart(IConsole console, IWorkbenchPartSite site) {
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

	/**
	 * Returns the console associated with this part.
	 *
	 * @return console associated with this part
	 */
	protected IConsole getConsole() {
		return fConsole;
	}
}
