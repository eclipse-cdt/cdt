/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Marc Khouzam (Ericsson) - Use IDsfBreakpointExtension directly  (Bug 355833)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.debug.service.IDsfBreakpointExtension;
import org.eclipse.cdt.dsf.gdb.breakpoints.CBreakpointGdbThreadsFilterExtension;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class CBreakpointGdbThreadFilterPage extends PropertyPage {

	private GdbThreadFilterEditor fThreadFilterEditor;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createThreadFilterEditor(mainComposite);
		setValid(true);
		return mainComposite;
	}

	public ICBreakpoint getBreakpoint() {
		return getElement().getAdapter(ICBreakpoint.class);
	}

	public IDsfBreakpointExtension getFilterExtension() {
		ICBreakpoint bp = getBreakpoint();
		if (bp != null) {
			try {
				IDsfBreakpointExtension filter = bp.getExtension(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID,
						CBreakpointGdbThreadsFilterExtension.class);
				filter.initialize(bp);
				return filter;
			} catch (CoreException e) {
			}
		}
		return null;
	}

	protected void createThreadFilterEditor(Composite parent) {
		fThreadFilterEditor = new GdbThreadFilterEditor(parent, this);
	}

	protected GdbThreadFilterEditor getThreadFilterEditor() {
		return fThreadFilterEditor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		doStore();
		return super.performOk();
	}

	/**
	 * Stores the values configured in this page.
	 */
	protected void doStore() {
		fThreadFilterEditor.doStore();
	}
}
