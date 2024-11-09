package SOMSServerJava;

import java.time.LocalDateTime;

/**
 * Purchase represents a transaction made by a customer.
 */
public class Purchase {
    private String userID;
    private String itemName;
    private int quantity;
    private LocalDateTime purchaseDate;
    private String sellerID; // "pending" initially
    private double totalCost;
    private String status; // "pending", "fulfilled", "unfulfilled"

    // Default constructor for GSON
    public Purchase() {}

    // Parameterized constructor
    public Purchase(String userID, String itemName, int quantity, LocalDateTime purchaseDate,
                    String sellerID, double totalCost, String status) {
        this.userID = userID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.sellerID = sellerID;
        this.totalCost = totalCost;
        setStatus(status);
    }

    // Getters and Setters

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Gets the current status of the purchase.
     *
     * @return The status ("pending", "fulfilled", "unfulfilled").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the purchase.
     *
     * @param status The new status ("pending", "fulfilled", "unfulfilled").
     * @throws IllegalArgumentException if the status is invalid.
     */
    public void setStatus(String status) {
        if (status.equalsIgnoreCase("pending") ||
                status.equalsIgnoreCase("fulfilled") ||
                status.equalsIgnoreCase("unfulfilled")) {
            this.status = status.toLowerCase();
        } else {
            throw new IllegalArgumentException("Invalid status. Must be 'pending', 'fulfilled', or 'unfulfilled'.");
        }
    }
}