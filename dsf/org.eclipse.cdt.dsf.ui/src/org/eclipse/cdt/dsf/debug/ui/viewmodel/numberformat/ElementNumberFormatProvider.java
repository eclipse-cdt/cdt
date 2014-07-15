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

import java.util.HashSet;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SimpleMapPersistable;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.ElementFormatEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Default implementation of the {@link IElementFormatProvider}.  It can be 
 * used within any {@link IVMProvider} to store and persist number-formats 
 * selected by user for different elements.
 * 
 * @since 2.5
 */
public class ElementNumberFormatProvider implements IElementFormatProvider
{
	private static String ELEMENT_FORMAT_PERSISTABLE_PROPERTY = "org.eclipse.cdt.dsf.ui.elementFormatPersistable"; //$NON-NLS-1$

    private final IVMProvider fVMProvider;
    private DsfSession fSession;
    
    public ElementNumberFormatProvider(IVMProvider vmProvider, DsfSession session) {
        fVMProvider = vmProvider;
        fSession = session;
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
            new ImmediateDataRequestMonitor<String>(rm) {
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
        final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor() {
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
                new ImmediateDataRequestMonitor<String>(crm) {
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
    	if (context instanceof IDMVMContext) {
    		// The expressions view supports expression groups, which have no value,
    		// so we should not support formatting for expression groups.
    		if (((IDMVMContext)context).getDMContext() instanceof IExpressionGroupDMContext) { 
    			return false;
    		}
    	}
        return context instanceof IFormattedValueVMContext;
    }

    protected void getElementKey(Object viewerInput, TreePath elementPath, final DataRequestMonitor<String> rm) {
    	Object element = elementPath.getLastSegment();
    	if (element instanceof IDMVMContext) {
			final IDMContext dmc = ((IDMVMContext)element).getDMContext();
			if (dmc instanceof IExpressionDMContext) {
				rm.done(((IExpressionDMContext)dmc).getExpression());
				return;
			} else if (dmc instanceof IRegisterDMContext) {
				fSession.getExecutor().execute(new DsfRunnable() {					
					@Override
					public void run() {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fSession.getId());
						IRegisters regService = tracker.getService(IRegisters.class);
						tracker.dispose();
						
						regService.getRegisterData((IRegisterDMContext)dmc, new ImmediateDataRequestMonitor<IRegisterDMData>(rm) {
							@Override
							protected void handleSuccess() {
								rm.done(getData().getName());
							}
						});
					}
				});
				return;
			}
		}
        rm.done(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Cannot calculate peristable key for element: " + element, null)); //$NON-NLS-1$
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
