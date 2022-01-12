/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.qt.ui.handlers;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ReloadAnalyzerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new Job("Reload QML Analyzer") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Activator.getService(IQMLAnalyzer.class).load();
				} catch (NoSuchMethodException | ScriptException | IOException e) {
					return Activator.error("Reloading QML Analyzer", e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
		return Status.OK_STATUS;
	}
}
