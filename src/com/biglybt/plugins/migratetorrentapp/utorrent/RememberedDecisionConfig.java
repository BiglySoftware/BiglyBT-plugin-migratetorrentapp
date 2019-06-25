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

package com.biglybt.plugins.migratetorrentapp.utorrent;

public class RememberedDecisionConfig
	implements ConfigMigrateItem
{

	public static int count = 0;

	final String utKey;

	final Object utValue;

	final String rememberID;

	final int value;

	public RememberedDecisionConfig(String utKey, Object utValue,
			String rememberID, int value) {
		this.utKey = utKey;
		this.utValue = utValue;
		this.rememberID = rememberID;
		this.value = value;
		count++;
	}

	@Override
	public StringBuilder toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append(utKey).append("(").append(utValue).append(
				") -> RememberID:").append(rememberID).append("(").append(value).append(
						")\n");
		return sb;
	}
}
