
package SOMSServerJava;

import java.time.LocalDateTime;


import com.google.gson.annotations.SerializedName;

/**
 * Purchase represents a transaction made by a customer.
 */
public class Purchase {
    private int purchaseId;
    private String userID;
    private String itemName;
    private int quantity;
    private LocalDateTime purchaseDate;
    private String sellerID;
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

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

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