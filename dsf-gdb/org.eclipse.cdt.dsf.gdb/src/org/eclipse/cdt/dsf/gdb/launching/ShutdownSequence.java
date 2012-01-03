/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ShutdownSequence extends Sequence {

	class ServiceShutdownStep extends Step {
		
		IDsfService fService;

		ServiceShutdownStep( IDsfService service ) {
			super();
			fService = service;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.concurrent.Sequence.Step#execute(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
		 */
		@Override
		public void execute( final RequestMonitor rm ) {
			fService.shutdown( new RequestMonitor( getExecutor(), rm ) {

				/* (non-Javadoc)
				 * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor#handleCompleted()
				 */
				@Override
				protected void handleCompleted() {
					if ( !isSuccess() ) {
						GdbPlugin.getDefault().getLog().log( getStatus() );
					}
					rm.done();
				}
			} );
		}
	}

    private String fSessionId;
    
    private Step[] fSteps;

    public ShutdownSequence(DsfExecutor executor, String sessionId, RequestMonitor requestMonitor) {
        super(executor, requestMonitor);
        fSessionId = sessionId;
        fSteps = createSteps();
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }

	private Step[] createSteps() {
		IDsfService[] services = getServices();
		ServiceShutdownStep[] steps = new ServiceShutdownStep[services.length];
		for ( int i = 0; i < steps.length; ++i )
			steps[i] = new ServiceShutdownStep( services[i] );
		return steps;
	}
	
	private IDsfService[] getServices() {
		IDsfService[] result = new IDsfService[0];
		try {
			ServiceReference<?>[] serviceRefs = GdbPlugin.getBundleContext().getServiceReferences( 
					IDsfService.class.getName(),
					String.format( "(%s=%s)", IDsfService.PROP_SESSION_ID, fSessionId ).intern() ); //$NON-NLS-1$
			List<IDsfService> services = new ArrayList<IDsfService>( serviceRefs.length );
			for ( ServiceReference<?> ref : serviceRefs ) {
				Object serviceObj = GdbPlugin.getBundleContext().getService( ref );
				if ( serviceObj instanceof IDsfService ) {
					services.add( (IDsfService)serviceObj );
				}
			}
			Collections.sort( services, new Comparator<IDsfService>() {

				@Override
				public int compare( IDsfService o1, IDsfService o2 ) {
					return o2.getStartupNumber() - o1.getStartupNumber();
				}
			} );
			result = services.toArray( new IDsfService[services.size()] );
		}
		catch( InvalidSyntaxException e ) {
			// Shouldn't happen
		}
		return result;
	}
}
