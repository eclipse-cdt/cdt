/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.makefile.IBadDirective;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefileValidator;
import org.eclipse.cdt.make.core.makefile.ISpecialRule;
import org.eclipse.cdt.make.core.makefile.gnu.IConditional;
import org.eclipse.cdt.make.core.makefile.gnu.ITerminal;
import org.eclipse.cdt.make.core.makefile.gnu.IVariableDefinition;
import org.eclipse.cdt.make.internal.core.makefile.MakefileMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class GNUMakefileValidator implements IMakefileValidator {

	IMarkerGenerator reporter;

	public GNUMakefileValidator() {
	}

	public GNUMakefileValidator(IMarkerGenerator errorHandler) {
		setMarkerGenerator(errorHandler);
	}

	public void setMarkerGenerator(IMarkerGenerator errorHandler) {
		reporter = errorHandler;
	}

	public IMarkerGenerator getMarkerGenerator() {
		if (reporter == null) {
			reporter = new IMarkerGenerator() {

				public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
					String name = "Makefile"; //$NON-NLS-1$
					if (file != null) {
						name = file.getName();
					}
					StringBuffer sb = new StringBuffer(name);
					sb.append(':').append(lineNumber).append(':').append(getSeverity(severity));
					if (errorDesc != null) {
						sb.append(':').append(errorDesc);
					}
					if (errorVar != null ) {
						sb.append(':').append(errorVar);
					}
					sb.append('\n');
					System.out.println(sb.toString());				
				}

				public String getSeverity(int severity) {
					if (severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
						return MakefileMessages.getString("MakefileValidator.errorBuild"); //$NON-NLS-1$
					} else if (severity == IMarkerGenerator.SEVERITY_ERROR_RESOURCE) {
						return MakefileMessages.getString("MakefileValidator.errorResource"); //$NON-NLS-1$
					} else if (severity == IMarkerGenerator.SEVERITY_INFO) {
						return MakefileMessages.getString("MakefileValidator.warningInfo"); //$NON-NLS-1$
					} else if (severity == IMarkerGenerator.SEVERITY_WARNING) {
						return MakefileMessages.getString("MakefileValidator.warning"); //$NON-NLS-1$
					}
					return MakefileMessages.getString("MakefileValidator.unknown"); //$NON-NLS-1$
				}
				
			};
		}
		return reporter;
	}
	
	public void checkFile(IFile file, IProgressMonitor monitor) {
		String message = MakefileMessages.getString("MakefileValidator.checkingFile") + file.getFullPath().toString(); //$NON-NLS-1$
		monitor.subTask(message);
		GNUMakefile gnu = new GNUMakefile();
		InputStream stream = null;
		try {
			stream = file.getContents();
			Reader source = new InputStreamReader(stream);
			gnu.parse(file.getFullPath().toString(), source);
			validateDirectives(file, gnu.getDirectives());
		} catch (CoreException e) {
		} catch (IOException e) {
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		monitor.subTask(MakefileMessages.getString("MakefileValidator.fileChecked")); //$NON-NLS-1$
		monitor.done();
	}

	public void validateDirectives(IResource res, IDirective[] directives) {

		IMarkerGenerator marker = getMarkerGenerator();
		int conditionCount = 0;
		int defineCount = 0;

		IDirective directive = null;
		for (int i = 0; i < directives.length; i++) {
			directive = directives[i];
			if (directive instanceof IConditional) {
				IConditional condition = (IConditional)directive;
				validateCondition(condition);
				if (!condition.isElse()) {
					conditionCount++;
				} else {
					if (conditionCount == 0) {
						// ERROR else missing conditon.
						int startLine = condition.getStartLine();
						String msg = MakefileMessages.getString("MakefileValidator.error.elseMissingIfCondition"); //$NON-NLS-1$
						int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
						String varName = condition.toString().trim();
						marker.addMarker(res, startLine, msg, severity, varName);
					}
				}
			} else if (directive instanceof ITerminal) {
				ITerminal terminal = (ITerminal)directive;
				if (terminal.isEndif()) {
					if (conditionCount == 0) {
						// ERROR missing condition.
						int startLine = terminal.getStartLine();
						String msg = MakefileMessages.getString("MakefileValidator.error.endifMissingIfElseCondition"); //$NON-NLS-1$
						int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
						String varName = terminal.toString().trim();
						marker.addMarker(res, startLine, msg, severity, varName);
					} else {
						conditionCount--;
					}
				} else if (terminal.isEndef()) {
					if (defineCount == 0) {
						// ERROR missing define.
						int startLine = terminal.getStartLine();
						String msg = MakefileMessages.getString("MakefileValidator.error.endefMissingOverrideDefine"); //$NON-NLS-1$
						int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
						String varName = terminal.toString().trim();
						marker.addMarker(res, startLine, msg, severity, varName);
					} else {
						defineCount--;
					}
				}
			} else if (directive instanceof IVariableDefinition) {
				IVariableDefinition definition = (IVariableDefinition)directive;
				if (definition.isMultiLine()) {
					defineCount++;
				}
			} else if (directive instanceof IBadDirective) {
				// ERROR unknow statement.
				int startLine = directive.getStartLine();
				String msg = MakefileMessages.getString("MakefileValidator.error.unknownDirective"); //$NON-NLS-1$
				int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				String varName = directive.toString().trim();
				marker.addMarker(res, startLine, msg, severity, varName);
			} else if (directive instanceof ISpecialRule) {
				validateSpecialRule((ISpecialRule)directive);
			}
		}
		if (conditionCount > 0) {
			// ERROR no matching endif for condition.
			int startLine = 0;
			String varName = ""; //$NON-NLS-1$
			int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			for (int i = directives.length - 1; i >= 0; i--) {
				if (directives[i] instanceof IConditional) {
					startLine = directives[i].getStartLine();
					varName = directives[i].toString().trim();
					break;
				}
			}
			String msg = MakefileMessages.getString("MakefileValidator.error.noMatchingEndifForCondition"); //$NON-NLS-1$
			marker.addMarker(res, startLine, msg, severity, varName);
		}
		if (defineCount > 0) {
			// ERROR no matching endef for define.
			int startLine = 0;
			String varName = ""; //$NON-NLS-1$
			int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			for (int i = directives.length - 1; i >= 0; i--) {
				if (directives[i] instanceof IVariableDefinition) {
					IVariableDefinition definition = (IVariableDefinition)directives[i];
					if (definition.isMultiLine()) {
						startLine = definition.getStartLine();
						varName = definition.toString().trim();
						break;
					}
				}
			}
			String msg = MakefileMessages.getString("MakefileValidator.error.noMatchingEndefForOverrideDefine"); //$NON-NLS-1$
			marker.addMarker(res, startLine, msg, severity, varName);
		}
	}

	public void validateCondition(IConditional condition) {
		// Check if the condition are good formats
	}

	public void validateSpecialRule(ISpecialRule rule) {
		// Check for special rules: .POSIX, .IGNORE etc ...
	}
}
