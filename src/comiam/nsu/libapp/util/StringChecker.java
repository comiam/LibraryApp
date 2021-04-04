package comiam.nsu.libapp.util;

import lombok.val;

public class StringChecker
{
    public static boolean checkStrArgs(String... args)
    {
        for(val i : args)
            if(i == null || i.trim().isEmpty())
                return false;
        return true;
    }
}
