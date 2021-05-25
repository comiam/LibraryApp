package comiam.nsu.libapp.db.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AcceptOrderRow
{
    @Getter
    private final String bookID;
    @Getter
    private final String bookName;
    @Getter
    private final String cardID;
    @Getter
    private final String orderDate;
    @Getter
    private final String returnedOrder;
}
