
package com.salonbook.model;

public record User(
        int userId,
        String username,
        String password,
        String fullName,
        String role,
        String status
) {

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isDeactivated() {
        return "DEACTIVATED".equalsIgnoreCase(status);
    }

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
