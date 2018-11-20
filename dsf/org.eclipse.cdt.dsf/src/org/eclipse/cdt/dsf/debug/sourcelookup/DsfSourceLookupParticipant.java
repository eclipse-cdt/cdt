/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * Source lookup participant that should be used with DSF-based debuggers.
 *
 * @since 1.0
 */
@ThreadSafe
public class DsfSourceLookupParticipant extends AbstractSourceLookupParticipant {
	private DsfExecutor fExecutor;
	private String fSessionId;
	private DsfServicesTracker fServicesTracker;
	private Map<String, List<Object>> fLookupCache = Collections.synchronizedMap(new HashMap<String, List<Object>>());

	public DsfSourceLookupParticipant(DsfSession session) {
		fSessionId = session.getId();
		fExecutor = session.getExecutor();
		fServicesTracker = new DsfServicesTracker(DsfPlugin.getBundleContext(), fSessionId);
	}

	@Override
	public void dispose() {
		fServicesTracker.dispose();
		super.dispose();
	}

	/**
	 * This method does the same thing (is almost copy-and-paste) as
	 * {@link AbstractSourceLookupParticipant#findSourceElements(Object), but it
	 * surrounds the lookup with a cache (#fLookupCache) that needs to be
	 * cleared if the source containers change.
	 */
	@Override
	public Object[] findSourceElements(Object object) throws CoreException {
		CoreException single = null;
		MultiStatus multiStatus = null;
		List<Object> results = null;

		String name = getSourceName(object);
		if (name != null) {
			results = fLookupCache.get(name);
			if (results != null) {
				return results.toArray();
			} else {
				results = new ArrayList<>();
			}
			ISourceContainer[] containers = getSourceContainers();
			// if there is no containers, we can default to absolute path, since we should be able resolve file by absolute path
			if (containers.length == 0)
				containers = new ISourceContainer[] { new AbsolutePathSourceContainer() };
			for (int i = 0; i < containers.length; i++) {
				try {
					ISourceContainer container = getDelegateContainer(containers[i]);
					if (container != null) {
						Object[] objects = container.findSourceElements(name);
						if (objects.length > 0) {
							if (isFindDuplicates()) {
								results.addAll(Arrays.asList(objects));
							} else {
								results.add(objects[0]);
								break;
							}
						}
					}
				} catch (CoreException e) {
					if (single == null) {
						single = e;
					} else if (multiStatus == null) {
						multiStatus = new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
								new IStatus[] { single.getStatus() }, "Source Lookup error", null); //$NON-NLS-1$
						multiStatus.add(e.getStatus());
					} else {
						multiStatus.add(e.getStatus());
					}
				}
			}

			if (!results.isEmpty()) {
				synchronized (fLookupCache) {
					if (!fLookupCache.containsKey(name)) {
						fLookupCache.put(name, results);
					}
				}
			}
		}
		if (results == null || results.isEmpty()) {
			if (multiStatus != null) {
				throw new CoreException(multiStatus);
			} else if (single != null) {
				throw single;
			}
			return EMPTY;
		}
		return results.toArray();
	}

	@Override
	public void sourceContainersChanged(ISourceLookupDirector director) {
		fLookupCache.clear();
	}

	@Override
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof String) {
			return (String) object;
		}
		if (!(object instanceof IDMContext) || !((IDMContext) object).getSessionId().equals(fSessionId)) {
			return null;
		}

		final IDMContext dmc = (IDMContext) object;
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				getSourceNameOnDispatchThread(dmc, rm);
			}
		};
		fExecutor.execute(query);
		try {
			String result = query.get();
			if ((result != null) && (result.length() == 0)) {
				// interface javadoc says we should return null
				result = null;
			}
			return result;
		} catch (InterruptedException e) {
			assert false : "Interrupted exception in DSF executor"; //$NON-NLS-1$
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				throw (CoreException) e.getCause();
			}
			assert false : "Unexptected exception"; //$NON-NLS-1$
		}
		return null; // Should never get here.
	}

	@ConfinedToDsfExecutor("fExecutor")
	private void getSourceNameOnDispatchThread(IDMContext dmc, final DataRequestMonitor<String> rm) {
		if (!(dmc instanceof IStack.IFrameDMContext)) {
			rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source for this object", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		IFrameDMContext frameDmc = (IFrameDMContext) dmc;

		IStack stackService = fServicesTracker.getService(IStack.class);
		if (stackService == null) {
			rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"Stack data not available", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		stackService.getFrameData(frameDmc, new DataRequestMonitor<IFrameDMData>(fExecutor, rm) {
			@Override
			public void handleSuccess() {
				rm.setData(getData().getFile());
				rm.done();
			}
		});
	}
}
