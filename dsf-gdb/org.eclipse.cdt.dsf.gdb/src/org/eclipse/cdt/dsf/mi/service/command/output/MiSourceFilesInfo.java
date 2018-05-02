/*******************************************************************************
 * Copyright (c) 2017, 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.LinkedList;
import java.util.List;

/**
 * Example output is:
 *
 * <pre>
 *    (gdb) -file-list-exec-source-files
 *    ^done,files=[{file=foo.c,fullname=/home/foo.c},
 *    {file=/home/bar.c,fullname=/home/bar.c},
 *    {file=gdb_could_not_find_fullpath.c}]
 * </pre>
 *
 * @since 5.5
 */
public class MiSourceFilesInfo extends MIInfo {

	private SourceFileInfo[] sourceFileInfos;

	public MiSourceFilesInfo(MIOutput record) {
		super(record);
		parse();
		if (sourceFileInfos == null) {
			sourceFileInfos = new SourceFileInfo[0];
		}
	}

	/**
	 * Returns array of source files infos
	 *
	 * @return
	 */
	public SourceFileInfo[] getSourceFiles() {
		return sourceFileInfos;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("files")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parseResults((MIList) value);
						}
					}
				}
			}
		}

	}

	private void parseResults(MIList list) {
		MIValue[] miValues = list.getMIValues();
		List<SourceFileInfo> infos = new LinkedList<>();
		if (miValues != null) {
			for (MIValue miValue : miValues) {
				if (miValue instanceof MITuple) {
					MITuple miTuple = (MITuple) miValue;
					SourceFileInfo info = new SourceFileInfo();
					info.parse(miTuple.getMIResults());
					infos.add(info);
				}
			}
		}
		sourceFileInfos = infos.toArray(new SourceFileInfo[infos.size()]);
	}

	public static class SourceFileInfo {
		private String file;
		private String fullname;

		public void setFile(String file) {
			this.file = file;
		}

		public String getFile() {
			return file;
		}

		public void setFullName(String fullname) {
			this.fullname = fullname;
		}

		public String getFullName() {
			return fullname;
		}

		private void parse(MIResult[] results) {
			for (MIResult result : results) {
				String variable = result.getVariable();
				MIValue miVal = result.getMIValue();
				if (!(miVal instanceof MIConst)) {
					continue;
				}
				String value = ((MIConst) miVal).getCString();
				switch (variable) {
				case "file": //$NON-NLS-1$
					file = value;
					break;
				case "fullname": //$NON-NLS-1$
					fullname = value;
					break;
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SourceFileInfo other = (SourceFileInfo) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			if (fullname == null) {
				if (other.fullname != null)
					return false;
			} else if (!fullname.equals(other.fullname))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SourceFileInfo [file=" + file + ", fullname=" + fullname + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	}
}
