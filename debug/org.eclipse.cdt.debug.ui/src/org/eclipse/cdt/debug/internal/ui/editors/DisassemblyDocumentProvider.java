/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.sourcelookup.IDisassemblyStorage;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * Enter type comment.
 * 
 * @since: Jan 6, 2003
 */
public class DisassemblyDocumentProvider extends StorageDocumentProvider
{
	/**
	 * Constructor for DisassemblyDocumentProvider.
	 */
	public DisassemblyDocumentProvider()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel( Object element ) throws CoreException
	{
		if ( element != null && element instanceof DisassemblyEditorInput ) 
		{
			IResource resource = (IResource)((DisassemblyEditorInput)element).getAdapter( IResource.class );
			IStorage storage = ((DisassemblyEditorInput)element).getStorage();
			if ( resource != null && storage != null && storage instanceof IDisassemblyStorage )
				return new DisassemblyMarkerAnnotationModel( (IDisassemblyStorage)storage, resource );
		}
		return super.createAnnotationModel( element );
	}
}
