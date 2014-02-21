package com.zhangyue.hella.common.util;

/**
 * Description:进度收集级别<br>
 * Copyright: Copyright (c) 2012 <br>
 * Company: www.renren.com
 * 
 * @author zhuhui{hui.zhu@renren-inc.com} 2012-7-20
 * @version 1.0
 */
public enum ProgressCollectorLevel {
    ALL("所有：汇报所有进度  -1<进度<100") {

        @Override
        public boolean isCollect(int progress) {
            return progress > 0 ? true : false;
        }
    },

    RUN("运行:汇报 -1==进度||0<进度<100||进度==100") {

        @Override
        public boolean isCollect(int progress) {
            return (Constant.PROGRESS_SUCCESS >= progress && Constant.PROGRESS_ERROR <= progress) ? true : false;
        }
    },

    DEBUG("异常:汇报 进度==-1||进度==100") {

        @Override
        public boolean isCollect(int progress) {
            return (Constant.PROGRESS_ERROR == progress && Constant.PROGRESS_SUCCESS == progress) ? true : false;

        }
    },

    SUCCESS("成功:汇报 进度==100") {

        @Override
        public boolean isCollect(int progress) {
            return Constant.PROGRESS_SUCCESS == progress ? true : false;

        }
    };

    /** 类型名称 */
    private final String typeName;

    /**
     * @param typeName {@link #typeName}
     */
    private ProgressCollectorLevel(String typeName){
        this.typeName = typeName;
    }

    /**
     * @return {@link #typeName}
     */
    public String getTypeName() {
        return typeName;
    }

    public abstract boolean isCollect(int progress);

}
