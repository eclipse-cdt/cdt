/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.RejectedExecutionException;

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
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Default implementation of the {@link IElementFormatProvider}.  It can be
 * used within any {@link IVMProvider} to store and persist number-formats
 * selected by user for different elements.
 *
 * @since 2.5
 */
public class ElementNumberFormatProvider implements IElementFormatProvider {
	private static final String ELEMENT_FORMAT_PERSISTABLE_PROPERTY = "org.eclipse.cdt.dsf.ui.elementFormatPersistable"; //$NON-NLS-1$
	private static final String FILTER_PROVIDER_ID = ElementNumberFormatProvider.class.getName() + ".eventFilter"; //$NON-NLS-1$

	private final IVMProvider fVMProvider;
	private final DsfSession fSession;
	private final Dictionary<String, String> fFilterProperties = new Hashtable<>();

	public ElementNumberFormatProvider(IVMProvider vmProvider, DsfSession session) {
		fVMProvider = vmProvider;
		fSession = session;
		initialize();
	}

	protected void initialize() {
		IPresentationContext presentationCtx = getVMProvider().getPresentationContext();

		IWorkbenchPart part = presentationCtx.getPart();
		String provider;
		if (part != null) {
			// Use an id that is unique to the instance of the view
			// Note that although each view, including cloned ones, has its own presentation context,
			// the presentation context id returned by getPresentationContext().getId() is the
			// same for cloned views even though the presentation context itself is different.
			// So we cannot use getPresentationContext().getId() as an unique id.
			// Using the title of the view is also problematic as that title can
			// be modified by a pin action (bug 511057)
			// To get a fixed unique id for each cloned view we can use the name of the part
			if (part instanceof IWorkbenchPart2) {
				provider = ((IWorkbenchPart2) part).getPartName();
			} else {
				provider = part.getTitle();
			}
		} else {
			// In some cases, we are not dealing with a part, e.g., the hover.
			// In this case, use the presentation context id directly.
			// Note that the hover will probably not provide per-element formating,
			// but some extenders may choose to do so.
			provider = getVMProvider().getPresentationContext().getId();
		}

		// Create the filter properties targeted at our provider, to be used when sending events
		fFilterProperties.put(FILTER_PROVIDER_ID, provider);

		// Properly formatted OSGI filter string aimed at our provider
		String filterStr = "(&(" + FILTER_PROVIDER_ID + "=" + provider + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			final Filter filter = DsfUIPlugin.getBundleContext().createFilter(filterStr);
			fSession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					// Only listen to events that are aimed at our provider by using a filter.
					// That avoids updating ourselves for an event that was triggered by another view.
					fSession.addServiceEventListener(ElementNumberFormatProvider.this, filter);
				}
			});
		} catch (InvalidSyntaxException e) {
			assert false : e.getMessage();
		} catch (RejectedExecutionException e) {
		}
	}

	public void dispose() {
		try {
			fSession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					fSession.removeServiceEventListener(ElementNumberFormatProvider.this);
				}
			});
		} catch (RejectedExecutionException e) {
		}
	}

	@DsfServiceEventHandler
	public final void eventDispatched(ElementFormatEvent event) {
		if (getVMProvider() instanceof AbstractVMProvider) {
			((AbstractVMProvider) getVMProvider()).handleEvent(event);
		}
	}

	private IVMProvider getVMProvider() {
		return fVMProvider;
	}

	@Override
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput,
			final TreePath elementPath, final DataRequestMonitor<String> rm) {
		getElementKey(viewerInput, elementPath, new ImmediateDataRequestMonitor<String>(rm) {
			@Override
			protected void handleSuccess() {
				SimpleMapPersistable<String> persistable = getPersistable();
				rm.done(persistable.getValue(getData()));
			}
		});
	}

	@Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput,
			TreePath[] elementPaths, final String format) {
		final HashSet<Object> elementsToRefresh = new HashSet<>();
		final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor() {
			@Override
			protected void handleCompleted() {
				if (!elementsToRefresh.isEmpty()) {
					// Send the event to all DSF sessions as they share the same view and the
					// change of format will affect them as well.  This is because they key
					// we use from this implementation of getElementKey() is not specific to
					// a session (and should not be if we want to have proper persistence).
					for (DsfSession session : DsfSession.getActiveSessions()) {
						// Use the filterProperties to specify that this event only impacts the current view.
						session.dispatchEvent(new ElementFormatEvent(elementsToRefresh, 1), fFilterProperties);
					}
				}
			}
		};
		for (final TreePath path : elementPaths) {
			getElementKey(viewerInput, path, new ImmediateDataRequestMonitor<String>(crm) {
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
			if (((IDMVMContext) context).getDMContext() instanceof IExpressionGroupDMContext) {
				return false;
			}
		}
		return context instanceof IFormattedValueVMContext;
	}

	// We do not make the element key session-specific or else when we start a new session for the same
	// program, the format we chose will not be persisted.  Instead, make the format change valid for
	// any session, even if other sessions run a different program.  The idea is that a user usually
	// names her variables similarly so the chosen format should apply properly anyway.
	protected void getElementKey(Object viewerInput, TreePath elementPath, final DataRequestMonitor<String> rm) {
		Object element = elementPath.getLastSegment();
		if (element instanceof IDMVMContext) {
			final IDMContext dmc = ((IDMVMContext) element).getDMContext();
			if (dmc instanceof IExpressionDMContext) {
				rm.done(((IExpressionDMContext) dmc).getExpression());
				return;
			} else if (dmc instanceof IRegisterDMContext) {
				fSession.getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(),
								fSession.getId());
						IRegisters regService = tracker.getService(IRegisters.class);
						tracker.dispose();

						regService.getRegisterData((IRegisterDMContext) dmc,
								new ImmediateDataRequestMonitor<IRegisterDMData>(rm) {
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
		rm.done(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
				"Cannot calculate peristable key for element: " + element, null)); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	protected SimpleMapPersistable<String> getPersistable() {
		Object p = getVMProvider().getPresentationContext().getProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY);
		if (p instanceof SimpleMapPersistable) {
			return (SimpleMapPersistable<String>) p;
		} else {
			SimpleMapPersistable<String> persistable = new SimpleMapPersistable<>(String.class);
			getVMProvider().getPresentationContext().setProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY, persistable);
			return persistable;
		}
	}
}
