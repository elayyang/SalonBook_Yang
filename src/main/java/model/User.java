
package com.salonbook.model;

public record User(
        int userId,
        String username,
        String password,
        String fullName,
        String role,
        String status
) {
}
