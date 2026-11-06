
package com.salonbook.model;

public record User(
        int userId,
        String username,
        String password,
        String fullName,
        String role,
        String status
) {

    // Check if the account is approved
    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    // Check if the account is pending
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    // Check if the account is deactivated
    public boolean isDeactivated() {
        return "DEACTIVATED".equalsIgnoreCase(status);
    }

    // Check user role
    public boolean isAdministrator() {
        return "ADMINISTRATOR".equalsIgnoreCase(role);
    }

    public boolean isReceptionist() {
        return "RECEPTIONIST".equalsIgnoreCase(role);
    }

    public boolean isSalonStaff() {
        return "SALON STAFF".equalsIgnoreCase(role);
    }
}
