/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for additional features in DSF Reference Implementation
 *     Roland Grunberg (RedHat) - Refresh all registers once one is changed (Bug 400840)
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug 323552)
 *     Alvaro Sanchez-Leon (Ericsson) - Register view does not refresh register names per process (Bug 418176)
 *     Alvaro Sanchez-Leon (Ericsson) - Allow user to edit the register groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIRegisterValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * 
 * <p> 
 * Implementation note:
 * This class implements event handlers for the events that are generated by 
 * this service itself.  When the event is dispatched, these handlers will
 * be called first, before any of the clients.  These handlers update the 
 * service's internal state information to make them consistent with the 
 * events being issued.  Doing this in the handlers as opposed to when 
 * the events are generated, guarantees that the state of the service will
 * always be consistent with the events.
 */

public class MIRegisters extends AbstractDsfService implements IRegisters, ICachingService {
	/**
	 * @since 4.6
	 */
	protected static final String BLANK_STRING = ""; //$NON-NLS-1$
	/**
	 * @since 4.6
	 */
	protected static final String ROOT_GROUP_NAME = Messages.MIRegisters_General_Registers;
	/**
	 * @since 4.6
	 */
	protected static final String ROOT_GROUP_DESCRIPTION = Messages.MIRegisters_General_Registers_description;
    /*
     * Support class used to construct Register Group DMCs.
     */
	
    public static class MIRegisterGroupDMC extends AbstractDMContext implements IRegisterGroupDMContext {
        private int fGroupNo;
        private String fGroupName;

        public MIRegisterGroupDMC(MIRegisters service, IContainerDMContext contDmc, int groupNo, String groupName) {
            super(service.getSession().getId(), new IDMContext[] { contDmc });
            fGroupNo = groupNo;
            fGroupName = groupName;
        }

        public int getGroupNo() { return fGroupNo; }
        public String getName() { return fGroupName; }
        /**
		 * @since 4.6
		 */
		public void setName(String groupName) {
			fGroupName = groupName;
		}

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) && (((MIRegisterGroupDMC) other).fGroupNo == fGroupNo) && 
                    (((MIRegisterGroupDMC) other).fGroupName.equals(fGroupName)));
        }
        
        @Override
        public int hashCode() { return super.baseHashCode() ^ fGroupNo; }
        @Override
        public String toString() { return baseToString() + ".group[" + fGroupNo + "," + fGroupName + "]"; }             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
       
    /*
     * Support class used to construct Register DMCs.
     */
    
    public static class MIRegisterDMC extends AbstractDMContext implements IRegisterDMContext {
        private int fRegNo;
        private String fRegName;

        /**
         * An MIRegiserDMC created with no frame context will not be able to resolve the associated register value.
         * However these instances are useful to provide the register name i.e. needed from the selection of a process,
         * or a running thread where it is not possible to associate registers to frames and therefore values.
         */
        public MIRegisterDMC(MIRegisters service, MIRegisterGroupDMC group, int regNo, String regName) {
            super(service.getSession().getId(), 
                    new IDMContext[] { group });
              fRegNo = regNo;
              fRegName = regName;
        }

        @Deprecated
        public MIRegisterDMC(MIRegisters service, MIRegisterGroupDMC group, IMIExecutionDMContext execDmc, int regNo, String regName) {
            super(service.getSession().getId(), 
                    new IDMContext[] { execDmc, group });
              fRegNo = regNo;
              fRegName = regName;
        }

		/**
		 * This Register context is associated to two parent contexts. A stack frame context (IFrameDMContext), and a
		 * register group context (MIRegisterGroupDMC). When the scenario requires to build a register contexts from the
		 * selection of a thread, then the top frame shall be resolved and be provided in this constructor.
		 * 
		 * The frame context is necessary to resolve the register's data e.g. value
		 * 
		 * @since 4.3
		 */
		public MIRegisterDMC(MIRegisters service, MIRegisterGroupDMC group, IFrameDMContext frameDmc, int regNo,
				String regName) {
			super(service.getSession().getId(), new IDMContext[] { frameDmc, group });
			fRegNo = regNo;
			fRegName = regName;
		}
        
        public int getRegNo() { return fRegNo; }
        public String getName() { return fRegName; }

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) && (((MIRegisterDMC) other).fRegNo == fRegNo) && 
                    (((MIRegisterDMC) other).fRegName.equals(fRegName)));
        }

        @Override
        public int hashCode() { return super.baseHashCode() ^ fRegNo; }
        @Override
        public String toString() { return baseToString() + ".register[" + fRegNo + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /*
     *  Event class to notify register value is changed
     */
    public static class RegisterChangedDMEvent implements IRegisters.IRegisterChangedDMEvent {

    	private final IRegisterDMContext fRegisterDmc;
    	
    	/**
		 * @since 4.6
		 */
    	public RegisterChangedDMEvent(IRegisterDMContext registerDMC) { 
    		fRegisterDmc = registerDMC;
        }
        
    	@Override
		public IRegisterDMContext getDMContext() {
			return fRegisterDmc;
		}
    }
    
    /**
     * Indicates a change in the list of Register groups e.g. after addition and removal
	 * @since 4.6
	 */
	public static class GroupsChangedDMEvent implements IRegisters.IGroupsChangedDMEvent {

		private final IContainerDMContext fGroupContainerDmc;

		public GroupsChangedDMEvent(IContainerDMContext containerDMC) {
			fGroupContainerDmc = containerDMC;
		}

		@Override
		public IContainerDMContext getDMContext() {
			return fGroupContainerDmc;
		}
	}

    /*
     *  Internal control variables.
     */
    
	private CommandFactory fCommandFactory;

    //One Group per container process
    private final Map<IContainerDMContext, MIRegisterGroupDMC> fContainerToGroupMap = new HashMap<IContainerDMContext, MIRegisterGroupDMC>();

    private CommandCache fRegisterNameCache;	 // Cache for holding the Register Names in the single Group
    private CommandCache fRegisterValueCache;  // Cache for holding the Register Values

    public MIRegisters(DsfSession session) 
    {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() 
    {
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
        /*
         * Create the lower level register cache.
         */
    	ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
		BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(commandControl, getExecutor(), 2);
		
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
        fRegisterValueCache = new CommandCache(getSession(), bufferedCommandControl);
        fRegisterValueCache.setContextAvailable(commandControl.getContext(), true);

        // This cache is not affected by events so does not need the bufferedCommandControl
        fRegisterNameCache  = new CommandCache(getSession(), commandControl);
        fRegisterNameCache.setContextAvailable(commandControl.getContext(), true);
               
        /*
         * Sign up so we see events. We use these events to decide how to manage
         * any local caches we are providing as well as the lower level register
         * cache we create to get/set registers on the target.
         */
        getSession().addServiceEventListener(this, null);
        
        /*
         * Make ourselves known so clients can use us.
         */
        register(new String[]{IRegisters.class.getName(), MIRegisters.class.getName()}, new Hashtable<String,String>());

        requestMonitor.done();
    }

    @Override
    public void shutdown(RequestMonitor requestMonitor) 
    {
        unregister();
        getSession().removeServiceEventListener(this);
        super.shutdown(requestMonitor);
    }

    public boolean isValid() { return true; }
    
	@Override
    public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
        if (dmc.getParents().length == 1 && dmc.getParents()[0] instanceof MIRegisterDMC) {
                getRegisterDataValue( (MIRegisterDMC) dmc.getParents()[0], dmc.getFormatID(), rm);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }
    
	@Override
    public void getRegisterGroupData(IRegisterGroupDMContext regGroupDmc, DataRequestMonitor<IRegisterGroupDMData> rm) {
        /**
         * For the GDB GDBMI implementation there is only on group. The GPR and FPU registers are grouped into 
         * one set. We are going to hard wire this set as the value of ROOT_GROUP_NAME.
         */
        class RegisterGroupData implements IRegisterGroupDMData {
        	@Override
            public String getName() { return ROOT_GROUP_NAME; }
        	@Override
            public String getDescription() { return ROOT_GROUP_DESCRIPTION; }
        }

        rm.setData( new RegisterGroupData() ) ;
        rm.done();
    }

	@Override
    public void getBitFieldData(IBitFieldDMContext dmc, DataRequestMonitor<IBitFieldDMData> rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Bit fields not yet supported", null));  //$NON-NLS-1$
        rm.done();
    }
    
    /**
     * For the GDB GDBMI implementation there is only on group. We represent
     * this group as a single list we maintain within this service. So we
     * need to search this list to see if we have a current value.
     */
	@Override
    public void getRegisterData(IRegisterDMContext regDmc , final DataRequestMonitor<IRegisterDMData> rm) {
        if (regDmc instanceof MIRegisterDMC) {
            final MIRegisterDMC miRegDmc = (MIRegisterDMC)regDmc;
            final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(regDmc, IFrameDMContext.class);
            // Create register data with name only e.g. not editable.
            if(frameDmc == null){
                rm.setData(new RegisterData(null, miRegDmc.getName(), BLANK_STRING, false));
                rm.done();
                return;
            }
            
            int[] regnos = {miRegDmc.getRegNo()};
            fRegisterValueCache.execute(
            	fCommandFactory.createMIDataListRegisterValues(frameDmc, MIFormat.HEXADECIMAL, regnos),
                new DataRequestMonitor<MIDataListRegisterValuesInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        // Retrieve the register value.
                        MIRegisterValue[] regValue = getData().getMIRegisterValues();
    
                        // If the list is empty just return empty handed.
                        // The only known case this happens is caused by a bug in GDB's Python
                        // scripts. See https://sourceware.org/bugzilla/show_bug.cgi?id=19637
                        if (regValue.length == 0) {
                            rm.setData(new RegisterData(frameDmc, miRegDmc.getName(), BLANK_STRING, false));
                            rm.done();
                            return;
                        }
                        
                        // the request was for only one register
                        assert regValue.length == 1;
                        
                        // We can determine if the register is floating point because
                        // GDB returns this additional information as part of the value.
                        MIRegisterValue reg = regValue[0];
                        boolean isFloat = false;
                        
                        if ( reg.getValue().contains("float")) { //$NON-NLS-1$
                            isFloat = true;
                        }

                        // Return the new register attributes.
                        rm.setData(new RegisterData(frameDmc, miRegDmc.getName(), BLANK_STRING, isFloat));
                        rm.done();
                    }
                });
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }
    
    private void getRegisterDataValue( final MIRegisterDMC regDmc, final String formatId, final DataRequestMonitor<FormattedValueDMData> rm) {
    	IFrameDMContext frameDmc = DMContexts.getAncestorOfType(regDmc, IFrameDMContext.class);
        if(frameDmc == null){
            // Set value to blank if frame dmc is not present
            rm.setData( new FormattedValueDMData( BLANK_STRING ) );
            rm.done();
            return;
        }

        // Select the format to be shown
        int NumberFormat = MIFormat.HEXADECIMAL;
        
        if ( HEX_FORMAT.equals    ( formatId ) ) { NumberFormat = MIFormat.HEXADECIMAL; }
        if ( OCTAL_FORMAT.equals  ( formatId ) ) { NumberFormat = MIFormat.OCTAL; }
        if ( NATURAL_FORMAT.equals( formatId ) ) { NumberFormat = MIFormat.NATURAL; }
        if ( BINARY_FORMAT.equals ( formatId ) ) { NumberFormat = MIFormat.BINARY; }
        if ( DECIMAL_FORMAT.equals( formatId ) ) { NumberFormat = MIFormat.DECIMAL; }
        
        int[] regnos = {regDmc.getRegNo()};
        fRegisterValueCache.execute(
        	fCommandFactory.createMIDataListRegisterValues(frameDmc, NumberFormat, regnos),
            new DataRequestMonitor<MIDataListRegisterValuesInfo>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    // Retrieve the register value.
                    MIRegisterValue[] regValue = getData().getMIRegisterValues();

                    // If the list is empty just return empty handed.
                    // The only known case this happens is caused by a bug in GDB's Python
                    // scripts. See https://sourceware.org/bugzilla/show_bug.cgi?id=19637
                    // In the display data, we show link to Eclipse Bugzilla entry which has
                    // a comment on how to fix this manually.
                    if (regValue.length == 0) {
							rm.setData(new FormattedValueDMData(
									"Encountered a GDB Error See http://eclip.se/506382#c7 for workarounds")); //$NON-NLS-1$
	                        rm.done();
                        return;
                    }

                    MIRegisterValue reg = regValue[0];

                    // Return the new register value.
                    rm.setData( new FormattedValueDMData( reg.getValue() ) );
                    rm.done();
                }
            });
    }
        
    /**
	 * @since 4.6
	 */
    protected static class RegisterData implements IRegisterDMData {
    	
        final private String fRegName;
        final private String fRegDesc;
        final private boolean fIsFloat;
        final private IFrameDMContext fContext;
    	
    	public RegisterData(IFrameDMContext context, String regName, String regDesc, boolean isFloat ) {
    		
            fRegName = regName;
            fRegDesc = regDesc;
            fIsFloat = isFloat;
            fContext = context;
    	}
    	
    	@Override
    	public boolean isReadable() { return true; }
    	@Override
        public boolean isReadOnce() { return false; }
    	@Override
		public boolean isWriteable() {
    		//The absence of the frame context is likely the result of a selection not specific to a stack frame
    		//e.g. the selection of a running thread, or the selection of a process.
    		//So, an instance with no stack frame is not writable
			return (fContext != null);
		}
    	@Override
        public boolean isWriteOnce() { return false; }
    	@Override
        public boolean hasSideEffects() { return false; }
    	@Override
        public boolean isVolatile() { return true; }

    	@Override
        public boolean isFloat() { return fIsFloat; }
    	@Override
        public String getName() { return fRegName; }
    	@Override
        public String getDescription() { return fRegDesc; }
    }

    // Wraps a list of registers in DMContexts.
    private MIRegisterDMC[] makeRegisterDMCs(MIRegisterGroupDMC groupDmc, String[] regNames) {
    	return makeRegisterDMCs(groupDmc, null, regNames);
    }
    
    // Wraps a list of registers in DMContexts.
    private MIRegisterDMC[] makeRegisterDMCs(MIRegisterGroupDMC groupDmc, IFrameDMContext frameDmc, String[] regNames) {
        List<MIRegisterDMC> regDmcList = new ArrayList<MIRegisters.MIRegisterDMC>( regNames.length );
        int regNo = 0;
        for (String regName : regNames) {
            if(regName != null && !regName.isEmpty()) {
            	if(frameDmc != null)
            		regDmcList.add(new MIRegisterDMC(this, groupDmc, frameDmc, regNo, regName));
            	else
            		regDmcList.add(new MIRegisterDMC(this, groupDmc, regNo, regName));
            }
            regNo++;
        }
        return regDmcList.toArray(new MIRegisterDMC[regDmcList.size()]);
    }

    /*
     *   Event handling section. These event handlers control the caching state of the
     *   register caches. This service creates several cache objects. Not all of which
     *   need to be flushed. These handlers maintain the state of the caches.
     */

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
        fRegisterValueCache.setContextAvailable(e.getDMContext(), false);
        if (e.getReason() != StateChangeReason.STEP) {
            fRegisterValueCache.reset();
        }
    }
    
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(
    IRunControl.ISuspendedDMEvent e) {
        fRegisterValueCache.setContextAvailable(e.getDMContext(), true);
        fRegisterValueCache.reset();
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(final IRegisters.IRegisterChangedDMEvent e) {
    	fRegisterValueCache.reset();
    }
    
    /**
	 * @since 4.6
	 */
    protected void generateRegisterChangedEvent(IRegisterDMContext dmc ) {
        getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());

        // Fix for Bug 400840
        // When one register is modified, it could affect other registers.
        // To properly reflect that, we send a change for all registers.
        // We cheat by pretending the group has changed, since we know there is 
        // only one group.
        // This method can be extended by group managers to propagate the event as needed 
        final IRegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(dmc, IRegisterGroupDMContext.class);
        if (groupDmc != null) {
        	IRegistersChangedDMEvent event = new IRegistersChangedDMEvent() {
        		@Override
        		public IRegisterGroupDMContext getDMContext() {
        			return groupDmc;
        		}
        	};
        	getSession().dispatchEvent(event, getProperties());
        }        
    }
    
    /*
     * These are the public interfaces for this service.
     * 
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisterGroups(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void getRegisterGroups(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext[]> rm ) {
    	IContainerDMContext contDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
        if (contDmc == null) {
            rm.setStatus( new Status( IStatus.ERROR , GdbPlugin.PLUGIN_ID , INVALID_HANDLE , "Container context not found", null ) ) ;   //$NON-NLS-1$
            rm.done();
            return;
        }
        
        //Bug 418176
        //Only one group per Process (container) is supported in the implementation of this class
        MIRegisterGroupDMC registerGroup = fContainerToGroupMap.get(contDmc);
        
		if (registerGroup == null) {
			registerGroup = new MIRegisterGroupDMC(this, contDmc, 0, ROOT_GROUP_NAME);
			fContainerToGroupMap.put(contDmc, registerGroup);
		}
        
        MIRegisterGroupDMC[] groupDMCs = new MIRegisterGroupDMC[] { registerGroup };

        rm.setData(groupDMCs) ;
        rm.done() ;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getRegisters(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
	public void getRegisters(final IDMContext dmc, final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(dmc, MIRegisterGroupDMC.class);
		if (groupDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"RegisterGroup context not found", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
		if (containerDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Container context not found", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// There is only one group and its number must be 0.
		if (groupDmc.getGroupNo() == 0) {
			final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(dmc, IFrameDMContext.class);
			if (frameDmc == null) {
				// The selection does not provide a specific frame, then resolve the top frame on the current thread
				IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);
				if (execDmc != null) {
					IStack stackService = getServicesTracker().getService(IStack.class);
					if (stackService != null) {
						stackService.getTopFrame(execDmc, new ImmediateDataRequestMonitor<IStack.IFrameDMContext>(rm) {
							@Override
							protected void handleSuccess() {
								getRegisters(getData(), groupDmc, containerDmc, rm);
							}
							
							@Override
							protected void handleFailure() {
								//Unable to resolve top frame on current thread.
								//The thread could e.g. be in running state, 
								//we return register instances with no associated execution context
								//i.e. unable to resolve its associated value.
								getRegisters(null, groupDmc, containerDmc, rm);
							}
						});

						return;
					}
				}
			}

			getRegisters(frameDmc, groupDmc, containerDmc, rm);

		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Invalid group = " + groupDmc, null)); //$NON-NLS-1$
			rm.done();
		}
	}
    
	private void getRegisters(final IFrameDMContext frameDmc, final MIRegisterGroupDMC groupDmc,
			IContainerDMContext containerDmc, final DataRequestMonitor<IRegisterDMContext[]> rm) {

		fRegisterNameCache.execute(fCommandFactory.createMIDataListRegisterNames(containerDmc),
				new ImmediateDataRequestMonitor<MIDataListRegisterNamesInfo>(rm) {
					@Override
					protected void handleSuccess() {
						// Retrieve the register names.
						String[] regNames = getData().getRegisterNames();

						// If the list is empty just return empty handed.
						if (regNames.length == 0) {
							rm.done();
							return;
						}

						if (frameDmc == null)
							// The selection does not provide a frame or thread context,
							// This can happen e.g. if a container /process is selected
							// Lets provide the list of register names applicable to the selected process
							// i.e. instances with only name information which can not resolve a value
							rm.setData(makeRegisterDMCs(groupDmc, regNames));
						else
							rm.setData(makeRegisterDMCs(groupDmc, frameDmc, regNames));
						rm.done();
					}
				});
	}
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#getBitFields(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void getBitFields( IDMContext regDmc , DataRequestMonitor<IBitFieldDMContext[]> rm ) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "BitField not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeRegister(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext, java.lang.String, java.lang.String, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
	@Override
	public void writeRegister(IRegisterDMContext regCtx, final String regValue, final String formatId,
			final RequestMonitor rm) {

		assert(regCtx instanceof MIRegisterDMC);
		final MIRegisterDMC regDmc = (MIRegisterDMC) regCtx;

		final IExpressions exprService = getServicesTracker().getService(IExpressions.class);
		String regName = regDmc.getName();
		final IExpressionDMContext exprCtxt = exprService.createExpression(regCtx, "$" + regName); //$NON-NLS-1$

		final FormattedValueDMContext valueDmc = exprService.getFormattedValueContext(exprCtxt, formatId);
		exprService.getFormattedExpressionValue(valueDmc, new DataRequestMonitor<FormattedValueDMData>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				if (!regValue.equals(getData().getFormattedValue()) || !valueDmc.getFormatID().equals(formatId)) {
					exprService.writeExpression(exprCtxt, regValue, formatId, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							generateRegisterChangedEvent(regDmc);
							rm.done();
						}
					});
				}// if
				else {
					rm.done();
				}
			}// handleSuccess
		});
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeBitField(org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext, java.lang.String, java.lang.String, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
	@Override
    public void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Writing bit field not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#writeBitField(org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext, org.eclipse.cdt.dsf.debug.service.IRegisters.IMnemonic, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
	@Override
    public void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Writing bit field not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues#getAvailableFormats(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        
        rm.setData(new String[] { HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT, NATURAL_FORMAT });
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues#getFormattedValueContext(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, java.lang.String)
     */
	@Override
    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
        if ( dmc instanceof MIRegisterDMC ) {
            MIRegisterDMC regDmc = (MIRegisterDMC) dmc;
            return( new FormattedValueDMContext( this, regDmc, formatId));
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findRegisterGroup(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Finding a Register Group context not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findRegister(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm) {
        getRegisters(ctx, new ImmediateDataRequestMonitor<IRegisterDMContext[]>() {
            @Override
            protected void handleSuccess () {
                IRegisterDMContext[] allRegs = getData();
                
                // in all registers found, look for one with the name we seek
                for (int i = 0; i < allRegs.length; i++) {
                    if (allRegs[i] instanceof MIRegisterDMC) {
                        if (name.equals(((MIRegisterDMC)allRegs[i]).getName())) {
                            // found it
                            rm.done(allRegs[i]);
                            return;
                        }
                    }
                }

                // register was not found
                rm.done(new Status(IStatus.WARNING, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown register name", null)); //$NON-NLS-1$
                return;
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IRegisters#findBitField(org.eclipse.cdt.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
	@Override
    public void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm) {
        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Finding a Register Group context not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /**
     * {@inheritDoc}
     * @since 1.1
     */
	@Override
    public void flushCache(IDMContext context) {
        fRegisterNameCache.reset(context);
        fRegisterValueCache.reset(context);
    }
}
