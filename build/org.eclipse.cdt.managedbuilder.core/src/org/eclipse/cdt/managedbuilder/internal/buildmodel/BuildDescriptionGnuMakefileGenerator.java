/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.buildmodel.IStepVisitor;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class BuildDescriptionGnuMakefileGenerator {
	private static final String OUT_STEP_RULE = "post_build"; //$NON-NLS-1$
	private static final String IN_STEP_RULE = "pre_build"; //$NON-NLS-1$
	private static final String ALL = "all"; //$NON-NLS-1$
	private static final String TARGET_SEPARATOR = ":"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR = "\n"; //$NON-NLS-1$
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String ENCODING = "utf-8"; //$NON-NLS-1$
	private static final String VAR_SOURCES = "SOURCES"; //$NON-NLS-1$
	private static final String VAR_TARGETS = "TARGETS"; //$NON-NLS-1$
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String VARREF_PREFIX = "${"; //$NON-NLS-1$
	private static final String VARREF_SUFFIX = "}"; //$NON-NLS-1$

	private static final String DOT_DOT_SLASH = "../"; //$NON-NLS-1$
	private static final String DOT_DOT_BACKSLASH = "..\\"; //$NON-NLS-1$

	private IBuildDescription fDes;

	private class DescriptionVisitor implements IStepVisitor {
		Writer fWriter;
		DescriptionVisitor(Writer writer){
			fWriter = writer;
		}

		@Override
		public int visit(IBuildStep step) throws CoreException {
			if(step == fDes.getInputStep() || step == fDes.getOutputStep())
				return VISIT_CONTINUE;

			try {
				write(fWriter, step);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), ManagedMakeMessages.getString("BuildDescriptionGnuMakefileGenerator.0"), e)); //$NON-NLS-1$
			}
			return VISIT_CONTINUE;
		}
	}

	public BuildDescriptionGnuMakefileGenerator(IBuildDescription des){
		fDes = des;
	}

	public void store(OutputStream stream) throws CoreException{
		Writer writer = createWriter(stream);

		try {
			writer.write(VAR_SOURCES);
			writer.write(EQUALS);
			IBuildStep step = fDes.getInputStep();
			String tmp = toString(step.getOutputResources());
			writer.write(tmp);
			writer.write(LINE_SEPARATOR);
			writer.write(LINE_SEPARATOR);

			writer.write(VAR_TARGETS);
			writer.write(EQUALS);
			step = fDes.getOutputStep();
			tmp = toString(step.getInputResources());
			writer.write(tmp);
			writer.write(LINE_SEPARATOR);
			writer.write(LINE_SEPARATOR);

			writer.write(LINE_SEPARATOR);
			writeRuleHeader(writer, ALL, IN_STEP_RULE + SPACE + OUT_STEP_RULE);
			writer.write(LINE_SEPARATOR);
			writer.write(LINE_SEPARATOR);

			write(writer, fDes.getOutputStep());

			write(writer, fDes.getInputStep());

			BuildDescriptionManager.accept(new DescriptionVisitor(writer), fDes, true);

			writer.flush();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), ManagedMakeMessages.getString("BuildDescriptionGnuMakefileGenerator.1"), e)); //$NON-NLS-1$
		}

	}

	protected Writer createWriter(OutputStream stream){
		try {
			return new OutputStreamWriter(stream, ENCODING);
		} catch (UnsupportedEncodingException e1) {
			ManagedBuilderCorePlugin.log(e1);
		}
		return new OutputStreamWriter(stream);

	}

	protected String createVarRef(String var){
		return new StringBuffer().append(VARREF_PREFIX).append(var).append(VARREF_SUFFIX).toString();
	}

	protected void write(Writer writer, IBuildStep step) throws CoreException, IOException {
		writer.write(LINE_SEPARATOR);

		String target, deps;
		if(step == fDes.getOutputStep()){
			target = OUT_STEP_RULE;
			deps = createVarRef(VAR_TARGETS);
		} else if (step == fDes.getInputStep()){
			target = IN_STEP_RULE;
			deps = ""; //$NON-NLS-1$
		} else {
			IBuildResource[] inputs = step.getInputResources();
			IBuildResource[] outputs = step.getOutputResources();
			target = toString(outputs);
			deps = toString(inputs);
		}

		writeRuleHeader(writer, target, deps);

		IBuildCommand[] cmds = step.getCommands(null, null, null, true);
		for(int i = 0; i < cmds.length; i++){
			String cmdStr = toString(cmds[i]);
			writeCommand(writer, cmdStr);
		}

		writer.write(LINE_SEPARATOR);
		writer.write(LINE_SEPARATOR);

	}

	protected void writeCommand(Writer writer, String cmd) throws IOException{
		writer.write(TAB);
		writer.write(cmd);
		writer.write(LINE_SEPARATOR);
	}

	protected String toString(IBuildCommand cmd){
		StringBuffer buf = new StringBuffer();
		buf.append(cmd.getCommand());
		String argsString = CDataUtil.arrayToString(cmd.getArgs(), SPACE);
		if(argsString != null && argsString.length() != 0){
			buf.append(SPACE);
			buf.append(argsString);
		}
		return removeDotDotSlashesAndBackSlashesHack(buf.toString());
	}

	protected void writeRuleHeader(Writer writer, String target, String deps) throws IOException{
		writer.write(target);
		writer.write(TARGET_SEPARATOR);
		writer.write(SPACE);
		writer.write(deps);
		writer.write(LINE_SEPARATOR);
	}

	protected String toString(IBuildResource[] rcs){
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < rcs.length; i++){
			if(i != 0)
				buf.append(SPACE);
			buf.append(toString(rcs[i]));

		}
		return buf.toString();
	}

	protected String toString(IBuildResource rc){
		return removeDotDotSlashesAndBackSlashesHack(BuildDescriptionManager.getRelPath(fDes.getDefaultBuildDirLocation(), rc.getLocation()).toString());
	}

	/*
	 * this is a very bad hack that removes the "../" and "..\" from the string
	 * this is needed to overcome an assumption that the source root is ../
	 * the BuildDescription calculation mechanism should be fixed to remove this assumption
	 */
	private String removeDotDotSlashesAndBackSlashesHack(String str){
		str = removeDotDotSlashes(str);
		return removeDotDotBackslashes(str);
	}

	private String removeDotDotSlashes(String str){
		int index = str.indexOf(DOT_DOT_SLASH, 0);
		if(index != -1){
			StringBuffer buf = new StringBuffer();
			int start = 0;
			for(; index != -1; index = str.indexOf(DOT_DOT_SLASH, start)){
				buf.append(str.substring(start, index));
				start = index + DOT_DOT_SLASH.length();
			}
			buf.append(str.substring(start));
			return buf.toString();
		}
		return str;
	}

	private String removeDotDotBackslashes(String str){
		int index = str.indexOf(DOT_DOT_BACKSLASH, 0);
		if(index != -1){
			StringBuffer buf = new StringBuffer();
			int start = 0;
			for(; index != -1; index = str.indexOf(DOT_DOT_BACKSLASH, start)){
				buf.append(str.substring(start, index));
				start = index + DOT_DOT_BACKSLASH.length();
			}
			buf.append(str.substring(start));
			return buf.toString();
		}
		return str;
	}

}
