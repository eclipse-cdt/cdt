/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia (QNX)- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.ICLIDebugActionEnabler;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This breakpoint action allows to pass arbitrary command line command to debugger backend.
 * For example in case of gdb it would be something like
 *   p myGlobal
 *   cont
 * @since 8.0
 */
public class CLICommandAction extends AbstractBreakpointAction {
	private static final String COMMAND_ATT = "command"; //$NON-NLS-1$
	private String command = ""; //$NON-NLS-1$

	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		ICLIDebugActionEnabler enabler = context.getAdapter(ICLIDebugActionEnabler.class);
		if (enabler != null) {
			try {
				enabler.execute(getCommand());
			} catch (Exception e) {
				return errorStatus(e);
			}
		} else
			return new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
					IInternalCDebugUIConstants.INTERNAL_ERROR, Messages.getString("CLICommandAction.NoSupport"), null); //$NON-NLS-1$
		return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
	}

	private IStatus errorStatus(Exception ex) {
		String errorMsg = MessageFormat.format(Messages.getString("CLICommandAction.error.0"), //$NON-NLS-1$
				new Object[] { getSummary() });
		return new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(),
				ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, ex);
	}

	@Override
	public String getDefaultName() {
		return Messages.getString("CLICommandAction.UntitledName"); //$NON-NLS-1$
	}

	@Override
	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.CLICommandAction"; //$NON-NLS-1$
	}

	@Override
	public String getMemento() {
		try {
			Document doc = DebugPlugin.newDocument();
			Element rootElement = doc.createElement(COMMAND_ATT);
			rootElement.setAttribute(COMMAND_ATT, command);
			doc.appendChild(rootElement);
			return DebugPlugin.serializeDocument(doc);
		} catch (DOMException | CoreException e) {
			CDebugUIPlugin.log(e);

		}
		return ""; //$NON-NLS-1$
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String getSummary() {
		String summary = getCommand();
		if (summary.length() > 32)
			summary = summary.substring(0, 32);
		return summary;
	}

	@Override
	public String getTypeName() {
		return Messages.getString("CLICommandAction.TypeName"); //$NON-NLS-1$
	}

	@Override
	public void initializeFromMemento(String data) {
		try {
			Element root = DebugPlugin.parseDocument(data);
			String value = root.getAttribute(COMMAND_ATT);
			if (value == null)
				value = ""; //$NON-NLS-1$
			command = value;
		} catch (Exception e) {
			CDebugUIPlugin.log(e);
		}
	}
}
