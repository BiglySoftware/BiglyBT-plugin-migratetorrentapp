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

package com.biglybt.plugins.migratetorrentapp;import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TuxTest {
	public static void main(String[] args) {
		System.setProperty("transitory.startup", "1");
		//0x00000000000000000000FFFFC0A8B680
		byte[] b = {
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0, //
			(byte) 0xff,
			(byte) 0xff,
			(byte) 0xc0,
			(byte) 0xa8,
			(byte) 0xb6,
			(byte) 0x80
		};
		try {
			InetAddress byAddress = Inet6Address.getByAddress(b);
			System.out.println(byAddress.toString());
			System.out.println(byAddress.getHostAddress());
			System.out.println(byAddress.getClass());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte[] p = {
			(byte) 0xE3,
			(byte) 0xB0
		};
		long l = ((p[0] & 0xff) << 8) | (p[1] & 0xff);
		System.out.println(l);
	}

	private static String getListTypeName(List list, String typeName) {
		if (!typeName.isEmpty()) {
			typeName += " ";
		}
		if (list.isEmpty()) {
			typeName += "list";
		} else {
			typeName += "List of ";
			List<String> listSubTypes = new ArrayList<>();
			for (Object val : list) {
				String name;
				if (val instanceof List) {
					name = getListTypeName((List) val, "");
				} else {
					name = val.getClass() != null ? val.getClass().getSimpleName() : "??";
				}
				if (!listSubTypes.contains(name)) {

					listSubTypes.add(name);
				}
			}
			typeName += listSubTypes.toString();
		}

		return typeName;
	}

}
