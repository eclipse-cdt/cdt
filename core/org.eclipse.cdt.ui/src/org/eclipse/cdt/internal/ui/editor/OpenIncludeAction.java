/*******************************************************************************
 *  Copyright (c) 2005, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Sergey Prigogin (Google) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=13221
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.UNCPathConverter;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;


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
			
	@Override
	public void run() {
		IInclude include= getIncludeStatement(fSelectionProvider.getSelection());
		if (include == null) {
			return;
		}
		
		try {
			IResource res = include.getUnderlyingResource();
			ArrayList<IPath> filesFound = new ArrayList<IPath>(4);
			String fullFileName= include.getFullFileName();
			if (fullFileName != null) {
				IPath fullPath = new Path(fullFileName);
				if (fullPath.isAbsolute() && fullPath.toFile().exists()) {
					filesFound.add(fullPath);
				} else if (fullPath.isUNC()) {
					IFileStore store = EFS.getStore(UNCPathConverter.getInstance().toURI(fullPath));
					if (store.fetchInfo().exists()) {
						filesFound.add(fullPath);
					}
				}
			}
			if (filesFound.isEmpty() && res != null) {
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
						IExtendedScannerInfo scanInfo = new ExtendedScannerInfo(info);
						
						boolean isSystemInclude = include.isStandard();
						
						if (!isSystemInclude) {
							// search in current directory
							IPath location= include.getTranslationUnit().getLocation();
							if (location != null) {
								String currentDir= location.removeLastSegments(1).toOSString();
								findFile(new String[] { currentDir }, includeName, filesFound);
							}
							if (filesFound.isEmpty()) {
								// search in "..." include directories
								String[] localIncludePaths = scanInfo.getLocalIncludePath();
								findFile(localIncludePaths, includeName, filesFound);
							}
						}
	
						if (filesFound.isEmpty()) {
							// search in <...> include directories
							String[] includePaths = scanInfo.getIncludePaths();
							findFile(includePaths, includeName, filesFound);
						}
					}
					
					if (filesFound.isEmpty()) {
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
				fileToOpen= filesFound.get(0);
			} else {
				fileToOpen= chooseFile(filesFound);
			}
			
			if (fileToOpen != null) {
				EditorUtility.openInEditor(fileToOpen, include);
			} 
		} catch (CModelException e) {
			CUIPlugin.log(e.getStatus());
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
		}
	}

	private void noElementsFound() {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("OpenIncludeAction.error")); //$NON-NLS-1$
		errorMsg.setMessage (CUIPlugin.getResourceString("OpenIncludeAction.error.description")); //$NON-NLS-1$
		errorMsg.open();
	}
	
	private boolean isInProject(IPath path) {
		return getWorkspaceRoot().getFileForLocation(path) != null;		
	}
	
	/**
	 * Returns the path as is, if it points to a workspace resource. If the path
	 * does not point to a workspace resource, but there are linked workspace
	 * resources pointing to it, returns the paths of these resources.
	 * Otherwise, returns the path as is. 
	 */
	private IPath[] resolveIncludeLink(IPath path) {
		if (!isInProject(path)) {
			IFile[] files = ResourceLookup.findFilesForLocation(path);
			if (files.length > 0) {
				IPath[] paths = new IPath[files.length];
				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getFullPath(); 
				}
				return paths;
			}
		}
		
		return new IPath[] { path };
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	private void findFile(String[] includePaths, String name, ArrayList<IPath> list)
			throws CoreException {
		// in case it is an absolute path
		IPath includeFile= new Path(name);		
		if (includeFile.isAbsolute()) {
			includeFile = PathUtil.getCanonicalPathWindows(includeFile);
			if (includeFile.toFile().exists()) {
				list.add(includeFile);
				return;
			}
		}
		HashSet<IPath> foundSet = new HashSet<IPath>();
		for (String includePath : includePaths) {
			IPath path = PathUtil.getCanonicalPathWindows(new Path(includePath).append(includeFile));
			File file = path.toFile();
			if (file.exists()) {
				IPath[] paths = resolveIncludeLink(path);
				for (IPath p : paths) {
					if (foundSet.add(p)) {
						list.add(p);
					}
				}
			} 
		}
	}

	/**
	 * Recurse in the project.
	 * @param parent
	 * @param name
	 * @param list
	 * @throws CoreException
	 */
	private void findFile(IContainer parent, final IPath name, final ArrayList<IPath> list) throws CoreException {
		parent.accept(new IResourceProxyVisitor() {

			@Override
			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (proxy.getType() == IResource.FILE && proxy.getName().equalsIgnoreCase(name.lastSegment())) {
					IPath rPath = proxy.requestResource().getLocation();
					if (rPath != null) {
						int numSegToRemove = rPath.segmentCount() - name.segmentCount();
						IPath sPath = rPath.removeFirstSegments(numSegToRemove);
						sPath = sPath.setDevice(name.getDevice());
						if (Platform.getOS().equals(Platform.OS_WIN32) ?
								sPath.toOSString().equalsIgnoreCase(name.toOSString()) :
								sPath.equals(name)) {
							list.add(rPath);
						}
						return false;
					}
				}
				return true;
			}
		}, 0);
	}

	private IPath chooseFile(ArrayList<IPath> filesFound) {
		ILabelProvider renderer= new LabelProvider() {
			@Override
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

	private static IInclude getIncludeStatement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List<?> list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof IInclude) {
					return (IInclude)element;
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
}
