package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * @since 5.3
 */
public class MISharedInfo {

		boolean isread;
		String name;
		private MISharedInfoSegment[] segments;

		public static class MISharedInfoSegment {
			MISharedInfoSegment(String from, String to) {
				this.from = from;
				this.to = to;
			}
			String from;
			String to;
			public String getTo() {
				return to;
			}
			public String getFrom() {
				return from;
			}
		}

		public MISharedInfo (boolean read, String location, MISharedInfoSegment[] segments) {
			isread = read;
			name = location;
			this.segments = segments;
		}

		public MISharedInfoSegment[] getSegments() {
			return segments;
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