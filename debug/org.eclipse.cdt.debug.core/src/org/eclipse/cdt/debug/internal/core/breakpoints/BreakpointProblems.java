/*******************************************************************************
 * Copyright (c) 2007, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class BreakpointProblems {
	
	/**
	 * The identifier for breakpoint problem markers.
	 */
	public static final String BREAKPOINT_PROBLEM_MARKER_ID = CDebugCorePlugin.PLUGIN_ID + ".breakpointproblem" ; //$NON-NLS-1$
	/**
	 * Breakpoint problem marker types.
	 */	
	public static final String BREAKPOINT_PROBLEM_TYPE = "bp_problem_type"; //$NON-NLS-1$
	public static final String UNRESOLVED = "unresolved"; //$NON-NLS-1$
	public static final String BAD_CONDITION = "bad_condition"; //$NON-NLS-1$
	public static final String MOVED = "moved"; //$NON-NLS-1$
	public static final String BREAKPOINT_CONTEXT_NAME = "bp_context_name"; //$NON-NLS-1$
	public static final String BREAKPOINT_CONTEXT_ID = "bp_context_id"; //$NON-NLS-1$

	public static IMarker reportBreakpointMoved(ICBreakpoint breakpoint, int oldLineNumber, int newLineNumber, String contextName, String contextID) throws CoreException {
		String message = MessageFormat.format(BreakpointMessages.getString("BreakpointProblems_Moved"), new Object[] { new Integer(oldLineNumber), new Integer(newLineNumber) }); //$NON-NLS-1$
		IMarker marker = BreakpointProblems.reportBreakpointProblem(breakpoint, message, IMarker.SEVERITY_INFO, MOVED, true, false, contextName, contextID);
		return marker;
	}

	public static IMarker reportUnresolvedBreakpoint(ICBreakpoint breakpoint, String contextName, String contextID) throws CoreException {
		IMarker marker = BreakpointProblems.reportBreakpointProblem(breakpoint, BreakpointMessages.getString("BreakpointProblems_Unresolved"), IMarker.SEVERITY_WARNING, UNRESOLVED, true, false, contextName, contextID); //$NON-NLS-1$
		return marker;
	}

	public static IMarker reportUnsupportedTracepoint(ICBreakpoint breakpoint, String contextName, String contextID) throws CoreException {
		IMarker marker = BreakpointProblems.reportBreakpointProblem(breakpoint, BreakpointMessages.getString("BreakpointProblems_UnsupportedTracepoint"), IMarker.SEVERITY_WARNING, UNRESOLVED, true, false, contextName, contextID); //$NON-NLS-1$
		return marker;
	}

	public static void removeProblemsForBreakpoint(ICBreakpoint breakpoint) throws CoreException {
		IMarker marker = breakpoint.getMarker();
		if (marker != null)
		{
			int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			IResource bpResource = marker.getResource();
			if (bpResource != null)
			{
				IMarker[] bpProblems = bpResource.findMarkers(BREAKPOINT_PROBLEM_MARKER_ID, true, IResource.DEPTH_INFINITE);
				for (int i = 0; i < bpProblems.length; i++) {
					if (bpProblems[i].getAttribute(IMarker.LINE_NUMBER, 0) == lineNumber)
					{
						bpProblems[i].delete();
					}
				}
			}
		}
		
	}

	public static void removeProblemsForResolvedBreakpoint(ICBreakpoint breakpoint, String contextID) throws CoreException {
		IMarker marker = breakpoint.getMarker();
		if (marker != null)
		{
			int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			IResource bpResource = marker.getResource();
			if (bpResource != null)
			{
				IMarker[] bpProblems = bpResource.findMarkers(BREAKPOINT_PROBLEM_MARKER_ID, true, IResource.DEPTH_INFINITE);
				for (int i = 0; i < bpProblems.length; i++) {
					if (bpProblems[i].getAttribute(BREAKPOINT_PROBLEM_TYPE, "").equalsIgnoreCase(UNRESOLVED) && //$NON-NLS-1$
							bpProblems[i].getAttribute(IMarker.LINE_NUMBER, 0) == lineNumber &&
							bpProblems[i].getAttribute(BREAKPOINT_CONTEXT_ID, "").equals(contextID)) //$NON-NLS-1$
					{
						bpProblems[i].delete();
					}
				}
			}
		}
		
	}

	public static IMarker reportBreakpointProblem(ICBreakpoint breakpoint,
			String description, int severity, String problemType, boolean removePrevious, boolean removeOnly, String contextName, String contextID) {
		try {
			if (breakpoint instanceof ICLineBreakpoint) {
				ICLineBreakpoint lineBreakpoint = (ICLineBreakpoint) breakpoint;
				IMarker marker = null;
				
				if (removePrevious)
				{
					IMarker existingMarker = lineBreakpoint.getMarker();
					if (existingMarker != null)
					{
						IResource bpResource = existingMarker.getResource();
						if (bpResource != null)
						{
							int lineNumber = existingMarker.getAttribute(IMarker.LINE_NUMBER, 0);
							IMarker[] bpProblems = bpResource.findMarkers(BREAKPOINT_PROBLEM_MARKER_ID, true, IResource.DEPTH_INFINITE);
							for (int i = 0; i < bpProblems.length; i++) {
								if (bpProblems[i].getAttribute(BREAKPOINT_PROBLEM_TYPE, "").equalsIgnoreCase(problemType) && //$NON-NLS-1$
										bpProblems[i].getAttribute(IMarker.LINE_NUMBER, 0) == lineNumber)
								{
									bpProblems[i].delete();
								}
							}
						}						
					}					
				}
				
				if (!removeOnly)
				{
					marker = reportBreakpointProblem(new ProblemMarkerInfo(
							lineBreakpoint.getMarker().getResource(),
							lineBreakpoint.getLineNumber(), description, severity,
							"")); //$NON-NLS-1$
					if (marker != null)
					{
						marker.setAttribute(BREAKPOINT_PROBLEM_TYPE, problemType);
						marker.setAttribute(BREAKPOINT_CONTEXT_NAME, contextName);
						marker.setAttribute(BREAKPOINT_CONTEXT_ID, contextID);						
					}
				}
				return marker;
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	public static IMarker reportBreakpointProblem(ProblemMarkerInfo problemMarkerInfo)
	{
		IResource markerResource = problemMarkerInfo.file ;
		if (markerResource==null)  {
			return null;
		}
		try {
			IMarker[] cur = markerResource.findMarkers(BREAKPOINT_PROBLEM_MARKER_ID, true, IResource.DEPTH_ONE);
			
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			if ((cur != null) && (cur.length > 0)) {
				for (int i = 0; i < cur.length; i++) {
					int line = ((Integer) cur[i].getAttribute(IMarker.LINE_NUMBER)).intValue();
					int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
					if (line == problemMarkerInfo.lineNumber && sev == problemMarkerInfo.severity && mesg.equals(problemMarkerInfo.description)) {
						return cur[i];
					}
				}
			}

			IMarker marker = markerResource.createMarker(BREAKPOINT_PROBLEM_MARKER_ID);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, problemMarkerInfo.severity);
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (problemMarkerInfo.externalPath != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, problemMarkerInfo.externalPath.toOSString());
			}
			
			return marker;

		} catch (CoreException e) {}
		return null;
	}

}
