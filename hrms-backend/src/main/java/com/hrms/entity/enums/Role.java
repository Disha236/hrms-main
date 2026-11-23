package com.hrms.entity.enums;

public enum Role {
    ADMIN(4),
    HR_MANAGER(3),
    MANAGER(2),
    EMPLOYEE(1);

    private final int rank;

    Role(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean hasMinRole(Role minRole) {
        return this.rank >= minRole.rank;
    }
}
