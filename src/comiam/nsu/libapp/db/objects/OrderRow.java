package comiam.nsu.libapp.db.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class OrderRow
{
    @Getter
    private final String ID;
    @Getter
    private final String bookName;
    @Getter
    private final String orderDate;
    @Getter
    private final String latePassDate;
    @Getter
    private final String takenDate;
    @Getter
    private final String returnedOrder;
}
