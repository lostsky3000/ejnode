package fun.lib.ejnode.api;

public interface Logger {
    public static final int LEVEL_TRACE = 1;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_WARN = 4;
    public static final int LEVEL_ERROR = 5;
    public static final int LEVEL_FATAL = 6;

    /**
     *
     * @param str
     */
    void trace(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void trace(String tag, String str);

    /**
     *
     * @param str
     */
    void debug(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void debug(String tag, String str);

    /**
     *
     * @param str
     */
    void info(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void info(String tag, String str);

    /**
     *
     * @param str
     */
    void warn(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void warn(String tag, String str);

    /**
     *
     * @param str
     */
    void error(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void error(String tag, String str);

    /**
     *
     * @param str
     */
    void fatal(String str);

    /**
     *
     * @param tag
     * @param str
     */
    void fatal(String tag, String str);
}
