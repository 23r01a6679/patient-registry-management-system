package prms.model;

/** User roles. Permissions live with the role so the UI never hard-codes "if admin". */
public enum Role {
    ADMIN(true, true),
    DOCTOR(true, false);

    private final boolean canEdit;
    private final boolean canDelete;

    Role(boolean canEdit, boolean canDelete) {
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public boolean canEdit() { return canEdit; }
    public boolean canDelete() { return canDelete; }
}
