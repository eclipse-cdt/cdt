/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IComment;
import org.eclipse.cdt.make.core.makefile.IEmptyLine;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.make.internal.core.makefile.NullMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * MakefileContentOutlinePage
 */
public class MakefileContentOutlinePage extends ContentOutlinePage implements IContentOutlinePage {

	private class MakefileContentProvider implements ITreeContentProvider {

		protected boolean showMacroDefinition = true;
		protected boolean showTargetRule = true;
		protected boolean showInferenceRule = true;

		protected IMakefile makefile;
		protected IMakefile nullMakefile = new NullMakefile();

		/* (non-Javadoc)
		* @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		*/
		public Object[] getChildren(Object element) {
			if (element == fInput) {
				return getElements(element);
			} else if (element instanceof IParent) {
				return getElements(element);
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IDirective)
				return fInput;
			return fInput;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element == fInput) {
				return true;
			} else if (element instanceof IParent) {
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			IDirective[] statements;
			if (inputElement == fInput) {
				statements = makefile.getStatements();
			} else if (inputElement instanceof IParent) {
				statements = ((IParent)inputElement).getStatements();
			} else {
				statements = new IDirective[0];
			}
			List list = new ArrayList(statements.length);
			for (int i = 0; i < statements.length; i++) {
				if (showMacroDefinition && statements[i] instanceof IMacroDefinition) {
					list.add(statements[i]);
				} else if (showInferenceRule && statements[i] instanceof IInferenceRule) {
					list.add(statements[i]);
				} else if (showTargetRule && statements[i] instanceof ITargetRule) {
					list.add(statements[i]);
				} else {
					boolean irrelevant = (statements[i] instanceof IComment ||
						statements[i] instanceof IEmptyLine); 
					if (!irrelevant) {
						list.add(statements[i]);
					}
				}
			}
			return list.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				makefile = nullMakefile;
			}

			if (newInput != null) {
				IWorkingCopyManager manager= MakeUIPlugin.getDefault().getWorkingCopyManager();
				makefile = manager.getWorkingCopy((IEditorInput)newInput);
				if (makefile == null) {
					makefile = nullMakefile;
				}
//				IDocument document= fDocumentProvider.getDocument(newInput);
//				makefile = fEditor.getMakefile();
//				try {
//					String content = document.get();
//					Reader r = new StringReader(content);
//					makefile.parse(r);
//				} catch (IOException e) {
//				}
			}
		}

	}

	private class MakefileLabelProvider extends LabelProvider implements ILabelProvider {

		/* (non-Javadoc)
		* @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		*/
		public Image getImage(Object element) {
			if (element instanceof ITargetRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_TARGET_RULE);
			} else if (element instanceof IInferenceRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_INFERENCE_RULE);
			} else if (element instanceof IMacroDefinition) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_MACRO);
			} else if (element instanceof IParent) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_RELATION);
			} else if (element instanceof ICommand) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_COMMAND);
			}
			return super.getImage(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String name;
			if (element instanceof IRule) {
				name = ((IRule) element).getTarget().toString().trim();
			} else if (element instanceof IMacroDefinition) {
				name = ((IMacroDefinition) element).getName().trim();
			} else {
				name = super.getText(element);
			}
			if (name != null) {
				name = name.trim();
				if (name.length() > 20) {
					name = name.substring(0, 20) + "..."; //$NON-NLS-1$
				}
			}
			return name;
		}

	}

	protected MakefileEditor fEditor;
	protected Object fInput;

	public MakefileContentOutlinePage(MakefileEditor editor) {
		super();
		fEditor = editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();

		/*
		 * We might want to implement our own content provider.
		 * This content provider should be able to work on a dom like tree
		 * structure that resembles the file contents.
		 */
		viewer.setContentProvider(new MakefileContentProvider());

		/*
		 * We probably also need our own label provider.
		 */
		viewer.setLabelProvider(new MakefileLabelProvider());

		if (fInput != null) {
			viewer.setInput(fInput);
		}
	}

	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input) {
		fInput = input;
		update();
	}

	/* (non-Javadoc)
	 * Method declared on ContentOutlinePage
	 */
	public void selectionChanged(SelectionChangedEvent event) {

		super.selectionChanged(event);

		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			fEditor.resetHighlightRange();
		} else if (selection instanceof IStructuredSelection){
			Object element =  ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IDirective) {
				IDirective statement = (IDirective)element;
				int startLine = statement.getStartLine() - 1;
				int endLine = statement.getEndLine() - 1;
				try {
					IDocument doc = fEditor.getDocumentProvider().getDocument(fInput);
					int start = doc.getLineOffset(startLine);
					int len = doc.getLineLength(endLine) - 1;
					int length = (doc.getLineOffset(endLine) + len) - start;
					fEditor.setHighlightRange(start, length, true);
				} catch (IllegalArgumentException x) {
					fEditor.resetHighlightRange();
				} catch (BadLocationException e) {
					fEditor.resetHighlightRange();
				}
			}
		}
	}

	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				//viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}

}
