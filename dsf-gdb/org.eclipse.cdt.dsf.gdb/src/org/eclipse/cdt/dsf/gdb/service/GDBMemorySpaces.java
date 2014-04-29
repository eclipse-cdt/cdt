/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces2;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * @since 4.4
 */
public class GDBMemorySpaces extends AbstractDsfService implements IMemorySpaces2 {
    
	/**
	 * A memory space qualified context for the IMemory methods. Used if
	 * required, otherwise the more basic IMemoryDMContext is used
	 */
	private static class GDBMemorySpaceDMContext extends AbstractDMContext implements IMemorySpaceDMContext {

		private final String fMemorySpaceId;

		public GDBMemorySpaceDMContext(String sessionId, String memorySpaceId, IDMContext parent) {
			super(sessionId, new IDMContext[] {parent});
			// A memorySpaceDMContext should not be created if the memorySpaceId is not valid.
			// However we need the id to calculate the hash, therefore we can not leave it as null
			assert(memorySpaceId != null);
			fMemorySpaceId = memorySpaceId == null ? "": memorySpaceId; //$NON-NLS-1$
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext#getMemorySpaceId()
		 */
		@Override
		public String getMemorySpaceId() {
			return fMemorySpaceId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other) {
            if (other instanceof GDBMemorySpaceDMContext) {
            	GDBMemorySpaceDMContext  dmc = (GDBMemorySpaceDMContext) other;
                return (super.baseEquals(other)) && (dmc.fMemorySpaceId.equals(fMemorySpaceId));
            } else {
                return false;
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#hashCode()
		 */
		@Override
        public int hashCode() { 
			return super.baseHashCode() + fMemorySpaceId.hashCode(); 
		}
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() { 
        	return baseToString() + ".memoryspace[" + fMemorySpaceId + ']';  //$NON-NLS-1$
        } 
	}

    public GDBMemorySpaces(DsfSession session) {
        super(session);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(new ImmediateRequestMonitor(rm) {
            @Override
            protected void handleSuccess() {
                doInitialize(rm);
            }
        });
    }

    private void doInitialize(final RequestMonitor rm) {
    	register(new String[] { IMemorySpaces.class.getName(), 
        						IMemorySpaces2.class.getName(),
        						GDBMemorySpaces.class.getName() },
                 new Hashtable<String, String>());
        rm.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void shutdown(RequestMonitor rm) {
        unregister();
		super.shutdown(rm);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
     */
    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }

	@Override
	public String encodeAddress(String expression, String memorySpaceID) {
		return null;
	}

	@Override
	public DecodeResult decodeAddress(String str) throws CoreException {
		return null;
	}

	@Override
	public void getMemorySpaces(IDMContext context, DataRequestMonitor<String[]> rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
	}

	@Override
	public boolean creatingBlockRequiresMemorySpaceID() {
		return false;
	}

	@Override
	public void decodeExpression(IDMContext context, String expression, DataRequestMonitor<DecodeResult> rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,	NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
	}

	@Override
	public IMemorySpaceDMContext createMemorySpaceContext(IDMContext ctx, String memorySpaceId) {
		return new GDBMemorySpaceDMContext(getSession().getId(), memorySpaceId, ctx);
	}
}
