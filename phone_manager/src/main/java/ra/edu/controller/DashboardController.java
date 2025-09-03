package ra.edu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ra.edu.service.InvoiceService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final InvoiceService invoiceService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Object[]> dayRevenue = invoiceService.revenueByDay();
        List<Object[]> monthRevenue = invoiceService.revenueByMonth();
        List<Object[]> yearRevenue = invoiceService.revenueByYear();

        model.addAttribute("dayRevenue", dayRevenue);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("yearRevenue", yearRevenue);

        return "dashboard";
    }
}
