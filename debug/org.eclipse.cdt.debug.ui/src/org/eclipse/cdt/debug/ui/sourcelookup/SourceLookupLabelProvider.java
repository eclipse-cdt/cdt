/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class SourceLookupLabelProvider extends LabelProvider implements ITableLabelProvider
{
	public Image getColumnImage( Object element, int columnIndex )
	{
		if ( columnIndex == 0 )
		{
			if ( element instanceof IProjectSourceLocation )
			{
				if ( ((IProjectSourceLocation)element).getProject().isOpen() )
					return CDebugImages.get( CDebugImages.IMG_OBJS_PROJECT );
				else
					return CDebugImages.get( CDebugImages.IMG_OBJS_CLOSED_PROJECT );
			}
			if ( element instanceof IDirectorySourceLocation )
			{
				return CDebugImages.get( CDebugImages.IMG_OBJS_FOLDER );
			}
		}
		return null;
	}

	public String getColumnText( Object element, int columnIndex )
	{
		if ( columnIndex == 0 )
		{
			if ( element instanceof IProjectSourceLocation )
			{
				return ((IProjectSourceLocation)element).getProject().getName();
			}
			if ( element instanceof IDirectorySourceLocation )
			{
				return ((IDirectorySourceLocation)element).getDirectory().toOSString();
			}
		}
		else if ( columnIndex == 1 )
		{
			if ( element instanceof IDirectorySourceLocation && ((IDirectorySourceLocation)element).getAssociation() != null )
			{
				return ((IDirectorySourceLocation)element).getAssociation().toOSString();
			}
		}
		else if ( columnIndex == 2 )
		{
			if ( element instanceof IDirectorySourceLocation )
				return  ( ((IDirectorySourceLocation)element).searchSubfolders() ) ? SourceListDialogField.YES_VALUE : SourceListDialogField.NO_VALUE;
		}
		return ""; //$NON-NLS-1$
	}
}