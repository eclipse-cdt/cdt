package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.EpiphanyVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

@SuppressWarnings("restriction")
public class DSFSessionStateEpiphany extends DSFSessionState {

	public DSFSessionStateEpiphany(String sessionId) {
		super(sessionId);
		m_sessionId = sessionId;
		m_sessionListeners = new ArrayList<Object>();
		m_servicesTracker = new DsfServicesTracker(EpiphanyVisualizerUIPlugin.getBundleContext(), m_sessionId);
	}
}
