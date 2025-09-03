package ra.edu.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceForm {
    private Integer customerId;
    private List<Integer> selectedProductIds;
    private Map<Integer, Integer> quantities; // key = productId, value = số lượng
}
