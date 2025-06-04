package git_kkalnane.backend.starbucks.global.utils;


import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class GlobalLogger {
    private static String BLOCK = "{} ";

    private static String format(int length){
        StringBuilder format = new StringBuilder();
        IntStream.range(0,length).forEach(i->format.append(BLOCK));

        return format.toString();
    }

    public static void warn(Object ... args){
        log.warn(format(args.length), args);
    }

    public static void info(Object ... args){
        log.info(format(args.length), args);
    }

    public static void error(Object ... args){
        log.error(format(args.length), args);
    }
}
