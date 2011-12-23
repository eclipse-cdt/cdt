/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modules implementation for GDB
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoSharedLibraryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoSharedLibraryInfo.DsfMISharedInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class MIModules extends AbstractDsfService implements IModules, ICachingService {
	private CommandCache fModulesCache;
	private CommandFactory fCommandFactory;

    public MIModules(DsfSession session) {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new ImmediateRequestMonitor(requestMonitor) { 
                @Override
                protected void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(RequestMonitor requestMonitor) {
    	// Cache for holding Modules data
    	ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
    	fModulesCache = new CommandCache(getSession(), commandControl);
    	fModulesCache.setContextAvailable(commandControl.getContext(), true);

    	fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        /*
         * Make ourselves known so clients can use us.
         */
        register(new String[]{IModules.class.getName(), MIModules.class.getName()}, new Hashtable<String,String>());

        requestMonitor.done();
    }

    @Override
    public void shutdown(RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }
    
    static class ModuleDMContext extends AbstractDMContext implements IModuleDMContext {
        private final String fFile;
        ModuleDMContext(MIModules service, IDMContext[] parents, String file) {
            super(service, parents);
            fFile = file; 
        }
        
        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && fFile.equals(((ModuleDMContext)obj).fFile);
        }
        
        @Override
        public int hashCode() {
            return baseHashCode() + fFile.hashCode();
        }
    }
    
    static class ModuleDMData implements IModuleDMData {
        private final String fFile;
        private final String fFromAddress;
        private final String fToAddress;
        private final boolean fIsSymbolsRead;
        
        public ModuleDMData(ModuleDMContext dmc) {
        	fFile = dmc.fFile;
        	fFromAddress = null;
        	fToAddress = null;
        	fIsSymbolsRead = false;
        }
        
        public ModuleDMData(String fileName, String fromAddress, String toAddress, boolean isSymsRead){
        	fFile = fileName;
        	fFromAddress = fromAddress;
        	fToAddress = toAddress;
        	fIsSymbolsRead = isSymsRead;
        }
        
    	@Override
        public String getFile() {
            return fFile;
        }
        
    	@Override
        public String getName() {
            return fFile;
        }
        
    	@Override
        public long getTimeStamp() {
            return 0;
        }
        
    	@Override
    	public String getBaseAddress() {
            return fFromAddress;
        }

    	@Override
        public String getToAddress() {
            return fToAddress;
        }

    	@Override
        public boolean isSymbolsLoaded() {
            return fIsSymbolsRead;
        }
        
    	@Override
    	public long getSize() {
    		long result = 0;
    		if(getBaseAddress() == null || getToAddress() == null)
    			return result;
			BigInteger start = MIFormat.getBigInteger(getBaseAddress());
			BigInteger end = MIFormat.getBigInteger(getToAddress());
			if ( end.compareTo( start ) > 0 )
				result = end.subtract( start ).longValue(); 
    		return result;
    	}

    }
    
	@Override
    public void getModules(final ISymbolDMContext symCtx, final DataRequestMonitor<IModuleDMContext[]> rm) {
    	if(symCtx != null){
    		fModulesCache.execute(fCommandFactory.createCLIInfoSharedLibrary(symCtx),
					 			  new DataRequestMonitor<CLIInfoSharedLibraryInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeModuleContexts(symCtx, getData()));
							rm.done();
						}
					});
    	}
    	else{
            rm.setData(new IModuleDMContext[] { new ModuleDMContext(this, DMContexts.EMPTY_CONTEXTS_ARRAY, "example module 1"), new ModuleDMContext(this, DMContexts.EMPTY_CONTEXTS_ARRAY, "example module 2") }); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
    	}
    }

    private IModuleDMContext[] makeModuleContexts(IDMContext symCtxt, CLIInfoSharedLibraryInfo info){
    	
    	DsfMISharedInfo[] sharedInfos = info.getMIShared();
    	ModuleDMContext[] modules = new ModuleDMContext[sharedInfos.length];
    	int i = 0;
    	for(DsfMISharedInfo shared : sharedInfos){
    		modules[i++] = new ModuleDMContext(this, new IDMContext[]{symCtxt}, shared.getName());
    	}
    	return modules;
    }
    
	@Override
    public void getModuleData(final IModuleDMContext dmc, final DataRequestMonitor<IModuleDMData> rm) {
        assert dmc != null; 
        if (dmc instanceof ModuleDMContext) {
            fModulesCache.execute(fCommandFactory.createCLIInfoSharedLibrary(dmc),
                    new DataRequestMonitor<CLIInfoSharedLibraryInfo>(getExecutor(), rm) {
                        @Override
                        protected void handleSuccess() {
                            rm.setData( createSharedLibInfo((ModuleDMContext)dmc, getData()) );
                            rm.done();
                        }
                    });
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DM Context", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    private IModuleDMData createSharedLibInfo(ModuleDMContext dmc, CLIInfoSharedLibraryInfo info){
        for (CLIInfoSharedLibraryInfo.DsfMISharedInfo shared : info.getMIShared()) {
            if(shared.getName().equals(dmc.fFile)){
                return new ModuleDMData(shared.getName(), shared.getFrom(), shared.getTo(), shared.isRead());       
            }
        }
        return  new ModuleDMData("","", "", false);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	
    }
    
    @Override
    public void calcAddressInfo(ISymbolDMContext symCtx, String file, int line, int col, DataRequestMonitor<AddressRange[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Functionality not supported", null)); //$NON-NLS-1$
        rm.done();
    }

	@Override
    public void calcLineInfo(ISymbolDMContext symCtx, IAddress address, DataRequestMonitor<LineInfo[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Functionality not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    /**
     * {@inheritDoc}
     * @since 1.1
     */
	@Override
	public void flushCache(IDMContext context) {
		fModulesCache.reset();		
	}
}
