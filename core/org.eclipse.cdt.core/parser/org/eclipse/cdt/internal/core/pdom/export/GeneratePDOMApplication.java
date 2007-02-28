/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.index.export.ExternalExportProjectProvider;
import org.eclipse.cdt.core.index.export.IExportProjectProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
	private static final String OPT_PROJECTPROVIDER= "-pprovider"; //$NON-NLS-1$
	private static final String OPT_TARGET= "-target"; //$NON-NLS-1$
	private static final String OPT_QUIET= "-quiet"; //$NON-NLS-1$

	private static Map/*<String,IProjectForExportManager>*/ projectInitializers;

	/**
	 * Starts this application
	 */
	public Object start(IApplicationContext context) throws CoreException {
		Map arguments= CLIUtil.parseToMap(Platform.getApplicationArgs());
		output(Messages.GeneratePDOMApplication_Initializing);

		setupCLIProgressProvider();

		String pproviderFQN;
		if(!arguments.containsKey(OPT_PROJECTPROVIDER)) {
			output(MessageFormat.format(Messages.GeneratePDOMApplication_UsingDefaultProjectProvider, new Object[] {DEFAULT_PROJECT_PROVIDER}));
			pproviderFQN= DEFAULT_PROJECT_PROVIDER;
		} else {
			pproviderFQN= (String) CLIUtil.getArg(arguments, OPT_PROJECTPROVIDER, 1).get(0);
		}
		String target= (String) CLIUtil.getArg(arguments, OPT_TARGET, 1).get(0); 
		boolean quiet= arguments.get(OPT_QUIET)!=null;

		if(!quiet) {
			System.setProperty(IPDOMIndexerTask.TRACE_ACTIVITY, Boolean.TRUE.toString());
			System.setProperty(IPDOMIndexerTask.TRACE_PROBLEMS, Boolean.TRUE.toString());
			System.setProperty(IPDOMIndexerTask.TRACE_STATISTICS, Boolean.TRUE.toString());
		}

		IExportProjectProvider pprovider = getExportProjectProvider(pproviderFQN);
		if(pprovider==null) {
			output(MessageFormat.format(Messages.GeneratePDOMApplication_CouldNotFindInitializer, new Object[]{pproviderFQN}));
			return null;
		}
		File targetLocation = new File(target);

		GeneratePDOM generate = new GeneratePDOM(pprovider,	Platform.getApplicationArgs(), targetLocation);
		output(Messages.GeneratePDOMApplication_GenerationStarts);
		generate.run();
		output(Messages.GeneratePDOMApplication_GenerationEnds);
		return null;
	}

	protected void output(String s) {
		System.out.println(s);
	}

	public void stop() {
		// do nothing
	}

	/**
	 * Returns the IExportProjectProvider registed in the plug-in registry under the
	 * specified fully qualified class name
	 * May return null
	 * @param fqn
	 * @return
	 */
	private static synchronized IExportProjectProvider getExportProjectProvider(String fqn) {
		if(projectInitializers==null) {
			projectInitializers = new HashMap();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint indexExtensions = registry.getExtensionPoint(CCorePlugin.INDEX_UNIQ_ID);
			IExtension[] extensions = indexExtensions.getExtensions();
			for(int i=0; i<extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] ce = extension.getConfigurationElements();

				IExportProjectProvider pfem = null;
				for(int j=0; j<ce.length; j++) {
					if(ce[j].getName().equals(EXPORT_PROJECT_PROVIDER)) {
						try {
							pfem = (IExportProjectProvider) ce[j].createExecutableExtension("class"); //$NON-NLS-1$
						} catch(CoreException cee) {
							CCorePlugin.log(cee);
						}
						break;
					}
				}
				if(pfem!=null) {
					projectInitializers.put(pfem.getClass().getName(), pfem);
				}
			}
		}

		IExportProjectProvider initer = (IExportProjectProvider) projectInitializers.get(fqn);
		return initer;
	}

	/**
	 * In this application, the usual progress reports are redirected to stdoutt
	 */
	private void setupCLIProgressProvider() {
		ProgressProvider pp = new ProgressProvider() {
			class IndexingStreamProgressMonitor extends StreamProgressMonitor {
				public IndexingStreamProgressMonitor(PrintStream writer) {
					super(writer);
				}
				protected boolean shouldOutput() {
					return taskName!=null && taskName.equals(CCorePlugin.getResourceString("pdom.indexer.task")); //$NON-NLS-1$
				}
			}
			public IProgressMonitor createMonitor(Job job) {
				return new IndexingStreamProgressMonitor(System.out);
			}
			public IProgressMonitor createMonitor(Job job,
					IProgressMonitor group, int ticks) {
				return new NullProgressMonitor();
			}
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

		public void done() {
		}
		public void worked(int work) {
			internalWorked(work);
		}
		public void beginTask(String name, int totalWork) {
			this.taskName = name;
			this.totalWork = totalWork;
		}
		public void internalWorked(double work) {
			synchronized(mutex) {
				worked += work;
				int pc = totalWork<1 ? 0 : (int) ((worked*100D)/totalWork);
				if(shouldOutput()) {
					writer.println(pc+"% "+subTask+" "+worked+" "+totalWork);  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				}
			}
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean value) {
			canceled = value;
		}
		public void setTaskName(String name) {
			taskName = name;
		}
		public void subTask(String name) {
			subTask = name;
		}
	}	
}
