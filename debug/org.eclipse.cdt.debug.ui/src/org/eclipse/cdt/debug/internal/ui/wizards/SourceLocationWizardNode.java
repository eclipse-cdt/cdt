/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.graphics.Point;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 25, 2002
 */
public class SourceLocationWizardNode implements IWizardNode
{
	private INewSourceLocationWizard fWizard = null;

	/**
	 * Constructor for SourceLocationWizardNode.
	 */
	public SourceLocationWizardNode( INewSourceLocationWizard wizard )
	{
		fWizard = wizard;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
	 */
	public Point getExtent()
	{
		return new Point( -1, -1 );
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#getWizard()
	 */
	public IWizard getWizard()
	{
		return fWizard;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
	 */
	public boolean isContentCreated()
	{
		return ( fWizard != null && fWizard.getPageCount() > 0 );
	}
}
