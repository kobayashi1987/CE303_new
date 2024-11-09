package SOMSServerJava;

/**
 * Item represents an item available for purchase.
 */
public class Item {
    private String name;
    private double price;
    private int quantityAvailable;

    // Constructors
    public Item() {}

    public Item(String name, double price, int quantityAvailable) {
        this.name = name;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     *
     * @param name The item's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of the item.
     *
     * @param price The item's price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    /**
     * Sets the quantity available for the item.
     *
     * @param quantityAvailable The available quantity.
     */
    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    /**
     * Reduces the available quantity by the specified amount.
     *
     * @param quantity The amount to reduce.
     * @return True if reduction is successful, else false.
     */
    public boolean reduceQuantity(int quantity) {
        if (this.quantityAvailable >= quantity) {
            this.quantityAvailable -= quantity;
            return true;
        }
        return false;
    }

    /**
     * Increases the available quantity by the specified amount.
     *
     * @param quantity The amount to increase.
     */
    public void increaseQuantity(int quantity) {
        this.quantityAvailable += quantity;
    }
}
