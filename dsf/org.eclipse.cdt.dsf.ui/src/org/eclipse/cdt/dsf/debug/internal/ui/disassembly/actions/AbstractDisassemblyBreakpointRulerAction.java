/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import java.util.Iterator;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Abstract implementation of a breakpoint ruler action.
 */
public abstract class AbstractDisassemblyBreakpointRulerAction extends AbstractDisassemblyRulerAction {

	/**
	 * Create breakpoint ruler action.
	 *
	 * @param disassemblyPart
	 * @param rulerInfo
	 */
	protected AbstractDisassemblyBreakpointRulerAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		super(disassemblyPart, rulerInfo);
	}

	/**
	 * Returns the breakpoint at the last line of mouse activity in the ruler
	 * or <code>null</code> if none.
	 *
	 * @return breakpoint associated with activity in the ruler or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint() {
		IAnnotationModel annotationModel = getAnnotationModel();
		IDocument document = getDocument();
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
							int line = document.getLineOfOffset(position.getOffset());
							if (line == getRulerInfo().getLineOfLastMouseButtonActivity()) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager()
										.getBreakpoint(marker);
								if (breakpoint != null) {
									return breakpoint;
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
		return null;
	}
}
