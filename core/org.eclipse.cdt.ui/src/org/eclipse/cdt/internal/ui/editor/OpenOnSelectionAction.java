package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.index.TagFlags;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;


/**
 * This action opens a java editor on the element represented by text selection of
 * the connected java source viewer.
 * 
 * Use action from package org.eclipse.jdt.ui.actions
 */
public class OpenOnSelectionAction extends Action {
	
	private class TagEntryLabelProvider extends LabelProvider {

		public TagEntryLabelProvider() {
		}
		
		public Image getImage(Object element) {
			if(element instanceof ITagEntry) {
				int kind = ((ITagEntry)element).getKind();
				switch (kind) {
					case TagFlags.T_PROTOTYPE:
						return CPluginImages.get(CPluginImages.IMG_OBJS_DECLARATION);
					case TagFlags.T_CLASS:
						return CPluginImages.get(CPluginImages.IMG_OBJS_CLASS);
					case TagFlags.T_ENUM:
					case TagFlags.T_VARIABLE:
					case TagFlags.T_MEMBER:
						return CPluginImages.get(CPluginImages.IMG_OBJS_FIELD);
					case TagFlags.T_FUNCTION:
						return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
					case TagFlags.T_STRUCT:
						return CPluginImages.get(CPluginImages.IMG_OBJS_STRUCT);
					case TagFlags.T_UNION:
						return CPluginImages.get(CPluginImages.IMG_OBJS_UNION);
					case TagFlags.T_MACRO:
						return CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
				}
			}
			return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
		}

		public String getText(Object element) {
			if(element instanceof ITagEntry) {
				ITagEntry entry = (ITagEntry) element;
				if(entry.getIFile() != null) {
					return entry.getIFile().getName() + ":" + entry.getTagName() + ":" + entry.getLineNumber() + " - " + entry.getIFile().getFullPath().toOSString();
				}
				return entry.getFileName() + ":" + entry.getTagName() + ":" + entry.getLineNumber();
			} else {
				return "";
			}
		}
	};
		
	private String fDialogTitle;
	private String fDialogMessage;
	protected CEditor fEditor;
	
	
	/**
	 * Creates a new action with the given label and image.
	 */
	protected OpenOnSelectionAction() {
		super();
		
		setText(CEditorMessages.getString("OpenOnSelection.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenOnSelection.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenOnSelection.description")); //$NON-NLS-1$
		setDialogTitle(CEditorMessages.getString("OpenOnSelection.dialog.title")); //$NON-NLS-1$
		setDialogMessage(CEditorMessages.getString("OpenOnSelection.dialog.message")); //$NON-NLS-1$
	}
	
	/**
	 * Creates a new action with the given image.
	 */
	public OpenOnSelectionAction(ImageDescriptor image) {
		this();
		setImageDescriptor(image);
	}
	
	/**
	 * Creates a new action with the given editor
	 */
	public OpenOnSelectionAction(CEditor editor) {
		this();
		fEditor = editor;
	}
	
	/**
	 * Creates a new action without label. Initializing is 
	 * subclass responsibility.
	 */
	protected void OOpenOnSelectionAction(String label) {
		//this();
		//super(label);
	}
	
	protected void setDialogTitle(String title) {
		fDialogTitle= title;
	}
	
	protected void setDialogMessage(String message) {
		fDialogMessage= message;
	}
	
	public void setContentEditor(CEditor editor) {	
		fEditor= editor;
	}
	
	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		
		IndexModel model = IndexModel.getDefault();
		if (model != null && fEditor.getSelectionProvider() != null) {
			ITextSelection selection= (ITextSelection) fEditor.getSelectionProvider().getSelection();
			try {
				String sel = selection.getText();
				IFile file = fEditor.getInputFile();
				if(file == null)
					return;
				IProject project = file.getProject();
				if(project == null)
					return;
				ITagEntry[] result= model.query(project,sel);
				
				List filtered = new ArrayList();
				if (result != null && result.length > 0) {
					filterResolveResults(result, filtered);
				}
						
				IProject[] p = project.getReferencedProjects();
				for ( int j= 0; j < p.length; j++ ) {
					result= model.query(p[j],sel);
					if (result != null && result.length > 0) {
						filterResolveResults(result, filtered);
					}
				}
						
				if (filtered.isEmpty() == false) {
					ITagEntry selected= selectCElement(filtered, getShell(), fDialogTitle, fDialogMessage);
					if (selected != null) {
						open(selected);
						return;
					}
				}
			} catch	 (CModelException x) {
				CUIPlugin.log(x.getStatus());
			} catch (PartInitException x) {
				CUIPlugin.log(x);
			} catch (CoreException x) {
				CUIPlugin.log(x);
			}
		}
		
		getShell().getDisplay().beep();		
	}

	protected Shell getShell() {
		return fEditor.getSite().getShell();
	}
	
	
	
	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	protected void open(ITagEntry element) throws CModelException, PartInitException {
		IEditorPart part= EditorUtility.openInEditor(element.getIFile());
		int line = element.getLineNumber();
		if(line > 0) line--;
		if(part instanceof CEditor) {
			CEditor ed = (CEditor)part;
			
			try {					
				IDocument document= ed.getDocumentProvider().getDocument(ed.getEditorInput());
				if(line > 3) {
					ed.selectAndReveal(document.getLineOffset(line - 3), 0);
				}
				ed.selectAndReveal(document.getLineOffset(line), 0);
			} catch (BadLocationException e) {}
		}
	}
	
	/**
	 * Filters out source references from the given code resolve results.
	 * A utility method that can be called by subclassers. 
	 */
	protected List filterResolveResults(ITagEntry[] codeResolveResults, List list) {
		int nResults= codeResolveResults.length;
		List refs= list;
		for (int i= 0; i < nResults; i++) {
			if (codeResolveResults[i].getKind() != TagFlags.T_PROTOTYPE) {
				refs.add(codeResolveResults[i]);
			}
		}
		return refs;
	}
						
	/**
	 * Shows a dialog for resolving an ambigous C element.
	 * Utility method that can be called by subclassers.
	 */
	protected ITagEntry selectCElement(List elements, Shell shell, String title, String message) {
		
		int nResults= elements.size();
		
		if (nResults == 0)
			return null;
		
		if (nResults == 1)
			return (ITagEntry) elements.get(0);
		
		//int flags= CElementLabelProvider.SHOW_DEFAULT
		//				| CElementLabelProvider.SHOW_QUALIFIED
		//				| CElementLabelProvider.SHOW_ROOT;
						
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new TagEntryLabelProvider(), false, false);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements);
		
		if (dialog.open() == Window.OK) {
			Object[] selection= dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults= selection.length;
				for (int i= 0; i < nResults; i++) {
					Object current= selection[i];
					if (current instanceof ITagEntry)
						return (ITagEntry) current;
				}
			}
		}		
		return null;
	}					
}

