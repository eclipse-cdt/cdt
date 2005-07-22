/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;


public class OpenIncludeAction extends Action {


	private static final String PREFIX= "OpenIncludeAction."; //$NON-NLS-1$
	
	private static final String DIALOG_TITLE= PREFIX + "dialog.title"; //$NON-NLS-1$
	private static final String DIALOG_MESSAGE= PREFIX + "dialog.message"; //$NON-NLS-1$
	
	private ISelectionProvider fSelectionProvider;


	public OpenIncludeAction(ISelectionProvider provider) {
		super(CUIPlugin.getResourceString(PREFIX + "label")); //$NON-NLS-1$
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$
		
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
			ArrayList filesFound = new ArrayList(4);
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
						HashSet found = new HashSet();
						findFile(includePaths, includeName, filesFound, found);
					}
					if (filesFound.size() == 0) {
						// Fall back and search the project
						findFile(proj, new Path(includeName), filesFound);
					}
				}
			}
			IPath fileToOpen;
			int nElementsFound= filesFound.size();
			if (nElementsFound == 0) {
				noElementsFound();
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
					ICProject cproject = include.getCProject();
					ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, fileToOpen);
					if (unit != null) {
						EditorUtility.openInEditor(unit);
					} else {
						// try linked files
						IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(fileToOpen);
						for(int i=0; i<files.length; i++) {
							if (files[i].isAccessible()) {
								EditorUtility.openInEditor(files[i]);
								break;
							}
						}
					}
				}
			} 
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e.getStatus());
		}
	}

	/**
	 * 
	 */
	private void noElementsFound() {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("OpenIncludeAction.error")); //$NON-NLS-1$
		errorMsg.setMessage (CUIPlugin.getResourceString("OpenIncludeAction.error.description")); //$NON-NLS-1$
		errorMsg.open();
	}
	
	private boolean isInProject(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path) != null;		
	}
	
	// If 'path' is not a resource in the current workspace and
	// it is a symlink to a resource that is in the current workspace,
	// use the symlink target instead
	private IPath resolveIncludeLink(File file, IPath path) {
		if (isInProject(path))
			return path;
		
		try {
			String canon = file.getCanonicalPath();
			if (canon.equals(file.getAbsolutePath()))
				return path;
			
			IPath p = Path.fromOSString(canon);
			if (isInProject(p))
			    return p;
		} catch (IOException e) {
			// Do nothing; the path is not resolved
		}
		
		return path;
	}
	
	private void findFile(String[] includePaths,  String name, ArrayList list,
			HashSet foundSet)  throws CoreException {
		for (int i = 0; i < includePaths.length; i++) {
			IPath path = new Path(includePaths[i] + "/" + name); //$NON-NLS-1$
			File file = path.toFile();
			if (file.exists()) {
				IPath p = resolveIncludeLink(file, path);
				if (!foundSet.contains(p)) {
					foundSet.add(p);
					list.add(p);
				}
			} 
		}
	}

	/**
	 * Recuse in the project.
	 * @param parent
	 * @param name
	 * @param list
	 * @throws CoreException
	 */
	private void findFile(IContainer parent, final IPath name, final ArrayList list) throws CoreException {
		parent.accept(new IResourceProxyVisitor() {

			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (proxy.getType() == IResource.FILE && proxy.getName().equalsIgnoreCase(name.lastSegment())) {
					list.add(proxy.requestResource().getLocation());
					return false;
				}
				return true;
			}
		}, 0);
	}


	private IPath chooseFile(ArrayList filesFound) {
		ILabelProvider renderer= new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IPath) {
					IPath file= (IPath)element;
					return file.lastSegment() + " - "  + file.toString(); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		};
		
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(CUIPlugin.getActiveWorkbenchShell(), renderer, false, false);
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
			}
			return IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID;
		}
		return null;
	}
}
