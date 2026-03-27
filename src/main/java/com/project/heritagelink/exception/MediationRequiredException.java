package com.project.heritagelink.exception;

/**
 * Thrown when an attempt is made to advance the workflow of an item
 * that has an unresolved claim conflict.
 */
public class MediationRequiredException extends RuntimeException {

    private final Long itemId;

    public MediationRequiredException(Long itemId) {
        super("Item " + itemId + " has an active mediation conflict. All claims must be resolved before the item can be updated.");
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }

}
