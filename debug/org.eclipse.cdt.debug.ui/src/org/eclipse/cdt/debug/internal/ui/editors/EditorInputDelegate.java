/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * 
 * Enter type comment.
 * 
 * @since Mar 4, 2003
 */
public class EditorInputDelegate implements IEditorInput
{
	public static final int TYPE_ATTACH_SOURCE = 0;
	public static final int TYPE_WORKSPACE_FILE = 1;
	public static final int TYPE_EXTERNAL_FILE = 2;

	private int fType = TYPE_ATTACH_SOURCE;
	private IEditorInput fDelegate = null;
	private FileNotFoundElement fElement = null;

	/**
	 * Constructor for EditorInputDelegate.
	 */
	public EditorInputDelegate( FileNotFoundElement element )
	{
		fElement = element;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists()
	{
		if ( fDelegate != null )
			return fDelegate.exists();
		return true;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor()
	{
		if ( fDelegate != null )
			return fDelegate.getImageDescriptor();
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName()
	{
		if ( fDelegate != null )
			return fDelegate.getName();
		return ( fElement != null ) ? fElement.getName() : "";
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable()
	{
		if ( fDelegate != null )
			return fDelegate.getPersistable();
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText()
	{
		if ( fDelegate != null )
			return fDelegate.getToolTipText();
		return "";
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( FileNotFoundElement.class ) )
			return fElement;
		if ( getDelegate() != null )
			return getDelegate().getAdapter( adapter );
		return null;
	}

	public int getType()
	{
		return fType;
	}

	public void setType( int type )
	{
		fType = type;
	}

	public IEditorInput getDelegate()
	{
		return fDelegate;
	}

	public void setDelegate( IEditorInput input )
	{
		fDelegate = input;
	}
	
	public IStorage getStorage() throws CoreException
	{
		if ( getDelegate() instanceof IStorageEditorInput )
			return ((IStorageEditorInput)getDelegate()).getStorage();
		return getDummyStorage();
	}
	
	private IStorage getDummyStorage()
	{
		return new IStorage()
					{
						public InputStream getContents() throws CoreException
						{
							return new ByteArrayInputStream( new byte[0] );
						}
						
						public IPath getFullPath()
						{
							if ( getElement() != null )
								return getElement().getFullPath();
							return null;
						}

						public String getName()
						{
							if ( getElement() != null )
								return getElement().getName();
							return "";
						}

						public boolean isReadOnly()
						{
							return true;
						}
						
						public Object getAdapter( Class adapter )
						{
							if ( adapter.equals( IStorage.class ) )
								return this;
							return null;
						}
					};
	}

	protected FileNotFoundElement getElement()
	{
		return fElement;
	}
}
