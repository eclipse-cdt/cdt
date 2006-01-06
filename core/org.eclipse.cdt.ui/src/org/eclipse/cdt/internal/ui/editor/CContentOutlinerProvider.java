/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CShiftData;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * Manages contents of the outliner.
 */
public class CContentOutlinerProvider extends BaseCElementContentProvider {

	/** Tree viewer which handles this content provider. */
	TreeViewer treeViewer;

	/** Translation unit's root. */
	ITranslationUnit root;

	/** Something changed listener. */
	private ElementChangedListener fListener;

	/** Property change listener. */
	private IPropertyChangeListener fPropertyListener;

	/** Filter for files to outline. */
	private StringMatcher filter = new StringMatcher("*", true, false); //$NON-NLS-1$

	/**
	 * Remote content manager to retrieve content in the background.
	 */
	private DeferredTreeContentManager fManager;

	/**
	 * We only want to use the DeferredContentManager, the first time
	 * because the Outliner is initialize in the UI thread.  So to not block
	 * the UI thread we deferred, after it is not necessary the reconciler is
	 * running in a separate thread not affecting the UI.
	 */
	private boolean fUseContentManager = false;

	public CContentOutlinerProvider(TreeViewer viewer) {
		this(viewer, null);
	}

	/**
	 * Creates new content provider for dialog.
	 * 
	 * @param viewer
	 *            Tree viewer.
	 */
	public CContentOutlinerProvider(TreeViewer viewer, IWorkbenchPartSite site) {
		super(true, true);
		treeViewer = viewer;
		setIncludesGrouping(PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES));
		fManager = createContentManager(viewer, site);
	}

	protected DeferredTreeContentManager createContentManager(TreeViewer viewer, IWorkbenchPartSite site) {
		if (site == null) {
			return new DeferredTreeContentManager(this, viewer);
		}
		return new DeferredTreeContentManager(this, viewer, site);
	}

	/**
	 * Sets new filter and updates contents.
	 * 
	 * @param newFilter
	 *            New filter.
	 */
	public void updateFilter(String newFilter) {
		filter = new StringMatcher(newFilter, true, false);
		contentUpdated();
	}

	/**
	 * Called by the editor to signal that the content has updated.
	 */
	public void contentUpdated() {
		if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
			treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!treeViewer.getControl().isDisposed()) {
						final ISelection sel = treeViewer.getSelection();
						treeViewer.setSelection(updateSelection(sel));
						treeViewer.refresh();
					}
				}
			});
		}
	}

	/**
	 * Called after CEditor contents is changed.
	 * Existing elements can change their offset and length.
	 * 
	 * @param offset
	 * 		position where source was changed
	 * @param size 
	 * 		length of ins
	 * ertion (negaive for deletion) 
	 */
	public void contentShift(CShiftData sdata) {
		try {
			ICElement[] el = root.getChildren();
			for (int i=0; i< el.length; i++) {
				if (!(el[i] instanceof SourceManipulation)) continue;

				SourceManipulation sm = (SourceManipulation) el[i];
				ISourceRange src = sm.getSourceRange();
				int endOffset = src.getStartPos() + src.getLength();
				
				// code BELOW this element changed - do nothing !
				if (sdata.getOffset() > endOffset) { continue;	}
				
				if (sdata.getOffset() < src.getStartPos()) {
					// code ABOVE this element changed - modify offset
					sm.setIdPos(src.getIdStartPos() + sdata.getSize(), src.getIdLength());
					sm.setPos(src.getStartPos() + sdata.getSize(), src.getLength());
					sm.setLines(src.getStartLine() + sdata.getLines(), src.getEndLine() + sdata.getLines());
				} else {
					// code INSIDE of this element changed - modify length
					sm.setPos(src.getStartPos(), src.getLength() + sdata.getSize());
					sm.setLines(src.getStartLine(), src.getEndLine() + sdata.getLines());
				}
			}
		} catch (CModelException e) {}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fListener != null) {
			CoreModel.getDefault().removeElementChangedListener(fListener);
			fListener = null;
		}
		if (fPropertyListener != null) {
			PreferenceConstants.getPreferenceStore().removePropertyChangeListener(fPropertyListener);
			fPropertyListener = null;
		}
		if (root != null) {
			fManager.cancel(root);
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		boolean isTU = newInput instanceof ITranslationUnit;

		if (isTU && fListener == null) {
			fUseContentManager = true;
			if (root != null) {
				fManager.cancel(root);
			}
			root = (ITranslationUnit) newInput;
			fListener = new ElementChangedListener();
			CoreModel.getDefault().addElementChangedListener(fListener);
			fPropertyListener = new PropertyListener();
			PreferenceConstants.getPreferenceStore().addPropertyChangeListener(
					fPropertyListener);
		} else if (!isTU && fListener != null) {
			fUseContentManager = false;
			CoreModel.getDefault().removeElementChangedListener(fListener);
			fListener = null;
			root = null;
			if (oldInput != null) {
				fManager.cancel(oldInput);
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		Object[] children = null;
		// Use the deferred manager for the first time (when parsing)
		if (fUseContentManager && element instanceof ITranslationUnit) {
			children = fManager.getChildren(element);
			fUseContentManager = false;
		}
		if (children == null) {
			children = super.getChildren(element);
		}
		List filtered = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (filter.match(children[i].toString())) {
				filtered.add(children[i]);
			}
		}
		int size = filtered.size();
		children = new Object[size];
		filtered.toArray(children);
		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (fUseContentManager) {
			return fManager.mayHaveChildren(element);
		}
		return super.hasChildren(element);
	}

	/**
	 * Updates current selection.
	 * 
	 * @param sel
	 *            Selection to update.
	 * @return Updated selection.
	 */
	protected ISelection updateSelection(ISelection sel) {
		final ArrayList newSelection = new ArrayList();
		if (sel instanceof IStructuredSelection) {
			final Iterator iter = ((IStructuredSelection) sel).iterator();
			while (iter.hasNext()) {
				final Object o = iter.next();
				if (o instanceof ICElement) {
					newSelection.add(o);
				}
			}
		}
		return new StructuredSelection(newSelection);
	}

	/**
	 * The element change listener of the C outline viewer.
	 * 
	 * @see IElementChangedListener
	 */
	class ElementChangedListener implements IElementChangedListener {

		/**
		 * Default constructor.
		 */
		public ElementChangedListener() {
			// nothing to initialize.
		}

		/**
		 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
		 */
		public void elementChanged(final ElementChangedEvent e) {
			if (e.getType() == ElementChangedEvent.POST_SHIFT && e.getDelta() instanceof CShiftData) {
				contentShift((CShiftData)(e.getDelta()));
				return;
			}
			
			final ICElementDelta delta = findElement(root, e.getDelta());
			if (delta != null) {
				contentUpdated();
				return;
			}
		}

		/**
		 * Determines is structural change.
		 * 
		 * @param cuDelta
		 *            Delta to check.
		 * @return <b>true</b> if structural change.
		 */
		private boolean isPossibleStructuralChange(ICElementDelta cuDelta) {
			boolean ret;
			if (cuDelta.getKind() != ICElementDelta.CHANGED) {
				ret = true; // add or remove
			} else {
				final int flags = cuDelta.getFlags();
				if ((flags & ICElementDelta.F_CHILDREN) != 0) {
					ret = true;
				} else {
					ret = (flags & (ICElementDelta.F_CONTENT | ICElementDelta.F_FINE_GRAINED)) == ICElementDelta.F_CONTENT;
				}
			}
			return ret;
		}

		/**
		 * Searches for element.
		 * 
		 * @param unit
		 *            Unit to search in.
		 * @param delta
		 *            Delta.
		 * @return Found element.
		 */
		protected ICElementDelta findElement(ICElement unit,
				ICElementDelta delta) {
			if (delta == null || unit == null) {
				return null;
			}

			final ICElement element = delta.getElement();

			if (unit.equals(element)) {
				if (isPossibleStructuralChange(delta)) {
					return delta;
				}
				return null;
			}

			if (element.getElementType() > ICElement.C_UNIT) {
				return null;
			}

			final ICElementDelta[] children = delta.getAffectedChildren();
			if (children == null || children.length == 0) {
				return null;
			}

			for (int i = 0; i < children.length; i++) {
				final ICElementDelta d = findElement(unit, children[i]);
				if (d != null) {
					return d;
				}
			}

			return null;
		}
	}

	/**
	 * 
	 * Property change listener.
	 * 
	 * @author P.Tomaszewski
	 */
	class PropertyListener implements IPropertyChangeListener {

		/**
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getProperty();
			if (prop.equals(PreferenceConstants.OUTLINE_GROUP_INCLUDES)) {
				Object newValue = event.getNewValue();
				if (newValue instanceof Boolean) {
					boolean value = ((Boolean) newValue).booleanValue();
					if (areIncludesGroup() != value) {
						setIncludesGrouping(value);
						contentUpdated();
					}
				}
			} else if (prop
					.equals(PreferenceConstants.OUTLINE_GROUP_NAMESPACES)) {
				Object newValue = event.getNewValue();
				if (newValue instanceof Boolean) {
					boolean value = ((Boolean) newValue).booleanValue();
					if (areNamespacesGroup() != value) {
						setNamespacesGrouping(value);
						contentUpdated();
					}
				}
			}
		}

	}

}
