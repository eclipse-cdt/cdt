/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * Enter type comment.
 * 
 * @since: Oct 8, 2002
 */
public class DisassemblyEditorInput implements IStorageEditorInput
{
	private final static String FILE_NAME_EXTENSION = ".s";
	protected IStorage fStorage;

	/**
	 * Constructor for DisassemblyEditorInput.
	 */
	public DisassemblyEditorInput( IStorage storage )
	{
		fStorage = storage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStorageEditorInput#getStorage()
	 */
	public IStorage getStorage() throws CoreException
	{
		return fStorage;
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
		return CDebugImages.DESC_OBJS_DISASSEMBLY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName()
	{
		try
		{
			if ( getStorage() != null )
			{
				return getStorage().getName() + FILE_NAME_EXTENSION;
			}
		}
		catch( CoreException e )
		{
			// ignore
		}
		return "";
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
		return "Disassembly";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( IResource.class ) )
		{
			try
			{
				IStorage storage = getStorage();
				if (  storage != null )
				{
					return storage.getAdapter( adapter );
				}
			}
			catch( CoreException e )
			{
				// ignore
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals( Object obj )
	{
		if ( obj != null && obj instanceof DisassemblyEditorInput )
		{
			try
			{
				IStorage storage = ((DisassemblyEditorInput)obj).getStorage();
				if ( storage != null && storage.equals( fStorage ) )
					return true;
				else if ( storage == null && fStorage == null )
					return true;
			}
			catch( CoreException e )
			{
			}
		}
		return false;
	}
}
