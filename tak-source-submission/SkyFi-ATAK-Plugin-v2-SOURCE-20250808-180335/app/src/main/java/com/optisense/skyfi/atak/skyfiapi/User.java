package com.optisense.skyfi.atak.skyfiapi;

public class User {
    private String id;
    private String organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isDemoAccount;
    private int currentBudgetUsage;
    private boolean hasValidSharedCard;

    public int getCurrentBudgetUsage() {
        return currentBudgetUsage;
    }

    public void setCurrentBudgetUsage(int currentBudgetUsage) {
        this.currentBudgetUsage = currentBudgetUsage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isHasValidSharedCard() {
        return hasValidSharedCard;
    }

    public void setHasValidSharedCard(boolean hasValidSharedCard) {
        this.hasValidSharedCard = hasValidSharedCard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDemoAccount() {
        return isDemoAccount;
    }

    public void setDemoAccount(boolean demoAccount) {
        isDemoAccount = demoAccount;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
