/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * 
 * Enter type comment.
 * 
 * @since Mar 4, 2003
 */
public class CDebugDocumentProvider extends CDocumentProvider
{
	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument( Object element ) throws CoreException
	{
		if ( element instanceof EditorInputDelegate )
		{
			if ( ((EditorInputDelegate)element).getDelegate() != null )
			{
				return super.createDocument( ((EditorInputDelegate)element).getDelegate() );
			}
			else
			{
				IDocument document = null;
				IStorage storage = ((EditorInputDelegate)element).getStorage();	
				if ( storage != null )
				{
					document = new CDocument();
					setDocumentContent( document, storage.getContents(), getDefaultEncoding() );
				}
				else 
				{
					return null;
				}
				if ( document != null) 
				{
					IDocumentPartitioner partitioner= CUIPlugin.getDefault().getTextTools().createDocumentPartitioner();
					partitioner.connect( document );
					document.setDocumentPartitioner( partitioner );
				}
				return document;
			}
		}
		return super.createDocument( element );
	}

	/**
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#getStatus(Object)
	 */
	public IStatus getStatus( Object element )
	{
		if ( element instanceof EditorInputDelegate )
		{
			if ( ((EditorInputDelegate)element).getDelegate() != null )
			{
				return super.getStatus( ((EditorInputDelegate)element).getDelegate() );
			}
			else
			{
				return createFileNotFoundStatus( ((EditorInputDelegate)element).getElement() );
			}
		}
		return super.getStatus( element );
	}

	private IStatus createFileNotFoundStatus( FileNotFoundElement element )
	{
		return new Status( IStatus.INFO, CDebugUIPlugin.getUniqueIdentifier(), 0, "", null );
	}

	protected IAnnotationModel createAnnotationModel( Object element ) throws CoreException
	{
		if ( element instanceof EditorInputDelegate && ((EditorInputDelegate)element).getDelegate() != null )
			return super.createAnnotationModel( ((EditorInputDelegate)element).getDelegate() );
		return super.createAnnotationModel( element );
	}

	/* (non-Javadoc)
	 * This hack is important for the the outliner to work correctly.
	 * The outliner looks at the working copy and it is maintain by
	 * CUIPlugin.getDefault().getWorkingCopyManager()
	 * CUIPlugin.getDefault().getDocumentProvider();
	 * They are singletons.
	 * 
	 * @see org.eclipse.cdt.internal.ui.editor.CDocumentProvider#getBufferFactory()
	 */
	public IBufferFactory getBufferFactory() {
		return CUIPlugin.getDefault().getDocumentProvider().getBufferFactory();
	}

}
