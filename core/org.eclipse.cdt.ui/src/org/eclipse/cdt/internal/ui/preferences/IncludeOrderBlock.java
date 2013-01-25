/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;

/**
 * The preference block for configuring relative order of include statements.
 */
public class IncludeOrderBlock extends OptionsConfigurationBlock {

	private static Key[] getAllKeys() {
		return new Key[] {
			};
	}

	private PixelConverter pixelConverter;

	public IncludeOrderBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
	}

	public void postSetSelection(Object element) {
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter =  new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite =  new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		updateControls();
		return composite;
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		// TODO Implement
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		// TODO Implement
	}
}
