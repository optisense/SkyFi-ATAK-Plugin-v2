package com.optisense.skyfi.atak.skyfiapi;

import androidx.annotation.NonNull;

public class MyProfile {
    private String id;
    private String organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isDemoAccount;
    private Integer currentBudgetUsage;
    private Integer budgetAmount;
    private Boolean hasValidSharedCard;

    @NonNull
    @Override
    public String toString() {
        return "MyProfile{" +
                "budgetAmount=" + budgetAmount +
                ", id='" + id + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isDemoAccount=" + isDemoAccount +
                ", currentBudgetUsage=" + currentBudgetUsage +
                ", hasValidSharedCard=" + hasValidSharedCard +
                '}';
    }

    public Integer getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Integer budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public Integer getCurrentBudgetUsage() {
        return currentBudgetUsage;
    }

    public void setCurrentBudgetUsage(Integer currentBudgetUsage) {
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

    public Boolean getHasValidSharedCard() {
        return hasValidSharedCard;
    }

    public void setHasValidSharedCard(Boolean hasValidSharedCard) {
        this.hasValidSharedCard = hasValidSharedCard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getDemoAccount() {
        return isDemoAccount;
    }

    public void setDemoAccount(Boolean demoAccount) {
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

    public int getBudgetRemaining() {
        if (budgetAmount == null) {
            return 0;
        }
        else if (currentBudgetUsage == null) {
            return budgetAmount;
        }

        return budgetAmount - currentBudgetUsage;
    }
}
