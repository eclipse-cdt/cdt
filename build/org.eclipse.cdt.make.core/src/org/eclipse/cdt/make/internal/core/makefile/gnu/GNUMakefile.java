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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

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
import org.eclipse.cdt.make.internal.core.makefile.MakefileReader;
import org.eclipse.cdt.make.internal.core.makefile.Parent;
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
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixBuiltinMacroDefinitions;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixBuiltinRules;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefileUtil;

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

public class GNUMakefile extends AbstractMakefile {

	public static String PATH_SEPARATOR = System.getProperty("path.separator", ":");
	public static String FILE_SEPARATOR = System.getProperty("file.separator", "/");

	String[] includeDirectories = new String[0];

	public GNUMakefile() {
		super();
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
		Stack conditions = new Stack();
		Stack defines = new Stack();
		int startLine = 0;
		int endLine = 0;

		// Clear any old statements.
		clearStatements();

		while ((line = reader.readLine()) != null) {
			startLine = endLine + 1;
			endLine = reader.getLineNumber();

			// Check if we enter in "define"
			if (GNUMakefileUtil.isEndef(line)) {
				// We should have a "define" for a "endef".
				if (!defines.empty()) {
					VariableDefinition def = (VariableDefinition) defines.pop();
					def.setEndLine(endLine);
				}
				Endef endef = createEndef();
				endef.setLines(startLine, endLine);
				addStatement(conditions, endef);
				continue;
			} else if (GNUMakefileUtil.isDefine(line)) {
				VariableDefinition def = createVariableDefinition(line);
				def.setLines(startLine, endLine);
				addStatement(conditions, def);
				defines.push(def);
				continue;
			} else if (GNUMakefileUtil.isOverrideDefine(line)) {
				VariableDefinition oDef = createVariableDefinition(line);
				oDef.setLines(startLine, endLine);
				addStatement(conditions, oDef);
				defines.push(oDef);
				continue;
			}

			// We still in a define.
			if (!defines.empty()) {
				VariableDefinition def = (VariableDefinition) defines.peek();
				StringBuffer sb = def.getValue();
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(line);
				continue;
			}

			// 1- Try command first, since we can not strip '#' in command line
			if (GNUMakefileUtil.isCommand(line)) {
				Command cmd = createCommand(line);
				cmd.setLines(startLine, endLine);
				if (!conditions.empty()) {
					addStatement(conditions, cmd);
					continue;
				} else if (rules != null) {
					// The command is added to the rules
					for (int i = 0; i < rules.length; i++) {
						rules[i].addStatement(cmd);
						rules[i].setEndLine(endLine);
					}
					continue;
				}
				// If we have no rules/condition for the command,
				// give the other statements a chance by falling through
			}

			// 2- Strip away any comments.
			int pound = Util.indexOfComment(line);
			if (pound != -1) {
				Comment cmt = createComment(line.substring(pound + 1));
				cmt.setLines(startLine, endLine);
				addStatement(conditions, cmt);
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
				addStatement(conditions, empty);
				continue;
			}

			// 4- reset the rules to null
			// The first non empty line that does not begin with a <TAB> or '#'
			// shall begin a new entry.
			rules = null;

			if (GNUMakefileUtil.isElse(line)) {
				Statement elseDirective = createConditional(line);
				elseDirective.setLines(startLine, endLine);
				// Are we missing a if condition ?
				if (!conditions.empty()) {
					Statement cond = (Statement) conditions.pop();
					cond.setEndLine(endLine - 1);
				}
				addStatement(conditions, elseDirective);
				conditions.push(elseDirective);
				continue;
			} else if (GNUMakefileUtil.isEndif(line)) {
				Conditional endif = createConditional(line);
				endif.setLines(startLine, endLine);
				// Are we missing a if/else condition ?
				if (!conditions.empty()) {
					Statement cond = (Statement) conditions.pop();
					cond.setEndLine(endLine - 1);
				}
				addStatement(conditions, endif);
				continue;
			}

			// 5- Check for the conditionnals.
			Statement statement = processConditions(line);
			if (statement != null) {
				statement.setLines(startLine, endLine);
				addStatement(conditions, statement);
				conditions.push(statement);
				continue;
			}

			// 6- Check for other special gnu directives.
			statement = processGNUDirectives(line);
			if (statement != null) {
				statement.setLines(startLine, endLine);
				addStatement(conditions, statement);
				continue;
			}

			// 7- Check for GNU special rules.
			Rule rule = processSpecialRules(line);
			if (rule != null) {
				rules = new Rule[] { rule };
				rule.setLines(startLine, endLine);
				addStatement(conditions, rule);
				continue;
			}

			// - Check for inference rule.
			if (GNUMakefileUtil.isInferenceRule(line)) {
				InferenceRule irule = createInferenceRule(line);
				irule.setLines(startLine, endLine);
				addStatement(conditions, irule);
				rules = new Rule[] { irule };
				continue;
			}

			// - Variable Definiton ?
			if (GNUMakefileUtil.isVariableDefinition(line)) {
				Statement stmt = createVariableDefinition(line);
				stmt.setLines(startLine, endLine);
				addStatement(conditions, stmt);
				continue;
			}

			if (GNUMakefileUtil.isStaticTargetRule(line)) {
				StaticTargetRule[] srules = createStaticTargetRule(line);
				for (int i = 0; i < srules.length; i++) {
					srules[i].setLines(startLine, endLine);
					addStatement(conditions, srules[i]);
				}
				rules = srules;
				continue;
			}

			// - Target Rule ?
			if (GNUMakefileUtil.isGNUTargetRule(line)) {
				TargetRule[] trules = createGNUTargetRules(line);
				for (int i = 0; i < trules.length; i++) {
					trules[i].setLines(startLine, endLine);
					addStatement(conditions, trules[i]);
				}
				rules = trules;
				continue;
			}

			// XXX ?? Should not be here.
			BadStatement stmt = new BadStatement(line);
			stmt.setLines(startLine, endLine);
			addStatement(conditions, stmt);

		}
		setLines(1, endLine);
	}

	protected void addStatement(Stack conditions, IDirective statement) {
		if (conditions.empty()) {
			addStatement(statement);
		} else {
			Parent p = (Parent) conditions.peek();
			p.addStatement(statement);
			p.setEndLine(statement.getEndLine());
		}
	}

	protected Statement processConditions(String line) {
		Statement stmt = null;
		if (GNUMakefileUtil.isIfdef(line)) {
			stmt = createConditional(line);
		} else if (GNUMakefileUtil.isIfndef(line)) {
			stmt = createConditional(line);
		} else if (GNUMakefileUtil.isIfeq(line)) {
			stmt = createConditional(line);
		} else if (GNUMakefileUtil.isIfneq(line)) {
			stmt = createConditional(line);
		}
		return stmt;
	}

	protected Statement processGNUDirectives(String line) {
		Statement stmt = null;
		if (GNUMakefileUtil.isUnExport(line)) {
			stmt = createUnExport(line);
		} else if (GNUMakefileUtil.isVPath(line)) {
			stmt = createVPath(line);
		} else if (GNUMakefileUtil.isInclude(line)) {
			stmt = createInclude(line);
		}
		return stmt;
	}

	protected Rule processSpecialRules(String line) {
		Rule stmt = null;
		if (GNUMakefileUtil.isIgnoreRule(line)) {
			stmt = createIgnoreRule(line);
		} else if (GNUMakefileUtil.isPosixRule(line)) {
			stmt = createPosixRule();
		} else if (GNUMakefileUtil.isPreciousRule(line)) {
			stmt = createPreciousRule(line);
		} else if (GNUMakefileUtil.isSilentRule(line)) {
			stmt = createSilentRule(line);
		} else if (GNUMakefileUtil.isSuffixesRule(line)) {
			stmt = createSuffixesRule(line);
		} else if (GNUMakefileUtil.isDefaultRule(line)) {
			stmt = createDefaultRule(line);
		} else if (GNUMakefileUtil.isSccsGetRule(line)) {
			stmt = createSccsGetRule(line);
		}
		return stmt;
	}

	/**
	 * @param line
	 * @return
	 */
	protected Rule createSuffixesRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = PosixMakefileUtil.findPrerequisites(req);
			return new SuffixesRule(reqs);
		}
		return new SuffixesRule(new String[0]);
	}

	/**
	 * @param line
	 * @return
	 */
	protected Rule createSilentRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = GNUMakefileUtil.findPrerequisites(req);
			return new SilentRule(reqs);
		}
		return new SilentRule(new String[0]);
	}

	/**
	 * @param line
	 * @return
	 */
	protected Rule createPreciousRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = GNUMakefileUtil.findPrerequisites(req);
			return new PreciousRule(reqs);
		}
		return new PreciousRule(new String[0]);
	}

	/**
	 * @param line
	 * @return
	 */
	protected Rule createIgnoreRule(String line) {
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String req = line.substring(index + 1);
			String[] reqs = GNUMakefileUtil.findPrerequisites(req);
			return new IgnoreRule(reqs);
		}
		return new IgnoreRule(new String[0]);
	}

	/**
	 *
	 * ifdef CONDITIONAL
	 * ifeq CONDITIONAL
	 * ifneq CONDITIONAL
	 * else
	 * endif
	 *
	 * @param line
	 * @return
	 */
	public Conditional createConditional(String line) {
		Conditional condition = null;
		line = line.trim();
		String keyword = null;
		// Move pass the keyword
		for (int i = 0; i < line.length(); i++) {
			if (Util.isSpace(line.charAt(i))) {
				keyword = line.substring(0, i);
				line = line.substring(i).trim();
				break;
			}
		}
		if (keyword == null) {
			keyword = line;
		}
		if (keyword.equals("ifdef")) {
			condition = new Ifdef(line);
		} else if (keyword.equals("ifndef")) {
			condition = new Ifndef(line);
		} else if (keyword.equals("ifeq")) {
			condition = new Ifeq(line);
		} else if (keyword.equals("ifneq")) {
			condition = new Ifneq(line);
		} else if (keyword.equals("else")) {
			condition = new Else();
		} else if (keyword.equals("endif")) {
			condition = new Endif();
		}
		return condition;
	}

	/**
	 *  Format of the include directive:
	 *      include filename1 filename2 ...
	 */
	protected Include createInclude(String line) {
		String[] filenames;
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		if (count > 0) {
			filenames = new String[count - 1];
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					st.nextToken();
					// ignore the "include" keyword.
					continue;
				}
				filenames[i - 1] = st.nextToken();
			}
		} else {
			filenames = new String[0];
		}
		return new Include(filenames, getIncludeDirectories());
	}

	/**
	   * There are three forms of the "vpath" directive:
	   *      "vpath PATTERN DIRECTORIES"
	   * Specify the search path DIRECTORIES for file names that match PATTERN.
	   *
	   * The search path, DIRECTORIES, is a list of directories to be
	   * searched, separated by colons (semi-colons on MS-DOS and
	   * MS-Windows) or blanks, just like the search path used in the `VPATH' variable.
	   *
	   *      "vpath PATTERN"
	   * Clear out the search path associated with PATTERN.
	   *
	   *      "vpath"
	   * Clear all search paths previously specified with `vpath' directives.
	   */
	public VPath createVPath(String line) {
		String pattern = null;
		String[] directories;
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		List dirs = new ArrayList(count);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (count == 0) {
					// ignore the "vpath" directive
					st.nextToken();
				} else if (count == 1) {
					pattern = st.nextToken();
				} else if (count == 3) {
					String delim = " \t\n\r\f" + GNUMakefile.PATH_SEPARATOR;
					dirs.add(st.nextToken(delim));
				} else {
					dirs.add(st.nextToken());
				}
			}
		}
		directories = (String[]) dirs.toArray(new String[0]);
		if (pattern == null) {
			pattern = new String();
		}
		return new VPath(pattern, directories);
	}

	public Endef createEndef() {
		return new Endef();
	}

	/**
	 * @param line
	 * @return
	 */
	protected UnExport createUnExport(String line) {
		// Pass over "unexport"
		for (int i = 0; i < line.length(); i++) {
			if (Util.isSpace(line.charAt(i))) {
				line = line.substring(i).trim();
				break;
			}
		}
		return new UnExport(line);
	}

	protected Command createCommand(String line) {
		return new Command(line);
	}

	protected Comment createComment(String line) {
		return new Comment(line);
	}

	protected EmptyLine createEmptyLine() {
		return new EmptyLine();
	}

	protected InferenceRule[] createInferenceRules(String line) {
		// Inference Rule
		String tgt;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			tgt = line.substring(0, index);
		} else {
			tgt = line;
		}
		return new InferenceRule[] { new InferenceRule(new Target(tgt))};
	}

	protected GNUTargetRule[] createGNUTargetRules(String line) {
		String[] targetNames;
		String[] normalReqs;
		String[] orderReqs;
		String cmd = null;
		boolean doubleColon = false;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			// Break the targets
			String target = line.substring(0, index);
			targetNames = GNUMakefileUtil.findTargets(target.trim());

			// Some TargetRule have "::" for separator
			String req = line.substring(index + 1);
			doubleColon = req.startsWith(":");
			if (doubleColon) {
				// move pass the second ':'
				req = req.substring(1);
			}

			// Check for command
			int semicolon = Util.indexOf(req, ';');
			if (semicolon != -1) {
				cmd = req.substring(semicolon + 1);
				req = req.substring(0, semicolon);
			}

			// Check for Normal and order prerequisites
			String normalReq = null;
			String orderReq = null;
			int pipe = Util.indexOf(req, '|');
			if (pipe != -1) {
				normalReq = req.substring(0, pipe);
				orderReq = req.substring(pipe + 1);
			} else {
				normalReq = req;
				orderReq = "";
			}

			normalReqs = GNUMakefileUtil.findPrerequisites(normalReq.trim());
			orderReqs = GNUMakefileUtil.findPrerequisites(orderReq.trim());
		} else {
			targetNames = GNUMakefileUtil.findTargets(line);
			normalReqs = new String[0];
			orderReqs = new String[0];
		}

		GNUTargetRule[] rules = new GNUTargetRule[targetNames.length];
		for (int i = 0; i < targetNames.length; i++) {
			rules[i] = new GNUTargetRule(createTarget(targetNames[i]), doubleColon, normalReqs, orderReqs, new ICommand[0]);
			if (cmd != null) {
				rules[i].addStatement(createCommand(cmd));
			}
		}
		return rules;
	}

	protected VariableDefinition createVariableDefinition(String line) {
		line = line.trim();
		VariableDefinition vd;

		// the default type.
		int type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
		boolean isDefine = false;
		boolean isOverride = false;
		boolean isTargetVariable = false;
		boolean isExport = false;
		String targetName = "";

		String name;
		StringBuffer value = new StringBuffer();

		// Check for Target: Variable-assignment
		if (GNUMakefileUtil.isTargetVariable(line)) {
			// move to the first ':'
			int colon = Util.indexOf(line, ':');
			if (colon != -1) {
				targetName = line.substring(0, colon).trim();
				line = line.substring(colon + 1).trim();
			} else {
				targetName = "";
			}
		}

		// Check for Override condition.
		if (GNUMakefileUtil.isOverride(line)) {
			isOverride = true;
			// Move pass the keyword override.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for "define"
		if (GNUMakefileUtil.isOverrideDefine(line)) {
			isDefine = true;
			// Move pass the keyword define.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for Override condition.
		if (GNUMakefileUtil.isExport(line)) {
			isExport = true;
			// Move pass the keyword export.
			for (int i = 0; i < line.length(); i++) {
				if (Util.isSpace(line.charAt(i))) {
					line = line.substring(i).trim();
					break;
				}
			}
		}

		// Check for Target-variable

		int index = line.indexOf('=');
		if (index != -1) {
			int separator = index;
			// Check for "+=",  ":=", "?="
			if (index > 0) {
				type = line.charAt(index - 1);
				if (type == VariableDefinition.TYPE_SIMPLE_EXPAND
					|| type == VariableDefinition.TYPE_APPEND
					|| type == VariableDefinition.TYPE_CONDITIONAL) {
					separator = index - 1;
				} else {
					type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
				}
			}
			name = line.substring(0, separator).trim();
			value.append(line.substring(index + 1).trim());
		} else {
			name = line;
		}

		if (isTargetVariable) {
			vd = new TargetVariable(targetName, name, value, isOverride, type);
		}
		if (isOverride && isDefine) {
			vd = new OverrideDefine(name, value);
		} else if (isDefine) {
			vd = new DefineVariable(name, value);
		} else if (isOverride) {
			vd = new OverrideVariable(name, value, type);
		} else if (isExport) {
			vd = new ExportVariable(name, value, type);
		} else {
			vd = new VariableDefinition(name, value, type);
		}
		return vd;
	}

	protected Target createTarget(String line) {
		return new Target(line);
	}

	protected StaticTargetRule[] createStaticTargetRule(String line) {
		// first colon: the Targets
		String targetPattern;
		String[] prereqPatterns;
		String[] targets;
		int colon = Util.indexOf(line, ':');
		if (colon > 1) {
			String targetLine = line.substring(0, colon).trim();
			targets = GNUMakefileUtil.findTargets(targetLine);
			// second colon: Target-Pattern
			line = line.substring(colon + 1);
			colon = Util.indexOf(line, ':');
			if (colon != -1) {
				targetPattern = line.substring(0, colon).trim();
				line = line.substring(colon + 1);
				StringTokenizer st = new StringTokenizer(line);
				int count = st.countTokens();
				prereqPatterns = new String[count];
				for (int i = 0; i < count; i++) {
					prereqPatterns[i] = st.nextToken();
				}
			} else {
				targetPattern = "";
				prereqPatterns = new String[0];
			}
		} else {
			targets = new String[0];
			targetPattern = "";
			prereqPatterns = new String[0];
		}

		StaticTargetRule[] staticRules = new StaticTargetRule[targets.length];
		for (int i = 0; i < targets.length; i++) {
			staticRules[i] = new StaticTargetRule(createTarget(targets[i]), targetPattern, prereqPatterns, new ICommand[0]);
		}
		return staticRules;
	}

	/**
	 * @param line
	 * @return
	 */
	private TargetRule[] createTargetRule(String line) {
		String[] targets;
		String[] reqs;
		String cmd = null;
		int index = Util.indexOf(line, ':');
		if (index != -1) {
			String target = line.substring(0, index);
			// Tokenize the targets
			targets = GNUMakefileUtil.findTargets(target);

			String req = line.substring(index + 1);
			int semicolon = Util.indexOf(req, ';');
			if (semicolon != -1) {
				String c = req.substring(semicolon + 1).trim();
				if (c.length() > 0) {
					cmd = c;
				}
				req = req.substring(0, semicolon);
			}
			reqs = GNUMakefileUtil.findPrerequisites(req);
		} else {
			targets = GNUMakefileUtil.findTargets(line);
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
	 * @param line
	 * @return
	 */
	private InferenceRule createInferenceRule(String line) {
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
	 * @param line
	 * @return
	 */
	private SccsGetRule createSccsGetRule(String line) {
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
	 * @param line
	 * @return
	 */
	private DefaultRule createDefaultRule(String line) {
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
	 * @return
	 */
	private PosixRule createPosixRule() {
		return new PosixRule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.makefile.AbstractMakefile#getBuiltins()
	 */
	public IDirective[] getBuiltins() {
		IDirective[] macros = new PosixBuiltinMacroDefinitions().getMacroDefinitions();
		IDirective[] rules = new PosixBuiltinRules().getInferenceRules();
		IDirective[] stmts = new IDirective[macros.length + rules.length];
		System.arraycopy(macros, 0, stmts, 0, macros.length);
		System.arraycopy(rules, 0, stmts, macros.length, rules.length);
		return stmts;
	}

	public void setIncludeDirectories(String[] dirs) {
		includeDirectories = dirs;
	}

	public String[] getIncludeDirectories() {
		return includeDirectories;
	}

	public static void main(String[] args) {
		try {
			String filename = "Makefile";
			if (args.length == 1) {
				filename = args[0];
			}
			GNUMakefile makefile = new GNUMakefile();
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
