/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

public class NewClassWizardUtil {

    /**
     * Returns the parent source folder of the given element. If the given
     * element is already a source folder, the element itself is returned.
     * 
     * @param element the C Element
     * @return the source folder
     */
    public static ICContainer getSourceFolder(ICElement element) {
        ICContainer folder = null;
        boolean foundSourceRoot = false;
        ICElement curr = element;
        while (curr != null && !foundSourceRoot) {
            if (curr instanceof ICContainer && folder == null) {
                folder = (ICContainer)curr;
            }
            foundSourceRoot = (curr instanceof ISourceRoot);
            curr = curr.getParent();
        }
        if (folder == null) {
            ICProject cproject = element.getCProject();
            folder = cproject.findSourceRoot(cproject.getProject());
        }
        return folder;
    }
    
    /**
     * Returns the parent source folder for the given path. If the given
     * path is already a source folder, the corresponding C element is returned.
     * 
     * @param path the path
     * @return the source folder
     */
    public static ICContainer getSourceFolder(IPath path) {
        if (path == null)
            return null;
        while (!path.isEmpty()) {
            IResource res = getWorkspaceRoot().findMember(path);
            if (res != null && res.exists()) {
                int resType = res.getType();
                if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                    ICElement elem = CoreModel.getDefault().create(res.getFullPath());
                    if (elem != null) {
                        ICContainer sourceFolder = getSourceFolder(elem);
                        if (sourceFolder != null)
                            return sourceFolder;
                        if (resType == IResource.PROJECT) {
                            return (ICContainer)elem;
                        }
                    }
                }
            }
            path = path.removeLastSegments(1);
        }
        return null;
    }
    
    /**
     * Returns the parent source folder for the given resource. If the given
     * resource is already a source folder, the corresponding C element is returned.
     * 
     * @param resource the resource
     * @return the source folder
     */
    public static ICContainer getSourceFolder(IResource resource) {
        if (resource != null && resource.exists()) {
            int resType = resource.getType();
            if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                ICElement elem = CoreModel.getDefault().create(resource.getFullPath());
                if (elem != null) {
                    ICContainer sourceFolder = getSourceFolder(elem);
                    if (sourceFolder != null)
                        return sourceFolder;
                }
            } else {
                return getSourceFolder(resource.getParent());
            }
        }
        return null;
    }
    
    /**
     * Returns the first source root in the given project. If the project has
     * no source roots as children, the project itself is returned.
     * 
     * @param cproject
     * @return the source root
     */
    public static ISourceRoot getFirstSourceRoot(ICProject cproject) {
        ISourceRoot folder = null;
        try {
            if (cproject.exists()) {
                ISourceRoot[] roots = cproject.getSourceRoots();
                if (roots != null && roots.length > 0)
                    folder = roots[0];
            }
        } catch (CModelException e) {
            CUIPlugin.getDefault().log(e);
        }
        if (folder == null) {
            folder = cproject.findSourceRoot(cproject.getResource());
        }
        return folder;
    }

    /**
     * Returns the C Element which corresponds to the given selection.
     * 
     * @param selection the selection to be inspected
     * @return a C element matching the selection, or <code>null</code>
     * if no C element exists in the given selection
     */
    public static ICElement getCElementFromSelection(IStructuredSelection selection) {
        ICElement celem = null;
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;            
                
                celem = (ICElement) adaptable.getAdapter(ICElement.class);
                if (celem == null) {
                    IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                    if (resource != null && resource.getType() != IResource.ROOT) {
                        while (celem == null && resource.getType() != IResource.PROJECT) {
                            resource = resource.getParent();
                            celem = (ICElement) resource.getAdapter(ICElement.class);
                        }
                        if (celem == null) {
                            celem = CoreModel.getDefault().create(resource); // c project
                        }
                    }
                }
            }
        }
        return celem;
    }
    
    /**
     * Returns the C Element which corresponds to the active editor.
     * 
     * @return a C element matching the active editor, or <code>null</code>
     * if no C element can be found
     */
    public static ICElement getCElementFromEditor() {
        ICElement celem = null;
        IWorkbenchPart part = CUIPlugin.getActivePage().getActivePart();
        if (part instanceof ContentOutline) {
            part = CUIPlugin.getActivePage().getActiveEditor();
        }
        if (part instanceof IViewPartInputProvider) {
            Object elem = ((IViewPartInputProvider)part).getViewPartInput();
            if (elem instanceof ICElement) {
                celem = (ICElement) elem;
            }
        }
        if (celem == null && part instanceof CEditor) {
            IEditorInput input = ((IEditorPart)part).getEditorInput();
            if (input != null) {
                final IResource res = (IResource) input.getAdapter(IResource.class);
                if (res != null && res instanceof IFile) {
                    celem = CoreModel.getDefault().create((IFile)res);
                }
            }
        }
        return celem;
    }
    
    /**
     * Returns the parent namespace for the given element. If the given element is
     * already a namespace, the element itself is returned.
     *
     * @param element the given C Element
     * @return the C Element for the namespace, or <code>null</code> if not found
     */
    public static ICElement getNamespace(ICElement element) {
        ICElement curr = element;
        while (curr != null) {
            int type = curr.getElementType();
            if (type == ICElement.C_UNIT) {
                break;
            }
            if (type == ICElement.C_NAMESPACE) {
                return curr;
            }
            curr = curr.getParent();
        }
        return null;
    }
    
    /**
     * Creates a header file name from the given class name. This is the file name
     * to be used when the class is created. eg. "MyClass" -> "MyClass.h"
     * 
     * @param className the class name
     * @return the header file name for the given class
     */
    public static String createHeaderFileName(String className) {
        return NewSourceFileGenerator.generateHeaderFileNameFromClass(className);
    }
    
    /**
     * Creates a source file name from the given class name. This is the file name
     * to be used when the class is created. eg. "MyClass" -> "MyClass.cpp"
     * 
     * @param className the class name
     * @return the source file name for the given class
     */
    public static String createSourceFileName(String className) {
        return NewSourceFileGenerator.generateSourceFileNameFromClass(className);
    }
    
    /**
     * Returns the workspace root.
     * 
     * @return the workspace root
     */ 
    public static IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    /**
     * Ensures the type cache is up to date.
     * 
     * @param context the runnable context
     */
    public static void prepareTypeCache(IRunnableContext context) {
        final ITypeSearchScope scope = new TypeSearchScope(true);
        if (!AllTypesCache.isCacheUpToDate(scope)) {
            IRunnableWithProgress runnable = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    AllTypesCache.updateCache(scope, monitor);
                    if (monitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                }
            };
            
            try {
                context.run(true, true, runnable);
            } catch (InvocationTargetException e) {
                String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getTypes.exception.title"); //$NON-NLS-1$
                String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getTypes.exception.message"); //$NON-NLS-1$
                ExceptionHandler.handle(e, title, message);
            } catch (InterruptedException e) {
                // cancelled by user
            }
        }
    }

    /**
     * Resolve the location of the given class.
     * 
     * @param type the class to resolve
     * @param context the runnable context
     * @return the class location, or <code>null</code> if not found
     */
    public static ITypeReference resolveClassLocation(ITypeInfo type, IRunnableContext context) {
        prepareTypeCache(context);
        
        // resolve location of base class
        if (type.getResolvedReference() == null) {
            final ITypeInfo[] typesToResolve = new ITypeInfo[] { type };
            IRunnableWithProgress runnable = new IRunnableWithProgress() {
                public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
                    AllTypesCache.resolveTypeLocation(typesToResolve[0], progressMonitor);
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                }
            };
            
            try {
                context.run(true, true, runnable);
            } catch (InvocationTargetException e) {
                String title = NewClassWizardMessages.getString("NewClassCreationWizardPage.getTypes.exception.title"); //$NON-NLS-1$
                String message = NewClassWizardMessages.getString("NewClassCreationWizardPage.getTypes.exception.message"); //$NON-NLS-1$
                ExceptionHandler.handle(e, title, message);
                return null;
            } catch (InterruptedException e) {
                // cancelled by user
                return null;
            }
        }
        return type.getResolvedReference();
    }

    private static final int[] CLASS_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };
    
    /**
     * Returns all classes/structs which are accessible from the include
     * paths of the given project.
     * 
     * @param project the given project
     * @return array of classes/structs
     */
    public static ITypeInfo[] getReachableClasses(IProject project) {
        ITypeInfo[] elements = AllTypesCache.getTypes(new TypeSearchScope(true), CLASS_TYPES);
        if (elements != null && elements.length > 0) {
            if (project != null) {
                IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
                if (provider != null) {
                    //TODO get the scanner info for the actual source folder
                    IScannerInfo info = provider.getScannerInformation(project);
                    if (info != null) {
                        String[] includePaths = info.getIncludePaths();
                        List filteredTypes = new ArrayList();
                        for (int i = 0; i < elements.length; ++i) {
                            ITypeInfo baseType = elements[i];
                            if (isTypeReachable(baseType, project, includePaths)) {
                                filteredTypes.add(baseType);
                            }
                        }
                        return (ITypeInfo[]) filteredTypes.toArray(new ITypeInfo[filteredTypes.size()]);
                    }
                }
            }
        }
        return elements;
    }
    
    /**
     * Checks whether the given type can be found in the given project or the
     * given include paths.
     * 
     * @param type the type
     * @param project the project
     * @param includePaths the include paths
     * @return <code>true</code> if the given type is found
     */
    public static boolean isTypeReachable(ITypeInfo type, IProject project, String[] includePaths) {
        IProject baseProject = type.getEnclosingProject();
        if (baseProject != null) {
            if (baseProject.equals(project)) {
                return true;
            }
            ITypeReference ref = type.getResolvedReference();
            for (int i = 0; i < includePaths.length; ++i) {
                IPath includePath = new Path(includePaths[i]);
                if (ref != null) {
                    if (includePath.isPrefixOf(ref.getLocation()))
                        return true;
                } else {
                    // we don't have the real location, so just check the project path
                    if (baseProject.getLocation().isPrefixOf(includePath))
                        return true;
                }
            }
        }
        return false;
    }
    
}
