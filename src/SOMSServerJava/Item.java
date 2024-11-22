
package SOMSServerJava;

/**
 * Represents an item in the inventory.
 */
public class Item {
    private String name;
    private double price;
    private int quantityAvailable;
    private String sellerID;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Item() {

    }

    /**
     * Constructs a new Item.
     *
     * @param name              The name of the item.
     * @param price             The price of the item.
     * @param quantityAvailable The available quantity of the item.
     */
    public Item(String name, double price, int quantityAvailable, String sellerID) {
        this.name = name;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.sellerID = sellerID;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    // get the seller ID
    public String getSellerID() {
        return sellerID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    /**
     * Reduces the available quantity of the item by the specified amount.
     *
     * @param quantity The quantity to reduce.
     */
    public synchronized void reduceQuantity(int quantity) {
        if (quantity > quantityAvailable) {
            throw new IllegalArgumentException("Insufficient quantity available.");
        }
        this.quantityAvailable -= quantity;
    }

    /**
     * Increases the available quantity of the item by the specified amount.
     *
     * @param quantity The quantity to increase.
     */
    public synchronized void increaseQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to increase must be non-negative.");
        }
        this.quantityAvailable += quantity;
    }
}