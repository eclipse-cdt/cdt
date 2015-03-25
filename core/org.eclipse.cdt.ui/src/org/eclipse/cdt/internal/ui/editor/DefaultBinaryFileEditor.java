/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * A readonly editor to view binary files. This default implementation displays the GNU objdump output of the
 * binary as plain text. If no objdump output can be obtained, the binary content is displayed.
 */
public class DefaultBinaryFileEditor extends AbstractTextEditor implements IResourceChangeListener {

	/**
	 * A storage editor input for binary files.
	 */
	public static class BinaryFileEditorInput extends PlatformObject implements IStorageEditorInput {
		private final IBinary fBinary;
		private IStorage fStorage;

		/**
		 * Create an editor input from the given binary.
		 * 
		 * @param binary
		 */
		public BinaryFileEditorInput(IBinary binary) {
			fBinary = binary;
		}

		/*
		 * @see org.eclipse.ui.IEditorInput#exists()
		 */
		@Override
		public boolean exists() {
			return fBinary.exists();
		}

		/*
		 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
		 */
		@Override
		public ImageDescriptor getImageDescriptor() {
			IFile file = (IFile) fBinary.getResource();
			IContentType contentType = IDE.getContentType(file);
			return PlatformUI.getWorkbench().getEditorRegistry()
					.getImageDescriptor(file.getName(), contentType);
		}

		/*
		 * @see org.eclipse.ui.IEditorInput#getName()
		 */
		@Override
		public String getName() {
			return fBinary.getElementName();
		}

		/*
		 * @see org.eclipse.ui.IEditorInput#getPersistable()
		 */
		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		/*
		 * @see org.eclipse.ui.IEditorInput#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			return fBinary.getResource().getFullPath().toString();
		}

		/*
		 * @see org.eclipse.ui.IStorageEditorInput#getStorage()
		 */
		@Override
		public IStorage getStorage() throws CoreException {
			if (fStorage == null) {
				IBinaryParser.IBinaryObject object = fBinary
						.getAdapter(IBinaryParser.IBinaryObject.class);
				if (object != null) {
					IGnuToolFactory factory = object.getBinaryParser().getAdapter(
							IGnuToolFactory.class);
					if (factory != null) {
						Objdump objdump = factory.getObjdump(object.getPath());
						if (objdump != null) {
							try {
								// limit editor to X MB, if more - users should use objdump in command
								// this is UI blocking call, on 56M binary it takes more than 15 min
								// and generates at least 2.5G of assembly
								int limitBytes = 6 * 1024 * 1024; // this can run reasonably within seconds
								byte[] output = objdump.getOutput(limitBytes);
								if (output.length >= limitBytes) {
									// add a message for user
									String text = CEditorMessages.DefaultBinaryFileEditor_TruncateMessage;
									String message = "\n\n--- " + text + " ---\n" + objdump.toString(); //$NON-NLS-1$ //$NON-NLS-2$
									System.arraycopy(message.getBytes(), 0, output,
											limitBytes - message.length(), message.length());
								}
								fStorage = new FileStorage(new ByteArrayInputStream(output), object.getPath());
							} catch (IOException exc) {
								CUIPlugin.log(exc);
							}
						}
					}
				}
				if (fStorage == null) {
					// backwards compatibility
					fStorage = EditorUtility.getStorage(fBinary);
					if (fStorage == null) {
						// fall back to binary content
						fStorage = (IFile) fBinary.getResource();
					}
				}
			}
			return fStorage;
		}
	}

	/**
	 * A storage document provider for binary files.
	 */
	public static class BinaryFileDocumentProvider extends StorageDocumentProvider {

		/*
		 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#createDocument(java.lang.Object)
		 */
		@Override
		protected IDocument createDocument(Object element) throws CoreException {
			IFile file = ResourceUtil.getFile(element);
			if (file != null) {
				ICElement cElement = CoreModel.getDefault().create(file);
				if (cElement instanceof IBinary) {
					element = new BinaryFileEditorInput((IBinary) cElement);
				}
			}
			return super.createDocument(element);
		}

		@Override
		public long getModificationStamp(Object element) {
			if (element instanceof FileEditorInput) {
				return ((FileEditorInput) element).getFile().getModificationStamp();
			}
			return 0;
		}

		/*
		 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#isModifiable(java.lang.Object)
		 */
		@Override
		public boolean isModifiable(Object element) {
			return false;
		}

		/*
		 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#isReadOnly(java.lang.Object)
		 */
		@Override
		public boolean isReadOnly(Object element) {
			return true;
		}

	}

	public DefaultBinaryFileEditor() {
		super();
		setDocumentProvider(new BinaryFileDocumentProvider());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite
	 * , org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);
		sourceViewer.setEditable(false);
		return sourceViewer;
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				event.getDelta().accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) {
						if (delta.getResource().getName().equals(getEditorInput().getName())) {
							refresh();
							return false;
						}
						return true;
					}
				});
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	protected void refresh() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					doSetInput(getEditorInput());
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		});
	}
}
