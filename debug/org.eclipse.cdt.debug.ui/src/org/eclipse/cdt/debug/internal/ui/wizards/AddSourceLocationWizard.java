/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.widgets.Composite;

/**
 * Enter type comment.
 * 
 * @since: Dec 20, 2002
 */
public class AddSourceLocationWizard extends Wizard
{
	/**
	 * Enter type comment.
	 * 
	 * @since: Dec 20, 2002
	 */
	public class SourceLocationSelectionPage extends WizardSelectionPage
	{

		/**
		 * Constructor for SourceLocationSelectionPage.
		 * @param pageName
		 */
		public SourceLocationSelectionPage( String pageName )
		{
			super( pageName );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
		 */
		public void createControl( Composite parent )
		{
		}
	}
	/**
	 * Constructor for AddSourceLocationWizard.
	 */
	public AddSourceLocationWizard()
	{
		super();
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
		addPage( new SourceLocationSelectionPage( "Add Source Location" ) );
	}
}
