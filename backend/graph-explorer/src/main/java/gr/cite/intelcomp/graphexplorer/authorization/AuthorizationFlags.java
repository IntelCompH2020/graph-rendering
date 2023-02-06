package gr.cite.intelcomp.graphexplorer.authorization;

import java.util.EnumSet;

public enum AuthorizationFlags {
	None, Permission, Owner, Affiliated;
	public static final EnumSet<AuthorizationFlags> OwnerOrPermission = EnumSet.of(Owner, Permission);
	public static final EnumSet<AuthorizationFlags> OwnerOrPermissionOrAffiliated = EnumSet.of(Owner, Permission, Affiliated);
}
