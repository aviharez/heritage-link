package com.project.heritagelink.model.enums;

/**
 * Lifecycle status of a family member's claim on an item.
 */
public enum ClaimStatus {
    /** The claim has been submitted and is awaiting resolution */
    ACTIVE,
    /** The claim was accepted; this family member receives the item. */
    APPROVED,
    /** The claim was withdrawn or overruled. */
    DISMISSED
}
