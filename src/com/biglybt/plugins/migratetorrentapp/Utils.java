/*
 * Copyright (C) Bigly Software.  All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.plugins.migratetorrentapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gudy.bouncycastle.util.encoders.Base64;

import com.biglybt.core.util.Constants;
import com.biglybt.core.util.Debug;
import com.biglybt.core.util.RandomUtils;

public class Utils
{
	public static final String NL = System.lineSeparator();

	public static final String[] PRIVWRAP = {
		"<private>",
		"</private>"
	};

	public static final Pattern PAT_PRIVWRAP = Pattern.compile(
			PRIVWRAP[0] + "((?:(?!" + PRIVWRAP[0] + ").)*?)" + PRIVWRAP[1]);

	public static final String SALT = RandomUtils.generateRandomAlphanumerics(5);

	public static void wrapString(StringBuilder sb, String s) {
		sb.append(PRIVWRAP[0]);
		sb.append(s);
		sb.append(PRIVWRAP[1]);
	}

	public static String wrapString(String s) {
		return PRIVWRAP[0] + s + PRIVWRAP[1];
	}

	public static String objectToString(Object o1) {
		if (o1 instanceof byte[]) {
			return new String((byte[]) o1, Constants.UTF_8);
		} else if (o1 instanceof String) {
			return (String) o1;
		}
		return null;
	}

	public static String wrapSubString(String s, String substring) {
		if (substring == null) {
			return s;
		}
		return s.replaceAll("\\Q" + substring + "\\E",
				Matcher.quoteReplacement(PRIVWRAP[0] + substring + PRIVWRAP[1]));
	}

	public static String hidePrivate(String s) {
		boolean loop;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ignored) {
		}
		do {
			StringBuilder sb = new StringBuilder();
			Matcher matcher = Utils.PAT_PRIVWRAP.matcher(s);
			loop = false;
			int lastPos = 0;
			while (matcher.find()) {
				loop = true;
				sb.append(s, lastPos, matcher.start());
				String match = matcher.group(1) + Utils.SALT;
				String secret = Integer.toHexString(md == null ? match.hashCode()
						: new String(Base64.encode(md.digest(match.getBytes())),
								Constants.UTF_8).hashCode());
				sb.append("<Secret").append(secret).append(">");
				lastPos = matcher.end();
			}
			sb.append(s, lastPos, s.length());
			s = sb.toString();
		} while (loop);
		return s;
	}

	public static String getErrorAndHideStuff(Throwable t, String... hideStrings) {
		String s = Debug.getNestedExceptionMessageAndStack(t);
		if (hideStrings == null || hideStrings.length == 0) {
			return s;
		}
		for (String hideString : hideStrings) {
			s = wrapSubString(s, hideString);
		}
		return s;
	}
}
