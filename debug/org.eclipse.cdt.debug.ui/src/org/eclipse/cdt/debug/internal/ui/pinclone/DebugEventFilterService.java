/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *     Patrick Chuong (Texas Instruments) - Bug 358135
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.pinclone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class provides debug event filtering service for the pin-able views.
 */
public class DebugEventFilterService {	
	
	/**
	 * A debug context event listen that provides filter support 
	 * for the pinned debug context.
	 */
	private class DebugEventFilter implements IDebugContextListener {
		private final DebugContextPinProvider fProvider;
		
		private DebugEventFilter(DebugContextPinProvider provider) {
			fProvider = provider;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
		 */
		@Override
		public void debugContextChanged(DebugContextEvent event) {
			ISelection eventContext = event.getContext();
			if (eventContext instanceof IStructuredSelection) {
				List<Object> filteredContextList = new ArrayList<Object>();
				List<?> eventContextList = ((IStructuredSelection)eventContext).toList();
				for (Object o : eventContextList) {
					if (fProvider.isPinnedTo(o)) {
						if (fProvider != event.getDebugContextProvider()) {
							filteredContextList.add(o);
						}
					}
				}
				if (filteredContextList.size() > 0) {
					fProvider.delegateEvent(new DebugContextEvent(fProvider, new StructuredSelection(filteredContextList), event.getFlags()));
				}
			}
 		}
		
		public DebugContextPinProvider getTranslator() {
			return fProvider;
		}
	}
	
	private static DebugEventFilterService INSTANCE;
	private Map<IWorkbenchPart, DebugEventFilter> fFilterMap = new HashMap<IWorkbenchPart, DebugEventFilter>();
	
	private DebugEventFilterService() {
	}
	
	public static synchronized DebugEventFilterService getInstance() {
		if (INSTANCE == null) 
			INSTANCE = new DebugEventFilterService();
		return INSTANCE;
	}
	
	/**
	 * Add debug event filter for the provided part and filter debug context change 
	 * event for the provided debug context.
	 * 
	 * @param part the part to filter debug context change event.
	 * @param debugContext the debug context that filter should stick to.
	 * @return the debug context provider that handles the filtering.
	 */
	public DebugContextPinProvider addDebugEventFilter(IWorkbenchPart part, ISelection debugContext) {
		DebugContextPinProvider contextProvider = null;
		DebugEventFilter filter = null;
		
		synchronized (fFilterMap) {
			if (fFilterMap.containsKey(part)) {
				return null;
			}			
			
			contextProvider = new DebugContextPinProvider(part, debugContext);
			filter = new DebugEventFilter(contextProvider);
			fFilterMap.put(part, filter);
		}
		
		assert contextProvider != null && filter != null;
		
		IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(part.getSite().getWorkbenchWindow());
		contextService.addDebugContextProvider(contextProvider);
		contextService.addDebugContextListener(filter);
		
		return contextProvider;
	}
	
	/**
	 * Remove debug event filter for the provided part.
	 * 
	 * @param part the workbench part.
	 */
	public void removeDebugEventFilter(IWorkbenchPart part) {
		DebugEventFilter filter = null;
		
		synchronized (fFilterMap) {
			if (!fFilterMap.containsKey(part)) {
				return;
			}
			
			filter = fFilterMap.remove(part);
		}

		assert filter != null;
		
		DebugContextPinProvider contextProvider = filter.getTranslator();
		IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(part.getSite().getWorkbenchWindow());
		
		// send a change notification to the listener to update with selected context
		contextProvider.delegateEvent(new DebugContextEvent(contextProvider, contextService.getActiveContext(), DebugContextEvent.ACTIVATED));
		
		// removes the listener and provider
		contextService.removeDebugContextListener(filter);
		contextService.removeDebugContextProvider(contextProvider);
		contextProvider.dispose();
	}
}
