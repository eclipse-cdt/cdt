/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Anna Dushistova (MontaVista) - bug [247087] 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.export.ExternalExportProjectProvider;
import org.eclipse.cdt.core.index.export.IExportProjectProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * An eclipse application for generating PDOM's without starting the Workbench
 */
public class GeneratePDOMApplication implements IApplication {
	private static final String EXPORT_PROJECT_PROVIDER = "ExportProjectProvider"; //$NON-NLS-1$
	private static final String DEFAULT_PROJECT_PROVIDER = ExternalExportProjectProvider.class.getName();
	public static final String OPT_PROJECTPROVIDER= "-pprovider"; //$NON-NLS-1$
	public static final String OPT_TARGET= "-target"; //$NON-NLS-1$
	public static final String OPT_QUIET= "-quiet"; //$NON-NLS-1$
	public static final String OPT_INDEXER_ID= "-indexer"; //$NON-NLS-1$

	/**
	 * Applications needing to fail in an expected way (without stack dump), should throw
	 * CoreExceptions with this error code.
	 */
	public static final int ECODE_EXPECTED_FAILURE= 1;
	
	private static Map<String,IExportProjectProvider> projectInitializers;

	/**
	 * Starts this application
	 * @throws CoreException on an unexpected failure
	 */
	@Override
	public Object start(IApplicationContext context) throws CoreException {
		Object result= IApplication.EXIT_OK;
		try {
			result= startImpl(context);
		} catch(CoreException ce) {
			IStatus s= ce.getStatus();
			if(s.getCode()==ECODE_EXPECTED_FAILURE) {
				output(s.getMessage());
			} else {
				throw ce;
			}
		}
		return result;
	}
	
	private Object startImpl(IApplicationContext context) throws CoreException {
		String[] appArgs= (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		Map<String,List<String>> arguments= CLIUtil.parseToMap(appArgs);
		output(Messages.GeneratePDOMApplication_Initializing);

		setupCLIProgressProvider();

		String pproviderFQN;
		if(!arguments.containsKey(OPT_PROJECTPROVIDER)) {
			output(MessageFormat.format(Messages.GeneratePDOMApplication_UsingDefaultProjectProvider, new Object[] {DEFAULT_PROJECT_PROVIDER}));
			pproviderFQN= DEFAULT_PROJECT_PROVIDER;
		} else {
			pproviderFQN= CLIUtil.getArg(arguments, OPT_PROJECTPROVIDER, 1).get(0);
		}
		String target= CLIUtil.getArg(arguments, OPT_TARGET, 1).get(0); 
		boolean quiet= arguments.get(OPT_QUIET)!=null;

		String indexerID= IPDOMManager.ID_FAST_INDEXER;
		List<String> indexerIDs= arguments.get(OPT_INDEXER_ID);
		if(indexerIDs!=null) {
			if(indexerIDs.size()==1) {
				indexerID= indexerIDs.get(0);
			} else if(indexerIDs.size()>1) {
				fail(MessageFormat.format(Messages.GeneratePDOMApplication_InvalidIndexerID, new Object[] {OPT_INDEXER_ID}));
			}
		}
		
		String[] oldvals= null;
		if(!quiet) {
			oldvals= new String[] {
					System.getProperty(IPDOMIndexerTask.TRACE_ACTIVITY),
					System.getProperty(IPDOMIndexerTask.TRACE_PROBLEMS),
					System.getProperty(IPDOMIndexerTask.TRACE_STATISTICS),
			};
			System.setProperty(IPDOMIndexerTask.TRACE_ACTIVITY, Boolean.TRUE.toString());
			System.setProperty(IPDOMIndexerTask.TRACE_PROBLEMS, Boolean.TRUE.toString());
			System.setProperty(IPDOMIndexerTask.TRACE_STATISTICS, Boolean.TRUE.toString());
		}
		try {
			IExportProjectProvider pprovider = getExportProjectProvider(pproviderFQN);
			if(pprovider==null) {
				fail(MessageFormat.format(Messages.GeneratePDOMApplication_CouldNotFindInitializer, new Object[]{pproviderFQN}));
			}
			File targetLocation = new File(target);

			GeneratePDOM generate = new GeneratePDOM(pprovider,	appArgs, targetLocation, indexerID);
			output(Messages.GeneratePDOMApplication_GenerationStarts);
			IStatus status = generate.run(); // CoreException handled in start method
			if(!status.isOK()){
				output(status.getMessage());
			}
			output(Messages.GeneratePDOMApplication_GenerationEnds);
		} finally {
			if (oldvals != null) {
				restoreSystemProperty(IPDOMIndexerTask.TRACE_ACTIVITY, oldvals[0]);
				restoreSystemProperty(IPDOMIndexerTask.TRACE_PROBLEMS, oldvals[1]);
				restoreSystemProperty(IPDOMIndexerTask.TRACE_STATISTICS, oldvals[2]);
			}
		}
		return null;
	}

	private void restoreSystemProperty(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}

	protected void output(String s) {
		System.out.println(s);
	}

	@Override
	public void stop() {
		// do nothing
	}

	/**
	 * Causes the application to fail in a way that has been anticipated (e.g. a command-line or interface
	 * contract violation by a extension implementation)
	 * @param message
	 * @throws CoreException
	 */
	public static final void fail(String message) throws CoreException {
		IStatus status= new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, ECODE_EXPECTED_FAILURE, message, null);
		CCorePlugin.log(status);
		throw new CoreException(status);
	}
	
	/**
	 * Returns the IExportProjectProvider registered in the plug-in registry under the
	 * specified fully qualified class name
	 * May return null
	 * @param fqn
	 * @return
	 */
	private static synchronized IExportProjectProvider getExportProjectProvider(String fqn) {
		if(projectInitializers==null) {
			projectInitializers = new HashMap<String, IExportProjectProvider>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint indexExtensions = registry.getExtensionPoint(CCorePlugin.INDEX_UNIQ_ID);
			IExtension[] extensions = indexExtensions.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] ce = extension.getConfigurationElements();

				for (IConfigurationElement element : ce) {
					if(element.getName().equals(EXPORT_PROJECT_PROVIDER)) {
						try {
							IExportProjectProvider epp = (IExportProjectProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
							projectInitializers.put(epp.getClass().getName(), epp);
						} catch(CoreException cee) {
							CCorePlugin.log(cee);
						}
					}
				}
			}
		}

		IExportProjectProvider initer = projectInitializers.get(fqn);
		return initer;
	}

	/**
	 * In this application, the usual progress reports are redirected to standard out
	 */
	private void setupCLIProgressProvider() {
		ProgressProvider pp = new ProgressProvider() {
			class IndexingStreamProgressMonitor extends StreamProgressMonitor {
				public IndexingStreamProgressMonitor(PrintStream writer) {
					super(writer);
				}
				@Override
				protected boolean shouldOutput() {
					return taskName!=null && taskName.equals(CCorePlugin.getResourceString("pdom.indexer.task")); //$NON-NLS-1$
				}
			}
			@Override
			public IProgressMonitor createMonitor(Job job) {
				return new IndexingStreamProgressMonitor(System.out);
			}
			@Override
			public IProgressMonitor createMonitor(Job job,
					IProgressMonitor group, int ticks) {
				return new NullProgressMonitor();
			}
			@Override
			public IProgressMonitor createProgressGroup() {
				return new NullProgressMonitor();
			}
		};
		Job.getJobManager().setProgressProvider(pp);
	}

	static class StreamProgressMonitor implements IProgressMonitor {
		volatile boolean canceled;
		volatile int totalWork;
		volatile double worked;
		final PrintStream writer;
		volatile String taskName, subTask;
		Object mutex = new Object();

		StreamProgressMonitor(PrintStream writer) {
			this.writer = writer;
			this.totalWork = -1;
		}

		protected boolean shouldOutput() {
			return true;
		}

		@Override
		public void done() {
		}
		@Override
		public void worked(int work) {
			internalWorked(work);
		}
		@Override
		public void beginTask(String name, int total) {
			this.taskName = name;
			this.totalWork = total;
		}
		@Override
		public void internalWorked(double work) {
			synchronized(mutex) {
				worked += work;
				int pc = totalWork<1 ? 0 : (int) ((worked*100D)/totalWork);
				if(shouldOutput()) {
					writer.println(pc+"% "+subTask);  //$NON-NLS-1$
				}
			}
		}

		@Override
		public boolean isCanceled() {
			return canceled;
		}

		@Override
		public void setCanceled(boolean value) {
			canceled = value;
		}
		@Override
		public void setTaskName(String name) {
			taskName = name;
		}
		@Override
		public void subTask(String name) {
			subTask = name;
		}
	}	
}
