package dev.luxorium.aureus.api;

public enum EconomyFailure {
    NONE,
    ACCOUNT_NOT_FOUND,
    INVALID_AMOUNT,
    BELOW_MINIMUM_PAYMENT,
    ABOVE_MAXIMUM_PAYMENT,
    INSUFFICIENT_FUNDS,
    SAME_ACCOUNT,
    STORAGE_ERROR
}
