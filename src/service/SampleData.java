package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.DuplicateIdException;
import model.Category;
import model.Invoice;
import model.InvoiceDetail;
import model.InvoiceStatus;
import model.PaymentMethod;
import model.Product;
import model.ProductStatus;
import model.Staff;
import model.StaffRole;
import repository.Repositories.CategoryRepository;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;
import repository.Repositories.StaffRepository;

public class SampleData {
    public static void seed(CategoryRepository categoryRepo, StaffRepository staffRepo,
                            ProductRepository productRepo, InvoiceRepository invoiceRepo)
            throws DuplicateIdException {

        categoryRepo.insert(new Category("DM01", "Cà phê"));
        categoryRepo.insert(new Category("DM02", "Trà"));
        categoryRepo.insert(new Category("DM03", "Sinh tố và nước ép"));
        categoryRepo.insert(new Category("DM04", "Bánh ngọt"));

        staffRepo.insert(new Staff("NV01", "Nguyễn Văn An", "0901234567", StaffRole.MANAGER));
        staffRepo.insert(new Staff("NV02", "Trần Thị Bình", "0912345678", StaffRole.CASHIER));
        staffRepo.insert(new Staff("NV03", "Lê Văn Chí", "0923456789", StaffRole.CASHIER));
        staffRepo.insert(new Staff("NV04", "Phạm Thị Dung", "0934567890", StaffRole.CASHIER));

        List<Product> products = List.of(
                new Product("SP01", "Cà phê đen", "DM01", 25000, 50, ProductStatus.SELLING),
                new Product("SP02", "Cà phê sữa", "DM01", 29000, 40, ProductStatus.SELLING),
                new Product("SP03", "Bạc xỉu", "DM01", 32000, 6, ProductStatus.SELLING),
                new Product("SP04", "Cappuccino", "DM01", 45000, 30, ProductStatus.STOPPED),
                new Product("SP05", "Trà đào", "DM02", 38000, 45, ProductStatus.SELLING),
                new Product("SP06", "Trà vải", "DM02", 38000, 8, ProductStatus.SELLING),
                new Product("SP07", "Trà sữa trân châu", "DM02", 42000, 60, ProductStatus.SELLING),
                new Product("SP08", "Trà xanh", "DM02", 30000, 20, ProductStatus.STOPPED),
                new Product("SP09", "Sinh tố xoài", "DM03", 48000, 25, ProductStatus.SELLING),
                new Product("SP10", "Sinh tố bơ", "DM03", 55000, 4, ProductStatus.SELLING),
                new Product("SP11", "Sinh tố dâu", "DM03", 50000, 22, ProductStatus.SELLING),
                new Product("SP12", "Nước cam", "DM03", 35000, 35, ProductStatus.SELLING),
                new Product("SP13", "Bánh tiramisu", "DM04", 55000, 15, ProductStatus.SELLING),
                new Product("SP14", "Bánh croissant", "DM04", 28000, 18, ProductStatus.SELLING),
                new Product("SP15", "Bánh flan", "DM04", 15000, 12, ProductStatus.STOPPED)
        );

        for (Product p : products) {
            productRepo.insert(p);
        }

        Map<String, Product> byId = new HashMap<>();
        for (Product p : products) {
            byId.put(p.getId(), p);
        }

        String[] pool = {"SP01", "SP02", "SP03", "SP05", "SP06", "SP07", "SP09", "SP10", "SP11", "SP12"};
        String[] stoppedPool = {"SP04", "SP08", "SP15"};
        PaymentMethod[] methods = PaymentMethod.values();
        int[] discounts = {0, 0, 5, 0, 10, 0, 15};
        int[] hours = {8, 9, 12, 8, 17, 12, 9, 19, 12, 10};
        Map<LocalDate, Integer> counter = new HashMap<>();
        String[] staffIds = {"NV01", "NV02", "NV03", "NV04"};

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate day = today.minusDays(i % 14);
            LocalDateTime createdAt = day.atTime(hours[i % hours.length], (i * 7) % 60);

            int seq = counter.merge(day, 1, Integer::sum);
            String id = "HD" + day.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%04d", seq);

            InvoiceStatus status = (i % 7 == 3) ? InvoiceStatus.CANCELLED : InvoiceStatus.PAID;

            Invoice inv = new Invoice(id, staffIds[i % 4], createdAt,
                    methods[i % methods.length], discounts[i % discounts.length], status);

            int lines = 1 + i % 3;
            for (int k = 0; k < lines; k++) {
                Product p = byId.get(pool[(i * 2 + k * 3) % pool.length]);
                int qty = 1 + (i + k) % 3;
                inv.getDetails().add(new InvoiceDetail(p.getId(), qty, p.getPrice()));
            }

            if (i % 14 >= 9 && i % 4 == 0) {
                Product p = byId.get(stoppedPool[i % stoppedPool.length]);
                inv.getDetails().add(new InvoiceDetail(p.getId(), 1, p.getPrice()));
            }

            // Dùng insert() cho hóa đơn lịch sử mẫu để giữ nguyên tồn kho hiện tại và cả các đơn hủy/món ngừng bán trước đó
            invoiceRepo.insert(inv);
        }
    }
}
