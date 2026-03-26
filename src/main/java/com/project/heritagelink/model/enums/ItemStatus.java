package com.project.heritagelink.model.enums;

/**
 * Represents the lifecycle stage of an inventory item.
 * Valid transitions: IDENTIFIED -> APPRAISED -> ASSIGNED -> DISPOSED
 * Items flagged for mediation cannot advance until the conflict is resolved.
 */
public enum ItemStatus {
    IDENTIFIED,
    APPRAISED,
    ASSIGNED,
    DISPOSED;

    public boolean canTransitionTo(ItemStatus target) {
        return switch(this) {
            case IDENTIFIED -> target == APPRAISED;
            case APPRAISED -> target == ASSIGNED;
            case ASSIGNED -> target == DISPOSED;
            case DISPOSED -> false;
        };
    }
}
