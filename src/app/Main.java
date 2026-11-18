package app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.Invoice;
import model.InvoiceStatus;
import model.PaymentMethod;
import model.Product;
import model.ProductStatus;

public class Main{
    public static void main(String[] args) {
            List<Product> products = new ArrayList<>();
            products.add(new Product("SP01", "Pepsi", "DM03", -5000, 10, ProductStatus.SELLING));
            // products.add(new Product("SP02", "Coca", "DM03", 5000, 100, ProductStatus.SELLING));
            System.out.println(products);


            //  List<Invoice> invoices = new ArrayList<>();
            //     invoices.add(new Invoice("HD01", "NV01", LocalDateTime.of(2026, 11, 17, 14, 30), PaymentMethod.CASH, 10, InvoiceStatus.PAID));
            //     invoices.get(0).addItem("SP02", 5, 4000);
            //     System.out.println("Subtotal: " + invoices.get(0).subtotal());
            //     System.out.println("Discount: " + invoices.get(0).subtotal()*invoices.get(0).getDiscountPercent()/100);
            //     System.out.println("Total: "  + invoices.get(0).total());
            }   
}