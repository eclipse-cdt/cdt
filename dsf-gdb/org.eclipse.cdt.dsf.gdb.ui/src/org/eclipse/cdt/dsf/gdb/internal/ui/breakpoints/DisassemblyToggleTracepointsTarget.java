/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblySelection;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Toggle tracepoint target implementation for the disassembly part.
 */
public class DisassemblyToggleTracepointsTarget implements IToggleBreakpointsTargetExtension {
	
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		assert part instanceof IDisassemblyPart && selection instanceof ITextSelection;

		if (!(selection instanceof IDisassemblySelection)) {
			selection = new DisassemblySelection((ITextSelection) selection, (IDisassemblyPart) part);
		}
		IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
		int line = disassemblySelection.getStartLine();
		IBreakpoint[] bp = getBreakpointsAtLine((IDisassemblyPart) part, line);
		if (bp == null || bp.length == 0) {
			insertBreakpoint(disassemblySelection);
		} else {
			for (int i = 0; i < bp.length; i++) {
				bp[i].delete();
			}
		}
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return part instanceof IDisassemblyPart && selection instanceof ITextSelection;
	}

	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}
	/*
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleLineBreakpoints(part, selection);
	}
	/*
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		toggleLineBreakpoints(part, selection);
	}

	private IBreakpoint[] getBreakpointsAtLine(IDisassemblyPart part, int line) {
		List<IBreakpoint> breakpoints = new ArrayList<IBreakpoint>();
		IAnnotationModel annotationModel = part.getTextViewer().getAnnotationModel();
		IDocument document = part.getTextViewer().getDocument();
		if (annotationModel != null) {
			Iterator<?> iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof SimpleMarkerAnnotation) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
							Position position = annotationModel.getPosition(markerAnnotation);
							int bpLine = document.getLineOfOffset(position.getOffset());
							if (line == bpLine) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null) {
									breakpoints.add(breakpoint);
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
		IBreakpoint[] breakpointsArray = new IBreakpoint[breakpoints.size()];
		return breakpoints.toArray(breakpointsArray );
	}
	private void insertBreakpoint(IDisassemblySelection selection) throws CoreException {
    	IAddress address = selection.getStartAddress();
    	if (address == null) {
    		return;
    	}
    	URI fileUri = selection.getSourceLocationURI();
		if (fileUri != null) {
            String filePath = null;
            IResource resource = selection.getSourceFile();
            if (resource != null) {
            	final IPath location= resource.getLocation();
            	if (location == null) {
            		return;
            	}
				filePath = location.toOSString();
            } else {
    		    resource = ResourcesPlugin.getWorkspace().getRoot();
            	filePath = URIUtil.toPath(fileUri).toOSString();
            }
            int srcLine = selection.getSourceLine();
            CDIDebugModel.createLineTracepoint(filePath, resource, ICBreakpointType.REGULAR, srcLine + 1, true, 0, "", true); //$NON-NLS-1$
    	} else {
    		IResource resource = ResourcesPlugin.getWorkspace().getRoot();
    		CDIDebugModel.createAddressTracepoint(null, null, resource, ICBreakpointType.REGULAR, -1, address, true, 0, "", true); //$NON-NLS-1$
    	}
    }
}