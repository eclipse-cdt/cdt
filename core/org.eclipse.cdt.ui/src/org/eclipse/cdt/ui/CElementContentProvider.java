/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;



import java.util.HashSet;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.ArchiveContainer;
import org.eclipse.cdt.internal.core.model.BinaryContainer;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A content provider for C elements.
 * <p>
 * The following C element hierarchy is surfaced by this content provider:
 * <p>
 * <pre>
C model (<code>ICModel</code>)<br>
   C project (<code>ICProject</code>)<br>
      Virtual binaries  container(<code>IBinaryContainery</code>)
      Virtual archives  container(<code>IArchiveContainery</code>)
      Source root (<code>ISourceRoot</code>)<br>
          C Container(folders) (<code>ICContainer</code>)<br>
          Translation unit (<code>ITranslationUnit</code>)<br>
          Binary file (<code>IBinary</code>)<br>
          Archive file (<code>IArchive</code>)<br>
      Non C Resource file (<code>Object</code>)<br>

 * </pre>
 */
public class CElementContentProvider extends BaseCElementContentProvider implements ITreeContentProvider, IElementChangedListener, IInformationProvider, IInformationProviderExtension{

	/** Editor. */
    protected ITextEditor fEditor;
    protected StructuredViewer fViewer;
	protected Object fInput;
	
	/** Remember what refreshes we already have pending so we don't post them again. */
	protected HashSet pendingRefreshes = new HashSet();

    /**
     * Creates a new content provider for C elements.
     */
    public CElementContentProvider()
    {
        // Empty.
    }
    
	/**
	 * Creates a new content provider for C elements.
     * @param editor Editor.
	 */
	public CElementContentProvider(ITextEditor editor)
    {
        fEditor = editor;
	}
    
	/**
	 * Creates a new content provider for C elements.
	 */
	public CElementContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		super(provideMembers, provideWorkingCopy);
	}

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        super.dispose();
        CoreModel.getDefault().removeElementChangedListener(this);
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        super.inputChanged(viewer, oldInput, newInput);

        fViewer = (StructuredViewer) viewer;

        if (oldInput == null && newInput != null) {
            CoreModel.getDefault().addElementChangedListener(this);
        } else if (oldInput != null && newInput == null) {
            CoreModel.getDefault().removeElementChangedListener(this);
        }
        fInput= newInput;
    }

	/**
     * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
    public void elementChanged(final ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
			CUIPlugin.getDefault().log(e);
			e.printStackTrace();
		}
	}

	protected boolean isPathEntryChange(ICElementDelta delta) {
		int flags= delta.getFlags();
		return (delta.getKind() == ICElementDelta.CHANGED && 
				((flags & ICElementDelta.F_BINARY_PARSER_CHANGED) != 0 ||
				(flags & ICElementDelta.F_ADDED_PATHENTRY_LIBRARY) != 0 ||
				(flags & ICElementDelta.F_ADDED_PATHENTRY_SOURCE) != 0 ||
				(flags & ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY) != 0 ||
				(flags & ICElementDelta.F_PATHENTRY_REORDER) != 0 ||
				(flags & ICElementDelta.F_REMOVED_PATHENTRY_SOURCE) != 0 ||
				(flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0));
	}

	/**
	 * Processes a delta recursively. When more than two children are affected the
	 * tree is fully refreshed starting at this node. The delta is processed in the
	 * current thread but the viewer updates are posted to the UI thread.
	 */
	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Processing " + element);

		// handle open and closing of a solution or project
		if (((flags & ICElementDelta.F_CLOSED) != 0) || ((flags & ICElementDelta.F_OPENED) != 0)) {
			postRefresh(element);
		}

		// We do not care about changes in Working copies
		if (element instanceof ITranslationUnit) {
			ITranslationUnit unit = (ITranslationUnit) element;
			if (unit.isWorkingCopy()) {
				return;
			}
		}

		if (kind == ICElementDelta.REMOVED) {
			Object parent = internalGetParent(element);
			postRemove(element);
			if (updateContainer(element)) {
				postRefresh(parent);
			}
		}

		if (kind == ICElementDelta.ADDED) {
			Object parent= internalGetParent(element);
			postAdd(parent, element);
			if (updateContainer(element)) {
				postRefresh(parent);
			}
		}

		if (kind == ICElementDelta.CHANGED) {
			// Binary/Archive changes is done differently since they
			// are at two places, they are in the {Binary,Archive}Container
			// and in the Tree hiearchy
			if (updateContainer(element)) {
				Object parent = getParent(element);
				postRefresh(parent);
			} else if (element instanceof ITranslationUnit) {
				postRefresh(element);
			} else if (element instanceof ArchiveContainer || element instanceof BinaryContainer) {
				postContainerRefresh((IParent) element, element.getCProject());
			}

		}

		if (isPathEntryChange(delta)) {
			 // throw the towel and do a full refresh of the affected C project. 
			postRefresh(element.getCProject());
			return;// bailout
		}
		
		if (delta.getResourceDeltas() != null) {
			IResourceDelta[] rd= delta.getResourceDeltas();
			for (int i= 0; i < rd.length; i++) {
				processResourceDelta(rd[i], element);
			}
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}

	/*
	 * Process resource deltas
	 */
	private void processResourceDelta(IResourceDelta delta, Object parent) {
		int status= delta.getKind();
		IResource resource= delta.getResource();
		// filter out changes affecting the output folder
		if (resource == null) {
			return;
		}
                        
		// this could be optimized by handling all the added children in the parent
		if ((status & IResourceDelta.REMOVED) != 0) {
			postRemove(resource);
		}
		if ((status & IResourceDelta.ADDED) != 0) {
			postAdd(parent, resource);
		}
		IResourceDelta[] affectedChildren= delta.getAffectedChildren();

		if (affectedChildren.length > 1) {
			// more than one child changed, refresh from here downwards
			postRefresh(resource);
			return;
		}

		for (int i= 0; i < affectedChildren.length; i++) {
			processResourceDelta(affectedChildren[i], resource);
		}
	}

	private boolean updateContainer(ICElement cfile) throws CModelException {
		IParent container = null;
		ICProject cproject = null;
		if (cfile instanceof IBinary) {
			IBinary bin = (IBinary)cfile;
			if (bin.isExecutable() || bin.isSharedLib()) {
				cproject = bin.getCProject();
				container = cproject.getBinaryContainer();
			}
		} else if (cfile instanceof IArchive) {
			cproject = cfile.getCProject();
			container = cproject.getArchiveContainer();
		}
		if (container != null) {
			postContainerRefresh(container, cproject);
			return true;
		}
		return false;
	}
	
	// Tree refresh system
	// We keep track of what we're going to refresh and avoid posting multiple refresh
	// messages for the same elements. This avoids major performance issues where
	// we update tree views hundreds or thousands of times.
	interface IRefreshable {
	    public void refresh();
	}
	final class RefreshContainer implements IRefreshable {
		private IParent container;
		private ICProject cproject;
		public RefreshContainer(IParent container, ICProject cproject) {
			this.container = container;
			this.cproject = cproject;
		}
	    public void refresh() {
			if (container.hasChildren()) {
				if (fViewer.testFindItem(container) != null) {
					fViewer.refresh(container);
				} else {
					fViewer.refresh(cproject);
				}
			} else {
				fViewer.refresh(cproject);
			}
	    }
	    public boolean equals(Object o) {
	    	if (o instanceof RefreshContainer) {
	    		RefreshContainer c = (RefreshContainer)o;
	    		return c.container.equals(container) && c.cproject.equals(cproject);
	    	}
	        return false;
	    }
	    public int hashCode() {
	    	return container.hashCode()*10903143 + 31181;
	    }
	}
	final class RefreshElement implements IRefreshable {
		private Object element;
		public RefreshElement(Object element) {
			this.element = element;
		}
		public void refresh() {
			if (element instanceof IWorkingCopy){
				if (fViewer.testFindItem(element) != null){
					fViewer.refresh(element);													
				} else {
					fViewer.refresh(((IWorkingCopy)element).getOriginalElement());
				}
			} else if (element instanceof IBinary) {
				fViewer.refresh(element, true);
			} else {
				fViewer.refresh(element);						
			}
		}
	    public boolean equals(Object o) {
	    	if (o instanceof RefreshElement) {
	    		RefreshElement c = (RefreshElement)o;
	    		return c.element.equals(element);
	    	}
	        return false;
	    }
	    public int hashCode() {
	    	return element.hashCode()*7 + 490487;
	    }
	}
	
	private void postContainerRefresh(final IParent container, final ICProject cproject) {
		//System.out.println("UI Container:" + cproject + " " + container);
		postRefreshable(new RefreshContainer(container, cproject));
	}

	private void postRefresh(final Object element) {
		//System.out.println("UI refresh:" + root);
		postRefreshable(new RefreshElement(element));
	}

	private void postAdd(final Object parent, final Object element) {
		//System.out.println("UI add:" + parent + " " + element);
		postRefreshable(new RefreshElement(parent));
	}

	private void postRemove(final Object element) {
		//System.out.println("UI remove:" + element);
		postRefreshable(new RefreshElement(internalGetParent(element)));
	}

	private void postRefreshable(final IRefreshable r) {
		Control ctrl= fViewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			if (pendingRefreshes.contains(r))
				return;
			pendingRefreshes.add(r);
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {
					pendingRefreshes.remove(r);
					Control ctrl= fViewer.getControl();
					if (ctrl != null && !ctrl.isDisposed()) {
						r.refresh();
					}
				}
			});
		}
	}

    /**
     * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
     */
    public IRegion getSubject(ITextViewer textViewer, int offset) {
		if (textViewer != null && fEditor != null) {
			IRegion region = CWordFinder.findWord(textViewer.getDocument(),
					offset);
			if (region != null) {
				return region;
			}
			return new Region(offset, 0);
		}
		return null;
	}

    /**
	 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
    public String getInformation(ITextViewer textViewer, IRegion subject)
    {
        return getInformation2(textViewer, subject).toString();
    }

    /**
     * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		if (fEditor == null)
			return null;
		try {
			ICElement element = SelectionConverter.getElementAtOffset(fEditor);
			if (element != null) {
				return element.toString();
			}
			return SelectionConverter.getInput(fEditor).toString();
		} catch (CModelException e) {
			return null;
		}
	}
}
