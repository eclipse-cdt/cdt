/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.ui.IncludesGrouping;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

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
    private String filter = "*";

    /**
     * Creates new content provider for dialog.
     * @param viewer Tree viewer.
     */
    public CContentOutlinerProvider(TreeViewer viewer)
    {
        super(true, true);
        treeViewer = viewer;
        setIncludesGrouping(PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES));
    }

    /**
     * Sets new filter and updates contents.
     * @param newFilter New filter.
     */
    public void updateFilter(String newFilter)
    {
        filter = newFilter;
        contentUpdated();
    }

    /**
     * Called by the editor to signal that the content has updated.
     */
    public void contentUpdated()
    {
        if (treeViewer != null && !treeViewer.getControl().isDisposed())
        {
            treeViewer.getControl().getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (!treeViewer.getControl().isDisposed())
                        {
                            final ISelection sel = treeViewer.getSelection();
                            treeViewer.setSelection(updateSelection(sel));
                            treeViewer.refresh();
                        }
                    }
                }
            );
        }
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
        super.dispose();
        if (fListener != null)
        {
            CoreModel.getDefault().removeElementChangedListener(fListener);
            fListener = null;
        }
        if (fPropertyListener != null) {
            PreferenceConstants.getPreferenceStore().removePropertyChangeListener(fPropertyListener);
            fPropertyListener = null;
        }
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        final boolean isTU = newInput instanceof ITranslationUnit;

        if (isTU && fListener == null)
        {
            root = (ITranslationUnit) newInput;
            fListener = new ElementChangedListener();
            CoreModel.getDefault().addElementChangedListener(fListener);
            fPropertyListener = new PropertyListener();
            PreferenceConstants.getPreferenceStore().addPropertyChangeListener(fPropertyListener);
        }
        else if (!isTU && fListener != null)
        {
            CoreModel.getDefault().removeElementChangedListener(fListener);
            fListener = null;
            root = null;
        }
    }

    /**
     * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object element)
    {
        final StringMatcher stringMatcher = new StringMatcher(filter, true, false);
        Object[] children = super.getChildren(element);
        final List filtered = new ArrayList();
        for (int i = 0; i < children.length; i++)
        {
            if (stringMatcher.match(children[i].toString()))
            {
                filtered.add(children[i]);
            }
        }
        final int size = filtered.size();
        children = new Object[size];
        filtered.toArray(children);
        return children;
    }

    /**
     * Updates current selection.
     * @param sel Selection to update.
     * @return Updated selection.
     */
    protected ISelection updateSelection(ISelection sel)
    {
        final ArrayList newSelection = new ArrayList();
        if (sel instanceof IStructuredSelection)
        {
            final Iterator iter = ((IStructuredSelection) sel).iterator();
            while (iter.hasNext())
            {
                final Object o = iter.next();
                if (o instanceof ICElement)
                {
                    newSelection.add(o);
                }
            }
        }
        return new StructuredSelection(newSelection);
    }

    /**
     * The element change listener of the C outline viewer.
     * @see IElementChangedListener
     */
    class ElementChangedListener implements IElementChangedListener
    {

        /**
         * Default constructor.
         */
        public ElementChangedListener()
        {
            // nothing to initialize.
        }

        /**
         * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
         */
        public void elementChanged(final ElementChangedEvent e)
        {
            final ICElementDelta delta = findElement(root, e.getDelta());
            if (delta != null)
            {
                contentUpdated();
                return;
            }
        }

        /**
         * Determines is structural change.
         * @param cuDelta Delta to check.
         * @return <b>true</b> if structural change.
         */
        private boolean isPossibleStructuralChange(ICElementDelta cuDelta)
        {
            boolean ret;
            if (cuDelta.getKind() != ICElementDelta.CHANGED)
            {
                ret = true; // add or remove
            }
            else
            {
                final int flags = cuDelta.getFlags();
                if ((flags & ICElementDelta.F_CHILDREN) != 0)
                {
                    ret = true;
                }
                else
                {
                    ret = (flags & (ICElementDelta.F_CONTENT | ICElementDelta.F_FINE_GRAINED)) == ICElementDelta.F_CONTENT;
                }
            }
            return ret;
        }

        /**
         * Searches for element.
         * @param unit Unit to search in.
         * @param delta Delta.
         * @return Found element.
         */
        protected ICElementDelta findElement(ICElement unit, ICElementDelta delta)
        {
            if (delta == null || unit == null)
            {
                return null;
            }

            final ICElement element = delta.getElement();

            if (unit.equals(element))
            {
                if (isPossibleStructuralChange(delta))
                {
                    return delta;
                }
                return null;
            }

            if (element.getElementType() > ICElement.C_UNIT)
            {
                return null;
            }

            final ICElementDelta[] children = delta.getAffectedChildren();
            if (children == null || children.length == 0)
            {
                return null;
            }

            for (int i = 0; i < children.length; i++)
            {
                final ICElementDelta d = findElement(unit, children[i]);
                if (d != null)
                {
                    return d;
                }
            }

            return null;
        }
    }

    /**
     * 
     * Property change listener.
     * @author P.Tomaszewski
     */
    class PropertyListener implements IPropertyChangeListener {

        /**
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event){
            String prop = event.getProperty();
            if (prop.equals(PreferenceConstants.OUTLINE_GROUP_INCLUDES)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof Boolean) {
                    boolean value = ((Boolean)newValue).booleanValue();
                    if (areIncludesGroup() != value) {
                        setIncludesGrouping(value);
                        contentUpdated();
                    }
                }
            } else if (prop.equals(PreferenceConstants.OUTLINE_GROUP_NAMESPACES)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof Boolean) {
                    boolean value = ((Boolean)newValue).booleanValue();
                    if (areNamespacesGroup() != value) {
                        setNamespacesGrouping(value);
                        contentUpdated();
                    }
                }
            }
        }

    }

}
