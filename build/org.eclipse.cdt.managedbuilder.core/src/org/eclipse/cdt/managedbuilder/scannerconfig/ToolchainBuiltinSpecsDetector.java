/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.scannerconfig;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;

public abstract class ToolchainBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
	private Map<String, ITool> toolMap = new HashMap<String, ITool>();
	/**
	 * TODO
	 */
	protected abstract String getToolchainId();

	private ITool getTool(String languageId) {
		ITool langTool = toolMap.get(languageId);
		if (langTool != null) {
			return langTool;
		}
		
		String toolchainId = getToolchainId();
		IToolChain toolchain = ManagedBuildManager.getExtensionToolChain(toolchainId);
		if (toolchain != null) {
			ITool[] tools = toolchain.getTools();
			for (ITool tool : tools) {
				IInputType[] inputTypes = tool.getInputTypes();
				for (IInputType inType : inputTypes) {
					String lang = inType.getLanguageId(tool);
					if (languageId.equals(lang)) {
						toolMap.put(languageId, tool);
						return tool;
					}
				}
			}
		}
		ManagedBuilderCorePlugin.error("Unable to find tool in toolchain="+toolchainId+" for language="+languageId);
		return null;
	}

	@Override
	protected String getCompilerCommand(String languageId) {
		ITool tool = getTool(languageId);
		String compiler = tool.getToolCommand();
		if (compiler.length() == 0) {
			String msg = "Unable to find compiler command in toolchain="+getToolchainId();
			ManagedBuilderCorePlugin.error(msg);
		}
		return compiler;
	}

	@Override
	protected String getSpecFileExtension(String languageId) {
		String ext = null;
		ITool tool = getTool(languageId);
		String[] srcFileExtensions = tool.getAllInputExtensions();
		if (srcFileExtensions != null && srcFileExtensions.length > 0) {
			ext = srcFileExtensions[0];
		}
		if (ext == null || ext.length() == 0) {
			ManagedBuilderCorePlugin.error("Unable to find file extension for language "+languageId);
		}
		return ext;
	}

}
