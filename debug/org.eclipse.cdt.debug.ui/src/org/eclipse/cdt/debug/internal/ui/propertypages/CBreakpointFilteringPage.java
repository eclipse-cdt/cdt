/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages; 

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class CBreakpointFilteringPage extends PropertyPage {

	private ThreadFilterEditor fThreadFilterEditor;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		noDefaultAndApplyButton();
		Composite mainComposite = new Composite( parent, SWT.NONE );
		mainComposite.setFont( parent.getFont() );
		mainComposite.setLayout( new GridLayout() );
		mainComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		createThreadFilterEditor( mainComposite );
		setValid( true );
		return mainComposite;
	}

	public ICBreakpoint getBreakpoint() {
		return (ICBreakpoint)getElement();
	}

	protected void createThreadFilterEditor( Composite parent ) {
		fThreadFilterEditor = new ThreadFilterEditor( parent, this );
	}

	protected ThreadFilterEditor getThreadFilterEditor() {
		return fThreadFilterEditor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
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
