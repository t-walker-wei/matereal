/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * An utility class to parse contents of a property file.
 *
 * @author Jun Kato
 * @see java.util.ListResourceBundle
 */
public class StringResourceParser {

	/** Parse a string as contents of a property file. */
	public static String[][] parse(String resources)
	{
		final ArrayList<String[]> list = new ArrayList<String[]>();
		final BufferedReader br = new BufferedReader(new StringReader(resources));
		String line;
		try {
			while ((line = br.readLine()) != null)
			{
				list.add(line.split("=", 2));
			}
		} catch (IOException e) {
			// Do nothing.
		}
		String[][] ret = new String[list.size()][];
		return list.toArray(ret);
	}

	private static final char[] hexChar = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	/** Get \udddd format string from UTF-8 string. */
	public static String escapeUnicode(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >> 7) > 0) {
				sb.append("\\u");
				sb.append(hexChar[(c >> 12) & 0xF]);
				sb.append(hexChar[(c >> 8) & 0xF]);
				sb.append(hexChar[(c >> 4) & 0xF]);
				sb.append(hexChar[c & 0xF]);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
