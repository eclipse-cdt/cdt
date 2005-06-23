/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views; 

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A label decorator which computes text for debug elements
 * in the background and updates them asynchronously.
 */
public class DebugViewLabelDecorator extends LabelProvider implements ILabelDecorator, IDebugEventSetListener {

	/**
	 * The presentation used to compute text.
	 */
	private IDebugModelPresentation fPresentation;

	/**
	 * The label provider notified when text is computed.
	 */
	protected DebugViewDecoratingLabelProvider fLabelProvider;

	/**
	 * The job which will be executed next. All new label requests
	 * are appended to this job.
	 */
	protected LabelJob fNextJob = null;

	/** 
	 * Constructor for DebugViewLabelDecorator. 
	 */
	public DebugViewLabelDecorator( IDebugModelPresentation presentation ) {
		fPresentation = presentation;
		DebugPlugin.getDefault().addDebugEventListener( this );
	}
	
	/**
	 * Sets the label provider which will be notified when a
	 * label has been computed in the background.
	 * 
	 * @param labelProvider the label provider to notify when text
	 *  is computed
	 */
	public void setLabelProvider( DebugViewDecoratingLabelProvider labelProvider ) {
		fLabelProvider = labelProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage( Image image, Object element ) {
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText( String text, final Object element ) {
		computeText( element );
		return text;
	}
	
	public void computeText( Object element ) {
		synchronized( this ) {
			if ( fNextJob == null ) {
				fNextJob = new LabelJob( "Debug", fPresentation ); //$NON-NLS-1$
			}
			fNextJob.computeText( element );
		}
	}
	
	public void labelsComputed( final Object[] computedElements ) {
		CDebugUIPlugin.getStandardDisplay().asyncExec( 
			new Runnable() {
	
				public void run() {
					fLabelProvider.labelsComputed( computedElements );
				}
			} );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents( DebugEvent[] events ) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener( this );
	}
	
	/**
	 * A job which computes text for a queue of elements. The job's label
	 * decorator is notified when text has been computed for some number
	 * of elements.
	 */
	protected class LabelJob extends Job implements ISchedulingRule {

		private Vector fElementQueue = new Vector();
		private IDebugModelPresentation fJobPresentation;
		
		/**
		 * Creates a new job with the given name which will use the given
		 * presentation to compute labels in the background
		 * @param name the job's name
		 * @param presentation the presentation to use for label
		 *  computation
		 */
		public LabelJob( String name, IDebugModelPresentation presentation ) {
			super( name );
			fJobPresentation = presentation;
			setSystem( true );
		}
		
		/**
		 * Queues up the given element to have its text computed.
		 * @param element the element whose text should be computed
		 *  in this background job
		 */
		public void computeText( Object element ) {
			if ( !fElementQueue.contains( element ) ) {
				fElementQueue.add( element );
			}
			schedule();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run( IProgressMonitor monitor ) {
			int numElements = fElementQueue.size();
			monitor.beginTask( MessageFormat.format( "Fetching {0} labels", new String[]{ Integer.toString( numElements ) } ), numElements ); //$NON-NLS-1$
			while( !fElementQueue.isEmpty() && !monitor.isCanceled() ) {
				StringBuffer message = new StringBuffer( MessageFormat.format( "Fetching {0} labels", new String[]{ Integer.toString( fElementQueue.size() ) } ) ); //$NON-NLS-1$
				message.append( MessageFormat.format( " ({0} pending)", new String[]{ Integer.toString( fNextJob.fElementQueue.size() ) } ) ); //$NON-NLS-1$
				monitor.setTaskName( message.toString() );
				int blockSize = 10;
				if ( fElementQueue.size() < blockSize ) {
					blockSize = fElementQueue.size();
				}
				final List computedElements = new ArrayList();
				for( int i = 0; i < blockSize; i++ ) {
					Object element = fElementQueue.remove( 0 );
					if ( element == null ) {
						break;
					}
					fLabelProvider.textComputed( element, fJobPresentation.getText( element ) );
					computedElements.add( element );
				}
				labelsComputed( computedElements.toArray() );
				monitor.worked( computedElements.size() );
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains( ISchedulingRule rule ) {
			return (rule instanceof LabelJob) && fJobPresentation == ((LabelJob)rule).fJobPresentation;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting( ISchedulingRule rule ) {
			return (rule instanceof LabelJob) && fJobPresentation == ((LabelJob)rule).fJobPresentation;
		}
	}
}
