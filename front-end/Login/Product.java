package Login;
public class Product {

    private String category;
    private String name;
    private String price;
    private String imagePath;

    public Product(String category, String name, String price, String imagePath) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }
}