/* MI

   (c) 2002 Copyright RedHat Inc

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -target-download
 *
 *  Load the executable to the remote target.  This command takes no args.
 *
 *
 *   Loads the executable onto the remote target. It prints out an
 *   update message every half second, which includes the fields:
 * 
 *  +download,{section=".text",section-size="6668",total-size="9880"}
 *  +download,{section=".text",section-sent="512",section-size="6668",
 *  total-sent="512",total-size="9880"}
 * 
 */
public class MITargetDownload extends MICommand 
{
	public MITargetDownload() {
		super("-target-download");
	}
}
