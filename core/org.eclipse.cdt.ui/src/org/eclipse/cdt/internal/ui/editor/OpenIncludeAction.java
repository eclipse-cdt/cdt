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
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;


public class OpenIncludeAction extends Action {
	private static final String PREFIX= "OpenIncludeAction.";
	
	private static final String DIALOG_TITLE= PREFIX + "dialog.title";
	private static final String DIALOG_MESSAGE= PREFIX + "dialog.message";
	
	private ISelectionProvider fSelectionProvider;
	private IResource		   fBaseResource;

	public OpenIncludeAction(ISelectionProvider provider) {
		this(provider, null);
	}

	public OpenIncludeAction(ISelectionProvider provider, IResource baseResource) {
		super(CUIPlugin.getResourceString(PREFIX + "label"));
		setDescription(CUIPlugin.getResourceString(PREFIX + "description"));
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip"));
		
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);
		
		fSelectionProvider= provider;
		fBaseResource = baseResource;
	}
	
	/**
	 * Sets the base resource which will be used as a reference to extract out include
	 * path information if the selected element does not already contain resource 
	 * reference information.
	 * 
	 * @param resource IResource used as a reference to extract include path information or null not to have one
	 */
	public void setBaseResource(IResource resource) {
		fBaseResource = resource;
	}

	/**
	 * Returns the base resource currently used as a reference to extract out include
	 * path information.
	 * 
	 * @param resource IResource used to extract the information or null if there is no base resource
	 */
	public IResource getBaseResource() {
		return fBaseResource;
	}
			
	public void run() {
		ICElement include= getIncludeStatement(fSelectionProvider.getSelection());
		if (include == null) {
			return;
		}

		/* FIXME 
		 * The information about whether this is a local include file or not
		 * a local include file is in the Include specific ICElement.  Unfortunately
		 * while we know that, it isn't part of the public interface.  For now we
		 * just assume that every header has the possibility of being local.
		 */
		boolean isLocal = true;
		
		try {
			IResource res = include.getUnderlyingResource();
			if(res == null) {
				res = fBaseResource;
			}
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
						//If the header is local, then look in the current location first
						String[] includePaths = info.getIncludePaths();

						if(isLocal) {		//Prepend our path at the start of this array
							String [] newIncludePaths = new String[includePaths.length + 1];
							newIncludePaths[0] = res.getLocation().removeLastSegments(1).toOSString();
							System.arraycopy(includePaths, 0, newIncludePaths, 1, includePaths.length);			
							includePaths = newIncludePaths;
						}
						
						findFile(includePaths, includeName, filesFound);
					} else {
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
				}  else if(activateExistingOpenedExternalFile(fileToOpen) == false){
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
	
	/**
	 * Check to see if this file has already been opened in another editor using an 
	 * external file storage mechanism
	 * @param path IPath with the path of the external file which is being edited.
	 * @return true if an editor was found and activated, false otherwise
	 */
	private boolean activateExistingOpenedExternalFile(IPath path) {
		IEditorReference [] editorRefs;
		editorRefs = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		
		for(int i = 0; i < editorRefs.length; i++) {
			IEditorPart editor = editorRefs[i].getEditor(true);
			IEditorInput input = editor.getEditorInput();
			if(input instanceof IStorageEditorInput) {
				IPath editorPath;
				try {
					editorPath = ((IStorageEditorInput)input).getStorage().getFullPath();
				} catch(Exception ex) {
					editorPath = null;
				}
				if(editorPath != null && editorPath.equals(path)) {
					UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(editor);
					return true;
				}
			}
		}
		
		return false;
	}

	private void findFile(String[] includePaths, String name, ArrayList list)  throws CoreException {		
		for (int i = 0; i < includePaths.length; i++) {
			IPath path = new Path(includePaths[i] + "/" + name);
			File file = path.toFile();
			if (file.exists() && !list.contains(path)) {
				list.add(path);
			} 
		}
	}

	private void findFile(IContainer parent, IPath name, final ArrayList list) throws CoreException {
		final String lastSegment = name.lastSegment();
		if(lastSegment == null) {
			return;
		}

		final IPath pathSegments = name.removeLastSegments(1);
		
		//We use the lastSegment as a fast key, but then make sure that we can match
		//the rest of the segments (if they exist) so a path like:
		//#include "subsystem/includefile.h" won't match with "a/b/c/includefile.h"
		IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
			private boolean checkSegments(IPath sourceSegments, IPath targetSegments) {
				if(sourceSegments == null) {
					return true;
				}
				if(targetSegments == null) {
					return false;
				}
				
				int segmentCount = sourceSegments.segmentCount();
				int targetCount = targetSegments.segmentCount();
				if(segmentCount > targetCount) {
					return false;
				}
				
				for(int i = segmentCount - 1; i >= 0; i--) {
					if(!sourceSegments.segment(i).equals(targetSegments.segment(--targetCount))) {
						return false;
					}	
				}
				
				return true;
			}
			
			public boolean visit(IResourceProxy proxy) throws CoreException {
				String resourceName = proxy.getName();
				if(resourceName.equals(lastSegment)) {
					IResource res = proxy.requestResource();
					if(!res.exists()) {
						return true;
					}
					
					IPath location = res.getLocation();
					if(list.contains(location)) {
						return true;
					}

					//Check segment match criteria to make sure we really match this entry
					if(checkSegments(pathSegments, location.removeLastSegments(1)) != true) {
						return true;
					} 
					
					list.add(location);
				}
				return true;
			}
		};
		
		parent.accept(visitor, IResource.NONE);		
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
