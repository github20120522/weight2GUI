package com.bolaihui.weight.gui.custom;

import com.bolaihui.weight.gui.po.OutData;
import com.bolaihui.weight.gui.util.Constants;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Created by fz on 2015/12/31.
 */
public class WaitOutListItem extends JLabel implements ListCellRenderer{

    public WaitOutListItem(){
        setOpaque(true);
    }

    private final Pattern passedOkRex = Pattern.compile(Constants.successStatus);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        Font font = new Font(null, 0, 13);
        Color background;
        Color foreground;
        OutData outData = (OutData) value;
        if(passedOkRex.matcher(outData.getStatus()).find()){
            background = Color.WHITE;
            foreground = Constants.okColor;
        }else{
            background = Color.WHITE;
            foreground = Color.RED;
        }
        if(isSelected){
            background = Color.ORANGE;
        }
        String content = (index + 1) + ")" + "运单号：" + outData.getEmsNo() + " | " + outData.getStatus();
        if (StringUtils.isNotBlank(outData.getOperateTime())) {
            content += " | " + outData.getOperateTime();
        }
        setText(content);
        setBackground(background);
        setForeground(foreground);
        setFont(font);
        return this;
    }
}
