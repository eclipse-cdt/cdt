/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.propertypages; 

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
 
public class ThreadFilterEditor {

	/**
	 * Comment for ThreadFilterEditor.
	 */
	public class CheckHandler implements ICheckStateListener {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged( CheckStateChangedEvent event ) {
			Object element = event.getElement();
			if ( element instanceof IDebugTarget ) {
				checkTarget( (IDebugTarget)element, event.getChecked() );
			}
			else if ( element instanceof IThread ) {
				checkThread( (IThread)element, event.getChecked() );
			}
		}

		/**
		 * Check or uncheck a debug target in the tree viewer.
		 * When a debug target is checked, attempt to
		 * check all of the target's threads by default.
		 * When a debug target is unchecked, uncheck all
		 * its threads.
		 */
		protected void checkTarget( IDebugTarget target, boolean checked ) {
			getThreadViewer().setChecked( target, checked );
			getThreadViewer().setGrayed( target, false );
			getThreadViewer().expandToLevel( target, AbstractTreeViewer.ALL_LEVELS );
			IThread[] threads;
			try {
				threads = target.getThreads();
			}
			catch( DebugException exception ) {
				DebugUIPlugin.log( exception );
				return;
			}
			for( int i = 0; i < threads.length; i++ ) {
				getThreadViewer().setChecked( threads[i], checked );
				getThreadViewer().setGrayed( threads[i], false );
			}
		}
	
		/**
		 * Check or uncheck a thread.
		 * Update the thread's debug target.
		 */
		protected void checkThread( IThread thread, boolean checked ) {
			getThreadViewer().setChecked( thread, checked );
			IDebugTarget target = (thread).getDebugTarget();
			IThread[] threads;
			try {
				threads = target.getThreads();
			}
			catch( DebugException exception ) {
				DebugUIPlugin.log( exception );
				return;
			}
			int checkedNumber = 0;
			for( int i = 0; i < threads.length; i++ ) {
				if ( getThreadViewer().getChecked( threads[i] ) ) {
					++checkedNumber;
				}
			}
			if ( checkedNumber == 0 ) {
				getThreadViewer().setChecked( target, false );
				getThreadViewer().setGrayed( target, false );
			}
			else if ( checkedNumber == threads.length ) {
				getThreadViewer().setChecked( target, true );
				getThreadViewer().setGrayed( target, false );
			}
			else {
				getThreadViewer().setGrayChecked( target, true );
			}
		}
	}

	/**
	 * Comment for ThreadFilterEditor.
	 */
	public class ThreadFilterContentProvider implements ITreeContentProvider {

		/** 
		 * Constructor for ThreadFilterContentProvider. 
		 */
		public ThreadFilterContentProvider() {
			super();
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren( Object parent ) {
			if ( parent instanceof IDebugTarget ) {
				ICDebugTarget target = (ICDebugTarget)((IDebugTarget)parent).getAdapter( ICDebugTarget.class );
				if ( target != null ) {
					try {
						return ((ICDebugTarget)parent).getThreads();
					}
					catch( DebugException e ) {
						DebugUIPlugin.log( e );
					}
				}
			}
			if ( parent instanceof ILaunchManager ) {
				List children = new ArrayList();
				ILaunch[] launches = ((ILaunchManager)parent).getLaunches();
				IDebugTarget[] targets;
				ICDebugTarget target;
				for( int i = 0, numLaunches = launches.length; i < numLaunches; i++ ) {
					targets = launches[i].getDebugTargets();
					for( int j = 0, numTargets = targets.length; j < numTargets; j++ ) {
						target = (ICDebugTarget)targets[j].getAdapter( ICDebugTarget.class );
						if ( target != null && !target.isDisconnected() && !target.isTerminated() ) {
							children.add( target );
						}
					}
				}
				return children.toArray();
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent( Object element ) {
			if ( element instanceof IThread ) {
				return ((IThread)element).getDebugTarget();
			}
			if ( element instanceof IDebugTarget ) {
				return ((IDebugElement)element).getLaunch();
			}
			if ( element instanceof ILaunch ) {
				return DebugPlugin.getDefault().getLaunchManager();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren( Object element ) {
			if ( element instanceof IStackFrame ) {
				return false;
			}
			if ( element instanceof IDebugElement ) {
				return getChildren( element ).length > 0;
			}
			if ( element instanceof ILaunch ) {
				return true;
			}
			if ( element instanceof ILaunchManager ) {
				return ((ILaunchManager)element).getLaunches().length > 0;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements( Object inputElement ) {
			return getChildren( inputElement );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
		}
	}

	private CBreakpointFilteringPage fPage;

	private CheckboxTreeViewer fThreadViewer;

	private ThreadFilterContentProvider fContentProvider;

	private CheckHandler fCheckHandler;

	/** 
	 * Constructor for ThreadFilterEditor. 
	 */
	public ThreadFilterEditor( Composite parent, CBreakpointFilteringPage page ) {
		fPage = page;
		fContentProvider = new ThreadFilterContentProvider();
		fCheckHandler = new CheckHandler();
		createThreadViewer( parent );
	}

	protected CBreakpointFilteringPage getPage() {
		return fPage;
	}

	private void createThreadViewer( Composite parent ) {
		Label label = new Label( parent, SWT.NONE );
		label.setText( PropertyPageMessages.getString( "ThreadFilterEditor.0" ) ); //$NON-NLS-1$
		label.setFont( parent.getFont() );
		label.setLayoutData( new GridData() );
		GridData data = new GridData( GridData.FILL_BOTH );
		data.heightHint = 100;
		fThreadViewer = new CheckboxTreeViewer( parent, SWT.BORDER );
		fThreadViewer.addCheckStateListener( fCheckHandler );
		fThreadViewer.getTree().setLayoutData( data );
		fThreadViewer.getTree().setFont( parent.getFont() );
		fThreadViewer.setContentProvider( fContentProvider );
		fThreadViewer.setLabelProvider( DebugUITools.newDebugModelPresentation() );
		fThreadViewer.setInput( DebugPlugin.getDefault().getLaunchManager() );
		setInitialCheckedState();
	}

	/**
	 * Returns the debug targets that appear in the tree
	 */
	protected IDebugTarget[] getDebugTargets() {
		Object input = fThreadViewer.getInput();
		if ( !(input instanceof ILaunchManager) ) {
			return new IDebugTarget[0];
		}
		ILaunchManager launchManager = (ILaunchManager)input;
		return launchManager.getDebugTargets();
	}

	protected CheckboxTreeViewer getThreadViewer() {
		return fThreadViewer;
	}

	/**
	 * Sets the initial checked state of the tree viewer.
	 * The initial state should reflect the current state
	 * of the breakpoint. If the breakpoint has a thread
	 * filter in a given thread, that thread should be
	 * checked.
	 */
	protected void setInitialCheckedState() {
		ICBreakpoint breakpoint = fPage.getBreakpoint(); 
		try {
			ICDebugTarget[] targets = breakpoint.getTargetFilters();
			for( int i = 0; i < targets.length; i++ ) {
				ICThread[] filteredThreads = breakpoint.getThreadFilters( targets[i] );
				if ( filteredThreads != null ) {
					for ( int j = 0; j < filteredThreads.length; ++j )
						fCheckHandler.checkThread( filteredThreads[j], true );
				}
				else {
					fCheckHandler.checkTarget( targets[i], true );
				}
			}
		}
		catch( CoreException e ) {
			DebugUIPlugin.log( e );
		}
	}

	protected void doStore() {
		ICBreakpoint breakpoint = fPage.getBreakpoint();
		IDebugTarget[] targets = getDebugTargets();
		for ( int i = 0; i < targets.length; ++i ) {
			if ( !(targets[i] instanceof ICDebugTarget) )
				continue;
			try {
				if ( getThreadViewer().getChecked( targets[i] ) ) {
					if ( getThreadViewer().getGrayed( targets[i] ) ) {
						ICThread[] threads = getTargetThreadFilters( (ICDebugTarget)targets[i] );
						breakpoint.setThreadFilters( threads );
					}
					else {
						breakpoint.setTargetFilter( (ICDebugTarget)targets[i] );
					}
				}
				else {
					breakpoint.removeTargetFilter( (ICDebugTarget)targets[i] );
				}
			}
			catch( CoreException e ) {
				DebugUIPlugin.log( e );
			}
		}
	}

	private ICThread[] getTargetThreadFilters( ICDebugTarget target ) {
		Object[] threads = ((ITreeContentProvider)getThreadViewer().getContentProvider()).getChildren( target );
		ArrayList list = new ArrayList( threads.length );
		for ( int i = 0; i < threads.length; ++i ) {
			if ( getThreadViewer().getChecked( threads[i] ) )
				list.add( threads[i] );
		}
		return (ICThread[])list.toArray( new ICThread[list.size()] );
	}
}
