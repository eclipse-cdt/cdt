package org.eclipse.cdt.internal.corext.textmanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.ui.CStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;


/* package */ class TextBufferFactory {

	protected IDocumentProvider fDocumentProvider;
	private Map fFileValueMap;
	private Map fBufferValueMap;
	
	private static class Value {
		TextBuffer buffer;
		FileEditorInput input;
		IDocument document;
		IAnnotationModel annotationModel;
		int references;
		public Value(TextBuffer b, FileEditorInput i, IDocument d, IAnnotationModel m) {
			buffer= b;
			input= i;
			document= d;
			annotationModel= m;
		}
	}

	public TextBufferFactory() {
		// XXX http://dev.eclipse.org/bugs/show_bug.cgi?id=5170
		// Need way to map a file to a document without knowing any kind of document provider.
		this(CUIPlugin.getDefault().getDocumentProvider());
	}
	
	public TextBufferFactory(IDocumentProvider provider) {
		fDocumentProvider= provider;
		Assert.isNotNull(fDocumentProvider);
		fFileValueMap= new HashMap(5);
		fBufferValueMap= new HashMap(5);
	}

	public TextBuffer acquire(IFile file) throws CoreException {
		FileEditorInput input= new FileEditorInput(file);	
		
		Value value= (Value)fFileValueMap.get(input);
		if (value != null) {
			value.references++;
			return value.buffer;
		}
		
		fDocumentProvider.connect(input);
		IDocument document= fDocumentProvider.getDocument(input);
		IAnnotationModel annotationModel= fDocumentProvider.getAnnotationModel(input);
		annotationModel.connect(document);
		value= new Value(new TextBuffer(document), input, document, annotationModel);
		fFileValueMap.put(input, value);
		fBufferValueMap.put(value.buffer, value);
		value.references++;
		return value.buffer;
	}
	
	public void release(TextBuffer buffer) {
		final Value value= (Value)fBufferValueMap.get(buffer);
		if (value == null)
			return;
						
		value.references--;
		if (value.references == 0) {
			buffer.release();	
			value.annotationModel.disconnect(value.document);
			fDocumentProvider.disconnect(value.input);
			fFileValueMap.remove(value.input);
			fBufferValueMap.remove(buffer);
		}
	}
	
	public void commitChanges(TextBuffer buffer, boolean force, IProgressMonitor pm) throws CoreException {
		final Value value= (Value)fBufferValueMap.get(buffer);
		if (value == null)
			return;
		
		boolean save= force || fDocumentProvider.mustSaveDocument(value.input);
		if (save) {
			IWorkspaceRunnable action= new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					fDocumentProvider.aboutToChange(value.input);
					fDocumentProvider.saveDocument(pm, value.input, value.document, true);
				}
			};
			try {
				ResourcesPlugin.getWorkspace().run(action, pm);
			} finally {
				fDocumentProvider.changed(value.input);
			}
		}
	}
	
	public TextBuffer create(IFile file) throws CoreException {
		FileEditorInput input= new FileEditorInput(file);	
		IDocument document= fDocumentProvider.getDocument(input);
		if (document != null) {
			return new TextBuffer(new Document(document.get()));
		}
		return createFromFile(file);
	}

	private TextBuffer createFromFile(IFile file) throws CoreException {
		IDocument document;
		InputStreamReader in= null;
		try {		
			document= new Document();
			in= new InputStreamReader(new BufferedInputStream(file.getContents()));
			StringBuffer buffer= new StringBuffer();
			char[] readBuffer= new char[2048];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			document.set(buffer.toString());
			return new TextBuffer(document);
		} catch (IOException x) {
			IStatus s= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, CStatusConstants.INTERNAL_ERROR, x.getMessage(), x);
			throw new CoreException(s);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
				}
			}
		}
	}
	
	public TextBuffer create(String content) throws CoreException {
		return new TextBuffer(new Document(content));
	}
	
	public void save(TextBuffer buffer, IProgressMonitor pm) throws CoreException {
		Value value= (Value)fBufferValueMap.get(buffer);
		if (value == null)
			throwNotManaged();
		fDocumentProvider.saveDocument(pm, value.input, value.document, true);
	}

	public void aboutToChange(TextBuffer buffer) throws CoreException {
		Value value= (Value)fBufferValueMap.get(buffer);
		if (value == null)
			throwNotManaged();
		fDocumentProvider.aboutToChange(value.input);
	}
		
	public void changed(TextBuffer buffer) throws CoreException {
		Value value= (Value)fBufferValueMap.get(buffer);
		if (value == null)
			throwNotManaged();
		fDocumentProvider.changed(value.input);
	}
	
	private void throwNotManaged() throws CoreException {
		IStatus s= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 
			CStatusConstants.INTERNAL_ERROR, "TextBufferFactory.bufferNotManaged", null); //$NON-NLS-1$
		throw new CoreException(s);
	}
}

