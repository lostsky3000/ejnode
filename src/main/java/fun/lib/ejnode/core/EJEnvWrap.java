package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Environment;

public final class EJEnvWrap implements Environment {

    public static final int OS_UNKNOWN = 0;
    public static final int OS_LINUX = 1;
    public static final int OS_WINDOWS = 2;
    public static final int OS_MAC = 3;
    private static int s_osType = OS_UNKNOWN;
    static{
        final String osName = System.getProperty("os.name");
        if(osName != null){
            final String osNameLow = osName.toLowerCase();
            if(osNameLow.contains("linux")){
                s_osType = OS_LINUX;
            }else if(osNameLow.contains("windows")){
                s_osType = OS_WINDOWS;
            }
        }
    }
    protected static boolean isLinux(){
        return s_osType == OS_LINUX;
    }

    private static volatile int _cpuCoreNum = 0;
    protected static int getCpuCoreNum(){
        if(_cpuCoreNum == 0){
            _cpuCoreNum = Runtime.getRuntime().availableProcessors();
        }
        return _cpuCoreNum;
    }

    @Override
    public int cpuCoreNum() {
        return getCpuCoreNum();
    }
}
