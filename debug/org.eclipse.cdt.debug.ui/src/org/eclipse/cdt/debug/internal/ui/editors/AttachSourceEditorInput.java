/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Enter type comment.
 * 
 * @since: Feb 21, 2003
 */
public class AttachSourceEditorInput implements IEditorInput
{
	private FileNotFoundElement fElement = null;

	/**
	 * Constructor for AttachSourceEditorInput.
	 */
	public AttachSourceEditorInput( FileNotFoundElement element )
	{
		fElement = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName()
	{
		return ( fElement != null ) ? fElement.getName() : "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( FileNotFoundElement.class ) )
			return fElement;
		return null;
	}
}
