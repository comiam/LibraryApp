package comiam.nsu.libapp.db.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BookOperationCardRow
{
    @Getter
    private final String ID;
    @Getter
    private final String name;
    @Getter
    private final String year;
    @Getter
    private final String operationTime;
    @Getter
    private final String operation;
    @Getter
    private final String count;
}
