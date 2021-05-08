package comiam.nsu.libapp.db.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ViolationRow
{
    @Getter
    private final String ID;
    @Getter
    private final String bookName;
    @Getter
    private final String hallID;
    @Getter
    private final String violationDate;
    @Getter
    private final String violationType;
    @Getter
    private final String fine;
}
