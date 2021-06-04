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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
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
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIArg;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;

import com.ashling.riscfree.globalvariable.view.Activator;
import com.ashling.riscfree.globalvariable.view.datamodel.GlobalVariableDMContext;
import com.ashling.riscfree.globalvariable.view.datamodel.GlobalVariableDMNode;
import com.ashling.riscfree.globalvariable.view.datamodel.IGlobalVariableDescriptor;
import com.ashling.riscfree.globalvariable.view.mi.MIGlobalVariableInfo;
import com.ashling.riscfree.globalvariable.view.mi.MIInfoVariables;

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
		if (fGlobals != null) {
			fGlobals.clear();
		}
		if (initialGlobals != null) {
			initialGlobals.clear();
		}
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

						globalVariableDMCList.add(new GlobalVariableDMContext(getSession(),
								new IDMContext[] { frameDmc }, (GlobalVariableDMNode) global));
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
		if (!(variableDmc instanceof GlobalVariableDMContext)) {
			rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, INVALID_HANDLE,
					"Invalid context type " + variableDmc, null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		final GlobalVariableDMContext miVariableDmc = (GlobalVariableDMContext) variableDmc;

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

		IExpressionDMContext expressionDMC = expressions.createExpression(execDmc,getFormattedExpression(miVariableDmc.getRelativeFilePath(), miVariableDmc.getName()));
		FormattedValueDMContext formattedValueContext = expressions.getFormattedValueContext(expressionDMC,
				IFormattedValues.NATURAL_FORMAT);
		expressions.getFormattedExpressionValue(formattedValueContext,
				new DataRequestMonitor<FormattedValueDMData>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						String value = getData().getFormattedValue();
						String name = getFormattedExpression(miVariableDmc.getRelativeFilePath(), miVariableDmc.getName());
						rm.setData(new VariableData(new MIArg(name, value)));
						rm.done();
					}

					@Override
					protected void handleFailure() {
						// Set the value as ??? => Failed to resolve, some cases gdb fails to resolve
						// specific variables!
						rm.done(new VariableData(new MIArg(getFormattedExpression(miVariableDmc.getRelativeFilePath(), miVariableDmc.getName()), Messages.GlobalVariableService_0)));
					}
				});

	}

	/**
	 * Same as with frame objects, this is a base class for the IVariableDMData
	 * object that uses an MIArg object to provide the data. Sub-classes must supply
	 * the MIArg object.
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
		for (IExpressionDMContext variableDMC : globals) {
			if (variableDMC instanceof IExpressionDMContext) {
				fGlobals.removeIf(p -> p.getName().equalsIgnoreCase(variableDMC.getExpression()));
			}
		}
		getSession().dispatchEvent(
				new GlobalsChangedEvent(DMContexts.getAncestorOfType(frameDmc, IExecutionDMContext.class)),
				getProperties());
	}

	private List<IGlobalVariableDescriptor> getInitialGlobalsFromElfs(IDMContext frameDmc) throws DebugException {

		List<IGlobalVariableDescriptor> list = new ArrayList<>();
		MIGlobalVariableInfo infoVariablesResponse = executeGDBCommand(frameDmc, "-symbol-info-variables"); //$NON-NLS-1$
		infoVariablesResponse.getGlobalVariableList().forEach(global -> {
			list.add(new GlobalVariableDMNode(global.getFileName(), global.getFullname(),
					Integer.parseInt(global.getLine().trim()), global.getName(), global.getType(),
					global.getDescription()));
		});
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

	private MIGlobalVariableInfo executeGDBCommand(IDMContext dmcontext, String command) {
		DsfSession session = getSession();
		GetGlobalVariablesQuery cliQuery = new GetGlobalVariablesQuery(dmcontext);
		session.getExecutor().execute(cliQuery);
		try {
			MIGlobalVariableInfo miInfo = cliQuery.get(1, TimeUnit.MINUTES);
			return miInfo;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Activator.logErrorMessage(Messages.GlobalVariableService_4);
			Activator.log(e);
			cliQuery.cancel(true);
		}
		return null;
	}

	private class GetGlobalVariablesQuery extends Query<MIGlobalVariableInfo> {
		IDMContext dmContext;

		public GetGlobalVariablesQuery(IDMContext dmContext) {
			this.dmContext = dmContext;
		}

		@Override
		protected void execute(DataRequestMonitor<MIGlobalVariableInfo> rm) {
			ICommandControlDMContext fContext = DMContexts.getAncestorOfType(dmContext, ICommandControlDMContext.class);
			MICommand<MIGlobalVariableInfo> cmd = new MIInfoVariables(fContext);
			ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
			if (commandControl != null) {
				commandControl.queueCommand(cmd, rm);
			} else {
				rm.setStatus(
						new Status(IStatus.INFO, Activator.PLUGIN_ID, "Unable to find service to execute mi command")); //$NON-NLS-1$
			}

		}

	}
	/**
	 * Return gdb acceptable expression format '<file_name/path>'::<variable_name>
	 * @param filePath
	 * @param globalVariableName
	 * @return
	 */
	private static String getFormattedExpression(String filePath, String globalVariableName) {
		return new StringBuilder().append('\'').append(filePath).append('\'').append("::").append(globalVariableName)
				.toString();
	}
}
