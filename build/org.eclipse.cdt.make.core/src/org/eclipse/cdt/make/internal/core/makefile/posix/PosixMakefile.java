/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.posix;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile;
import org.eclipse.cdt.make.internal.core.makefile.BadStatement;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Comment;
import org.eclipse.cdt.make.internal.core.makefile.DefaultRule;
import org.eclipse.cdt.make.internal.core.makefile.EmptyLine;
import org.eclipse.cdt.make.internal.core.makefile.IgnoreRule;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.PosixRule;
import org.eclipse.cdt.make.internal.core.makefile.PreciousRule;
import org.eclipse.cdt.make.internal.core.makefile.Rule;
import org.eclipse.cdt.make.internal.core.makefile.SccsGetRule;
import org.eclipse.cdt.make.internal.core.makefile.SilentRule;
import org.eclipse.cdt.make.internal.core.makefile.Statement;
import org.eclipse.cdt.make.internal.core.makefile.SuffixesRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;
import org.eclipse.cdt.make.internal.core.makefile.Util;

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

	List statements;

	public PosixMakefile() {
		statements = new ArrayList();
	}

	public void parse(String name) throws IOException {
		parse(new FileReader(name));
	}

	public void parse(Reader reader) throws IOException {
		parse(new MakefileReader(reader));
	}

	protected void parse(MakefileReader reader) throws IOException {
		String line;
		Rule[] rules = null;
		int startLine = 0;
		int endLine = 0;
		while ((line = reader.readLine()) != null) {
			startLine = endLine + 1;
			endLine = reader.getLineNumber();

			// 1- Try command first, since we can not strip '#' in command line
			if (PosixMakefileUtil.isCommand(line)) {
				Command cmd = createCommand(line);
				cmd.setLines(startLine, endLine);
				// The command is added to the rules
				if (rules != null) {
					for (int i = 0; i < rules.length; i++) {
						rules[i].addStatement(cmd);
						rules[i].setEndLine(endLine);
					}
					continue;
				}
				// If we have no rules for the command,
				// give the other statements a chance by falling through
			}

			// 2- Strip away any comments.
			int pound = Util.indexOfComment(line);
			if (pound != -1) {
				Comment cmt = createComment(line.substring(pound + 1));
				cmt.setLines(startLine, endLine);
				addStatement(cmt);
				line = line.substring(0, pound);
				// If all we have left are spaces continue
				if (Util.isEmptyLine(line)) {
					continue;
				}
				// The rest of the line maybe a valid statement.
				// keep on trying by falling through.
			}

			// 3- Empty lines ?
			if (Util.isEmptyLine(line)) {
				Statement empty = createEmptyLine();
				empty.setLines(startLine, endLine);
				addStatement(empty);
				continue;
			}

			// 4- reset the rules to null
			// The first non empty line that does not begin with a <TAB> or '#'
			// shall begin a new entry.
			rules = null;

			// 5- Check for the special targets.
			if (PosixMakefileUtil.isDefaultRule(line)) {
				DefaultRule dRule = createDefaultRule(line);
				rules = new Rule[] { dRule };
				dRule.setLines(startLine, endLine);
				addStatement(dRule);
				continue;
			} else if (PosixMakefileUtil.isIgnoreRule(line)) {
				IgnoreRule ignore = createIgnoreRule(line);
				ignore.setLines(startLine, endLine);
				addStatement(ignore);
				continue;
			} else if (PosixMakefileUtil.isPosixRule(line)) {
				PosixRule pRule = createPosixRule();
				pRule.setLines(startLine, endLine);
				addStatement(pRule);
				continue;
			} else if (PosixMakefileUtil.isPreciousRule(line)) {
				PreciousRule precious = createPreciousRule(line);
				precious.setLines(startLine, endLine);
				addStatement(precious);
				continue;
			} else if (PosixMakefileUtil.isSccsGetRule(line)) {
				SccsGetRule sccs = createSccsGetRule(line);
				rules = new Rule[] { sccs };
				sccs.setLines(startLine, endLine);
				addStatement(sccs);
				continue;
			} else if (PosixMakefileUtil.isSilentRule(line)) {
				SilentRule silent = createSilentRule(line);
				silent.setLines(startLine, endLine);
				addStatement(silent);
				continue;
			} else if (PosixMakefileUtil.isSuffixesRule(line)) {
				SuffixesRule suffixes = createSuffixesRule(line);
				suffixes.setLines(startLine, endLine);
				addStatement(suffixes);
				continue;
			}

			// 6- Check for inference rule.
			if (PosixMakefileUtil.isInferenceRule(line)) {
				InferenceRule irule = createInferenceRule(line);
				irule.setLines(startLine, endLine);
				addStatement(irule);
				rules = new Rule[]{irule};
				continue;
			}

			// 7- Macro Definiton ?
			if (PosixMakefileUtil.isMacroDefinition(line)) {
				Statement stmt = createMacroDefinition(line);
				stmt.setLines(startLine, endLine);
				addStatement(stmt);
				continue;
			}

			// 8- Target Rule ?
			if (PosixMakefileUtil.isTargetRule(line)) {
				TargetRule[] trules = createTargetRule(line);
				for (int i = 0; i < trules.length; i++) {
					trules[i].setLines(startLine, endLine);
					addStatement(trules[i]);
				}
				rules = trules;
				continue;
			}

			// XXX ?? Should not be here.
			BadStatement stmt = new BadStatement(line);
			stmt.setLines(startLine, endLine);
			addStatement(stmt);
		}
		setLines(1, endLine);
	}

	public IDirective[] getStatements() {
		return (IDirective[]) statements.toArray(new IDirective[0]);
	}

	public IDirective[] getBuiltins() {
		IDirective[] macros = new PosixBuiltinMacroDefinitions().getMacroDefinitions();
		IDirective[] rules = new PosixBuiltinRules().getInferenceRules();
		IDirective[] stmts = new IDirective[macros.length + rules.length];
		System.arraycopy(macros, 0, stmts, 0, macros.length);
		System.arraycopy(rules, 0, stmts, macros.length, rules.length);
		return stmts;
	}

	/**
	 * Comment
	 */
	public Comment createComment(String line) {
		return new Comment(line);
	}

	/**
	 * EmptyLine
	 */
	public EmptyLine createEmptyLine() {
		return new EmptyLine();
	}

	/**
	 * Command
	 */
	public Command createCommand(String line) {
		return new Command(line);
	}

	/**
	 * Inference Rule
	 */
	public InferenceRule createInferenceRule(String line) {
		String tgt;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			tgt = line.substring(0, index);
		} else {
			tgt = line;
		}
		return new InferenceRule(new Target(tgt));
	}

	/**
	 * MacroDefinition
	 */
	public MacroDefinition createMacroDefinition(String line) {
		String name;
		String value;
		int index = Util.indexOf(line, '=');
		if (index != -1) {
			name = line.substring(0, index).trim();
			value = line.substring(index + 1).trim();
		} else {
			name = line;
			value = "";
		}
		return new MacroDefinition(name, value);
	}

	/**
	 * TargetRule
	 */
	public TargetRule[] createTargetRule(String line) {
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
			targetRules[i] = new TargetRule(new Target(targets[i]), reqs);
			if (cmd != null) {
				Command command = createCommand(cmd);
				targetRules[i].addStatement(command);
			}
		}
		return targetRules;
	}

	/**
	 * .DEFAULT
	 */
	public DefaultRule createDefaultRule(String line) {
		int semicolon = Util.indexOf(line, ';');
		if (semicolon > 0) {
			String cmd = line.substring(semicolon + 1).trim();
			if (cmd.length() > 0) {
				ICommand[] cmds = new ICommand[] { new Command(cmd)};
				return new DefaultRule(cmds);
			}
		}
		return new DefaultRule(new ICommand[0]);
	}

	/**
	 * .IGNORE
	 */
	public IgnoreRule createIgnoreRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = PosixMakefileUtil.findPrerequisites(req);
			return new IgnoreRule(reqs);
		}
		return new IgnoreRule(new String[0]);
	}

	/**
	 * .POSIX
	 */
	public PosixRule createPosixRule() {
		return new PosixRule();
	}

	/**
	 * .PRECIOUS
	 */
	public PreciousRule createPreciousRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = PosixMakefileUtil.findPrerequisites(req);
			return new PreciousRule(reqs);
		}
		return new PreciousRule(new String[0]);
	}

	/**
	 * .SCCS_GET
	 */
	public SccsGetRule createSccsGetRule(String line) {
		int semicolon = Util.indexOf(line, ';');
		if (semicolon != -1) {
			String cmd = line.substring(semicolon + 1).trim();
			if (cmd.length() > 0) {
				ICommand[] cmds = new ICommand[] { new Command(cmd)};
				return new SccsGetRule(cmds);
			}
		}
		return new SccsGetRule(new ICommand[0]);
	}

	/**
	 * .SILENT
	 */
	public SilentRule createSilentRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = PosixMakefileUtil.findPrerequisites(req);
			return new SilentRule(reqs);
		}
		return new SilentRule(new String[0]);
	}

	/**
	 * .SUFFIXES
	 */
	public SuffixesRule createSuffixesRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = PosixMakefileUtil.findPrerequisites(req);
			return new SuffixesRule(reqs);
		}
		return new SuffixesRule(new String[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile#addStatement(org.eclipse.cdt.make.core.makefile.IDirective)
	 */
	public void addStatement(IDirective statement) {
		statements.add(statement);
	}

	public static void main(String[] args) {
		try {
			String filename = "Makefile";
			if (args.length == 1) {
				filename = args[0];
			}
			PosixMakefile makefile = new PosixMakefile();
			makefile.parse(filename);
			IDirective[] statements = makefile.getStatements();
			for (int i = 0; i < statements.length; i++) {
				//System.out.println("Rule[" + i +"]");
				System.out.print(statements[i]);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
