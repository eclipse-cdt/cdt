package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.CStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

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
	 * Bundle of all required informations to allow working copy management. 
	 */
	protected class TranslationUnitFileInfo extends FileInfo {
			
		IWorkingCopy fCopy;
			
		TranslationUnitFileInfo(IDocument document, IAnnotationModel model, FileSynchronizer fileSynchronizer, IWorkingCopy copy) {
			super(document, model, fileSynchronizer);
			fCopy= copy;
		}
			
		void setModificationStamp(long timeStamp) {
			fModificationStamp= timeStamp;
		}
	};
	/**
	 * Creates <code>IBuffer</code>s based on documents.
	 */
	protected class BufferFactory implements IBufferFactory {
			
		private IDocument internalGetDocument(IFileEditorInput input) throws CoreException {
			IDocument document= getDocument(input);
			if (document != null)
				return document;
			return CDocumentProvider.this.createDocument(input);
		}
			
		public IBuffer createBuffer(IOpenable owner) {
			if (owner instanceof IWorkingCopy) {
						
				IWorkingCopy unit= (IWorkingCopy) owner;
				ITranslationUnit original= (ITranslationUnit) unit.getOriginalElement();
				IResource resource= original.getResource();
				if (resource instanceof IFile) {
					IFileEditorInput providerKey= new FileEditorInput((IFile) resource);
						
					IDocument document= null;
					IStatus status= null;
						
					try {
						document= internalGetDocument(providerKey);
					} catch (CoreException x) {
						status= x.getStatus();
						document= new Document();
						initializeDocument(document);
					}
							
					DocumentAdapter adapter= new DocumentAdapter(unit, document, new DefaultLineTracker(), CDocumentProvider.this, providerKey);
					adapter.setStatus(status);
					return adapter;
				}
							
			}
			return DocumentAdapter.NULL;
		}
	};


	/** The buffer factory */
	private IBufferFactory fBufferFactory= new BufferFactory();
	/** Indicates whether the save has been initialized by this provider */
	private boolean fIsAboutToSave= false;

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
		initializeDocument(document);
		return document;
	}
	
	/*
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new CMarkerAnnotationModel(input.getFile());
		} else if (element instanceof IStorageEditorInput) {
			// Fall back on the adapter.
			IStorageEditorInput input = (IStorageEditorInput) element;
			IResource res = (IResource)input.getAdapter(IResource.class);
			if (res != null && res.exists()) {
				return new CMarkerAnnotationModel(res);
			}
		}
		
		return super.createAnnotationModel(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if ( !(element instanceof IFileEditorInput))
			return super.createElementInfo(element);
			
		IFileEditorInput input= (IFileEditorInput) element;
		ITranslationUnit original= createTranslationUnit(input.getFile());
		if (original != null) {
				
			try {
								
				try {
					refreshFile(input.getFile());
				} catch (CoreException x) {
					handleCoreException(x, CEditorMessages.getString("TranslationUnitDocumentProvider.error.createElementInfo")); //$NON-NLS-1$
				}
				
				IAnnotationModel m= createAnnotationModel(input);
				IWorkingCopy c= (IWorkingCopy) original.getSharedWorkingCopy(getProgressMonitor(), fBufferFactory);
				
				DocumentAdapter a= null;
				try {
					a= (DocumentAdapter) c.getBuffer();
				} catch (ClassCastException x) {
					IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, CStatusConstants.TEMPLATE_IO_EXCEPTION, "Shared working copy has wrong buffer", x); //$NON-NLS-1$
					throw new CoreException(status);
				}
				
				FileSynchronizer f= new FileSynchronizer(input); 
				f.install();
				
				TranslationUnitFileInfo info= new TranslationUnitFileInfo(a.getDocument(), m, f, c);
				info.setModificationStamp(computeModificationStamp(input.getFile()));
				info.fStatus= a.getStatus();
				info.fEncoding= getPersistedEncoding(input);
								
				return info;
				
			} catch (CModelException x) {
				throw new CoreException(x.getStatus());
			}
		} else {		
			return super.createElementInfo(element);
		}
	}
	/*
	 * Creates a translation unit using the core model
	 */
	protected ITranslationUnit createTranslationUnit(IFile file) {
		Object element= CoreModel.getDefault().create(file);
		if (element instanceof ITranslationUnit)
			return (ITranslationUnit) element;
		return null;
	}
	/*
	 * @see AbstractDocumentProvider#disposeElementInfo(Object, ElementInfo)
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
		
		if (info instanceof TranslationUnitFileInfo) {
			TranslationUnitFileInfo cuInfo= (TranslationUnitFileInfo) info;
			cuInfo.fCopy.destroy();
		}
		
		super.disposeElementInfo(element, info);
	}

	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {

		ElementInfo elementInfo= getElementInfo(element);		
		if (elementInfo instanceof TranslationUnitFileInfo) {
			TranslationUnitFileInfo info= (TranslationUnitFileInfo) elementInfo;
			
			// update structure, assumes lock on info.fCopy
			info.fCopy.reconcile();
			
			ITranslationUnit original= (ITranslationUnit) info.fCopy.getOriginalElement();
			IResource resource= original.getResource();
			
			if (resource == null) {
				// underlying resource has been deleted, just recreate file, ignore the rest
				super.doSaveDocument(monitor, element, document, overwrite);
				return;
			}
			
			if (resource != null && !overwrite)
				checkSynchronizationState(info.fModificationStamp, resource);
				
//			if (fSavePolicy != null)
//				fSavePolicy.preSave(info.fCopy);
			
			// inform about the upcoming content change
			fireElementStateChanging(element);	
			try {
				fIsAboutToSave= true;
				// commit working copy
				info.fCopy.commit(overwrite, monitor);
			} catch (CoreException x) {
				// inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			} catch (RuntimeException x) {
				// inform about the failure
				fireElementStateChangeFailed(element);
				throw x;
			} finally {
				fIsAboutToSave= false;
			}
			
			// If here, the dirty state of the editor will change to "not dirty".
			// Thus, the state changing flag will be reset.
			
			AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
			model.updateMarkers(info.fDocument);
			
			if (resource != null)
				info.setModificationStamp(computeModificationStamp(resource));
				
//			if (fSavePolicy != null) {
//				ICompilationUnit unit= fSavePolicy.postSave(original);
//				if (unit != null) {
//					IResource r= unit.getResource();
//					IMarker[] markers= r.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
//					if (markers != null && markers.length > 0) {
//						for (int i= 0; i < markers.length; i++)
//							model.updateMarker(markers[i], info.fDocument, null);
//					}
//				}
//			}
				
			
		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}		
	}

	/**
	 * Gets the BufferFactory.
	 */
	public IBufferFactory getBufferFactory() {
		return fBufferFactory;
	}
	/**
	 * Returns the underlying resource for the given element.
	 * 
	 * @param the element
	 * @return the underlying resource of the given element
	 */
	public IResource getUnderlyingResource(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return input.getFile();
		}
		return null;
	}

	public IWorkingCopy getWorkingCopy(IEditorInput element) {
		
		ElementInfo elementInfo= getElementInfo(element);		
		if (elementInfo instanceof TranslationUnitFileInfo) {
			TranslationUnitFileInfo info= (TranslationUnitFileInfo) elementInfo;
			return info.fCopy;
		}
		return null;
	}

	protected void initializeDocument(IDocument document) {
		if (document != null) {
			IDocumentPartitioner partitioner= CUIPlugin.getDefault().getTextTools().createDocumentPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}
	/**
	 * Saves the content of the given document to the given element.
	 * This is only performed when this provider initiated the save.
	 * 
	 * @param monitor the progress monitor
	 * @param element the element to which to save
	 * @param document the document to save
	 * @param overwrite <code>true</code> if the save should be enforced
	 */
	public void saveDocumentContent(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		
		if (!fIsAboutToSave)
			return;
		
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			try {
				String encoding= getEncoding(element);
				if (encoding == null)
					encoding= ResourcesPlugin.getEncoding();
				InputStream stream= new ByteArrayInputStream(document.get().getBytes(encoding));
				IFile file= input.getFile();
				file.setContents(stream, overwrite, true, monitor);
			} catch (IOException x)  {
				IStatus s= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, x.getMessage(), x);
				throw new CoreException(s);
			}
		}
	}

	/**
	 * 
	 */
	public void shutdown() {
		// TODO Auto-generated method stub	
	}

	/**
	 * @param input
	 * @return
	 */
	public boolean isConnected(IEditorInput input) {
		return getElementInfo(input) != null;
	}
	
}
