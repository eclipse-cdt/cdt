package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;

import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class CFileElementWorkingCopy extends WorkingCopy {
	
	private IDocumentProvider fProvider;
	//private IFileEditorInput input;
	private IEditorInput input;
	
	/**
	 * Creates a working copy of this element
	 */
	public CFileElementWorkingCopy(IFileEditorInput fileInput, IDocumentProvider provider) throws CoreException {
		super(null, fileInput.getFile(), null);
		input= fileInput;
		fProvider= provider;
	}

	/**
	 * Creates a working copy of this element
	 */
	public CFileElementWorkingCopy(IStorageEditorInput StoreInput, IDocumentProvider provider) throws CoreException {
		super(null, new Path(StoreInput.getName()), null);
		input = StoreInput;
		fProvider = provider;
		IStorage storage = StoreInput.getStorage();
		super.setLocation(storage.getFullPath());
	}

	/**
	 * @see CFileElement#update
	 */
	public void update() throws CoreException {
		IDocument doc= fProvider.getDocument(input);
		if (doc != null) {
			DocumentInputStream dis= new DocumentInputStream(doc);
			try {
				parse(dis); 
			} finally {
				try { dis.close(); } catch (IOException e) {}
			}
		}
	}
}
