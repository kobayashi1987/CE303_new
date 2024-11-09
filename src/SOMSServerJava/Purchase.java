package SOMSServerJava;

import java.time.LocalDateTime;

/**
 * Purchase represents a purchase made by a customer.
 */
public class Purchase {
    private String userID;
    private String itemName;
    private int quantity;
    private LocalDateTime purchaseDate;
    private String sellerID;
    private double totalCost;
    private String status; // "pending", "completed"

    // Constructors
    public Purchase() {}

    public Purchase(String userID, String itemName, int quantity, LocalDateTime purchaseDate,
                    String sellerID, double totalCost, String status) {
        this.userID = userID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.sellerID = sellerID;
        this.totalCost = totalCost;
        this.status = status;
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

    /**
     * Sets the name of the purchased item.
     *
     * @param itemName The item's name.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity purchased.
     *
     * @param quantity The quantity.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Sets the date and time of the purchase.
     *
     * @param purchaseDate The purchase date and time.
     */
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getSellerID() {
        return sellerID;
    }

    /**
     * Sets the seller's userID.
     *
     * @param sellerID The seller's userID.
     */
    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Sets the total cost of the purchase.
     *
     * @param totalCost The total cost.
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the purchase.
     *
     * @param status The status ("pending" or "completed").
     */
    public void setStatus(String status) {
        this.status = status;
    }
}