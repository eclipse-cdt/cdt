/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Bug 338936
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.posix;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile;
import org.eclipse.cdt.make.internal.core.makefile.BadDirective;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Comment;
import org.eclipse.cdt.make.internal.core.makefile.DefaultRule;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.EmptyLine;
import org.eclipse.cdt.make.internal.core.makefile.IgnoreRule;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;
import org.eclipse.cdt.make.internal.core.makefile.MakeFileConstants;
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.PosixRule;
import org.eclipse.cdt.make.internal.core.makefile.PreciousRule;
import org.eclipse.cdt.make.internal.core.makefile.Rule;
import org.eclipse.cdt.make.internal.core.makefile.SccsGetRule;
import org.eclipse.cdt.make.internal.core.makefile.SilentRule;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.SuffixesRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;
import org.eclipse.cdt.make.internal.core.makefile.Util;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule | special_rule
 * inference_rule : target ':' [ ';' command ] <nl>
		 [ ( command ) * ]
 * target_rule : [ ( target ) + ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl>
                 [ ( command ) *  ]
 * macro_definition : string '=' ( string )*
 * comments : ('#' ( string ) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%"
 */

public class PosixMakefile extends AbstractMakefile {
	private IDirective[] builtins = new IDirective[0];
	private IAutomaticVariable[] automaticVariables = new IAutomaticVariable[0];
	private IMakefileReaderProvider makefileReaderProvider;

	public PosixMakefile() {
		super(null);
	}

	@Override
	public IMakefileReaderProvider getMakefileReaderProvider() {
		return makefileReaderProvider;
	}

	@Override
	public void parse(String name, Reader reader) throws IOException {
		parse(URIUtil.toURI(name), new MakefileReader(reader));
	}

	@Override
	public void parse(URI fileURI, IMakefileReaderProvider makefileReaderProvider) throws IOException {
		this.makefileReaderProvider = makefileReaderProvider;
		MakefileReader reader;
		if (makefileReaderProvider == null) {
			try {
				reader = new MakefileReader(
						new InputStreamReader(EFS.getStore(fileURI).openInputStream(EFS.NONE, null)));
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				throw new IOException(e.getMessage());
			}
		} else {
			reader = new MakefileReader(makefileReaderProvider.getReader(fileURI));
		}
		parse(fileURI, reader);
	}

	@Override
	public void parse(URI fileURI, Reader reader) throws IOException {
		parse(fileURI, new MakefileReader(reader));
	}

	protected void parse(URI fileURI, MakefileReader reader) throws IOException {
		String line;
		Rule[] rules = null;
		int startLine = 0;
		int endLine = 0;

		// Clear any old directives.
		clearDirectives();

		setFileURI(fileURI);

		try {
			while ((line = reader.readLine()) != null) {
				startLine = endLine + 1;
				endLine = reader.getLineNumber();

				// 1- Try command first, since we can not strip '#' in command line
				if (PosixMakefileUtil.isCommand(line)) {
					Command cmd = new Command(this, line);
					cmd.setLines(startLine, endLine);
					// The command is added to the rules
					if (rules != null) {
						for (Rule rule : rules) {
							rule.addDirective(cmd);
							rule.setEndLine(endLine);
						}
						continue;
					}
					// If we have no rules for the command,
					// give the other directives a chance by falling through
				}

				// 2- Strip away any comments.
				int pound = Util.indexOfComment(line);
				if (pound != -1) {
					Comment cmt = new Comment(this, line.substring(pound + 1));
					cmt.setLines(startLine, endLine);
					if (rules != null) {
						for (Rule rule : rules) {
							rule.addDirective(cmt);
							rule.setEndLine(endLine);
						}
					} else {
						addDirective(cmt);
					}
					line = line.substring(0, pound);
					// If all we have left are spaces continue
					if (Util.isEmptyLine(line)) {
						continue;
					}
					// The rest of the line maybe a valid directive.
					// keep on trying by falling through.
				}

				// 3- Empty lines ?
				if (Util.isEmptyLine(line)) {
					Directive empty = new EmptyLine(this);
					empty.setLines(startLine, endLine);
					if (rules != null) {
						for (Rule rule : rules) {
							rule.addDirective(empty);
							rule.setEndLine(endLine);
						}
					} else {
						addDirective(empty);
					}
					continue;
				}

				// 4- reset the rules to null
				// The first non empty line that does not begin with a <TAB> or '#'
				// shall begin a new entry.
				rules = null;

				// 5- Check for the special rules.
				SpecialRule special = processSpecialRule(line);
				if (special != null) {
					rules = new Rule[] { special };
					special.setLines(startLine, endLine);
					addDirective(special);
					continue;
				}

				// 6- Check for inference rule.
				if (PosixMakefileUtil.isInferenceRule(line)) {
					InferenceRule irule = parseInferenceRule(line);
					irule.setLines(startLine, endLine);
					addDirective(irule);
					rules = new Rule[] { irule };
					continue;
				}

				// 7- Macro Definiton ?
				if (PosixMakefileUtil.isMacroDefinition(line)) {
					Directive stmt = parseMacroDefinition(line);
					stmt.setLines(startLine, endLine);
					addDirective(stmt);
					continue;
				}

				// 8- Target Rule ?
				if (PosixMakefileUtil.isTargetRule(line)) {
					TargetRule[] trules = parseTargetRule(line);
					for (TargetRule trule : trules) {
						trule.setLines(startLine, endLine);
						addDirective(trule);
					}
					rules = trules;
					continue;
				}

				// XXX ?? Should not be here.
				BadDirective stmt = new BadDirective(this, line);
				stmt.setLines(startLine, endLine);
				addDirective(stmt);
			}
			setLines(1, endLine);
		} finally {
			reader.close();
		}
	}

	@Override
	public IDirective[] getBuiltins() {
		return builtins;
	}

	@Override
	public IAutomaticVariable[] getAutomaticVariables() {
		return automaticVariables;
	}

	protected SpecialRule processSpecialRule(String line) {
		line = line.trim();
		String keyword = null;
		String[] reqs = null;
		SpecialRule special = null;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			keyword = line.substring(0, index).trim();
			String req = line.substring(index + 1);
			reqs = PosixMakefileUtil.findPrerequisites(req);
		} else {
			keyword = line;
			reqs = new String[0];
		}
		if (keyword.equals(MakeFileConstants.RULE_IGNORE)) {
			special = new IgnoreRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_POSIX)) {
			special = new PosixRule(this);
		} else if (keyword.equals(MakeFileConstants.RULE_PRECIOUS)) {
			special = new PreciousRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_SILENT)) {
			special = new SilentRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_SUFFIXES)) {
			special = new SuffixesRule(this, reqs);
		} else if (keyword.equals(MakeFileConstants.RULE_DEFAULT)) {
			special = new DefaultRule(this, new Command[0]);
		} else if (keyword.equals(MakeFileConstants.RULE_SCCS_GET)) {
			special = new SccsGetRule(this, new Command[0]);
		}
		return special;
	}

	/**
	 * Inference Rule
	 */
	protected InferenceRule parseInferenceRule(String line) {
		String tgt;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			tgt = line.substring(0, index);
		} else {
			tgt = line;
		}
		return new InferenceRule(this, new Target(tgt));
	}

	/**
	 * MacroDefinition
	 */
	protected MacroDefinition parseMacroDefinition(String line) {
		String name;
		String value;
		int index = Util.indexOf(line, '=');
		if (index != -1) {
			name = line.substring(0, index).trim();
			value = line.substring(index + 1).trim();
		} else {
			name = line;
			value = ""; //$NON-NLS-1$
		}
		return new MacroDefinition(this, name, new StringBuffer(value));
	}

	/**
	 * TargetRule
	 */
	protected TargetRule[] parseTargetRule(String line) {
		String[] targets;
		String[] reqs;
		String cmd = null;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String target = line.substring(0, index);
			// Tokenize the targets
			targets = PosixMakefileUtil.findTargets(target);

			String req = line.substring(index + 1);
			int semicolon = Util.indexOf(req, ';');
			if (semicolon != -1) {
				String c = req.substring(semicolon + 1).trim();
				if (c.length() > 0) {
					cmd = c;
				}
				req = req.substring(0, semicolon);
			}
			reqs = PosixMakefileUtil.findPrerequisites(req);
		} else {
			targets = PosixMakefileUtil.findTargets(line);
			reqs = new String[0];
		}

		TargetRule[] targetRules = new TargetRule[targets.length];
		for (int i = 0; i < targets.length; i++) {
			targetRules[i] = new TargetRule(this, new Target(targets[i]), reqs);
			if (cmd != null) {
				Command command = new Command(this, cmd);
				targetRules[i].addDirective(command);
			}
		}
		return targetRules;
	}

}
