package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;


public class OpenIncludeAction extends Action {


	private static final String PREFIX= "OpenIncludeAction.";
	
	private static final String DIALOG_TITLE= PREFIX + "dialog.title";
	private static final String DIALOG_MESSAGE= PREFIX + "dialog.message";
	
	private ISelectionProvider fSelectionProvider;


	public OpenIncludeAction(ISelectionProvider provider) {
		super(CUIPlugin.getResourceString(PREFIX + "label"));
		setDescription(CUIPlugin.getResourceString(PREFIX + "description"));
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip"));
		
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
				IProject proj = res.getProject();
				String includeName = include.getElementName();
				// Search in the scannerInfo information
				IScannerInfoProvider provider =  CCorePlugin.getDefault().getScannerInfoProvider(proj);
				if (provider != null) {
					IScannerInfo info = provider.getScannerInformation(res);
					// XXXX this should fall back to project by itself
					if (info == null) {
						info = provider.getScannerInformation(proj);
					}
					if (info != null) {
						String[] includePaths = info.getIncludePaths();
						findFile(includePaths, includeName, filesFound);
					} else {
						// Fall back and search the project
						findFile(proj, new Path(includeName), filesFound);
					}
				}
			}
			IPath fileToOpen;
			int nElementsFound= filesFound.size();
			if (nElementsFound == 0) {
				fileToOpen= null;
			} else if (nElementsFound == 1) {
				fileToOpen= (IPath) filesFound.get(0);
			} else {
				fileToOpen= chooseFile(filesFound);
			}
			
			if (fileToOpen != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(fileToOpen);
				if (file != null) {
					EditorUtility.openInEditor(file);
				}  else {
					FileStorage storage = new FileStorage(null, fileToOpen);
					EditorUtility.openInEditor(storage);
				}
			} 
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		}
	}

	private void findFile(String[] includePaths,  String name, ArrayList list)  throws CoreException {
		for (int i = 0; i < includePaths.length; i++) {
			IPath path = new Path(includePaths[i] + "/" + name);
			File file = path.toFile();
			if (file.exists()) {
				list.add(path);
			} 
		}
	}

	private void findFile(IContainer parent, IPath name, ArrayList list) throws CoreException {
		IResource found= parent.findMember(name);
		if (found != null && found.getType() == IResource.FILE) {
			list.add(found.getLocation());
		}
		IResource[] children= parent.members();
		for (int i= 0; i < children.length; i++) {
			if (children[i] instanceof IContainer) {
				findFile((IContainer)children[i], name, list);
			}
		}		
	}


	private IPath chooseFile(ArrayList filesFound) {
		ILabelProvider renderer= new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IPath) {
					IPath file= (IPath)element;
					return file.lastSegment() + " - "  + file.toString();
				}
				return super.getText(element);
			}
		};
		
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(CUIPlugin.getDefault().getActiveWorkbenchShell(), renderer, false, false);
		dialog.setTitle(CUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(CUIPlugin.getResourceString(DIALOG_MESSAGE));
		dialog.setElements(filesFound);
		
		if (dialog.open() == Window.OK) {
			return (IPath) dialog.getSelectedElement();
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
		ICElement include = getIncludeStatement(selection);
		if (include != null) {
			IResource res = include.getUnderlyingResource();
			if (res != null) {
				return true; 
			}
		}
		return false;
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
