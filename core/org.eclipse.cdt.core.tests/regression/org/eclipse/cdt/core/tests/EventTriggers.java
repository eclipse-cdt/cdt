package org.eclipse.cdt.core.tests;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.junit.BeforeClass;
import org.junit.Test;

public class EventTriggers {
	static public class ConfigurationChangeListener implements ICProjectDescriptionListener {
		private static Set<String> projectNames = new HashSet<>();

		public static boolean hasHit(String projectName) {
			return projectNames.contains(projectName);
		}

		public static void removeHit(String projectName) {
			projectNames.remove(projectName);
		}

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			IProject eventProject = event.getProject();
			projectNames.add(eventProject.getName());
		}
	}

	@BeforeClass
	public static void register() {
		CoreModel.getDefault().addCProjectDescriptionListener(new ConfigurationChangeListener(),
				CProjectDescriptionEvent.ABOUT_TO_APPLY);
	}

	@Test
	public void noEventWith_AVAOID_UPDATE() {
		final String projectName = "noEventWith_AVAOID_UPDATE";

		ICoreRunnable runnable = new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				createAProject(projectName);
			}
		};

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ConfigurationChangeListener.removeHit(projectName);
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

		} catch (Exception e) {
			fail("project creation failed");
		}
		if (ConfigurationChangeListener.hasHit(projectName)) {
			fail("Change listener should not be called");
		}
	}

	@Test
	public void noEventWith_WorkspaceModifyOperation() {
		final String projectName = "noEventWith_WorkspaceModifyOperation";

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor internalMonitor) throws CoreException {
				createAProject(projectName);
			}
		};

		try {
			ConfigurationChangeListener.removeHit(projectName);
			op.run(new NullProgressMonitor());
		} catch (Exception e) {
			fail("project creation failed");
		}
		if (ConfigurationChangeListener.hasHit(projectName)) {
			fail("Change listener should not be called");
		}
	}

	@Test
	public void withEvent() {
		final String projectName = "withEvent";

		ICoreRunnable runnable = new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				createAProject(projectName);

			}
		};

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ConfigurationChangeListener.removeHit(projectName);
			workspace.run(runnable, root, 0, new NullProgressMonitor());

		} catch (Exception e) {
			fail("project creation failed");
		}
		if (!ConfigurationChangeListener.hasHit(projectName)) {
			fail("Change listener should be called");
		}
	}

	public IProject createAProject(String realProjectName) {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject newProjectHandle = root.getProject(realProjectName);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(),
					IPDOMManager.ID_NO_INDEXER);
			CCorePlugin cCorePlugin = CCorePlugin.getDefault();
			ICProjectDescription prjCDesc = cCorePlugin.getProjectDescription(newProjectHandle);
			cCorePlugin.setProjectDescription(newProjectHandle, prjCDesc, true, null);
		} catch (Exception e) {
			fail("project creation failed");
		}
		return newProjectHandle;
	}

}
