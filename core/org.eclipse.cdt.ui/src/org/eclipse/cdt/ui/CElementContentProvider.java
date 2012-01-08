/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui;



import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
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
public class CElementContentProvider extends BaseCElementContentProvider implements IElementChangedListener, IInformationProvider, IInformationProviderExtension{

	/** Editor. */
    protected ITextEditor fEditor;
    protected StructuredViewer fViewer;
	protected Object fInput;
	
	/** Remember what refreshes we already have pending so we don't post them again. */
	protected HashSet<IRefreshable> pendingRefreshes = new HashSet<IRefreshable>();

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
    @Override
	public void dispose() {
        super.dispose();
        CoreModel.getDefault().removeElementChangedListener(this);
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
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
    @Override
	public void elementChanged(final ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
			CUIPlugin.log(e);
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

		// handle open and closing of a project
		if (((flags & ICElementDelta.F_CLOSED) != 0) || ((flags & ICElementDelta.F_OPENED) != 0)) {
			postRefresh(element);
			return;
		}

		// We do not care about changes in Working copies
		// well, we do see bug 147694
		if (element instanceof ITranslationUnit) {
			ITranslationUnit unit = (ITranslationUnit) element;
			if (unit.isWorkingCopy()) {
				if (!getProvideWorkingCopy() || kind == ICElementDelta.REMOVED || kind == ICElementDelta.ADDED) {
					return;
				}
			}
			if (!getProvideMembers() && kind == ICElementDelta.CHANGED) {
				return;
			}
		}

		if (kind == ICElementDelta.REMOVED) {
			postRemove(element);
			updateContainer(element);
			return;
		}

		if (kind == ICElementDelta.ADDED) {
			Object parent= internalGetParent(element);
			postAdd(parent, element);
			updateContainer(element);
		}

		if (isPathEntryChange(delta)) {
			postRefresh(element.getCProject());
			return;
		}

		if (kind == ICElementDelta.CHANGED) {
			// Binary/Archive changes is done differently since they
			// are at two places, they are in the {Binary,Archive}Container
			// and in the Tree hierarchy
			if (updateContainer(element)) {
				Object parent = getParent(element);
				postRefresh(parent);
				return;
			} else if (element instanceof ITranslationUnit) {
				postRefresh(element);
				return;
			} else if (element instanceof ICContainer) {
				// if element itself has changed, not its children
				if ((flags&~(ICElementDelta.F_CHILDREN|ICElementDelta.F_FINE_GRAINED))!=0) {
					postRefresh(element);
				}
			} else if (element instanceof ArchiveContainer || element instanceof BinaryContainer) {
				postContainerRefresh((IParent) element, element.getCProject());
			}

		}

		if (processResourceDeltas(delta.getResourceDeltas(), element))
			return;
	
		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (ICElementDelta element2 : affectedChildren) {
			processDelta(element2);
		}
	}

	/**
	 * Process resource deltas.
	 *
	 * @return true if the parent got refreshed
	 */
	private boolean processResourceDeltas(IResourceDelta[] deltas, Object parent) {
		if (deltas == null)
			return false;
		
		if (deltas.length > 1 && !(parent instanceof ICModel)) {
			// more than one child changed, refresh from here downwards
			// but not if the parent is ICModel
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=202085
			postRefresh(parent);
			return true;
		}

		for (IResourceDelta delta : deltas) {
			if (processResourceDelta(delta, parent))
				return true;
		}

		return false;
	}

	/**
	 * Process a resource delta.
	 * 
	 * @return true if the parent got refreshed
	 */
	private boolean processResourceDelta(IResourceDelta delta, Object parent) {
		int status= delta.getKind();
		IResource resource= delta.getResource();
		// filter out changes affecting the output folder
		if (resource == null) {
			return false;
		}
                        
		// this could be optimized by handling all the added children in the parent
		if ((status & IResourceDelta.REMOVED) != 0) {
			postRemove(resource);
		}
		if ((status & IResourceDelta.ADDED) != 0) {
			postAdd(parent, resource);
		}

		int flags= delta.getFlags();
		// open/close state change of a project
		if ((flags & IResourceDelta.OPEN) != 0) {
			postProjectStateChanged(parent);
			return true;
		}

		processResourceDeltas(delta.getAffectedChildren(), resource);
		return false;
	}

	private boolean updateContainer(ICElement cfile) throws CModelException {
		IParent container = null;
		ICProject cproject = null;
		if (cfile instanceof IBinary) {
			IBinary bin = (IBinary)cfile;
			if (bin.showInBinaryContainer()) {
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
	protected interface IRefreshable {
	    public void refresh();
	}
	protected final class RefreshContainer implements IRefreshable {
		private IParent container;
		private Object project;
		public RefreshContainer(IParent container, Object project) {
			this.container = container;
			this.project = project;
		}
	    @Override
		public void refresh() {
			if (container.hasChildren()) {
				if (fViewer.testFindItem(container) != null) {
					fViewer.refresh(container);
				} else {
					fViewer.refresh(project);
				}
			} else {
				fViewer.refresh(project);
			}
	    }
	    @Override
		public boolean equals(Object o) {
	    	if (o instanceof RefreshContainer) {
	    		RefreshContainer c = (RefreshContainer)o;
	    		return c.container.equals(container) && c.project.equals(project);
	    	}
	        return false;
	    }
	    @Override
		public int hashCode() {
	    	return container.hashCode()*10903143 + 31181;
	    }
	}
	protected final class RefreshElement implements IRefreshable {
		private Object element;
		public RefreshElement(Object element) {
			this.element = element;
		}
		@Override
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
	    @Override
		public boolean equals(Object o) {
	    	if (o instanceof RefreshElement) {
	    		RefreshElement c = (RefreshElement)o;
	    		return c.element.equals(element);
	    	}
	        return false;
	    }
	    @Override
		public int hashCode() {
	    	return element.hashCode()*7 + 490487;
	    }
	}

	protected final class RefreshProjectState implements IRefreshable {
		private Object element;
		public RefreshProjectState(Object element) {
			this.element = element;
		}
		@Override
		public void refresh() {
			fViewer.refresh(element, true);
			// trigger a syntetic selection change so that action refresh their
			// enable state.
			fViewer.setSelection(fViewer.getSelection());
		}
	    @Override
		public boolean equals(Object o) {
	    	if (o instanceof RefreshElement) {
	    		RefreshElement c = (RefreshElement)o;
	    		return c.element.equals(element);
	    	}
	        return false;
	    }
	    @Override
		public int hashCode() {
	    	return element.hashCode()*11 + 490487;
	    }
	}

	protected void postContainerRefresh(final IParent container, final ICProject cproject) {
		//System.out.println("UI Container:" + cproject + " " + container);
		postRefreshable(new RefreshContainer(container, cproject));
	}

	protected void postRefresh(final Object element) {
		//System.out.println("UI refresh:" + element);
		postRefreshable(new RefreshElement(element));
	}

	protected void postAdd(final Object parent, final Object element) {
		//System.out.println("UI add:" + parent + " " + element);
		postRefreshable(new RefreshElement(parent));
	}

	protected void postRemove(final Object element) {
		//System.out.println("UI remove:" + element);
		postRefreshable(new RefreshElement(internalGetParent(element)));
	}

	protected void postProjectStateChanged(final Object root) {
		postRefreshable(new RefreshProjectState(root));
	}

	protected final void postRefreshable(final IRefreshable r) {
		Control ctrl= fViewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			if (pendingRefreshes.contains(r))
				return;
			pendingRefreshes.add(r);
			ctrl.getDisplay().asyncExec(new Runnable() {
				@Override
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

    /*
     * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
     */
    @Override
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

    /*
	 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
    @Override
	public String getInformation(ITextViewer textViewer, IRegion subject) {
    	// deprecated API - not used anymore
        Object info = getInformation2(textViewer, subject);
        if (info != null) {
        	return info.toString();
        }
        return null;
    }

    /*
     * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    @Override
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		if (fEditor == null)
			return null;
		try {
			ICElement element = SelectionConverter.getElementAtOffset(fEditor);
			if (element != null) {
				return element;
			}
			return SelectionConverter.getInput(fEditor);
		} catch (CModelException e) {
			return null;
		}
	}
}
