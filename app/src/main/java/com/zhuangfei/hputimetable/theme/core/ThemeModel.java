package com.zhuangfei.hputimetable.theme.core;


import java.io.Serializable;
import java.util.List;

/**
 * 主题模型
 */
public class ThemeModel implements Serializable{


    /**
     * version : 1
     * minSupportVersion : 1
     * images : [{"tag":"img1","url":"http://vpn.hpu.edu.cn"},{"tag":"img2","url":"http:''vpn.hpu.edu.cn"}]
     * operator : wakeup
     * background : img/img1|#000000
     * backgroundScope : page|content
     * itemColors : ["#000000","#000FFF","#FFF000"]
     * itemColorsMode : append|cover
     * uselessColor : #000000
     * showWeekends : true
     * showNotCurWeek : true
     * maxSideItem : 10
     * showSideTime : true
     * sideWidth : 50
     * columnWidthRato : [1,1,3,1,1,1,1]
     * itemText : {$name}@{$room}
     * marTop : 3
     * marLeft : 3
     * itemHeight : 56
     * itemCorner : 10
     * itemAlpha : 0.6
     * dateAlpha : 0.1
     * sideAlpha : 0.1
     * showFlaglayout : true
     */

    /**
     * 当前主题版本
     */
    private int version;

    /**
     * 当前主题支持的TimetableView版本
     */
    private int minSupportVersion;

    /**
     * 课表模板：wakeup|md|nan'a|utimetable|qing
     */
    private String operator;

    /**
     * 背景
     */
    private String background;

    /**
     * 背景范围
     */
    private String backgroundScope;

    /**
     * 非本周课程颜色
     */
    private String uselessColor;

    /**
     * 是否显示周末
     */
    private boolean showWeekends;

    /**
     * 是否显示非本周
     */
    private boolean showNotCurWeek;

    /**
     * 最大节次
     */
    private int maxSideItem;

    /**
     * 是否显示侧边栏时间
     */
    private boolean showSideTime;

    /**
     * 侧边栏宽度
     */
    private int sideWidth;

    /**
     * 课程项文本显示模式
     */
    private String itemText;

    /**
     * 边距、高度、弧度
     */
    private int marTop;
    private int marLeft;
    private int itemHeight;
    private int itemCorner;

    /**
     * 透明度
     */
    private double itemAlpha;
    private double dateAlpha;
    private double sideAlpha;

    /**
     * 是否显示旗标布局
     */
    private boolean showFlaglayout;

    /**
     * 图片资源列表
     */
    private List<ImagesBean> images;

    /**
     * 课程颜色列表
     */
    private List<String> itemColors;

    /**
     * 课程颜色模式：append|cover
     */
    private String itemColorsMode;

    /**
     * 列宽比率
     */
    private List<Integer> columnWidthRato;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMinSupportVersion() {
        return minSupportVersion;
    }

    public void setMinSupportVersion(int minSupportVersion) {
        this.minSupportVersion = minSupportVersion;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBackgroundScope() {
        return backgroundScope;
    }

    public void setBackgroundScope(String backgroundScope) {
        this.backgroundScope = backgroundScope;
    }

    public String getItemColorsMode() {
        return itemColorsMode;
    }

    public void setItemColorsMode(String itemColorsMode) {
        this.itemColorsMode = itemColorsMode;
    }

    public String getUselessColor() {
        return uselessColor;
    }

    public void setUselessColor(String uselessColor) {
        this.uselessColor = uselessColor;
    }

    public boolean isShowWeekends() {
        return showWeekends;
    }

    public void setShowWeekends(boolean showWeekends) {
        this.showWeekends = showWeekends;
    }

    public boolean isShowNotCurWeek() {
        return showNotCurWeek;
    }

    public void setShowNotCurWeek(boolean showNotCurWeek) {
        this.showNotCurWeek = showNotCurWeek;
    }

    public int getMaxSideItem() {
        return maxSideItem;
    }

    public void setMaxSideItem(int maxSideItem) {
        this.maxSideItem = maxSideItem;
    }

    public boolean isShowSideTime() {
        return showSideTime;
    }

    public void setShowSideTime(boolean showSideTime) {
        this.showSideTime = showSideTime;
    }

    public int getSideWidth() {
        return sideWidth;
    }

    public void setSideWidth(int sideWidth) {
        this.sideWidth = sideWidth;
    }

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    public int getMarTop() {
        return marTop;
    }

    public void setMarTop(int marTop) {
        this.marTop = marTop;
    }

    public int getMarLeft() {
        return marLeft;
    }

    public void setMarLeft(int marLeft) {
        this.marLeft = marLeft;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public int getItemCorner() {
        return itemCorner;
    }

    public void setItemCorner(int itemCorner) {
        this.itemCorner = itemCorner;
    }

    public double getItemAlpha() {
        return itemAlpha;
    }

    public void setItemAlpha(double itemAlpha) {
        this.itemAlpha = itemAlpha;
    }

    public double getDateAlpha() {
        return dateAlpha;
    }

    public void setDateAlpha(double dateAlpha) {
        this.dateAlpha = dateAlpha;
    }

    public double getSideAlpha() {
        return sideAlpha;
    }

    public void setSideAlpha(double sideAlpha) {
        this.sideAlpha = sideAlpha;
    }

    public boolean isShowFlaglayout() {
        return showFlaglayout;
    }

    public void setShowFlaglayout(boolean showFlaglayout) {
        this.showFlaglayout = showFlaglayout;
    }

    public List<ImagesBean> getImages() {
        return images;
    }

    public void setImages(List<ImagesBean> images) {
        this.images = images;
    }

    public List<String> getItemColors() {
        return itemColors;
    }

    public void setItemColors(List<String> itemColors) {
        this.itemColors = itemColors;
    }

    public List<Integer> getColumnWidthRato() {
        return columnWidthRato;
    }

    public void setColumnWidthRato(List<Integer> columnWidthRato) {
        this.columnWidthRato = columnWidthRato;
    }

    public static class ImagesBean {
        /**
         * tag : img1
         * url : http://vpn.hpu.edu.cn
         */

        private String tag;
        private String url;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
