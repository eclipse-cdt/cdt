/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.mi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIList;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * @author vinod.appu
 *
 */
/**
 * Example output is:
 * 
 * <pre>
 * ^done,symbols={debug=[{filename="../src/main.c",fullname="/home/test/src/main.c",symbols=[{line=\
 * "19",name="a",type="int [3]",description="int a[3];"},{line="20",name="b",type="int [1000]",descript\
 * ion="int b[1000];"}]}]}
 *</pre>
 */
public class MIGlobalVariableInfo extends MIInfo {

	private List<GlobalVariableInfo> globalVariableList = new ArrayList<>();

	/**
	 * 
	 */
	public MIGlobalVariableInfo(MIOutput record) {
		super(record);
		parse();
	}

	private void parse() {

		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] mireslult = rr.getMIResults();
				for (int i = 0; i < mireslult.length; i++) {
					MIValue miValue = mireslult[i].getMIValue();
					parseTable(miValue);
				}
			}
		}
	}

	public static class GlobalVariableInfo {

		public GlobalVariableInfo(String fileName, String fullname, String line, String name, String type,
				String description) {
			this.fileName = fileName;
			this.fullname = fullname;
			this.line = line;
			this.name = name;
			this.type = type;
			this.description = description;
		}

		private String fileName = null;
		private String fullname = null;
		private String line = null;
		private String name = null;
		private String type = null;
		private String description = null;

		public String getFileName() {
			return fileName;
		}

		public String getFullname() {
			return fullname;
		}

		public String getLine() {
			return line;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}
	}

	void parseTable(MIValue val) {
		if (val instanceof MITuple) {
			MIResult[] table = ((MITuple) val).getMIResults();
			for (int j = 0; j < table.length; j++) {
				String variable = table[j].getVariable();
				if (variable.equals("debug")) { //$NON-NLS-1$
					parseBody(table[j].getMIValue());
				}
			}
		}
	}

	void parseBody(MIValue body) {
		if (body instanceof MIList) {
			MIValue[] bkpts = ((MIList) body).getMIValues();
			for (int i = 0; i < bkpts.length; i++) {
				if (bkpts[i] instanceof MITuple) {
					String fileName = null;
					String fullname = null;
					MIList symbols = null;
					MIResult[] table = ((MITuple) bkpts[i]).getMIResults();
					for (int j = 0; j < table.length; j++) {
						String variable = table[j].getVariable();
						if (variable.equalsIgnoreCase("filename")) { //$NON-NLS-1$
							fileName = table[j].getMIValue().toString();
						} else if (variable.equalsIgnoreCase("fullname")) { //$NON-NLS-1$
							fullname = table[j].getMIValue().toString();
						} else if (variable.equalsIgnoreCase("symbols")) { //$NON-NLS-1$
							symbols = (MIList) table[j].getMIValue();
						}
					}
					createMIGlobalVariable(fileName, fullname, symbols);
				}
			}
		}
	}

	private void createMIGlobalVariable(String fileName, String fullname, MIList symbols) {
		if (symbols != null) {
			MIValue[] globals = symbols.getMIValues();
			for (int i = 0; i < globals.length; i++) {
				if (globals[i] instanceof MITuple) {

					// line="19",name="a",type="int [3]",description="int a[3];"
					String line = null;
					String name = null;
					String type = null;
					String description = null;
					MIResult[] table = ((MITuple) globals[i]).getMIResults();
					for (int j = 0; j < table.length; j++) {
						String variable = table[j].getVariable();
						if (variable.equalsIgnoreCase("line")) { //$NON-NLS-1$
							line = table[j].getMIValue().toString();
						} else if (variable.equalsIgnoreCase("name")) { //$NON-NLS-1$
							name = table[j].getMIValue().toString();
						} else if (variable.equalsIgnoreCase("type")) { //$NON-NLS-1$
							type = table[j].getMIValue().toString();
						} else if (variable.equalsIgnoreCase("description")) { //$NON-NLS-1$
							description = table[j].getMIValue().toString();
						}
					}

					getGlobalVariableList().add(new GlobalVariableInfo(fileName, fullname, line, name, type, description));

				}
			}
		}

	}

	public List<GlobalVariableInfo> getGlobalVariableList() {
		return globalVariableList;
	}
}
