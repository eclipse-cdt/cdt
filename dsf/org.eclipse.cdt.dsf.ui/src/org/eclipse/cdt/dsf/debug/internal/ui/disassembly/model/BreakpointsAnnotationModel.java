/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - bug 300053
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.math.BigInteger;
import java.util.Iterator;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblyAnnotationModel;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IBreakpointLocationProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Annotation model for breakpoints in the disassembly.
 * Works only with {@link DisassemblyDocument}.
 */
public class BreakpointsAnnotationModel extends DisassemblyAnnotationModel implements IBreakpointListener, IDocumentListener {
	
	private Runnable fCatchup;

	@Override
	public void connect(IDocument document) {
		super.connect(document);
		if (document instanceof DisassemblyDocument) {
			final IBreakpointManager bpMgr= DebugPlugin.getDefault().getBreakpointManager();
			addBreakpoints(bpMgr.getBreakpoints());
			bpMgr.addBreakpointListener(this);
			document.addDocumentListener(this);
		}
	}

	@Override
	public void disconnect(IDocument document) {
		if (document instanceof DisassemblyDocument) {
			final IBreakpointManager bpMgr= DebugPlugin.getDefault().getBreakpointManager();
			bpMgr.removeBreakpointListener(this);
			document.removeDocumentListener(this);
			fCatchup= null;
		}
		super.disconnect(document);
	}

	private void catchupWithBreakpoints() {
		removeAllAnnotations(false);
		final IBreakpointManager bpMgr= DebugPlugin.getDefault().getBreakpointManager();
		addBreakpoints(bpMgr.getBreakpoints());
	}

	private void addBreakpoints(IBreakpoint[] breakpoints) {
		for (IBreakpoint breakpoint : breakpoints) {
			addBreakpointAnnotation(breakpoint, false);
		}
		fireModelChanged();
	}

	/*
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		addBreakpointAnnotation(breakpoint, true);
	}

	/*
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		Annotation a= findAnnotation(breakpoint.getMarker());
		if (a != null) {
			if (a instanceof SimpleMarkerAnnotation) {
				((SimpleMarkerAnnotation)a).update();
			}
			synchronized (getLockObject()) {
				getAnnotationModelEvent().annotationChanged(a);
			}
			fireModelChanged();
		} else {
			addBreakpointAnnotation(breakpoint, true);
		}
	}

	/*
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		Annotation a= findAnnotation(breakpoint.getMarker());
		if (a != null) {
			removeAnnotation(a, true);
		}
	}

	@SuppressWarnings("unchecked")
	private Annotation findAnnotation(IMarker marker) {
		for (Iterator<SimpleMarkerAnnotation> it= getAnnotationIterator(false); it.hasNext();) {
			SimpleMarkerAnnotation a= it.next();
			if (a.getMarker().equals(marker)) {
				return a;
			}
		}
		return null;
	}

	private void addBreakpointAnnotation(IBreakpoint breakpoint, boolean fireEvent) {
		if (fDocument == null) {
			return;
		}
		final IMarker marker= breakpoint.getMarker();
		if (marker == null) {
			return;
		}
		try {
			Position position = createPositionFromBreakpoint(breakpoint);
			if (position != null) {
				addAnnotation(new MarkerAnnotation(marker), position, fireEvent);
			}
		} catch (CoreException exc) {
			// ignore problems accessing attributes
		} catch (BadLocationException exc) {
			// ignore wrong positions
		}
	}
	
	private Position createPositionFromBreakpoint(IBreakpoint breakpoint) throws CoreException {
		IBreakpointLocationProvider locationProvider = (IBreakpointLocationProvider) breakpoint.getAdapter(IBreakpointLocationProvider.class);
			
		/* if there is a location provider, than use the provider to retrieve the location */
		if (locationProvider != null) {

			/* if there is source info, than create a source line position */
			String sourceFile = locationProvider.getSourceFile(breakpoint);
			if (sourceFile != null) {
				int lineNumber = locationProvider.getLineNumber(breakpoint) - 1;
				return createPositionFromSourceLine(sourceFile, lineNumber);
			
			} else {
				/* if there is label info, than create a label position */
				IAddress labelAddress = locationProvider.getLabelAddress(breakpoint);
				if (labelAddress != null) {
					return createPositionFromLabel(labelAddress.getValue());
				
				/* Otherwise, create an address position */
				} else {
					// See bug 300053 comment 5:
					// Since there can only be one annotation per marker and in order to support multiple 
					// annotations per breakpoint, we need a specialized annotation type.
					//
					// So for now, we only create an annotation for the first valid address. We can add 
					// support for multiple annotations per breakpoint when it's needed.
					IAddress[] addresses = locationProvider.getAddresses(breakpoint);
					for (int i = 0; addresses != null && i < addresses.length; ++i) {
						BigInteger address = addresses[i].getValue();
						Position position = createPositionFromAddress(address);
						if (position != null)
							return position;
					}
				}
			}
			
		/* otherwise, use legacy ICBreakpoint location info */
		} else {
			if (breakpoint instanceof ICAddressBreakpoint) {
				ICAddressBreakpoint addressBreakpoint= (ICAddressBreakpoint) breakpoint;
				return createPositionFromAddress(decodeAddress(addressBreakpoint.getAddress()));
			} else if (breakpoint instanceof ILineBreakpoint) {
				ILineBreakpoint lineBreakpoint= (ILineBreakpoint) breakpoint;
				Position position= null;
				final int lineNumber= lineBreakpoint.getLineNumber() - 1;
				final IMarker marker= breakpoint.getMarker();
				if (marker.getResource().getType() == IResource.FILE) {
					position= createPositionFromSourceLine((IFile) marker.getResource(), lineNumber);
					if (position != null) {
						return position;
					}
				}
				String fileName= marker.getAttribute(ICLineBreakpoint.SOURCE_HANDLE, null);
				position= createPositionFromSourceLine(fileName, lineNumber);
				if (position == null && breakpoint instanceof ICLineBreakpoint) {
					ICLineBreakpoint cBreakpoint= (ICLineBreakpoint) breakpoint;
					if (breakpoint instanceof ICFunctionBreakpoint) {
						position= createPositionFromLabel(cBreakpoint.getFunction());
					} else {
						position= createPositionFromAddress(decodeAddress(cBreakpoint.getAddress()));
					}
				}
				return position;
			}
		}
		return null;
	}

	/**
	 * Decode given string representation of a non-negative integer. A
	 * hexadecimal encoded integer is expected to start with <code>0x</code>.
	 * 
	 * @param string
	 *            decimal or hexadecimal representation of an non-negative integer
	 * @return address value as <code>BigInteger</code> or <code>null</code> in case of a <code>NumberFormatException</code>
	 */
	private static BigInteger decodeAddress(String string) {
		if (string != null) {
			try {
				if (string.startsWith("0x")) { //$NON-NLS-1$
					return new BigInteger(string.substring(2), 16);
				}
				if (string.length() > 0) {
					return new BigInteger(string);
				}
			} catch (NumberFormatException nfe) {
				// don't propagate
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void documentChanged(DocumentEvent event) {
		if (fCatchup == null && event.fText != null && event.fText.length() > 0) {
			fCatchup= new Runnable() {
				@Override
				public void run() {
					if (fCatchup == this) {
						catchupWithBreakpoints();
						fCatchup= null;
					}
				}};
			Display.getCurrent().timerExec(50, fCatchup);
		}
	}

}
