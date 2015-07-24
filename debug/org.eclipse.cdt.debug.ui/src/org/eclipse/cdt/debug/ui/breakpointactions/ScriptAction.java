/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.File;

import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.core.breakpointactions.IScriptActionEnabler;
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
 * @since 7.6
 */
public class ScriptAction extends AbstractBreakpointAction {
	
	final public static String SCRIPT_BREAKPOINT_ACTION_TYPE = "org.eclipse.cdt.debug.ui.breakpointactions.ScriptAction"; //$NON-NLS-1$
	final private static String ELEMENT_SCRIPT_ACTION = "scriptAction"; //$NON-NLS-1$
	final private static String ATTR_SCRIPT_FILE = "scriptFile"; //$NON-NLS-1$

	private File fScriptFile;

	public ScriptAction() {
	}

	@Override
	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		IScriptActionEnabler enabler = context.getAdapter(IScriptActionEnabler.class);
		if (enabler == null) {
			return new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), Messages.getString("ScriptAction.Enabler_not_registered"));			 //$NON-NLS-1$
		}
		try {
			enabler.runScript(fScriptFile, monitor);
			return Status.OK_STATUS;
		}
		catch(CoreException e) {
			return e.getStatus();
		}
	}

	@Override
	public String getMemento() {
		try {
			Document document = DebugPlugin.newDocument();
			Element actionElement = document.createElement(ELEMENT_SCRIPT_ACTION);
			if (fScriptFile != null) {
				actionElement.setAttribute(ATTR_SCRIPT_FILE, fScriptFile.getAbsolutePath());
			}
			document.appendChild(actionElement);
			return DebugPlugin.serializeDocument(document);
		}
		catch(DOMException e) {
			CDebugUIPlugin.log(e);
		}
		catch(CoreException e) {
			CDebugUIPlugin.log(e.getStatus());
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public void initializeFromMemento(String data) {
		if (data == null || data.isEmpty()) {
			return;
		}
		try {
			Element rootElement = DebugPlugin.parseDocument(data);
			if (rootElement.getNodeName().equals(ELEMENT_SCRIPT_ACTION)) {
				String fileName = rootElement.getAttribute(ATTR_SCRIPT_FILE);
				if (fileName != null && !fileName.isEmpty()) {
					fScriptFile = new File(fileName);
				}
			}
		}
		catch(CoreException e) {
			CDebugUIPlugin.log(e.getStatus());
		}
	}

	@Override
	public String getDefaultName() {
		return Messages.getString("ScriptAction.Untitled_script_action"); //$NON-NLS-1$
	}

	@Override
	public String getSummary() {
		return fScriptFile != null ? fScriptFile.getAbsolutePath() : ""; //$NON-NLS-1$
	}

	@Override
	public String getTypeName() {
		return Messages.getString("ScriptAction.Script_action"); //$NON-NLS-1$
	}

	@Override
	public String getIdentifier() {
		return SCRIPT_BREAKPOINT_ACTION_TYPE;
	}

	public File getScriptFile() {
		return fScriptFile;
	}

	public void setScriptFile(File scriptFile) {
		fScriptFile = scriptFile;
	}
}
