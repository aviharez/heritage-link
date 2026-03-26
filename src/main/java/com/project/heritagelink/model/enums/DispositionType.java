package com.project.heritagelink.model.enums;

/**
 * The intended final fate of an inventory item.
 */
public enum DispositionType {
    /** Item will be given to a family member */
    GIFTING,
    /** Item will be donated to a charity or organization */
    DONATION,
    /** Item will be sold; requires a verified appraisal value > $0. */
    SALE,
    /** Item will be moved with the client to their new residence. */
    RELOCATION
}
