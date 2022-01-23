package com.carson.click;

/**
 * Created by yujunyao on 1/22/22.
 */
public class ClickUtils {

    private static Long mLastClickTime = 0L;
    private static Long timeInterval = 500L;

    public static boolean preventRepeatClick() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > timeInterval) {
            mLastClickTime = nowTime;
            return false;
        } else {
            return true;
        }
    }

}
