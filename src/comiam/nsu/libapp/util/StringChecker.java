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

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
        } catch(Throwable e)
        {
            return false;
        }
        return true;
    }
}
