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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Comment;
import org.eclipse.cdt.make.internal.core.makefile.EmptyLine;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.Rule;
import org.eclipse.cdt.make.internal.core.makefile.Statement;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;
import org.eclipse.cdt.make.internal.core.makefile.Util;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;

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
		this(new BufferedReader(new FileReader(name)));
	}

	public PosixMakefile(BufferedReader reader) throws IOException {
		super();
		statements = new ArrayList();
		parse(reader);
	}

	void parse(BufferedReader br) throws IOException {
		String line;
		Rule rule = null;
		while ((line = Util.readLine(br)) != null) {
			if (line.length() == 0) {
				// Empty Line.
				statements.add(new EmptyLine());
			} else if (line.startsWith("#")) {
				// Comment.
				statements.add(new Comment(line));
			} else if (line.startsWith("\t")) {
				// Command.
				Command cmd = new Command(line);
				if (rule != null) {
					rule.addCommand(cmd);
				} else {
					throw new IOException("Error Parsing");
				}
			} else if (line.startsWith(".")) {
				// Inference Rule
				String tgt;
				int index = Util.indexOf(line, ':');
				if (index != -1) {
					tgt = line.substring(0, index);
				} else {
					tgt = line;
				}
				rule = new InferenceRule(tgt);
				statements.add(rule);
			} else {
				char[] array = line.toCharArray();
				if (Util.isMacroDefinition(array)) {
					// MacroDefinition
					statements.add(new MacroDefinition(line));
				} else if (Util.isRule(array)) {
					String[] targets;
					String[] reqs = new String[0];
					String cmd = null;
					int index = Util.indexOf(array, ':');
					if (index != -1) {
						String target = line.substring(0, index);
						// Break the targets
						targets = Util.findTargets(target.trim());

						String req = line.substring(index + 1);
						int semicolon = Util.indexOf(req, ';');
						if (semicolon != -1) {
							cmd = req.substring(semicolon + 1);
							req = req.substring(0, semicolon);
						}
						reqs = Util.findPrerequisites(req.trim());
					} else {
						targets = new String[] { line };
					}

					for (int i = 0; i < targets.length; i++) {
						rule = new TargetRule(targets[i], reqs);
						statements.add(rule);
						if (cmd != null) {
							rule.addCommand(new Command(cmd));
						}
					}
				} else {
				}
			}
		}
	}

	public Statement[] getStatements() {
		return (Statement[]) statements.toArray(new Statement[0]);
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
			Statement[] statements = makefile.getStatements();
			for (int i = 0; i < statements.length; i++) {
				//System.out.println("Rule[" + i +"]");
				System.out.print(statements[i]);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
