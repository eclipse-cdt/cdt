/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.util.Collection;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeCacheChangedListener;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class CBrowsingContentProvider extends BaseCElementContentProvider implements ITreeContentProvider,
        IElementChangedListener, ITypeCacheChangedListener {

    public static final Object CONTENT_CANCELLED = new Object();
    public static final Object CONTENT_ERROR = new Object();
    public static final Object CONTENT_EMPTY = new Object();
    
    protected static final Object[] ERROR_NO_CHILDREN = new Object[] { CONTENT_ERROR };
    protected static final Object[] ERROR_CANCELLED = new Object[] { CONTENT_CANCELLED };
    protected static final Object[] EMPTY_CHILDREN = NO_CHILDREN;
    protected static final Object[] INVALID_INPUT = NO_CHILDREN;
    
    protected StructuredViewer fViewer;
    protected Object fInput = null;
    protected IProject fProject = null;
    protected CBrowsingPart fBrowsingPart;
    protected int fReadsInDisplayThread;

    public CBrowsingContentProvider(CBrowsingPart browsingPart) {
        fBrowsingPart = browsingPart;
        fViewer = fBrowsingPart.getViewer();
        AllTypesCache.addTypeCacheChangedListener(this);
        CoreModel.getDefault().addElementChangedListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public synchronized void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof Collection) {
            // Get a template object from the collection
            Collection col = (Collection) newInput;
            if (!col.isEmpty())
                newInput = col.iterator().next();
            else
                newInput = null;
        }
        fInput = newInput;
        fProject = getProject(newInput);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        AllTypesCache.removeTypeCacheChangedListener(this);
        CoreModel.getDefault().removeElementChangedListener(this);
    }

    public void typeCacheChanged(IProject project) {
        if (fInput != null && (fProject == null || !fProject.isAccessible() || fProject.equals(project))) {
            Object input = null;
            if (project != null && project.isAccessible()) {
                input = getNewInput(project);
            }
            postAdjustInputAndSetSelection(input);
        }
    }

    private IProject getProject(Object input) {
        if (input instanceof ICElement) {
            ICProject cProj = ((ICElement) input).getCProject();
            if (cProj != null)
                return cProj.getProject();
        }
        if (input instanceof ITypeInfo) {
            return ((ITypeInfo) input).getEnclosingProject();
        }
        return null;
    }

    private Object getNewInput(IProject project) {
        if (fInput == null || fInput instanceof ICModel) {
            return fInput;
        }
        if (fInput instanceof ICProject) {
            ICProject cproject = CoreModel.getDefault().create(project);
            if (!cproject.equals(fInput))
                return cproject;
            return fInput;
        }
        if (fInput instanceof ISourceRoot) {
            ICProject cproject = CoreModel.getDefault().create(project);
            ISourceRoot cSourceRoot = (ISourceRoot) fInput;
            if (!cSourceRoot.getCProject().equals(cproject)) {
                return cSourceRoot;
            }
            try {
                ISourceRoot[] roots = cproject.getSourceRoots();
                for (int i = 0; i < roots.length; ++i) {
                    ISourceRoot root = roots[i];
                    if (!(root.getResource() instanceof IProject) && root.equals(cSourceRoot)) {
                        return root;
                    }
                }
            } catch (CModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (fInput instanceof ICElement) {
            ICProject cproject = CoreModel.getDefault().create(project);
            ICElement celem = (ICElement) fInput;
            if (!celem.getCProject().equals(cproject)) {
                return celem;
            }
            final ICElement[] searchInfo = new ICElement[] { celem, null };
            try {
                cproject.accept(new ICElementVisitor() {
                    public boolean visit(ICElement element) throws CoreException {
                        if (searchInfo[1] != null)
                            return false;
                        if (element.equals(searchInfo[0])) {
                            searchInfo[1] = element;
                            return false;
                        }
                        return true;
                    }
                });
            } catch (CoreException e) {
            }
            if (searchInfo[1] != null)
                return searchInfo[1];
            return null;
        }
        if (fInput instanceof ITypeInfo) {
            ITypeInfo info = (ITypeInfo) fInput;
            if (info.exists())
                return info;
            IProject infoProj = info.getEnclosingProject();
            if (infoProj == null) {
                return null;
            }
            if (!infoProj.equals(project)) {
                return info;
            }
            ITypeInfo globalNS = AllTypesCache.getGlobalNamespace(project);
            if (info.equals(globalNS))
                return globalNS;
            info = AllTypesCache.getType(project, info.getCElementType(), info.getQualifiedTypeName());
            if (info != null) {
                return info;
            }
            return null;
        }
        return null;
    }

    public void elementChanged(ElementChangedEvent event) {
        try {
            processDelta(event.getDelta());
        } catch (CModelException e) {
            CUIPlugin.getDefault().log(e.getStatus());
        }
    }

    protected boolean isPathEntryChange(ICElementDelta delta) {
        int flags = delta.getFlags();
        return (delta.getKind() == ICElementDelta.CHANGED && ((flags & ICElementDelta.F_BINARY_PARSER_CHANGED) != 0
                || (flags & ICElementDelta.F_ADDED_PATHENTRY_LIBRARY) != 0
                || (flags & ICElementDelta.F_ADDED_PATHENTRY_SOURCE) != 0
                || (flags & ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY) != 0
                || (flags & ICElementDelta.F_PATHENTRY_REORDER) != 0
                || (flags & ICElementDelta.F_REMOVED_PATHENTRY_SOURCE) != 0 || (flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0));
    }

    /**
     * Processes a delta recursively. When more than two children are affected
     * the tree is fully refreshed starting at this node. The delta is processed
     * in the current thread but the viewer updates are posted to the UI thread.
     */
    protected void processDelta(ICElementDelta delta) throws CModelException {
        int kind = delta.getKind();
        int flags = delta.getFlags();
        ICElement element = delta.getElement();

        if (element instanceof ITranslationUnit && ((ITranslationUnit) element).isWorkingCopy()) {
            // ignore working copies
            return;
        }

        //System.out.println("Processing " + element);

        // handle open and closing of a solution or project
        if (((flags & ICElementDelta.F_CLOSED) != 0) || ((flags & ICElementDelta.F_OPENED) != 0)) {
            postRefresh(element);
        }

        if (kind == ICElementDelta.REMOVED) {
            postRemove(element);
        }

        if (kind == ICElementDelta.ADDED) {
            Object parent = internalGetParent(element);
            postAdd(parent, element);
        }

        if (kind == ICElementDelta.CHANGED) {
            if (element instanceof ITranslationUnit || element instanceof IBinary || element instanceof IArchive) {
                postRefresh(element);
                return;
            }
        }

        if (isPathEntryChange(delta)) {
            // throw the towel and do a full refresh of the affected C project.
            postRefresh(element.getCProject());
        }

        ICElementDelta[] affectedChildren = delta.getAffectedChildren();
        for (int i = 0; i < affectedChildren.length; i++) {
            processDelta(affectedChildren[i]);
        }
    }

    private void postAdjustInputAndSetSelection(final Object input) {
        postRunnable(new Runnable() {
            public void run() {
                Control ctrl = fViewer.getControl();
                if (ctrl != null && !ctrl.isDisposed()) {
                    ctrl.setRedraw(false);
                    fBrowsingPart.adjustInputAndPreserveSelection(input);
                    ctrl.setRedraw(true);
                }
            }
        });
    }

    private void postRefresh(final Object element) {
        //System.out.println("UI refresh:" + root);
        postRunnable(new Runnable() {
            public void run() {
                // 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
                Control ctrl = fViewer.getControl();
                if (ctrl != null && !ctrl.isDisposed()) {
                    if (element instanceof IWorkingCopy) {
                        if (fViewer.testFindItem(element) != null) {
                            fViewer.refresh(element);
                        } else {
                            fViewer.refresh(((IWorkingCopy) element).getOriginalElement());
                        }
                    } else {
                        fViewer.refresh(element);
                    }
                }
            }
        });
    }

    private void postAdd(final Object parent, final Object element) {
        //System.out.println("UI add:" + parent + " " + element);
        postRunnable(new Runnable() {
            public void run() {
                // 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
                Control ctrl = fViewer.getControl();
                if (ctrl != null && !ctrl.isDisposed()) {
                    if (parent instanceof IWorkingCopy) {
                        if (fViewer.testFindItem(parent) != null) {
                            fViewer.refresh(parent);
                        } else {
                            fViewer.refresh(((IWorkingCopy) parent).getOriginalElement());
                        }
                    } else {
                        fViewer.refresh(parent);
                    }
                }
            }
        });
    }

    private void postRemove(final Object element) {
        //System.out.println("UI remove:" + element);
        postRunnable(new Runnable() {
            public void run() {
                // 1GF87WR: ITPUI:ALL - SWTEx + NPE closing a workbench window.
                Control ctrl = fViewer.getControl();
                if (ctrl != null && !ctrl.isDisposed()) {
                    Object parent = internalGetParent(element);
                    if (parent instanceof IWorkingCopy) {
                        if (fViewer.testFindItem(parent) != null) {
                            fViewer.refresh(parent);
                        } else {
                            fViewer.refresh(((IWorkingCopy) parent).getOriginalElement());
                        }
                    } else {
                        fViewer.refresh(parent);
                    }
                }
            }
        });
    }

    private void postRunnable(final Runnable r) {
        Control ctrl = fViewer.getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
            ctrl.getDisplay().asyncExec(r);
        }
    }

    protected void startReadInDisplayThread() {
        if (isDisplayThread())
            fReadsInDisplayThread++;
    }

    protected void finishedReadInDisplayThread() {
        if (isDisplayThread())
            fReadsInDisplayThread--;
    }

    private boolean isDisplayThread() {
        Control ctrl = fViewer.getControl();
        if (ctrl == null)
            return false;

        Display currentDisplay = Display.getCurrent();
        return currentDisplay != null && currentDisplay.equals(ctrl.getDisplay());
    }
}