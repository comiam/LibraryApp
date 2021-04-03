package comiam.nsu.libapp.db.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PersonCard
{
    @Getter
    private final String ID;
    @Getter
    private final String humanID;
    @Getter
    private final String firstName;
    @Getter
    private final String lastName;
    @Getter
    private final String patronymic;
    @Getter
    private final String regDate;
    @Getter
    private final String rewriteDate;
    @Getter
    private final String type;
    @Getter
    private final String canTakeAwayBook;
}
