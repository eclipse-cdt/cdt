/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

/**
 * Enter type comment.
 * 
 * @since: Dec 20, 2002
 */
public class AddSourceLocationWizard extends Wizard implements INewSourceLocationWizard
{
	private ICSourceLocation[] fLocations = null;

	/**
	 * Constructor for AddSourceLocationWizard.
	 */
	public AddSourceLocationWizard( ICSourceLocation[] locations )
	{
		super();
		setWindowTitle( CDebugUIPlugin.getResourceString("AddSourceLocationWizard.Window_Title") ); //$NON-NLS-1$
		setForcePreviousAndNextButtons( true );
		fLocations = locations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages()
	{
		addPage( new SourceLocationSelectionPage( fLocations ) );
	}
	
	public ICSourceLocation getSourceLocation()
	{
		SourceLocationSelectionPage page = (SourceLocationSelectionPage)getStartingPage();
		if ( page != null )
		{
			return page.getSourceLocation();
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getDescription()
	 */
	public String getDescription()
	{
		return ""; //$NON-NLS-1$
	}
}
