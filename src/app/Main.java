package app;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import exception.DuplicateIdException;
import repository.Repositories.CategoryRepository;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;
import repository.Repositories.StaffRepository;
import repository.memory.InMemoryCategoryRepository;
import repository.memory.InMemoryInvoiceRepository;
import repository.memory.InMemoryProductRepository;
import repository.memory.InMemoryStaffRepository;
import service.CatalogService;
import service.SalesService;
import service.SampleData;
import service.StaffService;
import test.SystemTestRunner;
import ui.CatalogMenu;
import ui.ConsoleUI;
import ui.StaffMenu;

public class Main {
    public static void main(String[] args) {
        // Đảm bảo hiển thị tiếng Việt có dấu chuẩn UTF-8 trên Console (Windows/Linux/IDE)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        // Chạy kiểm thử tự động các chức năng đã hoàn thiện nếu truyền tham số --test
        if (args != null && args.length > 0 && "--test".equalsIgnoreCase(args[0])) {
            SystemTestRunner.main(args);
            return;
        }

        // 1. Khởi tạo tầng Repository lưu trữ trên bộ nhớ RAM (In-Memory)
        CategoryRepository categoryRepo = new InMemoryCategoryRepository();
        ProductRepository productRepo = new InMemoryProductRepository();
        StaffRepository staffRepo = new InMemoryStaffRepository();
        InvoiceRepository invoiceRepo = new InMemoryInvoiceRepository(productRepo, staffRepo);

        // 2. Nạp dữ liệu mẫu (4 danh mục, 4 nhân viên, 15 sản phẩm, 30 hóa đơn)
        try {
            SampleData.seed(categoryRepo, staffRepo, productRepo, invoiceRepo);
        } catch (DuplicateIdException e) {
            System.out.println("Lỗi khởi tạo dữ liệu mẫu: " + e.getMessage());
            return;
        }

        // 3. Khởi tạo tầng Service nghiệp vụ
        CatalogService catalogService = new CatalogService(categoryRepo, productRepo, invoiceRepo);
        StaffService staffService = new StaffService(staffRepo, invoiceRepo);
        SalesService salesService = new SalesService(invoiceRepo, productRepo, staffRepo);

        // 4. Khởi tạo các Menu giao diện Console đã hoàn thiện
        CatalogMenu catalogMenu = new CatalogMenu(catalogService);
        StaffMenu staffMenu = new StaffMenu(staffService);

        // 5. Khởi chạy chương trình POS Console
        ConsoleUI consoleUI = new ConsoleUI(staffRepo, salesService, catalogMenu, staffMenu);
        consoleUI.start();
    }
}
