package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * @since 5.1
 */
public class MISharedInfo {

		String from;
		String to;
		boolean isread;
		String name;

		public MISharedInfo (String start, String end, boolean read, String location) {
			from = start;
			to = end;
			isread = read;
			name = location;
		}

		public String getFrom() {
			return from;
		}

		public String getTo() {
			return to;
		}

		public boolean isRead() {
			return isread;
		}

		public String getName() {
			return name;
		}

		public void setSymbolsRead(boolean read) {
			isread = read;
		}
	}