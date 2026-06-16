package dev.luxorium.aureus.api;

public record EconomyResult(
        boolean success,
        EconomyFailure failure,
        long balanceMinor,
        long transactionId,
        String message
) {
    public static EconomyResult success(long balanceMinor, long transactionId) {
        return new EconomyResult(true, EconomyFailure.NONE, balanceMinor, transactionId, "");
    }

    public static EconomyResult failure(EconomyFailure failure, String message) {
        return new EconomyResult(false, failure, 0L, 0L, message);
    }
}
