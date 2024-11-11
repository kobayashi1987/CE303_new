package SOMSServerJava;

import java.time.LocalDateTime;

/**
 * Purchase represents a transaction made by a customer.
 */
public class Purchase {
    private int purchaseId;
    private String userID;
    private String itemName;
    private int quantity;
    private LocalDateTime purchaseDate;
    private String sellerID; // "pending" initially
    private double totalCost;
    private String status; // "pending", "fulfilled", "unfulfilled"

    /**
     * Default constructor for GSON.
     */
    public Purchase() {}

    /**
     * Parameterized constructor to initialize a Purchase object.
     *
     * @param userID       The ID of the user who made the purchase.
     * @param itemName     The name of the item purchased.
     * @param quantity     The quantity of the item purchased.
     * @param purchaseDate The date and time when the purchase was made.
     * @param sellerID     The ID of the seller fulfilling the purchase. "pending" initially.
     * @param totalCost    The total cost of the purchase.
     * @param status       The status of the purchase ("pending", "fulfilled", "unfulfilled").
     */
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

    /**
     * Gets the unique purchase ID.
     *
     * @return The purchase ID.
     */
    public int getPurchaseId() {
        return purchaseId;
    }

    /**
     * Sets the unique purchase ID.
     *
     * @param purchaseId The purchase ID to set.
     */
    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    /**
     * Gets the user ID of the buyer.
     *
     * @return The user ID of the buyer.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the user ID of the buyer.
     *
     * @param userID The user ID to set.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Gets the name of the item purchased.
     *
     * @return The item name.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Sets the name of the item purchased.
     *
     * @param itemName The item name to set.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Gets the quantity of the item purchased.
     *
     * @return The quantity purchased.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item purchased.
     *
     * @param quantity The quantity to set.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the date and time of the purchase.
     *
     * @return The purchase date and time.
     */
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Sets the date and time of the purchase.
     *
     * @param purchaseDate The purchase date and time to set.
     */
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    /**
     * Gets the seller ID associated with the purchase.
     *
     * @return The seller ID.
     */
    public String getSellerID() {
        return sellerID;
    }

    /**
     * Sets the seller ID associated with the purchase.
     *
     * @param sellerID The seller ID to set.
     */
    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    /**
     * Gets the total cost of the purchase.
     *
     * @return The total cost.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Sets the total cost of the purchase.
     *
     * @param totalCost The total cost to set.
     */
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

    /**
     * Provides a string representation of the Purchase object.
     *
     * @return A string detailing the purchase information.
     */
    @Override
    public String toString() {
        return "Purchase{" +
                "purchaseId=" + purchaseId +
                ", userID='" + userID + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", purchaseDate=" + purchaseDate +
                ", sellerID='" + sellerID + '\'' +
                ", totalCost=" + totalCost +
                ", status='" + status + '\'' +
                '}';
    }
}