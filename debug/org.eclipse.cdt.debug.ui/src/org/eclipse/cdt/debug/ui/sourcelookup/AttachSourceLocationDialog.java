/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Enter type comment.
 * 
 * @since: Dec 12, 2002
 */
public class AttachSourceLocationDialog extends Dialog
{
	private IPath fLocationPath = null;
	private IPath fAssociationPath = null;
	private AttachSourceLocationBlock fAttachBlock;

	/**
	 * Constructor for AttachSourceLocationDialog.
	 * @param parentShell
	 */
	public AttachSourceLocationDialog( Shell parentShell )
	{
		super( parentShell );
		fAttachBlock = new AttachSourceLocationBlock();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea( Composite parent )
	{
		Composite composite = (Composite)super.createDialogArea( parent );
		getShell().setText( "Attach Source Location" );
		fAttachBlock.createControl( composite );
		fAttachBlock.setInitialAssociationPath( fAssociationPath );

		return composite;
	}

	public void setInitialPath( IPath path )
	{
		fAssociationPath = path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed()
	{
		String locationString = fAttachBlock.getLocationPath();
		if ( locationString.length() == 0 )
		{
			MessageDialog.openError( getShell(), getShell().getText(), "Location directory is not selected" );
			return;
		}
		if ( !isLocationPathValid( locationString ) )
		{
			MessageDialog.openError( getShell(), getShell().getText(), MessageFormat.format( "Invalid path: ''{0}''", new String[] { locationString } ) );
			return;
		}
		String associationString = fAttachBlock.getAssociationPath();
		if ( !isAssociationPathValid( associationString ) )
		{
			MessageDialog.openError( getShell(), getShell().getText(), MessageFormat.format( "Invalid path: ''{0}''", new String[] { associationString } ) );
			return;
		}
		fLocationPath = getLocation0();
		fAssociationPath = getAssociation0();
		super.okPressed();
	}

	public boolean isLocationPathValid( String pathString )
	{
		if ( Path.EMPTY.isValidPath( pathString ) )
		{
			Path path = new Path( pathString );
			return path.toFile().exists();
		}
		return false;
	}

	public boolean isAssociationPathValid( String pathString )
	{
		if ( pathString.length() > 0 )
		{
			return Path.EMPTY.isValidPath( pathString );
		}
		return true;
	}
	
	public IPath getLocation()
	{
		return fLocationPath;
	}

	private IPath getLocation0()
	{
		if ( Path.EMPTY.isValidPath( fAttachBlock.getLocationPath() ) )
		{
			return new Path( fAttachBlock.getLocationPath() );
		}
		return null;
	}	
	
	public IPath getAssociation()
	{
		return fAssociationPath;
	}

	private IPath getAssociation0()
	{
		if ( Path.EMPTY.isValidPath( fAttachBlock.getAssociationPath() ) )
		{
			return new Path( fAttachBlock.getAssociationPath() );
		}
		return null;
	}	
}
