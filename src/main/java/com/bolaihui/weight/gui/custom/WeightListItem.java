package com.bolaihui.weight.gui.custom;

import com.bolaihui.weight.gui.po.Weight;
import com.bolaihui.weight.gui.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Created by fz on 2015/12/31.
 */
public class WeightListItem extends JLabel implements ListCellRenderer{

    public WeightListItem(){
        setOpaque(true);
    }

    private final Pattern passedOkRex = Pattern.compile(Constants.successStatus);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        Font font = new Font(null, 0, 13);
        Color background;
        Color foreground;
        Weight weight = (Weight) value;
        if(passedOkRex.matcher(weight.getStatus()).find()){
            background = Color.WHITE;
            foreground = Constants.okColor;
        }else{
            background = Color.WHITE;
            foreground = Color.RED;
        }
        if(isSelected){
            background = Color.ORANGE;
        }
        setText((index + 1) + ")" + "运单号：" + weight.getEmsNo() + " | " + "重量：" + weight.getWeight() + " | " + weight.getStatus() + " | " + weight.getBoxNo() + " | " + weight.getOperateTime());
        setBackground(background);
        setForeground(foreground);
        setFont(font);
        return this;
    }
}
