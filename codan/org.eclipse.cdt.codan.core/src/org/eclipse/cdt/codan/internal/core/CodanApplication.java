package org.eclipse.cdt.codan.internal.core;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.Messages;
import org.eclipse.cdt.codan.internal.core.model.CodanMarkerProblemReporter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.util.NLS;

/**
 * Application to support headless build
 * 
 * @noextend This class is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CodanApplication implements IApplication {
	private Collection<String> projects = new ArrayList<String>();
	private boolean verbose = false;
	private boolean all = false;

	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(
				"application.args"); //$NON-NLS-1$
		if (args == null || args.length == 0) {
			help();
			return EXIT_OK;
		}
		extractArguments(args);
		CodanBuilder codanBuilder = new CodanBuilder();
		CodanRuntime runtime = CodanRuntime.getInstance();
		runtime.setProblemReporter(new CodanMarkerProblemReporter() {
			@Override
			public void reportProblem(String id, String markerType,
					int severity, IResource file, int lineNumber,
					int startChar, int endChar, String message) {
				System.out.println(file.getLocation() + ":" + lineNumber + ": " //$NON-NLS-1$ //$NON-NLS-2$
						+ message);
			}
		});
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (all) {
			log(Messages.CodanApplication_LogRunWorkspace);
			codanBuilder.processResource(root, new NullProgressMonitor());
		} else {
			for (String project : projects) {
				log(Messages.CodanApplication_LogRunProject + project);
				IProject wProject = root.getProject(project);
				if (!wProject.exists()) {
					System.err
							.println( //
							NLS.bind(
									Messages.CodanApplication_Error_ProjectDoesNotExists,
									project));
					continue;
				}
				codanBuilder.processResource(wProject,
						new NullProgressMonitor());
			}
		}
		return EXIT_OK;
	}

	/**
	 * @param string
	 */
	private void log(String string) {
		if (verbose)
			System.err.println(string);
	}

	/**
	 * @param args
	 */
	private void extractArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String string = args[i];
			if (string.equals("-verbose")) { //$NON-NLS-1$
				verbose = true;
			} else if (string.equals("-all")) { //$NON-NLS-1$
				all = true;
			} else {
				projects.add(string);
			}
		}
	}

	/**
	 * 
	 */
	private void help() {
		System.out.println(Messages.CodanApplication_Usage);
		System.out.println(Messages.CodanApplication_Options);
		System.out.println(Messages.CodanApplication_all_option);
		System.out.println(Messages.CodanApplication_verbose_option);
	}

	public void stop() {
		// nothing
	}
}
