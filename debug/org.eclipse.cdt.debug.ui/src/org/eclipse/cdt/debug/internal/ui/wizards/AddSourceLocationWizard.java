/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.jface.wizard.Wizard;

/**
 * The wizard to add a source location to the source locator.
 */
public class AddSourceLocationWizard extends Wizard implements INewSourceLocationWizard {

	private ICSourceLocation[] fLocations = null;

	/**
	 * Constructor for AddSourceLocationWizard.
	 */
	public AddSourceLocationWizard( ICSourceLocation[] locations ) {
		super();
		setWindowTitle( WizardMessages.getString( "AddSourceLocationWizard.0" ) ); //$NON-NLS-1$
		setForcePreviousAndNextButtons( true );
		fLocations = locations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage( new SourceLocationSelectionPage( fLocations ) );
	}

	public ICSourceLocation getSourceLocation() {
		SourceLocationSelectionPage page = (SourceLocationSelectionPage)getStartingPage();
		if ( page != null ) {
			return page.getSourceLocation();
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getDescription()
	 */
	public String getDescription() {
		return ""; //$NON-NLS-1$
	}
}