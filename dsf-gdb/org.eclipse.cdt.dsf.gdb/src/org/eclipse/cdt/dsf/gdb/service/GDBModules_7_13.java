package org.eclipse.cdt.dsf.gdb.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IModules2;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIFormat;
import org.eclipse.cdt.dsf.mi.service.MIModules;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFIleListSharedLibrariesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MISharedInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 5.1
 */
public class GDBModules_7_13 extends MIModules {

	private CommandCache fModulesCache;
	private CommandFactory fCommandFactory;

	public GDBModules_7_13(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
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
        register(new String[]{ IModules.class.getName(),
        		               IModules2.class.getName(),
        		               MIModules.class.getName() },
        		 new Hashtable<String,String>());
        requestMonitor.done();
	}
	
    @Override
    public void shutdown(RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }

	@Override
	public void getModuleData(IModuleDMContext dmc, DataRequestMonitor<IModuleDMData> rm) {
		assert dmc != null;
		ISymbolDMContext symDmc = DMContexts.getAncestorOfType(dmc, ISymbolDMContext.class);
		if (symDmc != null && dmc instanceof ModuleDMContext) {
			fModulesCache.execute(fCommandFactory.createMIFileListSharedLibraries(symDmc),
					new DataRequestMonitor<MIFIleListSharedLibrariesInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(createSharedLibInfo((ModuleDMContext) dmc, getData()));
							rm.done();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DM Context", null)); //$NON-NLS-1$
			rm.done();
		}
	}
	
    private IModuleDMData createSharedLibInfo(ModuleDMContext dmc, MIFIleListSharedLibrariesInfo info){
        for (MISharedInfo shared : info.getMIShared()) {
            if(shared.getName().equals(dmc.fFile)){
                return new ModuleDMData(shared.getName(), shared.getFrom(), shared.getTo(), shared.isRead());       
            }
        }
        return  new ModuleDMData("","", "", false);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	
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
	
    static class ModuleDMContext extends AbstractDMContext implements IModuleDMContext {
        private final String fFile;
        ModuleDMContext(MIModules service, IDMContext[] parents, String file) {
            super(service, parents);
            fFile = file; 
        }
        
        public String getFile() {
        	return fFile;
        }
        
        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && fFile.equals(((ModuleDMContext)obj).fFile);
        }
        
        @Override
        public int hashCode() {
            return baseHashCode() + fFile.hashCode();
        }
        
        @Override
        public String toString() {
        	return baseToString() + ".file[" + fFile + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

	@Override
	public void getModules(final ISymbolDMContext symCtx, final DataRequestMonitor<IModuleDMContext[]> rm) {
		if (symCtx != null) {
			fModulesCache.execute(fCommandFactory.createMIFileListSharedLibraries(symCtx),
					new DataRequestMonitor<MIFIleListSharedLibrariesInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeModuleContexts(symCtx, getData()));
							rm.done();
						}
					});
		} else {
			rm.setData(new IModuleDMContext[] {
					new ModuleDMContext(this, DMContexts.EMPTY_CONTEXTS_ARRAY, "example module 1"), //$NON-NLS-1$
					new ModuleDMContext(this, DMContexts.EMPTY_CONTEXTS_ARRAY, "example module 2") }); //$NON-NLS-1$
			rm.done();
		}
	}

	private IModuleDMContext[] makeModuleContexts(IDMContext symCtxt, MIFIleListSharedLibrariesInfo info) {

		MISharedInfo[] sharedInfos = info.getMIShared();
		ModuleDMContext[] modules = new ModuleDMContext[sharedInfos.length];
		int i = 0;
		for (MISharedInfo shared : sharedInfos) {
			modules[i++] = new ModuleDMContext(this, new IDMContext[] { symCtxt }, shared.getName());
		}
		return modules;
	}
}
