package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.internal.core.CommonMkInfo;

import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;


public class OpenIncludeAction extends Action {


	private static final String PREFIX= "OpenIncludeAction.";
	
	private static final String DIALOG_TITLE= PREFIX + "dialog.title";
	private static final String DIALOG_MESSAGE= PREFIX + "dialog.message";
	
	private ISelectionProvider fSelectionProvider;


	public OpenIncludeAction(ISelectionProvider provider) {
		super(CPlugin.getResourceString(PREFIX + "label"));
		setDescription(CPlugin.getResourceString(PREFIX + "description"));
		setToolTipText(CPlugin.getResourceString(PREFIX + "tooltip"));
		
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);
		
		fSelectionProvider= provider;
	}
			
	public void run() {
		ICElement include= getIncludeStatement(fSelectionProvider.getSelection());
		if (include == null) {
			return;
		}
		
		try {
			IResource res = include.getUnderlyingResource();
			ArrayList filesFound= new ArrayList(4);
			if (res != null) {
				findFile(res.getProject(), new Path(include.getElementName()), filesFound);
			}
			IFile fileToOpen;
			int nElementsFound= filesFound.size();
			if (nElementsFound == 0) {
				fileToOpen= null;
			} else if (nElementsFound == 1) {
				fileToOpen= (IFile) filesFound.get(0);
			} else {
				fileToOpen= chooseFile(filesFound);
			}
			
			if (fileToOpen != null) {
				EditorUtility.openInEditor(fileToOpen);
				//IWorkbenchWindow window= CPlugin.getActiveWorkbenchWindow();
				//if (window != null) {
				//	IWorkbenchPage p= window.getActivePage();
				//	if (p != null) {
				//		p.openEditor(fileToOpen);
				//	}
				//}
			} else { // Try to get via the include path.


				CommonMkInfo mk = null;
				if (res != null)
					mk = new CommonMkInfo(res.getProject().getLocation());
				else
					mk = new CommonMkInfo();
				IPath[] paths = mk.getIncludePaths();


				IPath includePath = null;
				for (int i = 0; i < paths.length; i++) {
					if (res != null) {
						// We've already scan the project.
						if (paths[i].isPrefixOf(res.getProject().getLocation()))
							continue;
					}
					IPath path = paths[i].append(include.getElementName());
					if (path.toFile().exists()) {
						includePath = path;
						break;
					}
				}
				if (includePath != null) {
					EditorUtility.openInEditor(includePath);
					//IStorage s = new FileStorage(includePath);
					//IEditorInput ei = new ExternalEditorInput(s);
					//IWorkbenchWindow window= CPlugin.getActiveWorkbenchWindow();
					//if (window != null) {
					//	IWorkbenchPage p = window.getActivePage();
					//	if (p != null) {
					//		p.openEditor(ei, getEditorID(include.getName()));
					//	}
					//}
				}
			}
		} catch (CModelException e) {
			CPlugin.log(e.getStatus());
		} catch (CoreException e) {
			CPlugin.log(e.getStatus());
		}
	}
	
	private void findFile(IContainer parent, IPath name, ArrayList res) throws CoreException {
		IResource found= parent.findMember(name);
		if (found != null && found.getType() == IResource.FILE) {
			res.add(found);
		}
		IResource[] children= parent.members();
		for (int i= 0; i < children.length; i++) {
			if (children[i] instanceof IContainer) {
				findFile((IContainer)children[i], name, res);
			}
		}		
	}


	private IFile chooseFile(ArrayList filesFound) {
		ILabelProvider renderer= new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IFile) {
					IFile file= (IFile)element;
					return file.getName() + " - " + file.getParent().getFullPath().toString();
				}
				return super.getText(element);
			}
		};
		
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(CPlugin.getActiveWorkbenchShell(), renderer, false, false);
		dialog.setTitle(CPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(CPlugin.getResourceString(DIALOG_MESSAGE));
		dialog.setElements(filesFound);
		
		if (dialog.open() == dialog.OK) {
			return (IFile) dialog.getSelectedElement();
		}
		return null;
	}


	private static ICElement getIncludeStatement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement && ((ICElement)element).getElementType() == ICElement.C_INCLUDE) {
					return (ICElement)element;
				}
			}
		}
		return null;
	}
	
	public static boolean canActionBeAdded(ISelection selection) {
		return getIncludeStatement(selection) != null;
	}	


	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			} else {
				return registry.getDefaultEditor().getId();
			}
		}
		return null;
	}
}
