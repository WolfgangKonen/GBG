/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package agentIO;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * Filter Files with predefined extension for the File-chooser
 * 
 * @author Oracle and/or its affiliates, changed by Markus Thill
 * 
 */
public class ExtensionFilter extends FileFilter {

	private String acceptExtension;
	private String description;

	public ExtensionFilter(String extension, String description) {
		this.acceptExtension = extension;
		this.description = description;
	}

	// Accept all directories and all agt files.
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		return Utils.containsExtension(f, acceptExtension);
	}

	// The description of this filter
	public String getDescription() {
		return this.description;
	}
}

class Utils {
	/*
	 * Get the extension of a file. This is everything after the first
	 * occurrence of a point...
	 */
	public static boolean containsExtension(File f, String extension) {
		//String ext = null;
		String s = f.getName();
		int i = s.indexOf(extension);
		return i != -1;
		
//		int i = s.indexOf('.');
//		if (i > 0 && i < s.length() - 1) {
//			ext = s.substring(i + 1).toLowerCase();
//		}
//
//		// if (i1 > 0) {
//		// String s1 = s.substring(0, i1);
//		// int i = s1.lastIndexOf('.');
//		//
//		// if (i > 0 && i < s.length() - 1) {
//		// ext = s.substring(i + 1).toLowerCase();
//		// }
//		// }
//		return ext;
	}
}
