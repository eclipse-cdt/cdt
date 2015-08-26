package org.eclipse.cdt.internal.qt.core.project;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.cdt.internal.qt.core.QtTemplateGenerator;
import org.eclipse.cdt.internal.qt.core.build.QtBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class QtProjectGenerator {

	private final IProject project;

	public QtProjectGenerator(IProject project) {
		this.project = project;
	}

	public void generate(IProgressMonitor monitor) throws CoreException {
		// Add natures to project: C, C++, Arduino
		IProjectDescription projDesc = project.getDescription();
		String[] oldIds = projDesc.getNatureIds();
		String[] newIds = new String[oldIds.length + 3];
		System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
		newIds[newIds.length - 3] = CProjectNature.C_NATURE_ID;
		newIds[newIds.length - 2] = CCProjectNature.CC_NATURE_ID;
		newIds[newIds.length - 1] = QtNature.ID;
		projDesc.setNatureIds(newIds);

		// Add Arduino Builder
		ICommand command = projDesc.newCommand();
		command.setBuilderName(QtBuilder.ID);
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		projDesc.setBuildSpec(new ICommand[] { command });

		project.setDescription(projDesc, monitor);

		// Generate the project
		QtTemplateGenerator templateGen = new QtTemplateGenerator();
		Map<String, Object> fmModel = new HashMap<>();
		fmModel.put("projectName", project.getName()); //$NON-NLS-1$

		IFile sourceFile = project.getFile("main.cpp"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "project2/appProject/main.cpp", sourceFile, monitor); //$NON-NLS-1$
		sourceFile = project.getFile("main.qml"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "project2/appProject/main.qml", sourceFile, monitor); //$NON-NLS-1$
		sourceFile = project.getFile("main.qrc"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "project2/appProject/main.qrc", sourceFile, monitor); //$NON-NLS-1$
		sourceFile = project.getFile("main.pro"); //$NON-NLS-1$
		templateGen.generateFile(fmModel, "project2/appProject/main.pro", sourceFile, monitor); //$NON-NLS-1$
	}

}
