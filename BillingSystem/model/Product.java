package BillingSystem.model;

public class Product {
    private String name;
    private int quantity;
    private double price;

    public Product(String name, int quantity, double price){
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName(){ return name; }
    public int getQuantity(){ return quantity; }
    public double getPrice(){ return price; }
    public double getTotal(){ return quantity * price; }
}