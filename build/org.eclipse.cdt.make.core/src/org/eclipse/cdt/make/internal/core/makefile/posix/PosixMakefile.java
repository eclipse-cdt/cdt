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

import org.eclipse.cdt.make.core.makefile.IStatement;
import org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile;
import org.eclipse.cdt.make.internal.core.makefile.BadStatement;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Comment;
import org.eclipse.cdt.make.internal.core.makefile.EmptyLine;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.MakefileUtil;
import org.eclipse.cdt.make.internal.core.makefile.Rule;
import org.eclipse.cdt.make.internal.core.makefile.Statement;
import org.eclipse.cdt.make.internal.core.makefile.Target;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public class PosixMakefile extends AbstractMakefile {

	List statements;

	public PosixMakefile(String name) throws IOException {
		this(new FileReader(name));
	}

	public PosixMakefile(Reader reader) throws IOException {
		this(new MakefileReader(reader));
	}

	public PosixMakefile(MakefileReader reader) throws IOException {
		super();
		statements = new ArrayList();
		parse(reader);
	}

	void parse(MakefileReader reader) throws IOException {
		String line;
		Rule rule = null;
		int startLine = 0;
		int endLine = 0;
		while ((line = reader.readLine()) != null) {
			startLine = endLine + 1;
			endLine = reader.getLineNumber();

			// Strip away any comments.
			int pound = MakefileUtil.indexOfComment(line);
			if (pound != -1) {
				// Comment.
				Statement stmt = new Comment(line.substring(pound + 1));
				stmt.setLines(startLine, endLine);
				addStatement(stmt);
				line = line.substring(0, pound);
				// If all we have left are spaces continue
				if (MakefileUtil.isEmptyLine(line)) {
					continue;
				}
			}

			// Empty lines ?
			if (MakefileUtil.isEmptyLine(line)) {
				// Empty Line.
				Statement stmt = new EmptyLine();
				stmt.setLines(startLine, endLine);
				addStatement(stmt);
				continue;
			}

			// Is this a command ?
			if (MakefileUtil.isCommand(line)) {
				Command cmd = new Command(line);
				// The commands are added to a Rule
				if (rule != null) {
					rule.addCommand(cmd);
					rule.setEndLine(endLine);
					continue;
				}
				// If it is not a command give the other a chance a fallthrough
			}

			// Check for inference rule.
			if (MakefileUtil.isInferenceRule(line)) {
				// Inference Rule
				String tgt;
				int index = MakefileUtil.indexOf(line, ':');
				if (index != -1) {
					tgt = line.substring(0, index);
				} else {
					tgt = line;
				}
				rule = new InferenceRule(new Target(tgt));
				rule.setLines(startLine, endLine);
				addStatement(rule);
				continue;
			}

			if (MakefileUtil.isTargetRule(line)) {
				String[] targets;
				String[] reqs = new String[0];
				String cmd = null;
				int index = MakefileUtil.indexOf(line.toCharArray(), ':');
				if (index != -1) {
					String target = line.substring(0, index);
					// Break the targets
					targets = MakefileUtil.findTargets(target.trim());

					String req = line.substring(index + 1);
					int semicolon = MakefileUtil.indexOf(req, ';');
					if (semicolon != -1) {
						cmd = req.substring(semicolon + 1);
						req = req.substring(0, semicolon);
					}
					reqs = MakefileUtil.findPrerequisites(req.trim());
				} else {
					targets = MakefileUtil.findTargets(line);
				}

				Target[] preqs = new Target[reqs.length];
				for (int i = 0; i < reqs.length; i++) {
					preqs[i] = new Target(reqs[i]);
				}
				for (int i = 0; i < targets.length; i++) {
					rule = new TargetRule(new Target(targets[i]), preqs);
					rule.setLines(startLine, endLine);
					addStatement(rule);
					if (cmd != null) {
						rule.addCommand(new Command(cmd));
					}
				}
				continue;
			}

			// Macro Definiton ?
			if (MakefileUtil.isMacroDefinition(line)) {
				// MacroDefinition
				Statement stmt = new MacroDefinition(line);
				stmt.setLines(startLine, endLine);
				addStatement(stmt);
				continue;
			}

			// Should not be here.
			Statement stmt = new BadStatement(line);
			stmt.setLines(startLine, endLine);
			addStatement(stmt);
		}
	}

	public IStatement[] getStatements() {
		return (IStatement[]) statements.toArray(new Statement[0]);
	}

	public IStatement[] getBuiltins() {
		IStatement[] macros = new PosixBuiltinMacroDefinitions().getMacroDefinitions();
		IStatement[] rules = new PosixBuiltinRules().getInferenceRules();
		IStatement[] stmts = new IStatement[macros.length + rules.length];
		System.arraycopy(macros, 0, stmts, 0, macros.length);
		System.arraycopy(rules, 0, stmts, macros.length, rules.length);
		return stmts;
	}

	public void addStatement(Statement stmt) {
		statements.add(stmt);
	}

	public static void main(String[] args) {
		try {
			String filename = "Makefile";
			if (args.length == 1) {
				filename = args[0];
			}
			PosixMakefile makefile = new PosixMakefile(filename);
			IStatement[] statements = makefile.getStatements();
			for (int i = 0; i < statements.length; i++) {
				//System.out.println("Rule[" + i +"]");
				System.out.print(statements[i]);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile#addStatement(org.eclipse.cdt.make.core.makefile.IStatement)
	 */
	public void addStatement(IStatement statement) {
		statements.add(statement);
	}

}
