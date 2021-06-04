package com.ashling.riscfree.globalvariable.view.dsf;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIArg;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;

import com.ashling.riscfree.globalvariable.view.Activator;

/**
 * @author vinod.appu
 *
 */
public class GlobalVariableService extends AbstractDsfService implements IGlobalVariableService {

	private List<IGlobalVariableDescriptor> fGlobals;
	private List<IGlobalVariableDescriptor> initialGlobals;

	public GlobalVariableService(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return Activator.getContext();
	}

	@Override
	public void initialize(RequestMonitor rm) {
		register(new String[] { IGlobalVariableService.class.getName() }, new Hashtable<>());
		super.initialize(rm);
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		super.shutdown(rm);
	}

	@Override
	public void getGlobalVariables(final DataRequestMonitor<IGlobalVariableDescriptor[]> rm) {
		rm.done(fGlobals.toArray(new IGlobalVariableDescriptor[0]));
	}

	@Override
	public List<IGlobalVariableDescriptor> getGlobals() throws DebugException {
		if (fGlobals == null) {
			fGlobals = new ArrayList<>();
		}
		return fGlobals;
	}

	@Override
	public void getGlobals(IFrameDMContext frameDmc, DataRequestMonitor<IVariableDMContext[]> rm) {
		DataRequestMonitor<IGlobalVariableDescriptor[]> globalVariableDescriptorDRM = new DataRequestMonitor<IGlobalVariableDescriptor[]>(
				getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					List<IVariableDMContext> globalVariableDMCList = new ArrayList<>();
					MIStack stackService = GlobalVariableService.this.getServicesTracker().getService(MIStack.class);
					IGlobalVariableDescriptor[] globals = getData();
					int index = 1;
					for (IGlobalVariableDescriptor global : globals) {

						globalVariableDMCList
								.add(new MIGlobalVariableDMC(stackService, frameDmc, global.getName(), index++));
					}
					rm.done(globalVariableDMCList.toArray(new IVariableDMContext[0]));
				} else {
					rm.done(new Status(IStatus.ERROR, Activator.PLUGIN_ID, REQUEST_FAILED,
							"Faied to get global variables ", null)); //$NON-NLS-1$
				}
			}
		};
		getGlobalVariables(globalVariableDescriptorDRM);

	}

	@Override
	public void getGlobalVariableData(IVariableDMContext variableDmc, DataRequestMonitor<IVariableDMData> rm) {
		if (!(variableDmc instanceof MIGlobalVariableDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, INVALID_HANDLE,
					"Invalid context type " + variableDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		final MIGlobalVariableDMC miVariableDmc = (MIGlobalVariableDMC) variableDmc;

		IExpressions expressions = getServicesTracker().getService(IExpressions.class);
		if (expressions == null) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
					"No expression service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		// Extract the frame DMC from the variable DMC.
		final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(variableDmc, IFrameDMContext.class);
		if (frameDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, INVALID_HANDLE,
					"No frame context found in " + variableDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
		if (execDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, INVALID_HANDLE,
					"No execution context found in " + frameDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		IExpressionDMContext expressionDMC = expressions.createExpression(execDmc, miVariableDmc.name);
		FormattedValueDMContext formattedValueContext = expressions.getFormattedValueContext(expressionDMC,
				IFormattedValues.NATURAL_FORMAT);
		expressions.getFormattedExpressionValue(formattedValueContext,
				new DataRequestMonitor<FormattedValueDMData>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						String value = getData().getFormattedValue();
						String name = miVariableDmc.name;
						rm.setData(new VariableData(new MIArg(name, value)));
						rm.done();
					}

					@Override
					protected void handleFailure() {
						//Set the value as ??? => Failed to resolve
						rm.done(new VariableData(new MIArg(miVariableDmc.name, Messages.GlobalVariableService_0)));
					}
				});

	}

	protected static class MIGlobalVariableDMC extends AbstractDMContext implements IVariableDMContext {
		public enum Type {
			GLOBAL
		}

		private final Type fType;
		private final int fIndex;
		private final String name;

		public MIGlobalVariableDMC(MIStack service, IFrameDMContext frame, String name, int index) {
			super(service, new IDMContext[] { frame });
			this.name = name;
			this.fIndex = index;
			this.fType = Type.GLOBAL;
		}

		public int getIndex() {
			return fIndex;
		}

		public Type getType() {
			return fType;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MIGlobalVariableDMC)) {
				return false;
			}

			return super.baseEquals(other) && ((MIGlobalVariableDMC) other).fIndex == fIndex;
		}

		@Override
		public int hashCode() {
			return super.baseHashCode() + fIndex;
		}

		@Override
		public String toString() {
			return baseToString() + ".variable(" + fType + ")[" + fIndex + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Same as with frame objects, this is a base class for the IVariableDMData object that uses an MIArg object to
	 * provide the data.  Sub-classes must supply the MIArg object.
	 */
	private static class VariableData implements IVariableDMData {
		private MIArg fMIArg;

		public VariableData(MIArg arg) {
			fMIArg = arg;
		}

		@Override
		public String getName() {
			return fMIArg.getName();
		}

		@Override
		public String getValue() {
			return fMIArg.getValue();
		}

		@Override
		public String toString() {
			return fMIArg.toString();
		}
	}

	@Override
	public void addGlobals(IDMContext frameDmc, IGlobalVariableDescriptor[] globals) {
		fGlobals = new ArrayList<>();
		fGlobals.addAll(Arrays.asList(globals));
		getSession().dispatchEvent(
				new GlobalsChangedEvent(DMContexts.getAncestorOfType(frameDmc, IExecutionDMContext.class)),
				getProperties());
	}

	@Override
	public void removeGlobals(IDMContext frameDmc, IGlobalVariableDescriptor[] globals) {
		if (fGlobals == null)
			return; // This sometimes happens at startup
		synchronized (fGlobals) {
			fGlobals.removeAll(Arrays.asList(globals));
		}
	}

	@Override
	public void removeGlobals(IDMContext frameDmc, IExpressionDMContext[] globals) {
		if (fGlobals == null)
			return; // This sometimes happens at startup
		synchronized (fGlobals) {
			for (IExpressionDMContext variableDMC : globals) {
				if (variableDMC instanceof IExpressionDMContext) {
					fGlobals.removeIf(p -> p.getName().equalsIgnoreCase(variableDMC.getExpression()));
				}
			}
		}
		getSession().dispatchEvent(
				new GlobalsChangedEvent(DMContexts.getAncestorOfType(frameDmc, IExecutionDMContext.class)),
				getProperties());
	}

	/**
	 * Send info variables gdb command and process the response to get the global variable information
	 *
	 * # Sample response for info variables is as below
	* 410,889 ~"All defined variables:\n"
	* 410,889 ~"\nFile "
	* 410,889 ~"C:/WDLABS/crex-rv/fwcode/Source/be/btst/wrapper/src/pl_wrapper_dummy.c:\n"
	* 410,889 ~"54:\tU_32 G_wrp_dummy_global;\n"
	* 410,889 ~"53:\tconst U_32 dummy_wrp_const_global;\n"
	* 410,889 ~"\nFile "
	* 410,889 ~"C:/WDLABS/crex-rv/fwcode/Source/be/cmn/die_mapping/src/dp_fg.c:\n"
	* 410,889 ~"45:\tS_DP_FG_CB G_dpFg_cb;\n"
	* 410,889 ~"\nFile "
	* 410,890 ~"C:/WDLABS/crex-rv/fwcode/Source/be/cmn/src/sys_shared_data.c:\n"
	* 410,890 ~"89:\tS_ARCS_SYNC_CB_PTR G_arcsSyncCb_p;\n"
	* 410,890 ~"\nFile "
	* 410,890 ~"C:/WDLABS/crex-rv/fwcode/Source/be/dp/cmn/src/dp_vars_arc2_x3.c:\n"
	* 410,890 ~"92:\tS_DP_VARS_CB *G_dpVars_cb_p;\n"
	* 410,896 ~"53:\tU_32 (*f_romMpc_semaphoreGet)(U_08, U_16);\n"
	* 410,896 ~"54:\tvoid (*f_romMpc_semaphorePost)(U_08, U_16);\n"
	* 410,896 ~"76:\tISR_PFV (*f_psp_intRegisterExtInterrupt)(U_32, ISR_PFV, void *);\n"
	*
	* Non-debugging symbols:
	* 0x800005ac  __frame_dummy_init_array_entry
	* 0x800005ac  __init_array_start
	* 0x800005ac  __preinit_array_end
	* 0x800005ac  __preinit_array_start
	 *
	 * @param frameDmc
	 * @return
	 * @throws DebugException
	 */
	//TODO: Move this to service initialization, use a thread pool too
	private List<IGlobalVariableDescriptor> getInitialGlobalsFromElfs(IDMContext frameDmc) throws DebugException {
		List<IGlobalVariableDescriptor> list = new ArrayList<>();
		MIInfo infoVariablesResponse = executeGDBCommand(frameDmc, "info variables"); //$NON-NLS-1$
		if(null == infoVariablesResponse)
		{
			return list;
		}
		
		MIOutput out = infoVariablesResponse.getMIOutput();
		MIOOBRecord[] rr = out.getMIOOBRecords();
		if (rr != null) {
			for (int i = 0; i < rr.length; i++) {
				final String line = rr[i].toString();
				String globalVariableString = null;
				// Function pointer, Eg: 410,896 ~"54:\tvoid (*f_romMpc_semaphorePost)(U_08, U_16);\n"
				if (line.endsWith(Messages.GlobalVariableService_1)) {
					globalVariableString = line.substring(line.indexOf(Messages.GlobalVariableService_2) + 1, line.indexOf(Messages.GlobalVariableService_3));
				}
				// Pointer, Eg: 306,643 ~"334:\tTCB_t * volatile pxCurrentTCB;\n"
				else if (line.endsWith(";\\n\"\n")) { //$NON-NLS-1$
					String[] splittedStrings = line.split("\\s+"); //$NON-NLS-1$
					String globalString = splittedStrings[splittedStrings.length - 1];
					int globalStart = 0;
					int globalEnd = globalString.length();

					// Array variable, EG: 306,643 ~"334:\t static int test_array[5]
					if (globalString.endsWith("]")) //$NON-NLS-1$
						globalEnd = globalString.indexOf("["); //$NON-NLS-1$
					if (globalString.startsWith("*")) //$NON-NLS-1$
						globalStart++;
					globalVariableString = globalString.trim().substring(globalStart, globalEnd-4);
				}
				final String globalVariableName = globalVariableString;
				if (globalVariableName != null) {
					list.add(new IGlobalVariableDescriptor() {

						@Override
						public IPath getPath() {
							// TODO Needed if user has to go to the definition, not very sure
							return null;
						}

						@Override
						public String getName() {
							return globalVariableName;
						}
					});
				}
			}
		}
		return list;
	}
	
	@Override
	public List<IGlobalVariableDescriptor> getInitialGlobals(IDMContext frameDmc) throws DebugException {
		if (initialGlobals == null || initialGlobals.isEmpty()) {
			initialGlobals = getInitialGlobalsFromElfs(frameDmc);
		}
		return initialGlobals;
	}

	@Override
	public void removeAllGlobals(IDMContext frameDmc) {
		if (fGlobals == null)
			return; // This sometimes happens at startup
		synchronized (fGlobals) {
			fGlobals.clear();
		}
		getSession().dispatchEvent(
				new GlobalsChangedEvent(DMContexts.getAncestorOfType(frameDmc, IExecutionDMContext.class)),
				getProperties());

	}

	private static class GlobalsChangedEvent extends AbstractDMEvent<IExecutionDMContext>
			implements IGlobalsChangedEvent {
		private IExecutionDMContext executionDMContext;

		public GlobalsChangedEvent(IExecutionDMContext context) {
			super(context);
			this.executionDMContext = context;
		}

		@Override
		public IExecutionDMContext getDMContext() {
			return executionDMContext;
		}

	}

	private MIInfo executeGDBCommand(IDMContext dmcontext, String command) {
		DsfSession session = getSession();
		GetCLIQuery cliQuery = new GetCLIQuery(command, getServicesTracker(), dmcontext);
		session.getExecutor().execute(cliQuery);
		try {
			MIInfo miInfo = cliQuery.get(1, TimeUnit.MINUTES);
			return miInfo;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Activator.logErrorMessage(Messages.GlobalVariableService_4);
			Activator.log(e);
			cliQuery.cancel(true);
		} 
		return null;
	}

	private class GetCLIQuery extends Query<MIInfo> {
		private String str;
		private DsfServicesTracker dsfServicesTracker;
		IDMContext dmContext;

		public GetCLIQuery(String str, DsfServicesTracker dsfServicesTracker, IDMContext dmContext) {
			this.str = str;
			this.dsfServicesTracker = dsfServicesTracker;
			this.dmContext = dmContext;
		}

		@Override
		protected void execute(DataRequestMonitor<MIInfo> rm) {

			// Do not use the interpreter-exec for stepping operation the UI will fall out
			// of step.
			// Also, do not use "interpreter-exec console" for MI commands.
			ICommandControlDMContext fContext = DMContexts.getAncestorOfType(dmContext, ICommandControlDMContext.class);
			ICommand<MIInfo> cmd = new CLICommand<>(fContext, str);

			// TODO: for print command would be nice to redirect to gdb console
			ICommandControlService commandControl = dsfServicesTracker.getService(ICommandControlService.class);
			if (commandControl != null) {
				commandControl.queueCommand(cmd, rm);
			} else {
				// Should not happen, so log the situation but then ignore it
				rm.setStatus(
						new Status(IStatus.INFO, Activator.PLUGIN_ID, "Unable to find service to execute CLI command")); //$NON-NLS-1$
			}

		}

	}
}
