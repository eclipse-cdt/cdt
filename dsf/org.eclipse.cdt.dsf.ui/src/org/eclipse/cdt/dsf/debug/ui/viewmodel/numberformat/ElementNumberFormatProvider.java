/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SimpleMapPersistable;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.ElementFormatEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMPropertiesUpdate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;

/**
 * Default implementation of the {@link IElementFormatProvider}.  It can be 
 * used within any {@link IVMProvider} to store and persist number-formats 
 * selected by user for different elements.  This implementation relies on the 
 * VM Nodes to supply the {@link IElementFormatProvider#PROP_FORMAT_KEY} 
 * property value.
 * 
 * @since 2.5
 */
public class ElementNumberFormatProvider implements IElementFormatProvider
{
	private static String ELEMENT_FORMAT_PERSISTABLE_PROPERTY = "org.eclipse.cdt.dsf.ui.elementFormatPersistable"; //$NON-NLS-1$

    private static final Set<String> PROPS_KEY = new TreeSet<String>(Arrays.asList(new String[] {
        IElementFormatProvider.PROP_FORMAT_KEY
    }));

    private final IVMProvider fVMProvider;
    
    public ElementNumberFormatProvider(IVMProvider vmProvider, DsfSession session) {
        fVMProvider = vmProvider;
    }
    
    private IVMProvider getVMProvider() {
        return fVMProvider;
    }

    @Override
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, final TreePath elementPath,
        final DataRequestMonitor<String> rm) 
    {
        getElementKey(
            viewerInput, elementPath, 
            new DataRequestMonitor<String>(SimpleDisplayExecutor.getSimpleDisplayExecutor(Display.getDefault()), rm) {
                @Override
                protected void handleSuccess() {
                    SimpleMapPersistable<String> persistable = getPersistable();
                    rm.done(persistable.getValue(getData()));
                }
            });
    }

    @Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput,
        TreePath[] elementPaths, final String format) 
    {
        final HashSet<Object> elementsToRefresh = new HashSet<>();
        final CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), null) {
            @Override
            protected void handleCompleted() {
            	if (elementsToRefresh.size() > 0) {
            		((AbstractVMProvider)getVMProvider()).handleEvent(new ElementFormatEvent(elementsToRefresh, 1));
            	}
            }
        };
        for (final TreePath path : elementPaths) {
            getElementKey(
                viewerInput, path, 
                new DataRequestMonitor<String>(ImmediateExecutor.getInstance(), crm) {
                    @Override
                    protected void handleSuccess() {
                        SimpleMapPersistable<String> persistable = getPersistable();
                        persistable.setValue(getData(), format);
                        elementsToRefresh.add(path.getLastSegment());
                        crm.done();
                    }
                });
        }
        crm.setDoneCount(elementPaths.length);
    }

    @Override
	public boolean supportFormat(IVMContext context) {
        return context instanceof IFormattedValueVMContext;
    }

    protected void getElementKey(Object viewerInput, TreePath elementPath, final DataRequestMonitor<String> rm) {
        Object element = elementPath.getLastSegment(); 
        IElementPropertiesProvider propertiesProvider = (IElementPropertiesProvider)
            DebugPlugin.getAdapter(element, IElementPropertiesProvider.class);
        if (element instanceof IVMContext && propertiesProvider != null) {
            IVMProvider provider = ((IVMContext)element).getVMNode().getVMProvider();
            propertiesProvider.update(new IPropertiesUpdate[] { 
                new VMPropertiesUpdate(
                    PROPS_KEY, elementPath, viewerInput, provider.getPresentationContext(), 
                    new DataRequestMonitor<Map<String,Object>>(getVMProvider().getExecutor(), rm) {
                        @Override
                        protected void handleSuccess() {
                        	rm.done((String)getData().get(IElementFormatProvider.PROP_FORMAT_KEY));
                        }
                    }) 
            });
        } else {
            rm.done(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Cannot calculate peristable key for element: " + element, null)); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unchecked")
	protected SimpleMapPersistable<String> getPersistable() {
        Object p = getVMProvider().getPresentationContext().getProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY);
        if (p instanceof SimpleMapPersistable) {
            return (SimpleMapPersistable<String>)p;
        } else {
        	SimpleMapPersistable<String> persistable = new SimpleMapPersistable<>(String.class);
            getVMProvider().getPresentationContext().setProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY, persistable);
            return persistable;
        }
    }
}
