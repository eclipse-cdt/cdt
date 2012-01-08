/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;

import org.eclipse.cdt.internal.ui.compare.CMergeViewer;
import org.eclipse.cdt.internal.ui.text.CTextTools;

/**
 * @author Emanuel Graf
 *
 */
public class CTextEditChangePreviewViewer  implements IChangePreviewViewer {
	
	private CPPMergeViewer viewer;
	private CTextEditChangePane viewerPane;
	private CTextEditChangePreviewViewerContentProvider textEditChangeContentProvider;
	
	private static class CTextEditChangePane extends CompareViewerPane{

		/**
		 * @param parent
		 * @param style
		 */
		public CTextEditChangePane(Composite parent, int style) {
			super(parent, style);
		}
		
	}
	
	private class CPPMergeViewer extends CMergeViewer{

		/**
		 * @param parent
		 * @param styles
		 * @param mp
		 */
		public CPPMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
			super(parent, styles, mp);
		}

		@Override
		protected CSourceViewerConfiguration getSourceViewerConfiguration() {
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
			return new CSourceViewerConfiguration(tools.getColorManager(), store, null, tools.getDocumentPartitioning());
		}

		@Override
		protected void configureTextViewer(TextViewer textViewer) {
			if (textViewer instanceof SourceViewer) {
				((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
			}
		}
		
		
	}

	
	private class CTextEditChangePreviewViewerContentProvider implements IMergeViewerContentProvider{

		@Override
		public Object getAncestorContent(Object input) {
			if (input instanceof ICompareInput)
				return ((ICompareInput) input).getAncestor();
			return null;
		}

		@Override
		public Image getAncestorImage(Object input) {
			if (input instanceof ICompareInput) {
				ITypedElement ancestor = ((ICompareInput) input).getAncestor();
				if(ancestor != null) {
					return ancestor.getImage();
				}
			}
			return null;
		}

		@Override
		public String getAncestorLabel(Object input) {
			if (input instanceof ICompareInput) {
				ITypedElement ancestor = ((ICompareInput) input).getAncestor();
				if(ancestor != null) {
					return ancestor.getName();
				}
			}
			return null;
		}

		@Override
		public Object getLeftContent(Object input) {
			if (input instanceof ICompareInput)
				return ((ICompareInput) input).getLeft();
			return null;
		}

		@Override
		public Image getLeftImage(Object input) {
			if (input instanceof ICompareInput)
				return ((ICompareInput) input).getLeft().getImage();
			return null;
		}

		@Override
		public String getLeftLabel(Object input) {
			return Messages.CTextEditChangePreviewViewer_OrgSource; 
		}

		@Override
		public Object getRightContent(Object input) {
			if (input instanceof ICompareInput)
				return ((ICompareInput) input).getRight();
			return null;
		}

		@Override
		public Image getRightImage(Object input) {
			if (input instanceof ICompareInput)
				return ((ICompareInput) input).getRight().getImage();
			return null;
		}

		@Override
		public String getRightLabel(Object input) {
			return Messages.CTextEditChangePreviewViewer_RefactoredSource; 
		}

		@Override
		public boolean isLeftEditable(Object input) {
			return false;
		}

		@Override
		public boolean isRightEditable(Object input) {
			return false;
		}

		@Override
		public void saveLeftContent(Object input, byte[] bytes) {
			//No Edits
		}

		@Override
		public void saveRightContent(Object input, byte[] bytes) {
			//No Edits
		}

		@Override
		public boolean showAncestor(Object input) {
			//no Ancestor
			return false;
		}

		@Override
		public void dispose() {
			//
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			//Nothing to do
		}
		
	}
	
	private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor, IResourceProvider {
		private static final String ENCODING= "UTF-8"; //$NON-NLS-1$
		private final String fContent;
		private final String fType;
		private final IResource fResource;
		public CompareElement(String content, String type, IResource resource) {
			fContent= content;
			fType= type;
			fResource= resource;
		}
		@Override
		public String getName() {
			return ""; //$NON-NLS-1$
		}
		@Override
		public Image getImage() {
			return null;
		}
		@Override
		public String getType() {
			return fType;
		}
		@Override
		public InputStream getContents() throws CoreException {
			try {
				return new ByteArrayInputStream(fContent.getBytes(ENCODING));
			} catch (UnsupportedEncodingException e) {
				return new ByteArrayInputStream(fContent.getBytes());
			}
		}
		@Override
		public String getCharset() {
			return ENCODING;
		}
		@Override
		public IResource getResource() {
			return fResource;
		}
	}

	@Override
	public void createControl(Composite parent) {
		CompareConfiguration compConfig = new CompareConfiguration();
		compConfig.setLeftEditable(false);
		compConfig.setRightEditable(false);
		viewerPane = new CTextEditChangePane(parent, SWT.BORDER | SWT.FLAT);
		viewer =  new CPPMergeViewer(viewerPane,SWT.MULTI | SWT.FULL_SELECTION, compConfig);
		textEditChangeContentProvider = new CTextEditChangePreviewViewerContentProvider();
		viewer.setContentProvider(textEditChangeContentProvider);
		viewerPane.setContent(viewer.getControl());
	}

	@Override
	public Control getControl() {
		return viewerPane;
	}

	@Override
	public void setInput(ChangePreviewViewerInput input) {
		try {
			Change change= input.getChange();
			if (change instanceof CTextFileChange) {
				CTextFileChange editChange= (CTextFileChange)change;
				setInput(editChange, editChange.getCurrentContent(new NullProgressMonitor()), editChange.getPreviewContent(new NullProgressMonitor()), editChange.getTextType());
				return;
			}
			viewer.setInput(null);
		} catch (CoreException e) {
			viewer.setInput(null);
		}
	}

	private void setInput(CTextFileChange change, String left, String right, String type) {
		IFile resource = change.getFile();
		viewerPane.setText(resource.getName());
		viewerPane.setImage(new CElementLabelProvider().getImage(resource));
		viewer.setInput(new DiffNode(
			new CompareElement(left, type, resource),
			new CompareElement(right, type, resource)));
	}

}
