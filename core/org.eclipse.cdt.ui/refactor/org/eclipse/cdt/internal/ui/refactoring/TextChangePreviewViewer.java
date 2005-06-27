/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange.EditChange;

public class TextChangePreviewViewer implements IChangePreviewViewer {

	private ComparePreviewer fViewer;
	
	private static class TextEditChangeInput {
		EditChange change;
		int surroundingLines;
		
		EditChange[] changes;
		IRegion range;
	}
	
	private static class ComparePreviewer extends CompareViewerSwitchingPane {
		private CompareConfiguration fCompareConfiguration;
		public ComparePreviewer(Composite parent) {
			super(parent, SWT.BORDER | SWT.FLAT, true);
			fCompareConfiguration= new CompareConfiguration();
			fCompareConfiguration.setLeftEditable(false);
			fCompareConfiguration.setLeftLabel(RefactoringMessages.getString("ComparePreviewer.original_source")); //$NON-NLS-1$
			fCompareConfiguration.setRightEditable(false);
			fCompareConfiguration.setRightLabel(RefactoringMessages.getString("ComparePreviewer.refactored_source")); //$NON-NLS-1$
		}
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			return CompareUI.findContentViewer(oldViewer, (ICompareInput)input, this, fCompareConfiguration);
		}
		public void setText(String text) {
			/*
			Object input= getInput();
			if (input instanceof CompareInput) {
				CompareInput cInput= (CompareInput)input;
				setImage(fLabelProvider.getImage(cInput.getChangeElement()));
				super.setText(fLabelProvider.getText(cInput.getChangeElement()));
			} else {
				super.setText(text);
				setImage(null);
			}
			*/
			super.setText(text);
		}
	}
	
	private static class CompareElement implements ITypedElement, IStreamContentAccessor {
		private InputStream fContent;
		private String fType;
		public CompareElement(String content, String type) {
			fContent= createInputStream(content);
			fType= type;
		}
		public String getName() {
			return RefactoringMessages.getString("ComparePreviewer.element_name"); //$NON-NLS-1$
		}
		public Image getImage() {
			return null;
		}
		public String getType() {
			return fType;
		}
		public InputStream getContents() throws CoreException {
			return fContent;
		}
		private static InputStream createInputStream(String s) {
			try {
				return new ByteArrayInputStream(s.getBytes(ResourcesPlugin.getEncoding()));
			} catch (UnsupportedEncodingException e) {
				return new ByteArrayInputStream(s.getBytes());
			}
		}
	}
	
	public static Object createInput(TextChange change) {
		return change;
	}
	
	public static Object createInput(EditChange change, int surroundingLines) {
		TextEditChangeInput result= new TextEditChangeInput();
		result.change= change;
		result.surroundingLines= surroundingLines;
		return result;
	}
	
	public static Object createInput(EditChange[] changes, IRegion range) {
		TextEditChangeInput result= new TextEditChangeInput();
		result.changes= changes;
		result.range= range;
		return result;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fViewer= new ComparePreviewer(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#getControl()
	 */
	public Control getControl() {
		return fViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#setInput(org.eclipse.jdt.internal.ui.refactoring.ChangeElement)
	 */
	public void setInput(Object input) throws CoreException {
		if (input instanceof TextChange) {
			TextChange change= (TextChange)input;
			setInput(change.getCurrentContent(), change.getPreviewContent(), change.getTextType());
			return;
		} else if (input instanceof TextEditChangeInput) {
			TextEditChangeInput edi= (TextEditChangeInput)input;
			if (edi.change != null && edi.surroundingLines >= 0) {
				TextChange.EditChange editChange= edi.change;
				TextChange change= editChange.getTextChange();
				setInput(change.getCurrentContent(editChange, 2),
					change.getPreviewContent(editChange, 2),
					change.getTextType());
				return;
			} else if (edi.changes != null && edi.changes.length > 0 && edi.range != null) {
				TextChange change= edi.changes[0].getTextChange();
				setInput(change.getCurrentContent(edi.range),
					change.getPreviewContent(edi.changes, edi.range),
					change.getTextType());
				return;
			}
		} else {
			fViewer.setInput(null);
		}
		/* 
		} else if (change instanceof CreateTextFileChange){
			CreateTextFileChange ctfc= (CreateTextFileChange)change;
			String type= ctfc.isJavaFile() ? JAVA_TYPE: TEXT_TYPE;
			setInput(input, ctfc.getCurrentContent(), ctfc.getPreview(), type);
			return;
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer#refresh()
	 */
	public void refresh() {
		fViewer.getViewer().refresh();
	}
	
	private void setInput(String left, String right, String type) {
		fViewer.setInput(new DiffNode( 
			new CompareElement(left, type), 
			new CompareElement(right, type)));
	}	
}
