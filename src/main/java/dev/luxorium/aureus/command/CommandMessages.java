package dev.luxorium.aureus.command;

import dev.luxorium.aureus.api.EconomyFailure;

final class CommandMessages {
    private CommandMessages() {
    }

    static String failure(EconomyFailure failure, String fallback) {
        return switch (failure) {
            case ACCOUNT_NOT_FOUND -> "No Aureus account was found for that player.";
            case INVALID_AMOUNT -> "Enter a valid positive amount.";
            case BELOW_MINIMUM_PAYMENT -> "That amount is below the minimum payment.";
            case ABOVE_MAXIMUM_PAYMENT -> "That amount is above the maximum payment.";
            case INSUFFICIENT_FUNDS -> "Insufficient Aurei.";
            case SAME_ACCOUNT -> "You cannot pay yourself.";
            case STORAGE_ERROR -> "A storage error occurred. Check the server log.";
            case NONE -> fallback;
        };
    }
}
