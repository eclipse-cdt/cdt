/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 18, 2002
 */
public class SourcePropertyPage extends PropertyPage
{
	private SourceLookupBlock fBlock = null;

	/**
	 * Constructor for SourcePropertyPage.
	 */
	public SourcePropertyPage()
	{
		fBlock = new SourceLookupBlock();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
		fBlock.createControl( parent );
		return fBlock.getControl();
	}

}
