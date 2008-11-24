/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - Added support for AbsoluteSourceContainer( 159833 ) 
 * Ken Ryall (Nokia) - Added support for CSourceNotFoundElement ( 167305 )
 * Ken Ryall (Nokia) - Option to open disassembly view when no source ( 81353 )
 * James Blackburn (Broadcom Corp.) - Linked Resources / Nested Projects ( 247948 )
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import java.io.File;

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.cdt.debug.internal.core.CBreakpointManager;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
 
/**
 * A source lookup participant that searches for C/C++ source code.
 */
public class CSourceLookupParticipant extends AbstractSourceLookupParticipant {

	static class NoSourceElement {
	}

	private static final NoSourceElement gfNoSource = new NoSourceElement();

	private ListenerList fListeners;

	/** 
	 * Constructor for CSourceLookupParticipant. 
	 */
	public CSourceLookupParticipant() {
		super();
		fListeners = new ListenerList( 1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	public String getSourceName( Object object ) throws CoreException {
		if ( object instanceof String ) {
			return (String)object;
		}
		if ( object instanceof IAdaptable ) {
			ICStackFrame frame = (ICStackFrame)((IAdaptable)object).getAdapter( ICStackFrame.class );
			if ( frame != null ) {
				String name = frame.getFile();
				return ( name != null && name.trim().length() > 0 ) ? name : null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#findSourceElements(java.lang.Object)
	 */
	@Override
	public Object[] findSourceElements( Object object ) throws CoreException {

		// Workaround for BUG247977
		// FIXME: Remove having switched to 3.5 platform
		initContainersSourceDirector();

		// Workaround for cases when the stack frame doesn't contain the source file name 
		String name = null;
		IBreakpoint breakpoint = null;
		if ( object instanceof IAdaptable ) {
			ICStackFrame frame = (ICStackFrame)((IAdaptable)object).getAdapter( ICStackFrame.class );
			if ( frame != null ) {
				name = frame.getFile().trim();
				if ( name == null || name.length() == 0 )
				{
					if (object instanceof IDebugElement)
						return new Object[] { new CSourceNotFoundElement( (IDebugElement) object ) };
					else
						return new Object[] { gfNoSource };					
				}
			}
			// See if findSourceElements(...) is the result of a Breakpoint Hit Event
			ICDebugTarget target =  (ICDebugTarget)((IAdaptable)object).getAdapter( ICDebugTarget.class );
			if (target != null) {
				CBreakpointManager bmanager = (CBreakpointManager)target.getAdapter(CBreakpointManager.class);
				Object stateInfo = target.getCurrentStateInfo();
				if (bmanager != null && stateInfo instanceof ICDIBreakpointHit) {
					breakpoint = bmanager.getBreakpoint(((ICDIBreakpointHit)stateInfo).getBreakpoint());
				}
			}
		} else if ( object instanceof String ) {
			name = (String)object;
		}

		// Actually query the source containers for the requested resource
		Object[] foundElements = super.findSourceElements(object);

		// If none found, invoke the absolute path container directly
		if (foundElements.length == 0 && (object instanceof IDebugElement)) {
			// debugger could have resolved it itself and "name" is an absolute path
			if (new File(name).exists()) {
				foundElements = new AbsolutePathSourceContainer().findSourceElements(name);
			} else {
				foundElements = new Object[] { new CSourceNotFoundElement((IDebugElement) object) };
			}
		}

		// Source lookup participant order is preserved where possible except for one case:
		//   - If we've stopped at a breakpoint the user has made on an IResource, we definitely want to show
		//     that IResource before others
		if (breakpoint != null && breakpoint.getMarker() != null && breakpoint.getMarker().getResource() != null) {
			IResource breakpointResource = breakpoint.getMarker().getResource();
			for (int i = 0; i < foundElements.length; i++) {
				if (foundElements[i].equals(breakpointResource)) {
					Object temp = foundElements[0];
					foundElements[0] = foundElements[i];
					foundElements[i] = temp;
					break;
				}
			}
		}

		// FIXME: remove when BUG247977 is fixed
		endContainersSourceDirector();

		return foundElements;
	}

	/**
	 * Override default.  We want all the source elements we can get from the source containers
	 * so we can select the 'correct' element on the user's behalf.
	 * {@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=247948}
	 * @see CSourceLookupParticipant#findSourceElements(Object)
	 */
	@Override
	public boolean isFindDuplicates() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#dispose()
	 */
	@Override
	public void dispose() {
		fListeners.removeAll();
		super.dispose();
	}

	public void addSourceLookupChangeListener( ISourceLookupChangeListener listener ) {
		fListeners.add( listener );
	}

	public void removeSourceLookupChangeListener( ISourceLookupChangeListener listener ) {
		fListeners.remove( listener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	@Override
	public void sourceContainersChanged( ISourceLookupDirector director ) {
		Object[] listeners = fListeners.getListeners();
		for ( int i = 0; i < listeners.length; ++i )
			((ISourceLookupChangeListener)listeners[i]).sourceContainersChanged( director );
		super.sourceContainersChanged( director );
	}

	/**
	 * FIXME: Workaround for BUG247977
	 * Remove before 3.5
	 * Remove when ISourceLocator.isFindDuplicates() queries the source lookup participant
	 * instead of the ISourceLookupDirector
	 */
	private ISourceContainer[] containers;
	private void initContainersSourceDirector() {
		ISourceLookupDirector dummySourceDirector = new ISourceLookupDirector() {

			public void addParticipants(ISourceLookupParticipant[] participants) {
				getDirector().addParticipants(participants);
			}

			public void clearSourceElements(Object element) {
				getDirector().clearSourceElements(element);
			}

			public Object[] findSourceElements(Object object) throws CoreException {
				return getDirector().findSourceElements(object);
			}

			public String getId() {
				return getDirector().getId();
			}

			public ILaunchConfiguration getLaunchConfiguration() {
				return getDirector().getLaunchConfiguration();
			}

			public ISourceLookupParticipant[] getParticipants() {
				return getDirector().getParticipants();
			}

			public ISourceContainer[] getSourceContainers() {
				return getDirector().getSourceContainers();
			}

			public Object getSourceElement(Object element) {
				return getDirector().getSourceElement(element);
			}

			public ISourcePathComputer getSourcePathComputer() {
				return getDirector().getSourcePathComputer();
			}

			public void initializeParticipants() {
				getDirector().initializeParticipants();
			}

			public boolean isFindDuplicates() {
				return CSourceLookupParticipant.this.isFindDuplicates();
			}

			public void removeParticipants(ISourceLookupParticipant[] participants) {
				getDirector().removeParticipants(participants);
			}

			public void setFindDuplicates(boolean findDuplicates) {
				getDirector().setFindDuplicates(findDuplicates);
			}

			public void setSourceContainers(ISourceContainer[] containers) {
				getDirector().setSourceContainers(containers);
			}

			public void setSourcePathComputer(ISourcePathComputer computer) {
				getDirector().setSourcePathComputer(computer);
			}

			public boolean supportsSourceContainerType(ISourceContainerType type) {
				return getDirector().supportsSourceContainerType(type);
			}

			public void dispose() {
				getDirector().dispose();
			}

			public void initializeFromMemento(String memento, ILaunchConfiguration configuration) throws CoreException {
				getDirector().initializeFromMemento(memento, configuration);
			}

			public String getMemento() throws CoreException {
				return getDirector().getMemento();
			}

			public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
				getDirector().initializeDefaults(configuration);
			}

			public void initializeFromMemento(String memento) throws CoreException {
				getDirector().initializeFromMemento(memento);
			}

			public Object getSourceElement(IStackFrame stackFrame) {
				return getDirector().getSourceElement(stackFrame);
			}
			
		};
		containers = getSourceContainers();
		for (ISourceContainer cont : containers)
			cont.init(dummySourceDirector);
	}
	
	private void endContainersSourceDirector() {
		for (ISourceContainer cont : containers)
			cont.init(getDirector());
		containers = null;
	}
}
