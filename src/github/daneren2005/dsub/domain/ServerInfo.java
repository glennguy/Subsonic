/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2010 (C) Sindre Mehus
 */
package github.daneren2005.dsub.domain;

import android.content.Context;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import github.daneren2005.dsub.util.FileUtil;
import github.daneren2005.dsub.util.Util;

/**
 * Information about the Subsonic server.
 *
 * @author Sindre Mehus
 */
public class ServerInfo implements Serializable {
	public static final int TYPE_SUBSONIC = 1;
	public static final int TYPE_MADSONIC = 2;
	private static final Map<Integer, ServerInfo> SERVERS = new ConcurrentHashMap<Integer, ServerInfo>();
	
	private boolean isLicenseValid;
	private Version restVersion;
	private int type;
	
	public ServerInfo() {
		type = TYPE_SUBSONIC;
	}

	public boolean isLicenseValid() {
		return isLicenseValid;
	}

	public void setLicenseValid(boolean licenseValid) {
		isLicenseValid = licenseValid;
	}

	public Version getRestVersion() {
		return restVersion;
	}

	public void setRestVersion(Version restVersion) {
		this.restVersion = restVersion;
	}
    
	public int getRestType() {
		return type;
	}
	public void setRestType(int type) {
		this.type = type;
	}
	
	public boolean isStockSubsonic() {
		return type == TYPE_SUBSONIC;
	}
	public boolean isMadsonic() {
		return type == TYPE_MADSONIC;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		} else if(o == null || getClass() != o.getClass()) {
			return false;
		}
	    
		final ServerInfo info = (ServerInfo) o;
	    
		if(this.type != info.type) {
			return false;
		} else if(this.restVersion == null || info.restVersion == null) {
			// Should never be null unless just starting up
			return false;
		} else {
			return this.restVersion.equals(info.restVersion);
		}
	}

	// Stub to make sure this is never used, too easy to screw up
	private void saveServerInfo(Context context) {

	}
	public void saveServerInfo(Context context, int instance) {
		ServerInfo current = SERVERS.get(instance);
		if(!this.equals(current)) {
			SERVERS.put(instance, this);
			FileUtil.serialize(context, this, getCacheName(context, instance));
		}
	}
	
	public static ServerInfo getServerInfo(Context context) {
		return getServerInfo(context, Util.getActiveServer(context));
	}
	public static ServerInfo getServerInfo(Context context, int instance) {
		ServerInfo current = SERVERS.get(instance);
		if(current != null) {
			return current;
		}
		
		current = FileUtil.deserialize(context, getCacheName(context, instance), ServerInfo.class);
		if(current != null) {
			SERVERS.put(instance, current);
		}
		
		return current;
	}

	public static Version getServerVersion(Context context) {
		return getServerVersion(context, Util.getActiveServer(context));
	}
	public static Version getServerVersion(Context context, int instance) {
		ServerInfo server = getServerInfo(context, instance);
		if(server == null) {
			return null;
		}

		return server.getRestVersion();
	}

	public static boolean checkServerVersion(Context context, String requiredVersion) {
		return checkServerVersion(context, requiredVersion, Util.getActiveServer(context));
	}
	public static boolean checkServerVersion(Context context, String requiredVersion, int instance) {
		ServerInfo server = getServerInfo(context, instance);
		if(server == null) {
			return false;
		}
		
		Version version = server.getRestVersion();
		if(version == null) {
			return false;
		}
		
		Version required = new Version(requiredVersion);
		return version.compareTo(required) >= 0;
	}

	public static int getServerType(Context context) {
		return getServerType(context, Util.getActiveServer(context));
	}
	public static int getServerType(Context context, int instance) {
		if(Util.isOffline(context)) {
			return 0;
		}

		ServerInfo server = getServerInfo(context, instance);
		if(server == null) {
			return 0;
		}

		return server.getRestType();
	}

	public static boolean isStockSubsonic(Context context) {
		return isStockSubsonic(context, Util.getActiveServer(context));
	}
	public static boolean isStockSubsonic(Context context, int instance) {
		return getServerType(context, instance) == TYPE_SUBSONIC;
	}

	public static boolean isMadsonic(Context context) {
		return isMadsonic(context, Util.getActiveServer(context));
	}
	public static boolean isMadsonic(Context context, int instance) {
		return getServerType(context, instance) == TYPE_MADSONIC;
	}
	
	private static String getCacheName(Context context, int instance) {
		return "server-" + Util.getRestUrl(context, null, instance, false).hashCode() + ".ser";
	}

	public static boolean hasArtistInfo(Context context) {
		if(isStockSubsonic(context) && ServerInfo.checkServerVersion(context, "1.11")) {
			return true;
		} else if(isMadsonic(context)) {
			// TODO: When madsonic adds support, figure out what REST version it is added on
			return false;
		} else {
			return false;
		}
	}
	
	public static boolean canBookmark(Context context) {
		return checkServerVersion(context, "1.9");
	}
}
