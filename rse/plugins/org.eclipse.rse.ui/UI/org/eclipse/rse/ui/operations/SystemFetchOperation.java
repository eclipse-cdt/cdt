/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189272] exception when canceling ssh connect
 * David Dykstal (IBM) - [189483] add notification when canceling password prompting
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight (IBM)          - [231964] [ssh] SSH login dialog appears twice after cancel, when doing Refresh on a node
 * David McKnight (IBM)          - [235164] SystemView should allow to create filter in disconnected mode
 * David McKnight (IBM)          - [239368] Expand to action ignores the filter string
 * David McKnight   (IBM)        - [244270] Explicit check for isOffline and just returning block implementing a cache for Work Offline
 * David McKnight   (IBM)        - [233160] [dstore] SSL/non-SSL alert are not appropriate
 * David McKnight   (IBM)        - [243263] NPE on expanding a filter
 * David McKnight   (IBM)        - [260777] [ssh] Deadlock when changing selection after multiple hibernate/resume cycles
 * David McKnight   (IBM)        - [283793] [dstore] Expansion indicator(+) does not reset after no connect
 * David McKnight   (IBM)        - [316565] Failed to resolve the filter for a non-connected subsystem
 *******************************************************************************/

package org.eclipse.rse.ui.operations;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.core.subsystems.SubSystem.DisplayErrorMessageJob;
import org.eclipse.rse.internal.ui.GenericMessages;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.IElementCollector;


/**
 * A SystemFetchOperation is used to perform a query of a remote system on behalf of a subsystem. The operation
 * has some knowledge of the containing user interface, e.g. the workbench part which is responsible for
 * issuing the query. It is created with a "collector" that will contain the results of the query.
 * <p>
 * This class may be subclassed but usually is used directly.
 */
public class SystemFetchOperation extends JobChangeAdapter implements IRunnableWithProgress
{
	private static class ConnectorServicePool {

		private static List _connectingConnectorServices = new ArrayList();

		public synchronized void add(IConnectorService cs) {
			_connectingConnectorServices.add(cs);
		}

		public synchronized void remove(IConnectorService cs) {
			_connectingConnectorServices.remove(cs);
			notifyAll();
		}

		public synchronized boolean contains(IConnectorService cs) {
			return _connectingConnectorServices.contains(cs);
		}

		public synchronized void waitUntilNotContained(IConnectorService cs) {
			while (contains(cs)){ // wait until the connector service is no longer in the list
				try {
						wait();
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

    protected IWorkbenchPart _part;
    protected Object _remoteObject;
    protected IElementCollector _collector;
    private IRunnableContext context;
    protected ISystemViewElementAdapter _adapter;
    protected boolean _canRunAsJob;
    protected InvocationTargetException _exc;

    private static ConnectorServicePool _connectorServicePool = new ConnectorServicePool();

    /**
     * Creates an instance of this fetch operation. This instance cannot be run in a job, but must be invoked directly.
     * @param part the workbench part associated with this fetch.
     * @param remoteObject the remote object that provides the context for this fetch
     * @param adapter the adapter that can be used to extract information from the remote objects that will be retrieved by this fetch.
     * @param collector the collector for the fetch results.
     */
    public SystemFetchOperation(IWorkbenchPart part, Object remoteObject, ISystemViewElementAdapter adapter, IElementCollector collector)
    {
		_part = part;
		_remoteObject = remoteObject;
		_collector = collector;
		_adapter = adapter;
		_canRunAsJob = false;
	}

    /**
     * Creates an instance of this fetch operation.
     * @param part the workbench part associated with this fetch.
     * @param remoteObject the remote object that provides the context for this fetch
     * @param adapter the adapter that can be used to extract information from the remote objects that will be retrieved by this fetch.
     * @param collector the collector for the fetch results.
     * @param canRunAsJob true if this fetch operation can be run in a job of its own, false otherwise
     */
    public SystemFetchOperation(IWorkbenchPart part, Object remoteObject, ISystemViewElementAdapter adapter, IElementCollector collector, boolean canRunAsJob)
    {
		_part = part;
		_remoteObject = remoteObject;
		_collector = collector;
		_adapter = adapter;
		_canRunAsJob = canRunAsJob;
	}

    public void setException(InvocationTargetException exc)
    {
    	_exc = exc;
    }

	/**
	 * Return the part that is associated with this operation.
	 *
	 * @return Returns the part or <code>null</code>
	 */
	public IWorkbenchPart getPart() {
		return _part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		startOperation();
		try {
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100);
			monitor.setTaskName(getTaskName());
			execute(Policy.subMonitorFor(monitor, 100));
			endOperation();
		} catch (InterruptedException e) { // operation was cancelled
			endOperation();
			monitor.setCanceled(true);
			throw e;
		} catch (Exception e) {
			// TODO: errors may not be empty (i.e. endOperation has not been executed)
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	protected void startOperation() {
		//statusCount = 0;
		//resetErrors();
		//confirmOverwrite = true;
	}

	protected void endOperation() {
		//handleErrors((IStatus[]) errors.toArray(new IStatus[errors.size()]));
	}


	/**
	 * An action that prompts the user for credentials to connect the subsystem that is issued the fetch.
	 * <p>
	 * This class is listed as public, but should not be used/referenced by others.
	 */
	public class PromptForPassword implements Runnable
	{
		public SubSystem _ss;
		private volatile boolean isCancelled = false;
		public PromptForPassword(SubSystem ss)
		{
			_ss = ss;
		}

		public void run()
		{
			try
			{
				isCancelled = false;
				_ss.promptForPassword();
			}
			catch (InterruptedException e) {
				isCancelled = true;
			}
			catch (OperationCanceledException e) {
				isCancelled = true;
			}
			catch (Exception e)
			{

			}
		}

		/**
		 * Test whether this operation is cancelled.
		 *
		 * This method was renamed from isCanceled to isCancelled in RSE 3.0.
		 *
		 * @since org.eclipse.rse.ui 3.0
		 * @return true if cancelled
		 */
		public boolean isCancelled() {
			return isCancelled;
		}
	}

	/**
	 * A sub-operation that broadcasts any connection status change.
	 * <p>
	 * Listed as public, but should not be used or referenced by others.
	 */
	public class UpdateRegistry implements Runnable
	{
		private SubSystem _ss;
		public UpdateRegistry(SubSystem ss)
		{
			_ss = ss;
		}

		public void run()
		{
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.connectedStatusChange(_ss, true, false);
		}
	}


	private boolean ensureConnected(SubSystem ss, IProgressMonitor monitor) throws InterruptedException {
		if (!ss.getSubSystemConfiguration().supportsSubSystemConnect()){
			return true;
		}
		
		if (!ss.isConnected() &&
				!ss.isOffline()) // skip the connect if offline, but still follow through because we need to follow through in the subsystem
		{
			IConnectorService connectorService = ss.getConnectorService();

			boolean alreadyConnecting = false;

			// is this connector service already connecting?
			alreadyConnecting = _connectorServicePool.contains(connectorService);

			if (alreadyConnecting){
				// connector service already attempting connect
				// need to wait for it to complete
				// before we can return out of this method
				_connectorServicePool.waitUntilNotContained(connectorService);
			}
			else {
				final Display dis = Display.getDefault();
				_connectorServicePool.add(connectorService);
				try
				{
					PromptForPassword prompter = new PromptForPassword(ss);
					dis.syncExec(prompter);
					if (prompter.isCancelled()) {
						SystemMessage cancelledMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_CANCELLED);
						SystemMessageObject cancelledMessageObject = new SystemMessageObject(cancelledMessage, ISystemMessageObject.MSGTYPE_CANCEL,
								_remoteObject);
						_collector.add(cancelledMessageObject, monitor);
						throw new InterruptedException();
					}
					connectorService.connect(monitor);
					if (_exc != null)
					{
						showOperationErrorMessage(null, _exc, ss);
					}
				}
				catch (InvocationTargetException exc)
				{
	          	  	showOperationErrorMessage(null, exc, ss);
					return false;
				}
				catch (Exception e)
				{
					showOperationErrorMessage(null, e, ss);
					return false;
				}
				finally {
					_connectorServicePool.remove(connectorService);
				}

				dis.asyncExec(new UpdateRegistry(ss));
			}
		}
		return ss.isConnected();

	}

	/**
	 * Subclasses must override this method to perform the operation.
	 * Clients should never call this method directly.
	 *
	 * @param monitor
	 * @throws Exception
	 * @throws InterruptedException
	 */
	protected void execute(IProgressMonitor monitor) throws Exception, InterruptedException
	{
		SubSystem ss = null;
		Object actualRemoteObj = _remoteObject;
		if (_remoteObject instanceof IContextObject)
		{
			ss = (SubSystem)((IContextObject)_remoteObject).getSubSystem();
			actualRemoteObj = ((IContextObject)_remoteObject).getModelObject();
		}
		else
		{
			ss = (SubSystem)_adapter.getSubSystem(_remoteObject);
		}


		boolean isPromptable = false;

		if (actualRemoteObj instanceof IAdaptable){
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)actualRemoteObj).getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null){
				isPromptable = adapter.isPromptable(actualRemoteObj);
			}
		}

		Object[] children = null;
		if (!isPromptable){
			if (!ensureConnected(ss, monitor)){
				children = new Object[0];
				_collector.add(children, monitor);
				return;
			}
		}



  	  	// we first test to see if this is an expand-to filter in effect for this
  	  	//  object, and if so use it...
  	  	if ((_part==null || _part instanceof SystemViewPart) && _adapter instanceof ISystemRemoteElementAdapter)
  	  	{
  	  		class GetExpandToFilter implements Runnable
  	  		{
  	  			private String expandToFilter = null;
  	  			public void run()
  	  			{
  	  				// fetching part here ourselves, because the correct part can not always be determined from a background Thread
  	  				// see bug 244454 for details
  	  				IWorkbenchPart activePart = _part;
  	  				if (activePart==null) {
  	  					IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
  		  				if (win != null){
	  	  					IWorkbenchPage page = win.getActivePage();
  	  						if (page != null){
  	  							activePart = page.getActivePart();
  	  							if (activePart != null){
  	  								_part = activePart;
  	  							}
  		  					}
	  	  				}
  	  				}

  	  				if (activePart instanceof SystemViewPart){
  	  					SystemView viewer = ((SystemViewPart)activePart).getSystemView();
  	  					if (_remoteObject instanceof IContextObject){
  	  						expandToFilter = viewer.getExpandToFilter(((IContextObject)_remoteObject).getModelObject());
  	  					}
  	  					else {
  	  						expandToFilter = viewer.getExpandToFilter(_remoteObject);
  	  					}
  	  				}
  	  			}

  	  			public String getExpandToFilter()
  	  			{
  	  				return expandToFilter;
  	  			}
  	  		}

  	  		Display dis = Display.getDefault();
  	  		GetExpandToFilter getExpandTo = new GetExpandToFilter();
  	  		dis.syncExec(getExpandTo);
  	  		String expandToFilter = getExpandTo.getExpandToFilter();

  	  		if (expandToFilter != null){
  	  			if (_remoteObject instanceof IContextObject){
  	  				children = _adapter.getChildrenUsingExpandToFilter(((IContextObject)_remoteObject).getModelObject(), expandToFilter);
  	  			}
  	  			else {
  	  				children = _adapter.getChildrenUsingExpandToFilter(_remoteObject, expandToFilter);
  	  			}
  	  		}
  	  	}
  	  	if (children == null){
  	  		if (_remoteObject instanceof IContextObject)
  	  		{
  	  			children = _adapter.getChildren((IContextObject)_remoteObject, monitor);
  	  		}
  	  		else
  	  		{
  	  			children = _adapter.getChildren((IAdaptable)_remoteObject, monitor);
  	  		}
  	  	}
	    _collector.add(children, monitor);
	    monitor.done();
	}

    /**
     * Show an error message as a result of running this operation.
     * Uses the user interface knowledge of this operation to show the message.
     * <p>
     * May be overridden by subclasses but it usually used directly.
     * @param shell The parent shell for a message dialog box.
     * @param exc the exception that was recieved and should be shown
     * @param ss the subsystem that this operation is being issued for
     */
    protected void showOperationErrorMessage(Shell shell, Throwable exc, SubSystem ss)
    {
    	if (exc instanceof InvocationTargetException) {
    		exc = ((InvocationTargetException)exc).getTargetException();
    	}
    	if (exc instanceof OperationCanceledException) {
    		return; //don't log or display user cancellation
    	}
    	SystemMessage sysMsg = null;
    	if (exc instanceof SystemMessageException)
    	{
    	        displayAsyncMsg(ss, (SystemMessageException)exc);
    	  //sysMsg = ((SystemMessageException)exc).getSystemMessage();
    	}
    	else
    	{
    	  String excMsg = exc.getMessage();
    	  if ((excMsg == null) || (excMsg.length()==0))
				exc.getClass().getName();
			else
				excMsg = exc.getClass().getName() + ": " + excMsg; //$NON-NLS-1$
          sysMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_FAILED);
          sysMsg.makeSubstitution(excMsg);

    	 SystemMessageDialog.displayErrorMessage(shell, sysMsg, exc);
    	}

    }

	/**
	 * Display message on message thread
	 */
	protected void displayAsyncMsg(SubSystem ss, org.eclipse.rse.services.clientserver.messages.SystemMessageException msg)
	{
		DisplayErrorMessageJob job = new DisplayErrorMessageJob(null, msg);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(true);
		job.schedule();
	}

	protected String getTaskName()
	{
	    return GenericMessages.RSEQuery_task;
	}

	/**
	 * Run the operation in a context that is determined by the {@link #canRunAsJob()}
	 * hint. If this operation can run as a job then it will be run in a background thread.
	 * Otherwise it will run in the foreground and block the caller.
	 */
	public final void run() throws InvocationTargetException, InterruptedException {
		if (shouldRun()) {
			getRunnableContext().run(this);
		}
	}

	/**
	 * This method is invoked from the <code>run()</code> method before
	 * the operation is run in the operation's context. Subclasses may
	 * override in order to perform prechecks to determine if the operation
	 * should run. This may include prompting the user for information, etc.
	 *
	 * @return whether the operation should be run.
	 */
	protected boolean shouldRun() {
		return true;
	}

	/**
	 * Returns the scheduling rule that is to be obtained before this
	 * operation is executed by it's context or <code>null</code> if
	 * no scheduling rule is to be obtained. If the operation is run
	 * as a job, the schdulin rule is used as the schduling rule of the
	 * job. Otherwise, it is obtained before execution of the operation
	 * occurs.
	 * <p>
	 * By default, no scheduling
	 * rule is obtained. Sublcasses can override to in order ot obtain a
	 * scheduling rule or can obtain schduling rules withing their operation
	 * if finer grained schduling is desired.
	 *
	 * @return the schduling rule to be obtained by this operation
	 * or <code>null</code>.
	 */
	protected ISchedulingRule getSchedulingRule() {
		return null;
	}

	/**
	 * Return whether the auto-build should be postponed until after
	 * the operation is complete. The default is to postpone the auto-build.
	 * subclas can override.
	 *
	 * @return whether to postpone the auto-build while the operation is executing.
	 */
	protected boolean isPostponeAutobuild() {
		return true;
	}


	/**
	 * If this operation can safely be run in the background, then subclasses can
	 * override this method and return <code>true</code>. This will make their
	 * action run in a {@link  org.eclipse.core.runtime.jobs.Job}.
	 * Subsclass that override this method should
	 * also override the <code>getJobName()</code> method.
	 *
	 * @return <code>true</code> if this action can be run in the background and
	 * <code>false</code> otherwise.
	 */
	protected boolean canRunAsJob() {
		return _canRunAsJob;
	}

	/**
	 * Return the job name to be used if the action can run as a job. (i.e.
	 * if <code>canRunAsJob()</code> returns <code>true</code>).
	 *
	 * @return the string to be used as the job name
	 */
	protected String getJobName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * This method is called to allow subclasses to configure an action that could be run to show
	 * the results of the action to the user. Default is to return null.
	 *
	 * @return an action that could be run to see the results of this operation
	 */
	protected IAction getGotoAction() {
		return null;
	}

	/**
	 * This method is called to allow subclasses to configure an icon to show when running this
	 * operation.
	 *
	 * @return an URL to an icon
	 */
	protected URL getOperationIcon() {
		return null;
	}

	/**
	 * This method is called to allow subclasses to have the operation remain in the progress
	 * indicator even after the job is done.
	 *
	 * @return <code>true</code> to keep the operation and <code>false</code> otherwise.
	 */
	protected boolean getKeepOperation() {
		return false;
	}

	/**
	 * Return a shell that can be used by the operation to display dialogs, etc.
	 *
	 * @return a shell
	 */
	protected Shell getShell()
	{
	    return SystemBasePlugin.getActiveWorkbenchShell();
	}

	private ISystemRunnableContext getRunnableContext() {
		if (context == null && canRunAsJob()) {
			SystemJobRunnableContext context = new SystemJobRunnableContext(getJobName(), getOperationIcon(), getGotoAction(), getKeepOperation(), this, getSite());
			context.setPostponeBuild(isPostponeAutobuild());
			context.setSchedulingRule(getSchedulingRule());
			return context;
		} else {
			SystemProgressDialogRunnableContext context = new SystemProgressDialogRunnableContext(getShell());
			context.setPostponeBuild(isPostponeAutobuild());
			context.setSchedulingRule(getSchedulingRule());
			if (this.context != null) {
				context.setRunnableContext(this.context);
			}
			return context;
		}
	}




	private IWorkbenchSite getSite() {
		IWorkbenchSite site = null;
		if(_part != null) {
			site = _part.getSite();
		}
		return site;
	}
}
