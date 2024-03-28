package StorageFiles;

import java.util.Objects;

public class Product {
    private String category;
    private String brand;
    private double price;
    private String code;
    private int quantity;
//----------------------------------------------------------------------------------------------------------------------
    public Product(String category, String brand, double price, String code, int quantity) {
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.code = code;
        this.quantity = quantity;
    }
//----------------------------------------------------------------------------------------------------------------------
    public String getCategory() {return category;}
    public String getBrand() {return brand;}
    public double getPrice() {
        return price;
    }
    public String getCode() {
        return code;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {this.quantity = quantity;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(price, product.price) == 0 && quantity == product.quantity && Objects.equals(category, product.category) && Objects.equals(brand, product.brand) && Objects.equals(code, product.code);
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public String toString() {
        return "Classi.CartManagement.StorageFiles.Product{" +
                "category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", code='" + code + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}