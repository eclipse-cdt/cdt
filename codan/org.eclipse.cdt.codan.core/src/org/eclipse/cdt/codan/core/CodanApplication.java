package org.eclipse.cdt.codan.core;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.codan.core.builder.CodanBuilder;
import org.eclipse.cdt.codan.core.model.CodanProblemReporter;
import org.eclipse.cdt.codan.core.model.CodanRuntime;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class CodanApplication implements IApplication {
	private Collection<String> projects = new ArrayList<String>();
	private boolean verbose = false;
	private boolean all = false;

	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(
				"application.args");
		if (args == null || args.length == 0) {
			help();
			return EXIT_OK;
		}
		extractArguments(args);
		CodanBuilder codanBuilder = new CodanBuilder();
		CodanRuntime runtime = CodanRuntime.getInstance();
		runtime.setProblemReporter(new CodanProblemReporter() {
			@Override
			public void reportProblem(String id, IFile file, int lineNumber,
					String message) {
				System.out.println(file.getLocation() + ":" + lineNumber + ": "
						+ message);
			}
		});
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (all) {
			log("Launching analysis on workspace");
			root.accept(codanBuilder.new CodanResourceVisitor());
		} else {
			for (String project : projects) {
				log("Launching analysis on project " + project);
				IProject wProject = root.getProject(project);
				if (!wProject.exists()) {
					System.err.println("Error: project " + project
							+ " does not exist");
					continue;
				}
				wProject.accept(codanBuilder.new CodanResourceVisitor());
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
			if (string.equals("-verbose")) {
				verbose = true;
			} else if (string.equals("-all")) {
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
		System.out.println("Usage: [options] <project1> <project2> ...");
		System.out.println("Options:");
		System.out.println("  -all - run on all projects in workspace");
		System.out.println("  -verbose - print extra build information");
	}

	public void stop() {
		// nothing
	}
}
