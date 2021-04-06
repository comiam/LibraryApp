package comiam.nsu.libapp.db.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.Statement;

@AllArgsConstructor
public class Response
{
    @Getter
    private final String errorMsg;
    @Getter
    private final ResultSet set;
    @Getter
    private final Statement st;

    void closeAll()
    {
        try
        {
            set.close();
        }catch (Throwable ignored) {}

        try
        {
            st.close();
        }catch (Throwable ignored) {}
    }
}
