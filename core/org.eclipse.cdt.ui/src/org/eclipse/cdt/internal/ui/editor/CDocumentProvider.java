package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;


import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;


import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import org.eclipse.cdt.internal.ui.CPlugin;

public class CDocumentProvider extends FileDocumentProvider {

	static private class RegisteredReplace {
		IDocumentListener fOwner;
		IDocumentExtension.IReplace fReplace;
			
		RegisteredReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {
			fOwner= owner;
			fReplace= replace;
		}
	};
	/**
	 * Bundle of all required informations to allow working copy management. 
	 */
	protected class CDocument extends AbstractDocument {
		
		/**
		 * Creates a new empty document.
		 */
		public CDocument() {
			super();
			setTextStore(new GapTextStore(50, 300));
			setLineTracker(new DefaultLineTracker());
			completeInitialization();
		}
		
		/**
		 * Creates a new document with the given initial content.
		 *
		 * @param initialContent the document's initial content
		 */
		public CDocument(String initialContent) {
			super();
			setTextStore(new GapTextStore(50, 300));
			setLineTracker(new DefaultLineTracker());	
			getStore().set(initialContent);
			getTracker().set(initialContent);
			completeInitialization();
		}
	};
	
	/**
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */ 
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document;
			
		if (element instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput) element).getStorage();
			
			document= new CDocument();
			setDocumentContent(document, storage.getContents(), getDefaultEncoding());
		} else {
			return null;
		}
		//IDocument document= super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner= CPlugin.getDefault().getTextTools().createDocumentPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	/*
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new CMarkerAnnotationModel(input.getFile());
		}
		
		return super.createAnnotationModel(element);
	}
	
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) element;
			
			try {
				input.getFile().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException x) {
				handleCoreException(x,"CFileDocumentProvider.createElementInfo"); //$NON-NLS-1$
			}
			
			IDocument d= createDocument(element);
			IAnnotationModel m= new CMarkerAnnotationModel(((IFileEditorInput)element).getFile());
			FileSynchronizer f= new FileSynchronizer(input);
			f.install();
			
			FileInfo info= new FileInfo(d, m, f);
			info.fModificationStamp= computeModificationStamp(input.getFile());
			
			return info;
		}
		
		return super.createElementInfo(element);
	}
}