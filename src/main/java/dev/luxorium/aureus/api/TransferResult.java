package dev.luxorium.aureus.api;

public record TransferResult(
        boolean success,
        EconomyFailure failure,
        long sourceBalanceMinor,
        long targetBalanceMinor,
        long transactionId,
        String message
) {
    public static TransferResult success(long sourceBalanceMinor, long targetBalanceMinor, long transactionId) {
        return new TransferResult(true, EconomyFailure.NONE, sourceBalanceMinor, targetBalanceMinor, transactionId, "");
    }

    public static TransferResult failure(EconomyFailure failure, String message) {
        return new TransferResult(false, failure, 0L, 0L, 0L, message);
    }
}
